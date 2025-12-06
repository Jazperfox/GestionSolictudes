package com.example.MoonPhase.Repository;

import com.example.MoonPhase.Model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaSolicitudRepository extends JpaRepository<Categoria, Long> {}
