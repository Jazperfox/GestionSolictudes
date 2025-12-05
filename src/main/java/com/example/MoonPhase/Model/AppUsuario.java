package com.example.MoonPhase.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario") 
public class AppUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // ← Cambiar de AUTO a IDENTITY
    @Column(name = "IdUsuario")  // ← Agregar nombre de columna
    private Long idUsuario;  // ← Cambiar a camelCase
    
    @Column(name = "NombreUsuario", length = 100)
    private String nombreUsuario;  // ← Cambiar a camelCase
    
    @Column(name = "IdTipoUsuario")
    private Long idTipoUsuario;  // ← Cambiar a camelCase
    
    @Column(name = "clave", length = 255)
    private String clave;  // ← Cambiar a camelCase

    // Getters
    public Long getIdUsuario() {
        return idUsuario;
    }
    
    public String getNombreUsuario() {
        return nombreUsuario;
    }
    
    public Long getIdTipoUsuario() {
        return idTipoUsuario;
    }
    
    public String getClave() {
        return clave;
    }

    // Setters
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }
    
    public void setIdTipoUsuario(Long idTipoUsuario) {
        this.idTipoUsuario = idTipoUsuario;
    }
    
    public void setClave(String clave) {
        this.clave = clave;
    }
}