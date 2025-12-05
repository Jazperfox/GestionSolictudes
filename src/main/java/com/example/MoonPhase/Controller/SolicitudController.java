package com.example.MoonPhase.Controller;

import com.example.MoonPhase.Model.*;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

// import java.time.LocalDate;

@Controller
@RequestMapping("/solicitud")
public class SolicitudController {
    private final SolicitudRepository solicitudRepo;
    private final CategoriaSolicitudRepository categoriaRepo;
    private final AppUsuarioRepository usuarioRepo;
    private final EstadoRepository estadoRepo;
    private final PrioridadRepository prioridadRepo;
    private  EmailService emailService;


    public SolicitudController(SolicitudRepository solicitudRepo,
                               CategoriaSolicitudRepository categoriaRepo,
                               AppUsuarioRepository usuarioRepo,
                               EstadoRepository estadoRepo,
                               PrioridadRepository prioridadRepo,
                               EmailService emailService) {
        this.solicitudRepo = solicitudRepo;
        this.categoriaRepo = categoriaRepo;
        this.usuarioRepo = usuarioRepo;
        this.estadoRepo = estadoRepo;
        this.prioridadRepo = prioridadRepo;
        this.emailService = emailService;

    }

    @GetMapping("/crear")
    public String mostrarFormulario(Model model) {
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("solicitud", new Solicitud());
        return "crearSolicitud";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Solicitud solicitud) {

        solicitud.setIdPrioridad(1L);
        solicitud.setIdEstadoSolicitud(1L);
        solicitud.setFechaCreacion(new java.sql.Date(System.currentTimeMillis()));

        solicitudRepo.save(solicitud);
        return "redirect:/index";
    }

    @GetMapping("/pendientes")
    public String solicitudesPendientes(Model model) {
        //Busca solicitudes pendientes con idusuario = null
        List<Solicitud> pendientes = solicitudRepo.findSolicitudesNoAsignadasJPQL();
        // Carga los usuarios
        List<AppUsuario> usuarios = usuarioRepo.findAll();

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("usuarios", usuarios);

        return "pendientes";
    }

    @PostMapping("/asignar")
    public String asignarSolicitud(@RequestParam("idSolicitud") Long idSolicitud,
                                   @RequestParam("idUsuario") Long idUsuario) {

        //busca solicitud por id
        Solicitud solicitud = solicitudRepo.findById(idSolicitud)
                .orElseThrow(() -> new IllegalArgumentException("ID de solicitud inválida: " + idSolicitud));
        //asigna el nuevo id de usuario a la solicitud
        solicitud.setIdUsuario(idUsuario);
        //cambia el estado a EN PROCESO
        solicitud.setIdEstadoSolicitud(3L);
        // Guardar los cambios
        solicitudRepo.save(solicitud);

        return "redirect:/solicitud/pendientes";
    }

    private Optional<AppUsuario> getLoggedInUser(Authentication auth) {
        if (auth == null) {
            return Optional.empty();
        }
        String username = auth.getName();
        return usuarioRepo.findByNombreUsuario(username);
    }

    @GetMapping("/autorizar")
    public String solicitudesAutorizar(Model model, Authentication auth) {

        Optional<AppUsuario> userOptional = getLoggedInUser(auth);
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        AppUsuario user = userOptional.get();
        Long idUsuarioLogueado = user.getIdUsuario();
        //Busca solicitudes diferentes de estado 4, o sea, que no estén denegadas y que estén asignadas al usuario logueado
        List<Solicitud> autorizar = solicitudRepo.findSolicitudesAAutorizar(idUsuarioLogueado);
        // carga los listados para que se muestren los nombre en la vista, y no los ID
        List<AppUsuario> usuarios = usuarioRepo.findAll();
        List<Categoria> categorias = categoriaRepo.findAll();
        List<Estado> estados = estadoRepo.findAll();
        List<Prioridad> prioridades = prioridadRepo.findAll();

        model.addAttribute("autorizar", autorizar);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", estados);
        model.addAttribute("prioridades", prioridades);

        return "autorizar";
    }

    @GetMapping("/actualizar")
    public String actualizarAutorizacion(Model model, Authentication auth) {

        Optional<AppUsuario> userOptional = getLoggedInUser(auth);
        if (userOptional.isEmpty()) {
            return "redirect:/login";
        }

        AppUsuario user = userOptional.get();
        Long idUsuarioLogueado = user.getIdUsuario();
        //Busca solicitudes diferentes de estado 4, o sea, que no estén denegadas y que estén asignadas al usuario logueado
        List<Solicitud> autorizar = solicitudRepo.findSolicitudesAAutorizar(idUsuarioLogueado);
        // carga los listado para que se muestren los nombre y no los ID de los registros
        List<AppUsuario> usuarios = usuarioRepo.findAll();
        List<Categoria> categorias = categoriaRepo.findAll();
        List<Estado> estados = estadoRepo.findAll();
        List<Prioridad> prioridades = prioridadRepo.findAll();

        model.addAttribute("autorizar", autorizar);
        model.addAttribute("usuarios", usuarios);
        model.addAttribute("categorias", categorias);
        model.addAttribute("estados", estados);
        model.addAttribute("prioridades", prioridades);

        return "redirect:/index";
    }


    @PostMapping("/actualizarAutorizacion")
    public String actualizarAutorizacion(
            @RequestParam("idSolicitud") Long idSolicitud,
            @RequestParam("idEstado") Integer idEstado,
            @RequestParam(value = "comentario", required = false) String comentario,
            RedirectAttributes redirectAttributes) {

        // Actualizar registro
        solicitudRepo.actualizarEstado(idSolicitud, idEstado, comentario);

        // Buscar la solicitud para enviar el correo
        Solicitud sol = solicitudRepo.findById(idSolicitud).orElse(null);
        if (sol != null && sol.getCorreo() != null) {


            try {
                emailService.enviarCorreoHtml(
                        sol.getCorreo(),
                        "Actualización de tu Solicitud #" + sol.getIdSolicitud(),
                        sol.getIdSolicitud(),
                        idEstado,
                        comentario
                );

            } catch (MessagingException e) {
                e.printStackTrace();
                System.out.println("Error enviando correo: " + e.getMessage());
            }
        }

        redirectAttributes.addFlashAttribute("msg", "Solicitud actualizada correctamente");

        return "redirect:/solicitud/autorizar";
    }

}
