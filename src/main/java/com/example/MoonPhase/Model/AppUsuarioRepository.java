package com.example.MoonPhase.Model;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUsuarioRepository extends JpaRepository<AppUsuario, Long> {

    Optional<AppUsuario> findByNombreUsuario(String nombreUsuario);

}
