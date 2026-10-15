
package com.josbin.phishguard.service;

import com.josbin.phishguard.model.*;
import com.josbin.phishguard.repository.*;

import jakarta.mail.internet.MimeMessage;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class CampaignService {

    private final CampaignRepository campaigns;
    private final PhishTemplateRepository templates;
    private final AdminUserRepository admins;
    private final TargetRepository targets;
    private final EventRepository events;
    private final JavaMailSender mail;

    @Value("${app.base-url:http://localhost:5000}")
    private String baseUrl;

    private static final Pattern EMAIL =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    public CampaignService(
            CampaignRepository campaigns,
            PhishTemplateRepository templates,
            AdminUserRepository admins,
            TargetRepository targets,
            EventRepository events,
            JavaMailSender mail) {

        this.campaigns = campaigns;
        this.templates = templates;
        this.admins = admins;
        this.targets = targets;
        this.events = events;
        this.mail = mail;
    }

    @Transactional
    public Campaign create(
            String name,
            Long templateId,
            String email,
            String uploaderEmail,
            MultipartFile file) throws IOException {

        AdminUser admin = admins.findByEmail(uploaderEmail)
                .orElseThrow(() ->
                        new IllegalArgumentException("Admin not found"));

        PhishTemplate template = templates.findById(templateId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Template not found"));

        Campaign campaign =
                campaigns.save(new Campaign(name, template, admin));

        List<String> emailAddresses = new ArrayList<>();

        if (file != null
                && !file.isEmpty()
                && file.getOriginalFilename() != null
                && file.getOriginalFilename()
                        .toLowerCase()
                        .endsWith(".docx")) {

            emailAddresses = extractEmails(file);

        } else if (email != null
                && EMAIL.matcher(email.trim()).matches()) {

            emailAddresses.add(email.trim().toLowerCase());
        }

        for (String address : emailAddresses.stream().distinct().toList()) {
            Target target = new Target(campaign, address);
            targets.save(target);
            campaign.getTargets().add(target);
        }

        campaign = campaigns.save(campaign);

        for (Target target : campaign.getTargets()) {
            try {
                send(target, campaign);

                events.save(new Event(
                        target, "email_sent", "", ""));

            } catch (Exception ex) {
                System.err.println(
                        "[MAIL ERROR] " + target.getEmail()
                                + ": " + ex.getMessage());
            }
        }

        return campaign;
    }

    private List<String> extractEmails(MultipartFile file)
            throws IOException {

        List<String> result = new ArrayList<>();

        try (XWPFDocument document =
                     new XWPFDocument(file.getInputStream())) {

            for (var paragraph : document.getParagraphs()) {
                String line = paragraph.getText().trim();

                if (EMAIL.matcher(line).matches()) {
                    result.add(line.toLowerCase());
                }
            }
        }

        return result.stream().distinct().toList();
    }

    public void send(Target target, Campaign campaign) {
        try {
            String trackingUrl =
                    baseUrl + "/p/" + target.getTrackingToken();

            MimeMessage message = mail.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(target.getEmail());

            String subject =
                    campaign.getTemplate().getEmailSubject();

            helper.setSubject(
                    subject != null && !subject.isBlank()
                            ? subject
                            : "Account Notification"
            );

            
String html = """
        <!DOCTYPE html>
        <html lang="en">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport"
                content="width=device-width, initial-scale=1">
        </head>
        <body style="margin:0;padding:32px 16px;
                     background:#f5f7fa;
                     font-family:Arial,Helvetica,sans-serif;
                     color:#202124;">

          <div style="max-width:560px;margin:0 auto;
                      background:#ffffff;
                      border:1px solid #e0e4e8;
                      border-radius:8px;">

            <div style="padding:24px 30px;
                        border-bottom:1px solid #edf0f2;">
              <div style="font-size:12px;color:#687385;">
                Account Notifications
              </div>
            </div>

            <div style="padding:30px;">
              <h2 style="font-size:21px;font-weight:600;
                         margin:0 0 22px;">
                Account Notification
              </h2>

              <p style="font-size:15px;line-height:1.7;">
                Hello,
              </p>

              <p style="font-size:15px;line-height:1.7;">
                There is an update available for you to review.
              </p>

              <p style="font-size:15px;line-height:1.7;">
                Use the link below to continue.
              </p>

              <p style="margin:26px 0;">
                <a href="%s"
                   style="display:inline-block;
                          background:#2457a7;color:#ffffff;
                          padding:12px 22px;border-radius:5px;
                          text-decoration:none;font-size:14px;
                          font-weight:600;">
                  View Notification
                </a>
              </p>

              <p style="font-size:13px;line-height:1.6;
                        color:#687385;">
                If you weren't expecting this message,
                verify the sender before opening the link.
              </p>
            </div>
          </div>
        </body>
        </html>
        """.formatted(trackingUrl);


            String plainText =
                    "Account Notification\n\n"
                    + "There is an update available for you to review.\n\n"
                    + "View Notification: " + trackingUrl + "\n\n"
                    + "Verify the sender before opening unexpected links.";

            helper.setText(plainText, html);

            mail.send(message);

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to send email to "
                            + target.getEmail(), ex);
        }
    }

    public int resend(Campaign campaign) {
        int sent = 0;

        for (Target target : campaign.getTargets()) {
            try {
                send(target, campaign);

                events.save(new Event(
                        target, "email_sent", "", ""));

                sent++;

            } catch (Exception ex) {
                System.err.println(
                        "[MAIL ERROR] " + target.getEmail()
                                + ": " + ex.getMessage());
            }
        }

        return sent;
    }
}
