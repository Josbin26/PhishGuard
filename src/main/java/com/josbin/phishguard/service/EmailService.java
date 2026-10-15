
package com.josbin.phishguard.service;

import com.josbin.phishguard.model.Campaign;
import com.josbin.phishguard.model.Target;
import com.josbin.phishguard.model.Event;
import com.josbin.phishguard.repository.EventRepository;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final EventRepository eventRepo;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, EventRepository eventRepo) {
        this.mailSender = mailSender;
        this.eventRepo = eventRepo;
    }

    public boolean sendSimulationEmail(Target target, Campaign campaign) {
        try {
            String trackingUrl = baseUrl + "/p/" + target.getTrackingToken();
            String body = buildEmailBody(
                trackingUrl,
                campaign.getTemplate().getSlug()
            );

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(target.getEmail());
            helper.setSubject(campaign.getTemplate().getEmailSubject());
            helper.setText(body, true);

            mailSender.send(message);

            // Record the email-sent event using the existing constructor.
            Event event = new Event(target, "email_sent", "", "");
            eventRepo.save(event);

            return true;

        } catch (Exception e) {
            System.err.println(
                "[MAIL ERROR] " + target.getEmail() + ": " + e.getMessage()
            );
            return false;
        }
    }

    private String buildEmailBody(String trackingUrl, String slug) {
        return switch (slug) {
            case "helpdesk" -> helpdeskEmail(trackingUrl);
            case "hr_portal" -> hrPortalEmail(trackingUrl);
            default -> ms365Email(trackingUrl);
        };
    }

    private String ms365Email(String url) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:8px;overflow:hidden">
              <div style="background:#0067b8;padding:20px 24px">
                <span style="color:white;font-size:20px;font-weight:bold">Microsoft</span>
              </div>
              <div style="padding:32px 24px">
                <h2 style="color:#1a1a2e;margin-top:0">Verify your Microsoft account</h2>
                <p style="color:#444">We noticed unusual sign-in activity on your account.
                   Please verify your identity to continue using Microsoft 365 services.</p>
                <div style="text-align:center;margin:32px 0">
                  <a href="%s" style="background:#0067b8;color:white;padding:14px 32px;
                     text-decoration:none;border-radius:4px;font-size:15px;font-weight:bold">
                    Verify Account
                  </a>
                </div>
                <p style="color:#888;font-size:12px">
                  If you did not request this, you can safely ignore this email.
                </p>
              </div>
            </div>""".formatted(url);
    }

    private String helpdeskEmail(String url) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:8px;overflow:hidden">
              <div style="background:#cc0000;padding:20px 24px">
                <span style="color:white;font-size:20px;font-weight:bold">🔐 IT Helpdesk</span>
              </div>
              <div style="padding:32px 24px">
                <div style="background:#fff3cd;border:1px solid #ffc107;border-radius:4px;
                            padding:12px 16px;margin-bottom:24px;color:#856404;font-size:14px">
                  ⚠️ <strong>Urgent:</strong> Your network password expires in
                  <strong>24 hours</strong>.
                </div>
                <h2 style="color:#1a1a2e;margin-top:0">Password Reset Required</h2>
                <p style="color:#444">Your corporate account password has reached its expiry date.
                   Please reset your password immediately.</p>
                <div style="text-align:center;margin:32px 0">
                  <a href="%s" style="background:#cc0000;color:white;padding:14px 32px;
                     text-decoration:none;border-radius:4px;font-size:15px;font-weight:bold">
                    Reset Password Now
                  </a>
                </div>
                <p style="color:#888;font-size:12px">
                  For assistance contact: it-support@company.com
                </p>
              </div>
            </div>""".formatted(url);
    }

    private String hrPortalEmail(String url) {
        return """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:auto;
                        border:1px solid #e0e0e0;border-radius:8px;overflow:hidden">
              <div style="background:#0067b8;padding:20px 24px">
                <span style="color:white;font-size:20px;font-weight:bold">👤 HR Employee Portal</span>
              </div>
              <div style="padding:32px 24px">
                <div style="background:#fff8e1;border-left:4px solid #ffc107;
                            padding:12px 16px;margin-bottom:24px;font-size:14px;color:#5a4000">
                  📋 Your payslip and appraisal report are now available.
                </div>
                <h2 style="color:#1a1a2e;margin-top:0">Documents Ready for Review</h2>
                <p style="color:#444">Please sign in to access your latest payslip,
                   leave balance, and performance appraisal records.</p>
                <div style="text-align:center;margin:32px 0">
                  <a href="%s" style="background:#0067b8;color:white;padding:14px 32px;
                     text-decoration:none;border-radius:4px;font-size:15px;font-weight:bold">
                    Access HR Portal
                  </a>
                </div>
                <p style="color:#888;font-size:12px">
                  For issues contact: hr-support@company.com
                </p>
              </div>
            </div>""".formatted(url);
    }
}

