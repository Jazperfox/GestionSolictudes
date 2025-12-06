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

    @Autowired
    private AppUsuarioRepository usuarioRepository;

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



    @GetMapping("/index")
    public String index(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);

        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }


        model.addAttribute("idUsuario", userOptional.get().getIdTipoUsuario());
        model.addAttribute("nombreUsuario", userOptional.get().getIdTipoUsuario());

        if (userOptional.get().getIdTipoUsuario() == 1) {
            return "indexadmin";
        } else if (userOptional.get().getIdTipoUsuario() == 2) {
            return "index";
        } else if (userOptional.get().getIdTipoUsuario() == 3) {
            return "indexAutoriza";
        }

        return "index";
    }


    @GetMapping("/indexadmin")
    public String indexAdmin(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);

        if (userOptional.isPresent() && userOptional.get().getIdTipoUsuario() == 1) {
            AppUsuario user = userOptional.get();

            model.addAttribute("idUsuario", user.getIdUsuario());
            model.addAttribute("nombreUsuario", user.getNombreUsuario());

            return "indexadmin";
        }

        return "redirect:/index";
    }


    @GetMapping("/indexAutoriza")
    public String indexAutoriza(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);

        if (userOptional.isPresent() && userOptional.get().getIdTipoUsuario() == 3) {
            AppUsuario user = userOptional.get();

            model.addAttribute("idUsuario", user.getIdUsuario());
            model.addAttribute("nombreUsuario", user.getNombreUsuario());

            return "indexAutoriza";
        }

        return "redirect:/index";
    }

}
