package com.example.MoonPhase.Repository;

import com.example.MoonPhase.Model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Long> {

    @Query("SELECT m FROM Mensaje m WHERE " +
            "(m.remitente.idUsuario = :usuario1 AND m.destinatario.idUsuario = :usuario2) OR " +
            "(m.remitente.idUsuario = :usuario2 AND m.destinatario.idUsuario = :usuario1) " +
            "ORDER BY m.fechaHora ASC")
    List<Mensaje> obtenerConversacion(@Param("usuario1") Long usuario1, @Param("usuario2") Long usuario2);
}