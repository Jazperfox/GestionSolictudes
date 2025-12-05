package com.example.MoonPhase.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "prioridad")
public class Prioridad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdPrioridad")
    private Integer idPrioridad;

    @Column(name = "NombrePrioridad", length = 50)
    private String nombrePrioridad;

    // Getters y setters
    public Integer getIdPrioridad() { return idPrioridad; }
    public void setIdPrioridad(Integer idPrioridad) { this.idPrioridad = idPrioridad; }

    public String getNombrePrioridad() { return nombrePrioridad; }
    public void setNombrePrioridad(String nombrePrioridad) { this.nombrePrioridad = nombrePrioridad; }
}
