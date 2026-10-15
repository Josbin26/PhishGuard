package com.josbin.phishguard.controller;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import com.josbin.phishguard.model.Campaign;
import com.josbin.phishguard.model.Event;
import com.josbin.phishguard.model.Target;
import com.josbin.phishguard.repository.CampaignRepository;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
public class ReportController {

    private final CampaignRepository campaigns;

    public ReportController(CampaignRepository campaigns) {
        this.campaigns = campaigns;
    }

    @GetMapping("/campaigns/{id}/report")
    public String report(
            @PathVariable Long id,
            Model model) {

        Campaign campaign = get(id);

        List<Map<String, Object>> summary = summary(campaign);

        int sent = count(summary, "email_sent");
        int clicked = count(summary, "clicked");
        int trained = count(summary, "trained");

        int safe = (int) summary.stream()
                .filter(item ->
                        Boolean.TRUE.equals(item.get("email_sent"))
                                && !Boolean.TRUE.equals(item.get("clicked")))
                .count();

        double clickRate = sent == 0
                ? 0
                : Math.round(clicked * 1000.0 / sent) / 10.0;

        model.addAttribute("campaign", campaign);
        model.addAttribute("summary", summary);
        model.addAttribute("sent_c", sent);
        model.addAttribute("clicked_c", clicked);
        model.addAttribute("trained_c", trained);
        model.addAttribute("safe_c", safe);
        model.addAttribute("click_rate", clickRate);

        return "reports/report";
    }

    @GetMapping("/campaigns/{id}/report/download")
    public void download(
            @PathVariable Long id,
            HttpServletResponse response) throws Exception {

        Campaign campaign = get(id);

        response.setContentType("application/pdf");
        response.setHeader(
                "Content-Disposition",
                "attachment; filename=PhishGuard_"
                        + campaign.getName().replaceAll("\\s+", "_")
                        + "_Report.pdf"
        );

        Document document = new Document(
                PageSize.A4,
                50,
                50,
                50,
                50
        );

        PdfWriter.getInstance(
                document,
                response.getOutputStream()
        );

        document.open();

        // Header
        Font titleFont = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                18,
                BaseColor.WHITE
        );

        PdfPTable headerTable = new PdfPTable(1);

        PdfPCell headerCell = new PdfPCell(
                new Phrase(
                        "PhishGuard — Security Awareness Campaign Report",
                        titleFont
                )
        );

        headerCell.setBackgroundColor(
                new BaseColor(26, 26, 46)
        );

        headerCell.setPadding(14);

        headerTable.addCell(headerCell);

        document.add(headerTable);

        // Campaign information
        document.add(
                new Paragraph(
                        "Campaign: " + campaign.getName()
                )
        );

        document.add(
                new Paragraph(
                        "Template: "
                                + campaign.getTemplate().getName()
                )
        );

        document.add(
                new Paragraph(
                        "Status: " + campaign.getStatus()
                )
        );

        document.add(
                new Paragraph(
                        "Created: "
                                + campaign.getCreatedAt().format(
                                DateTimeFormatter.ofPattern(
                                        "yyyy-MM-dd HH:mm"
                                )
                        )
                )
        );

        document.add(Chunk.NEWLINE);

        // Statistics
        List<Map<String, Object>> summary = summary(campaign);

        int sent = count(summary, "email_sent");
        int clicked = count(summary, "clicked");
        int trained = count(summary, "trained");

        int safe = (int) summary.stream()
                .filter(item ->
                        Boolean.TRUE.equals(item.get("email_sent"))
                                && !Boolean.TRUE.equals(item.get("clicked")))
                .count();

        double clickRate = sent == 0
                ? 0
                : Math.round(clicked * 1000.0 / sent) / 10.0;

        PdfPTable statisticsTable = new PdfPTable(5);

        String[] statisticValues = {
                String.valueOf(sent),
                String.valueOf(clicked),
                String.valueOf(safe),
                String.valueOf(trained),
                clickRate + "%"
        };

        for (String value : statisticValues) {

            PdfPCell cell = new PdfPCell(
                    new Phrase(
                            value,
                            FontFactory.getFont(
                                    FontFactory.HELVETICA_BOLD,
                                    16
                            )
                    )
            );

            cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            cell.setPadding(10);

            statisticsTable.addCell(cell);
        }

        String[] statisticLabels = {
                "Emails Sent",
                "Clicked",
                "Did Not Click",
                "Trained",
                "Click Rate"
        };

        for (String label : statisticLabels) {

            PdfPCell cell = new PdfPCell(
                    new Phrase(label)
            );

            cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            statisticsTable.addCell(cell);
        }

        document.add(statisticsTable);

        document.add(Chunk.NEWLINE);

