
package com.josbin.phishguard.controller;

import com.josbin.phishguard.model.Target;
import com.josbin.phishguard.model.Event;
import com.josbin.phishguard.repository.TargetRepository;
import com.josbin.phishguard.repository.EventRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class TrackerController {

    private final TargetRepository targets;
    private final EventRepository events;

    public TrackerController(
            TargetRepository targets,
            EventRepository events) {
        this.targets = targets;
        this.events = events;
    }

    @GetMapping("/p/{token}")
    public String landing(
            @PathVariable String token,
            HttpServletRequest request,
            Model model) {

        Target target = targets.findByTrackingToken(token)
                .orElseThrow();

        events.save(new Event(
                target,
                "link_clicked",
                ip(request),
                userAgent(request)
        ));

        model.addAttribute("token", token);

        return "lures/" +
                target.getCampaign().getTemplate().getSlug();
    }

    @PostMapping("/submit/{token}")
    public String submit(
            @PathVariable String token,
            HttpServletRequest request) {

        Target target = targets.findByTrackingToken(token)
                .orElseThrow();

        events.save(new Event(
                target,
                "credentials_submitted",
                ip(request),
                userAgent(request)
        ));

        return "redirect:/awareness/" + token;
    }

    /**
     * Gets the remote address of the incoming request.
     */
    private String ip(HttpServletRequest request) {

        String address = request.getRemoteAddr();

        if (address == null || address.isBlank()) {
            return "Unknown";
        }

        return address;
    }

    /**
     * Gets the browser User-Agent, limited to 300 characters.
     */
    private String userAgent(HttpServletRequest request) {

        String agent = request.getHeader("User-Agent");

        if (agent == null || agent.isBlank()) {
            return "";
        }

        return agent.substring(0, Math.min(300, agent.length()));
    }
}

