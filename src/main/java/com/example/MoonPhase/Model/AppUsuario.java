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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdUsuario")
    private Long idUsuario;
    
    @Column(name = "NombreUsuario", length = 100)
    private String nombreUsuario;
    
    @Column(name = "IdTipoUsuario")
    private Long idTipoUsuario;
    
    @Column(name = "clave", length = 255)
    private String clave;

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