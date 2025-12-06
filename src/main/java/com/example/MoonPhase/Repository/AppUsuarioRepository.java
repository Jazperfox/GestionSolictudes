package com.example.MoonPhase.Repository;

import java.util.List;
import java.util.Optional;

import com.example.MoonPhase.Model.AppUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUsuarioRepository extends JpaRepository<AppUsuario, Long> {

    Optional<AppUsuario> findByNombreUsuario(String nombreUsuario);

    List<AppUsuario> findByIdTipoUsuario(Long idTipoUsuario);

}
