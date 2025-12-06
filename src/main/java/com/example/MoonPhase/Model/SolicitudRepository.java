package com.example.MoonPhase.Model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    @Query("SELECT s FROM Solicitud s WHERE s.idUsuario IS NULL")
    List<Solicitud> findSolicitudesNoAsignadasJPQL();


    @Query("SELECT s FROM Solicitud s  WHERE s.idEstadoSolicitud <> 4 AND s.idUsuario = :idUsuario")
    List<Solicitud> findSolicitudesAAutorizar(@Param("idUsuario") Long idUsuario);


    @Query("SELECT s FROM Solicitud s WHERE s.idUsuarioCreacion = :idUsuario AND s.idEstadoSolicitud IN (1, 3)")
    List<Solicitud> findMisSolicitudesActivas(@Param("idUsuario") Long idUsuario);

    @Query("SELECT s FROM Solicitud s WHERE s.idUsuarioCreacion = :idUsuario AND s.idEstadoSolicitud IN (2, 4)")
    List<Solicitud> findMisSolicitudesHistorial(@Param("idUsuario") Long idUsuario);

    long countByIdUsuarioIsNull();

    long countByIdUsuarioAndIdEstadoSolicitud(Long idUsuario, Long idEstado);

    List<Solicitud> findTop5ByIdUsuarioIsNullOrderByFechaCreacionDesc();

    List<Solicitud> findTop5ByIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);

    @Modifying
    @Transactional
    @Query("UPDATE Solicitud s SET s.idEstadoSolicitud = :estado, s.comentario = :comentario WHERE s.idSolicitud = :idSolicitud")
    void actualizarEstado(
            @Param("idSolicitud") Long idSolicitud,
            @Param("estado") int estado,
            @Param("comentario") String comentario
    );
}
