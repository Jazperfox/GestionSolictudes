package com.example.MoonPhase.Controller;

import com.example.MoonPhase.Model.*;
import com.example.MoonPhase.Repository.AppUsuarioRepository;
import com.example.MoonPhase.Repository.MensajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/mensajes")
public class MensajeriaController {

    @Autowired
    private AppUsuarioRepository usuarioRepo;
    @Autowired
    private MensajeRepository mensajeRepo;

    private AppUsuario getLoggedUser(Authentication auth) {
        if (auth == null) return null;
        return usuarioRepo.findByNombreUsuario(auth.getName()).orElse(null);
    }

    @GetMapping
    public String index(Model model, Authentication auth) {
        return cargarVistaMensajeria(null, model, auth);
    }

    @GetMapping("/{idUsuario}")
    public String chatConUsuario(@PathVariable Long idUsuario, Model model, Authentication auth) {
        return cargarVistaMensajeria(idUsuario, model, auth);
    }

    private String cargarVistaMensajeria(Long idSeleccionado, Model model, Authentication auth) {
        AppUsuario yo = getLoggedUser(auth);
        if (yo == null) return "redirect:/login";

        // 1. Cargar lista de contactos (todos menos yo)
        List<AppUsuario> contactos = usuarioRepo.findAll();
        contactos.removeIf(u -> u.getIdUsuario().equals(yo.getIdUsuario()));
        model.addAttribute("contactos", contactos);
        model.addAttribute("usuarioActual", yo);

        // 2. Si hay un chat seleccionado, cargar mensajes
        if (idSeleccionado != null) {
            Optional<AppUsuario> dest = usuarioRepo.findById(idSeleccionado);
            if (dest.isPresent()) {
                model.addAttribute("chatSeleccionado", dest.get());
                List<Mensaje> historial = mensajeRepo.obtenerConversacion(yo.getIdUsuario(), idSeleccionado);
                model.addAttribute("historial", historial);
            }
        } else {
            model.addAttribute("chatSeleccionado", null);
        }

        return "mensajeria";
    }

    @PostMapping("/enviar")
    public String enviar(@RequestParam("idDestinatario") Long idDestinatario,
                         @RequestParam("contenido") String contenido,
                         Authentication auth) {
        AppUsuario yo = getLoggedUser(auth);
        Optional<AppUsuario> dest = usuarioRepo.findById(idDestinatario);

        if (yo != null && dest.isPresent() && !contenido.trim().isEmpty()) {
            Mensaje msg = new Mensaje(yo, dest.get(), contenido);
            mensajeRepo.save(msg);
        }
        return "redirect:/mensajes/" + idDestinatario;
    }
}