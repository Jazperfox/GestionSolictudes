package com.example.MoonPhase.Controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.MoonPhase.Model.AppUsuario;
import com.example.MoonPhase.Model.AppUsuarioRepository;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

@Controller
public class ContentController {

    // Inyectamos el Repositorio para buscar el ID y el Tipo de Usuario
    @Autowired
    private AppUsuarioRepository usuarioRepository;

    // --- Método de utilidad para simplificar la obtención del usuario ---
    private Optional<AppUsuario> getLoggedInUser(Authentication auth) {
        if (auth == null) {
            return Optional.empty();
        }
        String username = auth.getName();
        return usuarioRepository.findByNombreUsuario(username);
    }


    @GetMapping("/login")
    public String login() {
        return "login";
    }

/*  @GetMapping("/index")
    public String home(){
      return "index";
    } */

//    @GetMapping("/index")
//    public String index(Model model, Authentication auth) {
//        Optional<AppUsuario> userOptional = getLoggedInUser(auth);
//
//        if (userOptional.isPresent()) {
//            AppUsuario user = userOptional.get();
//            // Pasamos el ID y el Nombre a la vista
//            model.addAttribute("idUsuario", user.getIdUsuario());
//            model.addAttribute("nombreUsuario", user.getNombreUsuario());
//            return "index";
//        }
//        // Si por alguna razón la sesión falla (improbable), redirigir al login
//        return "redirect:/login";
//    }

    @GetMapping("/index")
    public String index(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);

        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }



        // Agregamos variables comunes
        model.addAttribute("idUsuario", userOptional.get().getIdTipoUsuario());
        model.addAttribute("nombreUsuario", userOptional.get().getIdTipoUsuario());

        // Redirecciones según el tipo
        if (userOptional.get().getIdTipoUsuario() == 1) {
            return "indexadmin";
        } else if (userOptional.get().getIdTipoUsuario() == 2) {
            return "index";
        } else if (userOptional.get().getIdTipoUsuario() == 3) {
            return "indexAutoriza";
        }

        // Si no coincide ningún tipo, lo enviamos a index normal
        return "index";
    }


    @GetMapping("/indexadmin")
    public String indexAdmin(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);

        // 1. Verificamos que el usuario exista y que sea de Tipo 1 (Admin)
        if (userOptional.isPresent() && userOptional.get().getIdTipoUsuario() == 1) {
            AppUsuario user = userOptional.get();

            // 2. Pasamos el ID y el Nombre a la vista (como solicitaste)
            model.addAttribute("idUsuario", user.getIdUsuario());
            model.addAttribute("nombreUsuario", user.getNombreUsuario());

            return "indexadmin";
        }

        // Si el usuario es Tipo 2 y trata de entrar manualmente, se redirige a /index
        return "redirect:/index";
    }

}
