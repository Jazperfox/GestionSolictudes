package com.example.MoonPhase.Repository;

import com.example.MoonPhase.Model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    // 1. Para el Asignador (Admin Global): Ver qué solicitudes no tienen técnico
    @Query("SELECT s FROM Solicitud s WHERE s.idUsuario IS NULL")
    List<Solicitud> findSolicitudesNoAsignadasJPQL();

    // 2. Para la pantalla "Autorizar/Gestionar" (Técnico):
    // IMPORTANTE: Se filtra por estado 3 (En Proceso) para que desaparezcan las ya resueltas.
    @Query("SELECT s FROM Solicitud s WHERE s.idUsuario = :idUsuario AND s.idEstadoSolicitud = 3")
    List<Solicitud> findSolicitudesAAutorizar(@Param("idUsuario") Long idUsuario);

    // 3. Para el Dashboard (IndexAdmin) - Tarjeta Superior (Activas)
    @Query("SELECT s FROM Solicitud s WHERE s.idUsuario = :idUsuario AND s.idEstadoSolicitud = 3 ORDER BY s.fechaCreacion ASC")
    List<Solicitud> findMisPendientes(@Param("idUsuario") Long idUsuario);

    // 4. Para el Dashboard (IndexAdmin) - Tarjeta Inferior (Historial Resueltas)
    @Query("SELECT s FROM Solicitud s WHERE s.idUsuario = :idUsuario AND s.idEstadoSolicitud IN (4, 5) ORDER BY s.fechaCreacion DESC")
    List<Solicitud> findMisResueltas(@Param("idUsuario") Long idUsuario);

    // 5. Para el Solicitante (Empleado): Ver sus solicitudes abiertas
    @Query("SELECT s FROM Solicitud s WHERE s.idUsuarioCreacion = :idUsuario AND s.idEstadoSolicitud IN (1, 3)")
    List<Solicitud> findMisSolicitudesActivas(@Param("idUsuario") Long idUsuario);

    // 6. Para el Solicitante (Empleado): Ver su historial cerrado
    // Nota: Revisa si tus IDs de historial son 2,4,5. Aquí puse 2 y 4 basado en tu código anterior.
    @Query("SELECT s FROM Solicitud s WHERE s.idUsuarioCreacion = :idUsuario AND s.idEstadoSolicitud IN (2, 4, 5)")
    List<Solicitud> findMisSolicitudesHistorial(@Param("idUsuario") Long idUsuario);

    // Contadores y Utilidades
    long countByIdUsuarioIsNull();

    long countByIdUsuarioAndIdEstadoSolicitud(Long idUsuario, Long idEstado);

    List<Solicitud> findTop5ByIdUsuarioIsNullOrderByFechaCreacionDesc();

    List<Solicitud> findTop5ByIdUsuarioOrderByFechaCreacionDesc(Long idUsuario);

    // Actualización de estado
    @Modifying
    @Transactional
    @Query("UPDATE Solicitud s SET s.idEstadoSolicitud = :estado, s.comentario = :comentario WHERE s.idSolicitud = :idSolicitud")
    void actualizarEstado(
            @Param("idSolicitud") Long idSolicitud,
            @Param("estado") int estado,
            @Param("comentario") String comentario
    );
}