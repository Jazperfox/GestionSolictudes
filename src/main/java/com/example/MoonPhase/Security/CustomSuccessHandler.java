package com.example.MoonPhase.Security;

import java.io.IOException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.MoonPhase.Model.AppUsuario;
import com.example.MoonPhase.Repository.AppUsuarioRepository;

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
        
        String username = authentication.getName();

        Optional<AppUsuario> userOptional = usuarioRepository.findByNombreUsuario(username);

        if (userOptional.isPresent()) {
            AppUsuario usuario = userOptional.get();

            if (usuario.getIdTipoUsuario() == 1) {
                response.sendRedirect("/indexadmin");
            } else if (usuario.getIdTipoUsuario() == 2) {
                response.sendRedirect("/index");
            }
            else if(usuario.getIdTipoUsuario() == 3) {
                response.sendRedirect("/indexAutoriza");
            }
            else {
                response.sendRedirect("/index");
            }
        } else {
            response.sendRedirect("/login?error");
        }
    }
}