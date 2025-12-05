package com.example.MoonPhase.Model;

import jakarta.persistence.*;

@Entity
@Table(name = "categoriaSolicitud")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCategoria;

    @Column(name = "NombreCategoria")
    private String nombre;

    @Column(name = "Descripcion")
    private String descripcion;

    public Categoria() {}

    public Categoria(Long id, String nombre) {
        this.idCategoria = id;
        this.nombre = nombre;
    }

    // Getts y Setts
    public Long getIdCategoria() { return idCategoria; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }

    public void setIdCategoria(Long idCategoria) { this.idCategoria = idCategoria; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
