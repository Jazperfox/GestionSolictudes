package com.example.MoonPhase.Controller;

import java.util.Optional;

import com.example.MoonPhase.Model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ContentController {

    @Autowired
    private AppUsuarioRepository usuarioRepository;
    @Autowired
    private SolicitudRepository solicitudRepository;

    @Autowired
    private CategoriaSolicitudRepository categoriaRepo;

    @Autowired
    private PrioridadRepository prioridadRepo;

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
        model.addAttribute("nombreUsuario", userOptional.get().getNombreUsuario());

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

            long tareasPendientes = solicitudRepository.countByIdUsuarioAndIdEstadoSolicitud(user.getIdUsuario(), 3L);
            model.addAttribute("tareasPendientes", tareasPendientes);

            List<Solicitud> misTareasRecientes = solicitudRepository.findTop5ByIdUsuarioOrderByFechaCreacionDesc(user.getIdUsuario());
            model.addAttribute("misTareas", misTareasRecientes);

            model.addAttribute("categorias", categoriaRepo.findAll()); // Asegúrate de inyectar categoriaRepo en este Controller si no está
            model.addAttribute("prioridades", prioridadRepo.findAll());

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

            long pendientes = solicitudRepository.countByIdUsuarioIsNull();
            model.addAttribute("cantPendientes", pendientes);

            List<AppUsuario> tecnicos = usuarioRepository.findByIdTipoUsuario(1L);
            Map<String, Long> cargaTrabajo = new HashMap<>();

            for (AppUsuario tech : tecnicos) {
                long tareasActivas = solicitudRepository.countByIdUsuarioAndIdEstadoSolicitud(tech.getIdUsuario(), 3L);
                cargaTrabajo.put(tech.getNombreUsuario(), tareasActivas);
            }
            model.addAttribute("cargaTrabajo", cargaTrabajo);

            List<Solicitud> ultimasPendientes = solicitudRepository.findTop5ByIdUsuarioIsNullOrderByFechaCreacionDesc();
            model.addAttribute("ultimasPendientes", ultimasPendientes);

            model.addAttribute("usuarios", usuarioRepository.findAll());

            return "indexAutoriza";
        }

        return "redirect:/index";
    }

}