        // Who clicked
        document.add(
                new Paragraph(
                        "Who Clicked the Link",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                13
                        )
                )
        );

        PdfPTable clickedTable = new PdfPTable(4);

        String[] clickedHeaders = {
                "#",
                "Email",
                "Clicked",
                "Training Done"
        };

        for (String header : clickedHeaders) {
            clickedTable.addCell(header);
        }

        int number = 0;

        for (Map<String, Object> row : summary) {

            if (Boolean.TRUE.equals(row.get("clicked"))) {

                number++;

                clickedTable.addCell(
                        String.valueOf(number)
                );

                clickedTable.addCell(
                        (String) row.get("email")
                );

                clickedTable.addCell("Yes");

                clickedTable.addCell(
                        Boolean.TRUE.equals(
                                row.get("trained")
                        )
                                ? "Trained"
                                : "Not Yet"
                );
            }
        }

        if (number == 0) {

            PdfPCell emptyCell = new PdfPCell(
                    new Phrase("No one clicked the link.")
            );

            emptyCell.setColspan(4);

            clickedTable.addCell(emptyCell);
        }

        document.add(clickedTable);

        document.add(Chunk.NEWLINE);

        // Who did not click
        document.add(
                new Paragraph(
                        "Who Did Not Click",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                13
                        )
                )
        );

        PdfPTable safeTable = new PdfPTable(3);

        String[] safeHeaders = {
                "#",
                "Email",
                "Status"
        };

        for (String header : safeHeaders) {
            safeTable.addCell(header);
        }

        number = 0;

        for (Map<String, Object> row : summary) {

            if (Boolean.TRUE.equals(row.get("email_sent"))
                    && !Boolean.TRUE.equals(row.get("clicked"))) {

                number++;

                safeTable.addCell(
                        String.valueOf(number)
                );

                safeTable.addCell(
                        (String) row.get("email")
                );

                safeTable.addCell(
                        "Resisted Phishing"
                );
            }
        }

        if (number == 0) {

            PdfPCell emptyCell = new PdfPCell(
                    new Phrase("—")
            );

            emptyCell.setColspan(3);

            safeTable.addCell(emptyCell);
        }

        document.add(safeTable);

        document.add(Chunk.NEWLINE);

        // Recommendations
        document.add(
                new Paragraph(
                        "Recommendations",
                        FontFactory.getFont(
                                FontFactory.HELVETICA_BOLD,
                                13
                        )
                )
        );

        String[] recommendations;

        if (clickRate >= 50) {

            recommendations = new String[]{
                    "Conduct mandatory phishing awareness training.",
                    "Schedule monthly simulations.",
                    "Consider a dedicated security awareness program.",
                    "Enforce multi-factor authentication (MFA)."
            };

        } else if (clickRate >= 20) {

            recommendations = new String[]{
                    "Target training to employees who clicked.",
                    "Run a follow-up simulation within 60 days.",
                    "Share phishing tips with staff.",
                    "Ensure MFA is enabled for critical systems."
            };

        } else {

            recommendations = new String[]{
                    "Maintain the current awareness training schedule.",
                    "Continue quarterly simulations.",
                    "Acknowledge teams with zero click rates.",
                    "Share results with management."
            };
        }

        for (int i = 0; i < recommendations.length; i++) {

            document.add(
                    new Paragraph(
                            (i + 1)
                                    + ". "
                                    + recommendations[i]
                    )
            );
        }

        document.add(Chunk.NEWLINE);

        document.add(
                new Paragraph(
                        "PhishGuard — Security Awareness Platform | "
                                + "Confidential — Internal Use Only"
                )
        );

        document.close();
    }

    private Campaign get(Long id) {
        return campaigns
                .findById(id)
                .orElseThrow();
    }

    private int count(
            List<Map<String, Object>> summary,
            String key) {

        return (int) summary.stream()
                .filter(item ->
                        Boolean.TRUE.equals(item.get(key)))
                .count();
    }

    private List<Map<String, Object>> summary(
            Campaign campaign) {

        List<Map<String, Object>> output =
                new ArrayList<>();

        for (Target target : campaign.getTargets()) {

            if ("test@simulation.local".equals(
                    target.getEmail())) {
                continue;
            }

            Set<String> events = new HashSet<>();

            for (Event event : target.getEvents()) {
                events.add(event.getEventType());
            }

            Map<String, Object> data =
                    new HashMap<>();

            data.put(
                    "email",
                    target.getEmail()
            );

            data.put(
                    "email_sent",
                    events.contains("email_sent")
            );

            data.put(
                    "clicked",
                    events.contains("link_clicked")
            );

            data.put(
                    "submitted",
                    events.contains("credentials_submitted")
            );

            data.put(
                    "trained",
                    events.contains("training_shown")
            );

            output.add(data);
        }

        return output;
    }
}
