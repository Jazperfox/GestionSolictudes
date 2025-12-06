package com.example.MoonPhase.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "mensajes")
public class Mensaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensaje") // <--- CORRECCIÓN IMPORTANTE
    private Long idMensaje;

    @ManyToOne
    @JoinColumn(name = "id_remitente", nullable = false)
    private AppUsuario remitente;

    @ManyToOne
    @JoinColumn(name = "id_destinatario", nullable = false)
    private AppUsuario destinatario;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "fecha_hora") // <--- CORRECCIÓN IMPORTANTE
    private LocalDateTime fechaHora;

    // Constructor vacío
    public Mensaje() {}

    // Constructor útil
    public Mensaje(AppUsuario remitente, AppUsuario destinatario, String contenido) {
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.contenido = contenido;
        this.fechaHora = LocalDateTime.now();
    }
}