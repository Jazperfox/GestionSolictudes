package com.example.MoonPhase.Model;

import jakarta.persistence.*;
import lombok.Data;
import java.sql.Date;

@Data
@Entity
@Table(name = "solicitud")
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idSolicitud;

    @Column(name = "IdCategoriaSolicitud", nullable = false)
    private Long idCategoriaSolicitud;

    @Column(name = "IdUsuarioCreacion", nullable = false)
    private Long idUsuarioCreacion;

    @ManyToOne
    @JoinColumn(name = "IdUsuarioCreacion", insertable = false, updatable = false)
    private AppUsuario usuarioCreacion;

    @Column(name = "FechaCreacion", nullable = false)
    private Date fechaCreacion;

    @Column(name = "IdEstadoSolicitud", nullable = false)
    private Long idEstadoSolicitud;

    @Column(name = "IdPrioridad", nullable = false)
    private Long idPrioridad;

    @Column(name = "IdUsuario", nullable = true)
    private Long idUsuario;

    @Column(name = "Comentario", nullable = true)
    private String comentario;

    @Column(name = "Descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "correoSeguimiento", nullable = true)
    private String correo;

    @Column(name = "ruta_adjunto", nullable = true)
    private String rutaAdjunto;
}