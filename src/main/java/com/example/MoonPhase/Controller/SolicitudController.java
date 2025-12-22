package com.example.MoonPhase.Controller;

import com.example.MoonPhase.Model.*;
import com.example.MoonPhase.Repository.*;
import com.example.MoonPhase.Service.EmailService;
import com.example.MoonPhase.Service.FtpStorageService;
import jakarta.mail.MessagingException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.ResponseEntity;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/solicitud")
public class SolicitudController {
    private final SolicitudRepository solicitudRepo;
    private final CategoriaSolicitudRepository categoriaRepo;
    private final AppUsuarioRepository usuarioRepo;
    private final EstadoRepository estadoRepo;
    private final PrioridadRepository prioridadRepo;
    private final EmailService emailService;
    private final FtpStorageService ftpStorageService;


    public SolicitudController(SolicitudRepository solicitudRepo,
                               CategoriaSolicitudRepository categoriaRepo,
                               AppUsuarioRepository usuarioRepo,
                               EstadoRepository estadoRepo,
                               PrioridadRepository prioridadRepo,
                               EmailService emailService,
    FtpStorageService ftpStorageService){
        this.solicitudRepo = solicitudRepo;
        this.categoriaRepo = categoriaRepo;
        this.usuarioRepo = usuarioRepo;
        this.estadoRepo = estadoRepo;
        this.prioridadRepo = prioridadRepo;
        this.emailService = emailService;
        this.ftpStorageService = ftpStorageService;
    }



