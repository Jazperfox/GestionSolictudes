package com.example.MoonPhase.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    public void enviarCorreoHtml(String destinatario, String asunto,
                                 Long idSolicitud, Integer estado, String comentario)
            throws MessagingException {

        // Cargar variables para la plantilla
        Context context = new Context();
        context.setVariable("idSolicitud", idSolicitud);
        context.setVariable("estado", estado);
        context.setVariable("comentario", comentario);

        // Procesar el archivo correoSolicitud.html
        String contenidoHtml = templateEngine.process("emailSolicitud", context);

        // Crear correo tipo HTML
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(contenidoHtml, true);

        mailSender.send(mensaje);
    }
}
