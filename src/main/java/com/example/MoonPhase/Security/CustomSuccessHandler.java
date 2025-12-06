package com.example.MoonPhase.Security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.MoonPhase.Model.AppUsuario;
import com.example.MoonPhase.Model.AppUsuarioRepository; // Importa tu repositorio

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private AppUsuarioRepository usuarioRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        // 1. Obtenemos el username del usuario que acaba de loguearse
        String username = authentication.getName();

        // 2. Buscamos sus datos completos en la BD usando tu repositorio
        Optional<AppUsuario> userOptional = usuarioRepository.findByNombreUsuario(username);

        // 3. Verificamos el tipo y redireccionamos
        if (userOptional.isPresent()) {
            AppUsuario usuario = userOptional.get();

            // Lógica solicitada: 1 -> index, 2 -> indexadmin
            if (usuario.getIdTipoUsuario() == 1) {
                response.sendRedirect("/indexadmin");
            } else if (usuario.getIdTipoUsuario() == 2) {
                response.sendRedirect("/index");
            }
            else if(usuario.getIdTipoUsuario() == 3) {
                response.sendRedirect("/indexAutoriza");
            }
            else {
                // Por seguridad, si es otro número, mandamos a index
                response.sendRedirect("/index");
            }
        } else {
            // Caso raro: se logueó pero no se encuentra en BD (improbable)
            response.sendRedirect("/login?error");
        }
    }
}