    @GetMapping("/crear")
    public String mostrarFormulario(Model model) {
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("prioridades", prioridadRepo.findAll());
        model.addAttribute("solicitud", new Solicitud());
        return "crearSolicitud";
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Solicitud solicitud
            ,@RequestParam("archivo") MultipartFile file,
                          RedirectAttributes redirectAttributes,Authentication auth) {

        try{
            solicitud.setIdEstadoSolicitud(1L);
            solicitud.setFechaCreacion(new java.sql.Date(System.currentTimeMillis()));

            Optional<AppUsuario> usuarioLogueado=getLoggedInUser(auth);
            if (usuarioLogueado.isPresent()) {
                solicitud.setIdUsuarioCreacion(usuarioLogueado.get().getIdUsuario());
            } else {
                solicitud.setIdUsuarioCreacion(1L);
            }

            Solicitud solicitudGuardada=solicitudRepo.save(solicitud);
            if(!file.isEmpty()){
                String rutaFtp=ftpStorageService.subirArchivoSolicitud(file,solicitudGuardada.getIdSolicitud());

                solicitudGuardada.setRutaAdjunto(rutaFtp);
                solicitudRepo.save(solicitudGuardada);
            }

            if (solicitudGuardada.getCorreo() != null && !solicitudGuardada.getCorreo().isEmpty()) {
                try {
                    String nombreEstado = "CREADA";
                    Optional<Estado> estOpt = estadoRepo.findById(1L);
                    if (estOpt.isPresent()) {
                        nombreEstado = estOpt.get().getEstado();
                    }

                    emailService.enviarCorreoHtml(
                            solicitudGuardada.getCorreo(),
                            "Solicitud Recibida - Ticket #" + solicitudGuardada.getIdSolicitud(),
                            solicitudGuardada.getIdSolicitud(),
                            nombreEstado,
                            "Hemos recibido tu solicitud correctamente. Un técnico la revisará pronto."
                    );

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


            redirectAttributes.addFlashAttribute("msg", "¡Solicitud creada exitosamente!");
            redirectAttributes.addFlashAttribute("tipo", "success");

        }catch (IOException e){
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg", "Error al cargar el archivo o guardar la solicitud.");
            redirectAttributes.addFlashAttribute("tipo", "error");


            return  "redirect:/solicitud/crear";
        }

        return "redirect:/solicitud/crear";
    }

    @GetMapping("/pendientes")
    public String solicitudesPendientes(Model model) {
        List<Solicitud> pendientes = solicitudRepo.findSolicitudesNoAsignadasJPQL();
        List<AppUsuario> usuarios = usuarioRepo.findByIdTipoUsuario(1L);

        model.addAttribute("pendientes", pendientes);
        model.addAttribute("usuarios", usuarios);

        return "pendientes";
    }

    @PostMapping("/asignar")
    public String asignarSolicitud(@RequestParam("idSolicitud") Long idSolicitud,
                                   @RequestParam("idUsuario") Long idUsuario,
                                   RedirectAttributes redirectAttributes) {

        try{
            Solicitud solicitud = solicitudRepo.findById(idSolicitud)
                    .orElseThrow(() -> new IllegalArgumentException("ID de solicitud inválida: " + idSolicitud));
            solicitud.setIdUsuario(idUsuario);
            solicitud.setIdEstadoSolicitud(3L);
            solicitudRepo.save(solicitud);

            redirectAttributes.addFlashAttribute("msg", "Técnico asignado correctamente.");
            redirectAttributes.addFlashAttribute("tipo", "success");
        }
        catch (Exception e){
            redirectAttributes.addFlashAttribute("msg", "Error al intentar asignar la solicitud.");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

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
        List<Solicitud> autorizar = solicitudRepo.findSolicitudesAAutorizar(idUsuarioLogueado);
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




    @PostMapping("/actualizarAutorizacion")
    public String actualizarAutorizacion(
            @RequestParam("idSolicitud") Long idSolicitud,
            @RequestParam("idEstado") Integer idEstado,
            @RequestParam(value = "comentario", required = false) String comentario,
            RedirectAttributes redirectAttributes) {

        try {
            solicitudRepo.actualizarEstado(idSolicitud, idEstado, comentario);

            Solicitud sol = solicitudRepo.findById(idSolicitud).orElse(null);
            if (sol != null && sol.getCorreo() != null) {
                try {
                    String nombreEstado = "ACTUALIZADO";
                    Optional<Estado> estOpt = estadoRepo.findById(Long.valueOf(idEstado));
                    if (estOpt.isPresent()) {
                        nombreEstado = estOpt.get().getEstado();
                    }

                    emailService.enviarCorreoHtml(
                            sol.getCorreo(),
                            "Actualización de tu Solicitud #" + sol.getIdSolicitud(),
                            sol.getIdSolicitud(),
                            nombreEstado,
                            comentario
                    );
                } catch (MessagingException e) {
                    e.printStackTrace();
                    System.out.println("Error enviando correo: " + e.getMessage());
                }
            }

            redirectAttributes.addFlashAttribute("msg", "Estado de la solicitud actualizado.");
            redirectAttributes.addFlashAttribute("tipo", "success");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("msg", "Hubo un error al actualizar.");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/solicitud/autorizar";
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<Resource> descargarAdjunto(@PathVariable Long id) {
        Solicitud solicitud = solicitudRepo.findById(id).orElse(null);

        if (solicitud == null || solicitud.getRutaAdjunto() == null || solicitud.getRutaAdjunto().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource archivo = ftpStorageService.descargarArchivo(solicitud.getRutaAdjunto());

            String nombreArchivo = solicitud.getRutaAdjunto().substring(solicitud.getRutaAdjunto().lastIndexOf("/") + 1);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + nombreArchivo + "\"")
                    .body(archivo);
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ver-archivo/{id}")
    public ResponseEntity<Resource> verAdjunto(@PathVariable Long id) {
        Solicitud solicitud = solicitudRepo.findById(id).orElse(null);

        if (solicitud == null || solicitud.getRutaAdjunto() == null || solicitud.getRutaAdjunto().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        try {
            Resource archivo = ftpStorageService.descargarArchivo(solicitud.getRutaAdjunto());
            String nombreArchivo = solicitud.getRutaAdjunto();

            MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
            String extension = nombreArchivo.substring(nombreArchivo.lastIndexOf(".") + 1).toLowerCase();

            switch (extension) {
                case "pdf": contentType = MediaType.APPLICATION_PDF; break;
                case "png": contentType = MediaType.IMAGE_PNG; break;
                case "jpg":
                case "jpeg": contentType = MediaType.IMAGE_JPEG; break;
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + nombreArchivo + "\"")
                    .body(archivo);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/mis-solicitudes")
    public String verMisSolicitudes(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);
        if (userOptional.isEmpty()) return "redirect:/login";

        Long idUsuario = userOptional.get().getIdUsuario();

        List<Solicitud> misSolicitudes = solicitudRepo.findMisSolicitudesActivas(idUsuario);

        model.addAttribute("solicitudes", misSolicitudes);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("estados", estadoRepo.findAll());
        model.addAttribute("prioridades", prioridadRepo.findAll());
        model.addAttribute("titulo", "Mis Solicitudes Activas");

        return "misSolicitudes";
    }

    @GetMapping("/historial")
    public String verHistorial(Model model, Authentication auth) {
        Optional<AppUsuario> userOptional = getLoggedInUser(auth);
        if (userOptional.isEmpty()) return "redirect:/login";

        Long idUsuario = userOptional.get().getIdUsuario();

        List<Solicitud> historial = solicitudRepo.findMisSolicitudesHistorial(idUsuario);

        model.addAttribute("solicitudes", historial);
        model.addAttribute("categorias", categoriaRepo.findAll());
        model.addAttribute("estados", estadoRepo.findAll());
        model.addAttribute("prioridades", prioridadRepo.findAll());
        model.addAttribute("titulo", "Historial de Solicitudes");

        return "historial";
    }


    @PostMapping("/eliminar")
    public String eliminarSolicitud(@RequestParam("idSolicitud") Long idSolicitud,
                                    RedirectAttributes redirectAttributes) {
        try {
            Optional<Solicitud> solicitudOpt = solicitudRepo.findById(idSolicitud);

            if (solicitudOpt.isPresent()) {
                Solicitud solicitud = solicitudOpt.get();

                if (solicitud.getRutaAdjunto() != null && !solicitud.getRutaAdjunto().isEmpty()) {
                    try {
                        ftpStorageService.eliminarArchivo(solicitud.getRutaAdjunto());
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.out.println("Advertencia: No se pudo borrar el archivo físico, pero se borrará el registro.");
                    }
                }

                solicitudRepo.deleteById(idSolicitud);

                redirectAttributes.addFlashAttribute("msg", "Solicitud eliminada correctamente.");
                redirectAttributes.addFlashAttribute("tipo", "success");

            } else {
                redirectAttributes.addFlashAttribute("msg", "La solicitud no existe.");
                redirectAttributes.addFlashAttribute("tipo", "warning");
            }

        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("msg", "Error crítico al eliminar.");
            redirectAttributes.addFlashAttribute("tipo", "error");
        }

        return "redirect:/solicitud/mis-solicitudes";
    }

}
