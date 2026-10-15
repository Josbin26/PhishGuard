
package com.josbin.phishguard.controller;

import com.josbin.phishguard.model.Campaign;
import com.josbin.phishguard.model.Event;
import com.josbin.phishguard.model.Target;
import com.josbin.phishguard.repository.CampaignRepository;
import com.josbin.phishguard.repository.PhishTemplateRepository;
import com.josbin.phishguard.service.CampaignService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignRepository campaigns;
    private final PhishTemplateRepository templates;
    private final CampaignService service;

    public CampaignController(
            CampaignRepository campaigns,
            PhishTemplateRepository templates,
            CampaignService service) {

        this.campaigns = campaigns;
        this.templates = templates;
        this.service = service;
    }

    // Display all campaigns
    @GetMapping
    public String list(Model model) {
        model.addAttribute("campaigns", campaigns.findAll());
        return "campaigns/list";
    }

    // Display campaign creation form
    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("templates", templates.findAll());
        return "campaigns/new";
    }

    // Create a campaign
    @PostMapping("/new")
    public String create(
            @RequestParam String name,
            @RequestParam("template_id") Long templateId,
            @RequestParam(value = "emails_docx", required = false)
            org.springframework.web.multipart.MultipartFile emailsDocx,
            Authentication auth,
            RedirectAttributes ra) {

        try {
            service.create(
                    name,
                    templateId,
                    null,
                    auth.getName(),
                    emailsDocx
            );

            ra.addFlashAttribute(
                    "message",
                    "Campaign created. Check the Event Log for results."
            );

        } catch (Exception e) {
            ra.addFlashAttribute(
                    "error",
                    "Campaign creation failed: " + e.getMessage()
            );
        }

        return "redirect:/campaigns";
    }

    // Display campaign details and event log
    @GetMapping("/{id}")
    public String view(
            @PathVariable Long id,
            Model model) {

        Campaign campaign = campaigns.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Campaign not found: " + id));

        List<Target> targets = campaign.getTargets();

        List<Event> allEvents = targets.stream()
                .flatMap(target -> target.getEvents().stream())
                .toList();

        long sent = countEvents(allEvents, "email_sent");
        long clicks = countEvents(allEvents, "link_clicked");
        long submits = countEvents(allEvents, "credentials_submitted");

        model.addAttribute("campaign", campaign);
        model.addAttribute("targets", targets);
        model.addAttribute("events", allEvents);
        model.addAttribute("totalEvents", allEvents.size());
        model.addAttribute("sent", sent);
        model.addAttribute("clicks", clicks);
        model.addAttribute("submits", submits);

        return "campaigns/view";
    }

    // Automatic dashboard refresh endpoint
    @GetMapping("/{id}/stats")
    @ResponseBody
    public Map<String, Object> getCampaignStats(
            @PathVariable Long id) {

        Campaign campaign = campaigns.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Campaign not found: " + id));

        List<Target> targets = campaign.getTargets();

        List<Event> allEvents = targets.stream()
                .flatMap(target -> target.getEvents().stream())
                .toList();

        Map<String, Object> stats = new HashMap<>();

        stats.put("totalTargets", targets.size());
        stats.put("totalEvents", allEvents.size());
        stats.put("sent", countEvents(allEvents, "email_sent"));
        stats.put("clicks", countEvents(allEvents, "link_clicked"));
        stats.put("submits",
                countEvents(allEvents, "credentials_submitted"));

        // Return event details for the live Event Log table.
        List<Map<String, Object>> eventDetails = allEvents.stream()
                .sorted((a, b) ->
                        b.getTimestamp().compareTo(a.getTimestamp()))
                .map(event -> {
                    Map<String, Object> details = new HashMap<>();

                    details.put(
                            "email",
                            event.getTarget() != null
                                    ? event.getTarget().getEmail()
                                    : ""
                    );

                    details.put("eventType", event.getEventType());

                    details.put(
                            "timestamp",
                            event.getTimestamp() != null
                                    ? event.getTimestamp().toString()
                                    : ""
                    );

                    details.put("ipAddress", event.getIpAddress());
                    details.put("userAgent", event.getUserAgent());

                    return details;
                })
                .toList();

        stats.put("events", eventDetails);

        return stats;
    }

    // Count events by type
    private long countEvents(List<Event> events, String eventType) {
        return events.stream()
                .filter(event -> eventType.equals(event.getEventType()))
                .count();
    }

    // Resend campaign emails to authorised test recipients
    @PostMapping("/{id}/send")
    public String send(
            @PathVariable Long id,
            RedirectAttributes ra) {

        Campaign campaign = campaigns.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Campaign not found: " + id));

        int sent = service.resend(campaign);

        ra.addFlashAttribute(
                "message",
                "Email sending attempted. Successful sends: " + sent
        );

        return "redirect:/campaigns/" + id;
    }

    // Pause campaign
    @PostMapping("/{id}/pause")
    public String pause(
            @PathVariable Long id,
            RedirectAttributes ra) {

        Campaign campaign = campaigns.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Campaign not found: " + id));

        campaign.setStatus("paused");
        campaigns.save(campaign);

        ra.addFlashAttribute("message", "Campaign paused.");

        return "redirect:/campaigns/" + id;
    }

    // Resume campaign
    @PostMapping("/{id}/resume")
    public String resume(
            @PathVariable Long id,
            RedirectAttributes ra) {

        Campaign campaign = campaigns.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Campaign not found: " + id));

        campaign.setStatus("active");
        campaigns.save(campaign);

        ra.addFlashAttribute("message", "Campaign resumed.");

        return "redirect:/campaigns/" + id;
    }

    // Delete campaign
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes ra) {

        campaigns.deleteById(id);

        ra.addFlashAttribute("message", "Campaign deleted.");

        return "redirect:/campaigns";
    }
}
