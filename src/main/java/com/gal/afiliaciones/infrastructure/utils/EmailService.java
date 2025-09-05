package com.gal.afiliaciones.infrastructure.utils;

import com.gal.afiliaciones.infrastructure.dto.otp.email.EmailDataDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;

    @Value("${from.email.address}")
    private String fromEmailAddress;

    /**
     * Sends a simple email message with optional attachment.
     *
     * @param emailDataDTO contains email data such as recipient, template, and attachments.
     * @param subject the subject of the email.
     * @throws MessagingException if there is a failure in the messaging process.
     * @throws IOException if there is an I/O error.
     */
    public void sendSimpleMessage(EmailDataDTO emailDataDTO, String subject) throws MessagingException, IOException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        // Read the email template content from the file
        Resource resource = new ClassPathResource("templates/" + emailDataDTO.getPlantilla());
        String templateContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Replace placeholders in the template with provided data
        String message = EmailUtils.replaceTemplatePlaceholders(templateContent, emailDataDTO.getDatos());

        // Attach the file to the email if provided
        if (emailDataDTO.getAdjunto() != null && emailDataDTO.getAdjunto().length > 0) {
            String fileName = Optional.ofNullable(emailDataDTO.getDatos().get("idRadicado"))
                    .map(Object::toString)
                    .filter(id -> !id.isEmpty())
                    .map(id -> id + ".pdf")
                    .orElse("Informe.pdf");

            helper.addAttachment(fileName, new ByteArrayResource(emailDataDTO.getAdjunto()));
        }

        helper.setFrom(fromEmailAddress);
        helper.setTo(emailDataDTO.getDestinatario());
        helper.setSubject(subject);
        helper.setText(message, true);

        emailSender.send(mimeMessage);
    }

    /**
     * Sends an email message with multiple file attachments.
     *
     * @param emailDataDTO contains email data such as recipient, template, and attachments.
     * @param subject the subject of the email.
     * @throws MessagingException if there is a failure in the messaging process.
     * @throws IOException if there is an I/O error.
     */
    public void sendManyFilesMessage(EmailDataDTO emailDataDTO, String subject) throws MessagingException, IOException {
        MimeMessage mimeMessage = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        // Set maximum email message size property
        JavaMailSenderImpl javaMailSenderImpl = (JavaMailSenderImpl) emailSender;
        Properties javaMailProperties = javaMailSenderImpl.getJavaMailProperties();
        javaMailProperties.setProperty("mail.smtp.message.size", "10485760");

        // Read the email template content from the file
        Resource resource = new ClassPathResource("templates/" + emailDataDTO.getPlantilla());
        String templateContent = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        // Replace placeholders in the template with provided data
        String message = EmailUtils.replaceTemplatePlaceholders(templateContent, emailDataDTO.getDatos());

        // Attach multiple files to the email
        if (emailDataDTO.getAdjuntos() != null && !emailDataDTO.getAdjuntos().isEmpty()) {
            for (MultipartFile file : emailDataDTO.getAdjuntos()) {
                helper.addAttachment(file.getOriginalFilename(), new ByteArrayResource(file.getBytes()));
            }
        }

        helper.setFrom(fromEmailAddress);
        helper.setTo(emailDataDTO.getDestinatario());
        helper.setSubject(subject);
        helper.setText(message, true);

        if (emailDataDTO.getConcopia() != null && emailDataDTO.getConcopia().length > 0 && !emailDataDTO.getConcopia()[0].isEmpty()) {
            helper.setCc(emailDataDTO.getConcopia());
        }

        emailSender.send(mimeMessage);
    }
}
