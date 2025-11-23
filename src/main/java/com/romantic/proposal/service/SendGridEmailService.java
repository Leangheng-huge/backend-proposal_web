package com.romantic.proposal.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    public void sendProposalResponseEmail(String toEmail, String response, String proposalLink) {
        try {
            log.info("📧 Preparing SendGrid email to: {}", toEmail);

            Email from = new Email(fromEmail, fromName);
            Email to = new Email(toEmail);

            String subject = response.equals("YES")
                    ? "🎉 Great News! Someone Accepted Your Proposal!"
                    : "💙 Response to Your Proposal";

            Content content = new Content("text/html", buildEmailHtml(response, proposalLink));
            Mail mail = new Mail(from, subject, to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response sgResponse = sg.api(request);

            if (sgResponse.getStatusCode() >= 200 && sgResponse.getStatusCode() < 300) {
                log.info("✅ SendGrid email sent successfully to: {} (Status: {})",
                        toEmail, sgResponse.getStatusCode());
            } else {
                log.error("❌ SendGrid failed with status: {} - Body: {}",
                        sgResponse.getStatusCode(), sgResponse.getBody());
                throw new RuntimeException("SendGrid returned status: " + sgResponse.getStatusCode());
            }

        } catch (IOException e) {
            log.error("❌ SendGrid IO error", e);
            throw new RuntimeException("Failed to send email via SendGrid", e);
        } catch (Exception e) {
            log.error("❌ SendGrid unexpected error", e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String buildEmailHtml(String response, String proposalLink) {
        boolean isYes = response.equals("YES");
        String headerColor = isYes ? "#f093fb" : "#89f7fe";
        String emoji = isYes ? "🎉" : "💙";
        String title = isYes ? "They said YES!" : "Response to Your Proposal";
        String message = isYes
                ? "Congratulations!! Someone accepted your romantic proposal! I am so, so happy for you! You totally deserve this. 💕"
                : "Hello, Someone has responded to your romantic proposal. It's okay to feel disappointed, sad, or angry. Take all the time you need to process it. 💙";

        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); padding: 20px; margin: 0; }
                    .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 20px; overflow: hidden; box-shadow: 0 20px 60px rgba(0,0,0,0.3); }
                    .header { background: linear-gradient(135deg, %s 0%%, #f5576c 100%%); padding: 40px; text-align: center; color: white; }
                    .emoji { font-size: 60px; margin: 20px 0; }
                    .content { padding: 40px 30px; line-height: 1.8; }
                    .button { display: inline-block; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; text-decoration: none; padding: 15px 40px; border-radius: 50px; font-weight: bold; margin: 20px 0; }
                    .footer { background: #f8f9fa; padding: 30px; text-align: center; color: #666; font-size: 14px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <div class="emoji">%s</div>
                        <h1>%s</h1>
                    </div>
                    <div class="content">
                        <p>%s</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">View Your Proposal</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>💌 Romantic Proposal App</p>
                        <p style="font-size: 12px;">This is an automated message</p>
                    </div>
                </div>
            </body>
            </html>
            """, headerColor, emoji, title, message, proposalLink);
    }
}