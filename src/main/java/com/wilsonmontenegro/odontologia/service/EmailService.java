package com.wilsonmontenegro.odontologia.service;

import com.wilsonmontenegro.odontologia.model.Cita;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String correoRemitente;

    private static final ZoneId ZONA_COLOMBIA = ZoneId.of("America/Bogota");

    private static final ZoneId ZONA_UTC = ZoneOffset.UTC;

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("hh:mm a");

    private static final DateTimeFormatter FORMATO_GOOGLE = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    private static final DateTimeFormatter FORMATO_ICS = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    // ============================================================
    // CORREO DE BIENVENIDA
    // ============================================================

    public void enviarBienvenida(
            String destinatario,
            String nombre) {

        if (destinatario == null
                || destinatario.trim().isEmpty()) {
            return;
        }

        if (nombre == null
                || nombre.trim().isEmpty()) {
            nombre = "Usuario";
        }

        try {

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(correoRemitente);
            helper.setTo(destinatario);
            helper.setSubject(
                    "Bienvenido a Dr. Wilson Montenegro");

            String contenido = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                    </head>

                    <body style="
                        margin:0;
                        padding:0;
                        background-color:#f4f7fb;
                        font-family:Arial,Helvetica,sans-serif;
                        color:#333333;
                    ">

                        <div style="
                            max-width:600px;
                            margin:30px auto;
                            background:#ffffff;
                            border-radius:12px;
                            overflow:hidden;
                            box-shadow:0 4px 15px rgba(0,0,0,0.08);
                        ">

                            <div style="
                                background:#0d6efd;
                                color:#ffffff;
                                padding:30px;
                                text-align:center;
                            ">

                                <h1 style="
                                    margin:0;
                                    font-size:28px;
                                ">
                                    Dr. Wilson Montenegro
                                </h1>

                                <p style="
                                    margin:10px 0 0;
                                    font-size:16px;
                                ">
                                    Odontología General y Especializada
                                </p>

                            </div>

                            <div style="
                                padding:35px;
                            ">

                                <h2 style="
                                    color:#0d6efd;
                                    margin-top:0;
                                ">
                                    ¡Bienvenido, %s!
                                </h2>

                                <p style="font-size:16px;line-height:1.6;">
                                    Tu cuenta ha sido creada correctamente
                                    en nuestro sistema de gestión
                                    odontológica.
                                </p>

                                <p style="font-size:16px;line-height:1.6;">
                                    Ahora podrás gestionar tus citas y
                                    consultar la información relacionada
                                    con tus servicios odontológicos.
                                </p>

                                <div style="
                                    text-align:center;
                                    margin:30px 0;
                                ">

                                    <a href="http://localhost:8080/login"
                                       style="
                                        display:inline-block;
                                        background:#0d6efd;
                                        color:#ffffff;
                                        text-decoration:none;
                                        padding:14px 28px;
                                        border-radius:8px;
                                        font-weight:bold;
                                       ">
                                        Ingresar al sistema
                                    </a>

                                </div>

                                <p style="
                                    font-size:14px;
                                    color:#666666;
                                    line-height:1.5;
                                ">
                                    Si no realizaste este registro,
                                    puedes ignorar este mensaje.
                                </p>

                            </div>

                            <div style="
                                background:#f8f9fa;
                                padding:20px;
                                text-align:center;
                                font-size:13px;
                                color:#777777;
                            ">

                                Dr. Wilson Montenegro<br>
                                Odontología General y Especializada

                            </div>

                        </div>

                    </body>
                    </html>
                    """.formatted(
                    escaparHtml(nombre));

            helper.setText(
                    contenido,
                    true);

            mailSender.send(mensaje);

        } catch (MessagingException
                | RuntimeException e) {

            System.err.println(
                    "No se pudo enviar el correo de bienvenida: "
                            + e.getMessage());
        }
    }

    // ============================================================
    // CORREO DE CONFIRMACIÓN DE CITA
    // ============================================================

    public void enviarConfirmacionCita(
            Cita cita) {

        if (!citaValida(cita)) {
            return;
        }

        try {

            String destinatario = cita.getCliente()
                    .getUsuario()
                    .getEmail();

            String nombre = cita.getCliente()
                    .getUsuario()
                    .getName();

            String servicio = cita.getServicio() != null
                    ? cita.getServicio().getNombre()
                    : "Consulta odontológica";

            ZonedDateTime inicio = cita.getFechaEntrada()
                    .atZone(ZONA_COLOMBIA);

            ZonedDateTime fin = cita.getFechaSalida()
                    .atZone(ZONA_COLOMBIA);

            String fecha = inicio.format(FORMATO_FECHA);

            String horaInicio = inicio.format(FORMATO_HORA);

            String horaFin = fin.format(FORMATO_HORA);

            String titulo = "Cita odontológica - "
                    + servicio;

            String ubicacion = "Dr. Wilson Montenegro - "
                    + "Consultorio odontológico";

            String descripcion = "Cita odontológica confirmada para "
                    + nombre
                    + ". Servicio: "
                    + servicio
                    + ". Fecha: "
                    + fecha
                    + ". Hora: "
                    + horaInicio
                    + " - "
                    + horaFin
                    + ".";

            // ====================================================
            // GOOGLE CALENDAR
            // ====================================================

            String fechaInicioGoogle = inicio.format(FORMATO_GOOGLE);

            String fechaFinGoogle = fin.format(FORMATO_GOOGLE);

            String googleCalendarUrl = "https://calendar.google.com/calendar/render"
                    + "?action=TEMPLATE"
                    + "&text="
                    + URLEncoder.encode(
                            titulo,
                            StandardCharsets.UTF_8)
                    + "&dates="
                    + fechaInicioGoogle
                    + "/"
                    + fechaFinGoogle
                    + "&ctz=America%2FBogota"
                    + "&details="
                    + URLEncoder.encode(
                            descripcion,
                            StandardCharsets.UTF_8)
                    + "&location="
                    + URLEncoder.encode(
                            ubicacion,
                            StandardCharsets.UTF_8);

            // ====================================================
            // CREAR MENSAJE
            // ====================================================

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(correoRemitente);
            helper.setTo(destinatario);

            helper.setSubject(
                    "Confirmación de cita odontológica");

            String contenido = """
                    <!DOCTYPE html>
                    <html lang="es">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                    </head>

                    <body style="
                        margin:0;
                        padding:0;
                        background-color:#f4f7fb;
                        font-family:Arial,Helvetica,sans-serif;
                        color:#333333;
                    ">

                        <div style="
                            max-width:600px;
                            margin:30px auto;
                            background:#ffffff;
                            border-radius:12px;
                            overflow:hidden;
                            box-shadow:0 4px 15px rgba(0,0,0,0.08);
                        ">

                            <div style="
                                background:#0d6efd;
                                color:#ffffff;
                                padding:30px;
                                text-align:center;
                            ">

                                <h1 style="
                                    margin:0;
                                    font-size:26px;
                                ">
                                    Dr. Wilson Montenegro Odontología General y Especializada
                                </h1>

                                <p style="
                                    margin:10px 0 0;
                                    font-size:15px;
                                ">
                                    Confirmación de cita odontológica
                                </p>

                            </div>

                            <div style="
                                padding:35px;
                            ">

                                <h2 style="
                                    color:#0d6efd;
                                    margin-top:0;
                                ">
                                    ¡Hola, %s!
                                </h2>

                                <p style="
                                    font-size:16px;
                                    line-height:1.6;
                                ">
                                    Nos complace informarte que tu cita
                                    odontológica ha sido
                                    <strong>confirmada</strong>.
                                </p>

                                <div style="
                                    background:#f4f7fb;
                                    border-radius:10px;
                                    padding:20px;
                                    margin:25px 0;
                                ">

                                    <h3 style="
                                        margin-top:0;
                                        color:#0d6efd;
                                    ">
                                        Detalles de la cita
                                    </h3>

                                    <p>
                                        <strong>Servicio:</strong>
                                        %s
                                    </p>

                                    <p>
                                        <strong>Fecha:</strong>
                                        %s
                                    </p>

                                    <p>
                                        <strong>Hora:</strong>
                                        %s - %s
                                    </p>

                                    <p>
                                        <strong>Estado:</strong>
                                        Confirmada
                                    </p>

                                </div>

                                <p style="
                                    font-size:15px;
                                    line-height:1.6;
                                ">
                                    Te recomendamos agregar la cita a tu
                                    calendario para que puedas recordar
                                    fácilmente la fecha y hora programadas.
                                </p>

                                <div style="
                                    text-align:center;
                                    margin:30px 0;
                                ">

                                    <a href="%s"
                                       target="_blank"
                                       style="
                                        display:inline-block;
                                        background:#0d6efd;
                                        color:#ffffff;
                                        text-decoration:none;
                                        padding:14px 24px;
                                        border-radius:8px;
                                        font-weight:bold;
                                       ">
                                        Agregar a Google Calendar
                                    </a>

                                </div>

                                <p style="
                                    font-size:14px;
                                    color:#666666;
                                    line-height:1.5;
                                ">
                                    Si tienes alguna pregunta o necesitas
                                    modificar tu cita, comunícate con
                                    nosotros llamando o escribiendo al 318 5377946.
                                </p>

                            </div>

                            <div style="
                                background:#f8f9fa;
                                padding:20px;
                                text-align:center;
                                font-size:13px;
                                color:#777777;
                            ">

                                Dr. Wilson Montenegro<br>
                                Odontología General y Especializada

                            </div>

                        </div>

                    </body>
                    </html>
                    """.formatted(
                    escaparHtml(nombre),
                    escaparHtml(servicio),
                    fecha,
                    horaInicio,
                    horaFin,
                    googleCalendarUrl);

            helper.setText(
                    contenido,
                    true);

            // ====================================================
            // ARCHIVO .ICS
            // ====================================================

            String contenidoIcs = generarArchivoIcs(
                    cita,
                    titulo,
                    ubicacion,
                    descripcion,
                    inicio,
                    fin);

            helper.addAttachment(
                    "cita.ics",
                    new ByteArrayResource(
                            contenidoIcs.getBytes(
                                    StandardCharsets.UTF_8)));

            mailSender.send(mensaje);

        } catch (MessagingException
                | RuntimeException e) {

            System.err.println(
                    "No se pudo enviar el correo de confirmación: "
                            + e.getMessage());
        }
    }

    // ============================================================
    // CORREO DE CANCELACIÓN DE CITA
    // ============================================================

    public void enviarCancelacionCita(
            Cita cita) {

        if (!citaValida(cita)) {
            return;
        }

        try {

            String destinatario = cita.getCliente()
                    .getUsuario()
                    .getEmail();

            String nombre = cita.getCliente()
                    .getUsuario()
                    .getName();

            String servicio = cita.getServicio() != null
                    ? cita.getServicio().getNombre()
                    : "Consulta odontológica";

            ZonedDateTime inicio = cita.getFechaEntrada()
                    .atZone(ZONA_COLOMBIA);

            ZonedDateTime fin = cita.getFechaSalida()
                    .atZone(ZONA_COLOMBIA);

            String fecha = inicio.format(FORMATO_FECHA);

            String horaInicio = inicio.format(FORMATO_HORA);

            String horaFin = fin.format(FORMATO_HORA);

            MimeMessage mensaje = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mensaje,
                    true,
                    StandardCharsets.UTF_8.name());

            helper.setFrom(correoRemitente);
            helper.setTo(destinatario);

            helper.setSubject(
                    "Cancelación de cita odontológica");

            String contenido = """
                    <!DOCTYPE html>
                    <html lang="es">

                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport"
                              content="width=device-width, initial-scale=1.0">
                    </head>

                    <body style="
                        margin:0;
                        padding:0;
                        background-color:#f4f7fb;
                        font-family:Arial,Helvetica,sans-serif;
                        color:#333333;
                    ">

                        <div style="
                            max-width:600px;
                            margin:30px auto;
                            background:#ffffff;
                            border-radius:12px;
                            overflow:hidden;
                            box-shadow:0 4px 15px rgba(0,0,0,0.08);
                        ">

                            <div style="
                                background:#dc3545;
                                color:#ffffff;
                                padding:30px;
                                text-align:center;
                            ">

                                <h1 style="
                                    margin:0;
                                    font-size:26px;
                                ">
                                    Dr. Wilson Montenegro Odontología General y Especializada
                                </h1>

                                <p style="
                                    margin:10px 0 0;
                                    font-size:15px;
                                ">
                                    Cancelación de cita odontológica
                                </p>

                            </div>

                            <div style="
                                padding:35px;
                            ">

                                <h2 style="
                                    color:#dc3545;
                                    margin-top:0;
                                ">
                                    Hola, %s
                                </h2>

                                <p style="
                                    font-size:16px;
                                    line-height:1.6;
                                ">
                                    Lamentamos informarte que tu cita
                                    odontológica ha sido
                                    <strong>cancelada</strong>.
                                </p>

                                <div style="
                                    background:#f8f9fa;
                                    border-radius:10px;
                                    padding:20px;
                                    margin:25px 0;
                                ">

                                    <h3 style="
                                        margin-top:0;
                                        color:#dc3545;
                                    ">
                                        Detalles de la cita
                                    </h3>

                                    <p>
                                        <strong>Servicio:</strong>
                                        %s
                                    </p>

                                    <p>
                                        <strong>Fecha:</strong>
                                        %s
                                    </p>

                                    <p>
                                        <strong>Hora:</strong>
                                        %s - %s
                                    </p>

                                    <p>
                                        <strong>Estado:</strong>
                                        Cancelada
                                    </p>

                                </div>

                                <p style="
                                    font-size:15px;
                                    line-height:1.6;
                                ">
                                    Si necesitas información adicional,
                                    deseas resolver alguna inquietud o
                                    quieres comunicarte con nosotros,
                                    puedes hacerlo llamando al:
                                </p>

                                <div style="
                                    text-align:center;
                                    margin:25px 0;
                                ">

                                    <div style="
                                        display:inline-block;
                                        background:#f8f9fa;
                                        padding:15px 25px;
                                        border-radius:8px;
                                        font-size:20px;
                                        font-weight:bold;
                                        color:#333333;
                                    ">
                                        318 5377946
                                    </div>

                                </div>

                                <p style="
                                    font-size:14px;
                                    color:#666666;
                                    line-height:1.5;
                                ">
                                    Estaremos atentos para ayudarte y,
                                    si lo deseas, podrás solicitar una
                                    nueva cita.
                                </p>

                            </div>

                            <div style="
                                background:#f8f9fa;
                                padding:20px;
                                text-align:center;
                                font-size:13px;
                                color:#777777;
                            ">

                                Dr. Wilson Montenegro<br>
                                Odontología General y Especializada

                            </div>

                        </div>

                    </body>
                    </html>
                    """.formatted(
                    escaparHtml(nombre),
                    escaparHtml(servicio),
                    fecha,
                    horaInicio,
                    horaFin);

            helper.setText(
                    contenido,
                    true);

            mailSender.send(mensaje);

        } catch (MessagingException
                | RuntimeException e) {

            System.err.println(
                    "No se pudo enviar el correo de cancelación: "
                            + e.getMessage());
        }
    }

    // ============================================================
    // GENERAR ARCHIVO ICS
    // ============================================================

    private String generarArchivoIcs(
            Cita cita,
            String titulo,
            String ubicacion,
            String descripcion,
            ZonedDateTime inicio,
            ZonedDateTime fin) {

        String fechaCreacion = ZonedDateTime.now(ZONA_UTC)
                .format(FORMATO_ICS);

        String fechaInicio = inicio.withZoneSameInstant(ZONA_UTC)
                .format(FORMATO_ICS);

        String fechaFin = fin.withZoneSameInstant(ZONA_UTC)
                .format(FORMATO_ICS);

        String uid = "cita-"
                + cita.getIdCita()
                + "@drwilsonmontenegro";

        StringBuilder ics = new StringBuilder();

        ics.append("BEGIN:VCALENDAR\r\n");
        ics.append("VERSION:2.0\r\n");
        ics.append("PRODID:-//Dr. Wilson Montenegro//Citas//ES\r\n");
        ics.append("CALSCALE:GREGORIAN\r\n");
        ics.append("METHOD:PUBLISH\r\n");

        ics.append("BEGIN:VEVENT\r\n");

        ics.append("UID:")
                .append(uid)
                .append("\r\n");

        ics.append("DTSTAMP:")
                .append(fechaCreacion)
                .append("\r\n");

        ics.append("DTSTART:")
                .append(fechaInicio)
                .append("\r\n");

        ics.append("DTEND:")
                .append(fechaFin)
                .append("\r\n");

        ics.append("SUMMARY:")
                .append(escaparIcs(titulo))
                .append("\r\n");

        ics.append("DESCRIPTION:")
                .append(escaparIcs(descripcion))
                .append("\r\n");

        ics.append("LOCATION:")
                .append(escaparIcs(ubicacion))
                .append("\r\n");

        ics.append("STATUS:CONFIRMED\r\n");

        ics.append("END:VEVENT\r\n");
        ics.append("END:VCALENDAR\r\n");

        return ics.toString();
    }

    // ============================================================
    // VALIDAR CITA
    // ============================================================

    private boolean citaValida(Cita cita) {

        if (cita == null) {
            return false;
        }

        if (cita.getCliente() == null) {
            return false;
        }

        if (cita.getCliente().getUsuario() == null) {
            return false;
        }

        if (cita.getCliente()
                .getUsuario()
                .getEmail() == null
                || cita.getCliente()
                        .getUsuario()
                        .getEmail()
                        .trim()
                        .isEmpty()) {

            return false;
        }

        if (cita.getFechaEntrada() == null) {
            return false;
        }

        if (cita.getFechaSalida() == null) {
            return false;
        }

        return true;
    }

    // ============================================================
    // ESCAPAR HTML
    // ============================================================

    private String escaparHtml(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    // ============================================================
    // ESCAPAR ICS
    // ============================================================

    private String escaparIcs(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("\\", "\\\\")
                .replace(";", "\\;")
                .replace(",", "\\,")
                .replace("\r\n", "\\n")
                .replace("\n", "\\n")
                .replace("\r", "\\n");
    }
}