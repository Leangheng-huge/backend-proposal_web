package com.romantic.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.from}")
    private String fromEmail;

    /**
     * Sends an email notification to the user about the response to their proposal.
     *
     * @param toEmail      Recipient email address
     * @param response     Proposal response ("YES" or other)
     * @param proposalLink Link to view the proposal
     */
    public void sendProposalResponseEmail(String toEmail, String response, String proposalLink) {
        try {
            log.info("📧 Preparing to send email to: {}", toEmail);
            log.info("Response type: {}, From: {}", response, fromEmail);

            // Try HTML email first, fallback to simple text if it fails
            try {
                sendHtmlEmail(toEmail, response, proposalLink);
                log.info("✅ HTML email sent successfully to: {}", toEmail);
            } catch (Exception htmlError) {
                log.warn("⚠️ HTML email failed, trying simple text email", htmlError);
                sendSimpleEmail(toEmail, response, proposalLink);
                log.info("✅ Simple text email sent successfully to: {}", toEmail);
            }

        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", toEmail, e);
            log.error("Error details: {}", e.getMessage());
            // Do not throw exception: email failure should not break the app
        }
    }

    /**
     * Send HTML formatted email (preferred)
     */
    private void sendHtmlEmail(String toEmail, String response, String proposalLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);

        if ("YES".equalsIgnoreCase(response)) {
            helper.setSubject("🎉 Great News! Someone Accepted Your Proposal!");
            helper.setText(buildPositiveHtmlEmail(proposalLink), true);
        } else {
            helper.setSubject("💙 Response to Your Proposal");
            helper.setText(buildNegativeHtmlEmail(proposalLink), true);
        }

        mailSender.send(message);
    }

    /**
     * Send simple text email (fallback)
     */
    private void sendSimpleEmail(String toEmail, String response, String proposalLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);

        if ("YES".equalsIgnoreCase(response)) {
            preparePositiveResponseEmail(message, proposalLink);
        } else {
            prepareNegativeResponseEmail(message, proposalLink);
        }

        mailSender.send(message);
    }

    private void preparePositiveResponseEmail(SimpleMailMessage message, String proposalLink) {
        message.setSubject("🎉 Great News! Someone Accepted Your Proposal!");
        message.setText(
                "Congratulations!! 🎉🎉\n\n" +
                        "Someone accepted your romantic proposal!\n\n" +
                        "I am so, so happy for you! You totally deserve this. 💕\n\n" +
                        "Proposal Link: " + proposalLink + "\n\n" +
                        "Best wishes,\n" +
                        "Romantic Proposal App"
        );
    }

    private void prepareNegativeResponseEmail(SimpleMailMessage message, String proposalLink) {
        message.setSubject("💙 Response to Your Proposal");
        message.setText(
                "Hello,\n\n" +
                        "Someone has responded to your romantic proposal.\n\n" +
                        "It's okay to feel disappointed, sad, or angry. Take all the time you need to process it. 💙\n\n" +
                        "Remember, this doesn't define your worth. Keep your head up!\n\n" +
                        "Proposal Link: " + proposalLink + "\n\n" +
                        "Take care,\n" +
                        "Romantic Proposal App"
        );
    }

    /**
     * HTML email template for positive response
     */
    private String buildPositiveHtmlEmail(String proposalLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
                    .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); padding: 40px; text-align: center; color: white; }
                    .emoji { font-size: 60px; margin: 20px 0; }
                    .content { padding: 40px 30px; }
                    .button { display: inline-block; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 50px; font-weight: bold; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 30px; text-align: center; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="emoji">🎉</div>
                        <h1>Congratulations!</h1>
                    </div>
                    <div class="content">
                        <h2>Great News! 💕</h2>
                        <p>Someone accepted your romantic proposal!</p>
                        <p>I am so, so happy for you! You totally deserve this.</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">View Your Proposal</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>💌 Romantic Proposal App</p>
                        <p style="font-size: 12px;">Link: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(proposalLink, proposalLink);
    }

    /**
     * HTML email template for negative response
     */
    private String buildNegativeHtmlEmail(String proposalLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
                    .header { background: linear-gradient(135deg, #89f7fe 0%%, #66a6ff 100%%); padding: 40px; text-align: center; color: white; }
                    .emoji { font-size: 60px; margin: 20px 0; }
                    .content { padding: 40px 30px; line-height: 1.8; }
                    .button { display: inline-block; background: linear-gradient(135deg, #89f7fe 0%%, #66a6ff 100%%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 50px; font-weight: bold; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 30px; text-align: center; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="emoji">💙</div>
                        <h1>Response to Your Proposal</h1>
                    </div>
                    <div class="content">
                        <p>Hello,</p>
                        <p>Someone has responded to your romantic proposal.</p>
                        <p>It's okay to feel disappointed, sad, or angry. Take all the time you need to process it. 💙</p>
                        <p>Remember, this doesn't define your worth. Keep your head up!</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">View Response</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>💌 Take care - Romantic Proposal App</p>
                        <p style="font-size: 12px;">Link: %s</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(proposalLink, proposalLink);
    }
}