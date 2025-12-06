package com.example.MoonPhase.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.MoonPhase.Repository.AppUsuarioRepository;
import com.example.MoonPhase.Repository.CategoriaSolicitudRepository;
import com.example.MoonPhase.Repository.PrioridadRepository;
import com.example.MoonPhase.Repository.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

// IMPORTANTE: Tus repositorios están en el paquete Model según tus archivos
import com.example.MoonPhase.Model.*;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

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

        // Enviar ID y Nombre correctos del usuario logueado
        model.addAttribute("idUsuario", userOptional.get().getIdUsuario());
        model.addAttribute("nombreUsuario", userOptional.get().getNombreUsuario());

        long tipo = userOptional.get().getIdTipoUsuario();

        if (tipo == 1) {
            return "redirect:/indexadmin";
        } else if (tipo == 2) {
            return "index";
        } else if (tipo == 3) {
            return "redirect:/indexAutoriza";
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

            // Datos para el dashboard de ADMIN
            long tareasPendientes = solicitudRepository.countByIdUsuarioAndIdEstadoSolicitud(user.getIdUsuario(), 3L);
            model.addAttribute("tareasPendientes", tareasPendientes);

            List<Solicitud> misTareasRecientes = solicitudRepository.findTop5ByIdUsuarioOrderByFechaCreacionDesc(user.getIdUsuario());
            model.addAttribute("misTareas", misTareasRecientes);

            model.addAttribute("categorias", categoriaRepo.findAll());
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

            // --- ESTO ES LO NUEVO QUE ARREGLA EL ERROR ---

            // 1. Contador grande
            long pendientes = solicitudRepository.countByIdUsuarioIsNull();
            model.addAttribute("cantPendientes", pendientes);

            // 2. Carga de trabajo (Esto faltaba y causaba el error null en el HTML)
            List<AppUsuario> tecnicos = usuarioRepository.findByIdTipoUsuario(1L);
            Map<String, Long> cargaTrabajo = new HashMap<>();

            for (AppUsuario tech : tecnicos) {
                long tareasActivas = solicitudRepository.countByIdUsuarioAndIdEstadoSolicitud(tech.getIdUsuario(), 3L);
                cargaTrabajo.put(tech.getNombreUsuario(), tareasActivas);
            }
            model.addAttribute("cargaTrabajo", cargaTrabajo);

            // 3. Tabla inferior de últimas solicitudes
            List<Solicitud> ultimasPendientes = solicitudRepository.findTop5ByIdUsuarioIsNullOrderByFechaCreacionDesc();
            model.addAttribute("ultimasPendientes", ultimasPendientes);

            // 4. Lista de usuarios para traducir IDs a Nombres en la tabla
            model.addAttribute("usuarios", usuarioRepository.findAll());

            return "indexAutoriza";
        }

        return "redirect:/index";
    }
}