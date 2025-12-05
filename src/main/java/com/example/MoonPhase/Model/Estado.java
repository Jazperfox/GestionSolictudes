package com.example.MoonPhase.Model;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "estadoSolicitud")
public class Estado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstado;

    @Column(name="NombreEstado", nullable = false)
    private String estado;
}
