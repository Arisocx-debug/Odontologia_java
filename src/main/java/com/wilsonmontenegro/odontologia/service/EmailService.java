package com.wilsonmontenegro.odontologia.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarBienvenida(String destinatario, String nombre) {

        try {

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(destinatario);
            helper.setSubject("¡Bienvenido a Dr. Wilson Montenegro!");

            String contenido = """
                    <html>
                    <body style="font-family: Arial, sans-serif;">

                        <h2>¡Bienvenido, %s! 🦷</h2>

                        <p>
                            Gracias por registrarte en el sistema de
                            <strong>Dr. Wilson Montenegro</strong>.
                        </p>

                        <p>
                            Tu cuenta ha sido creada correctamente y
                            ya puedes utilizar nuestros servicios.
                        </p>

                        <p>
                            Esperamos que tengas una excelente experiencia
                            con nosotros.
                        </p>

                        <br>

                        <p>
                            <strong>Dr. Wilson Montenegro</strong><br>
                            Sistema de gestión odontológica
                        </p>

                    </body>
                    </html>
                    """.formatted(nombre);

            helper.setText(contenido, true);

            mailSender.send(mensaje);

        } catch (MessagingException e) {

            System.err.println(
                    "Error enviando correo de bienvenida: "
                            + e.getMessage());
        }
    }
}