package com.example.MoonPhase.Service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@Service
public class FtpStorageService {

    @Value("${ftp.server.host}")
    private String server;

    @Value("${ftp.server.port}")
    private int port;

    @Value("${ftp.server.user}")
    private String user;

    @Value("${ftp.server.password}")
    private String password;

    @Value("${ftp.server.base-dir-solicitudes}")
    private String baseDirSolicitudes;


    public String subirArchivoSolicitud(MultipartFile file, Long idSolicitud) throws IOException {
        FTPClient ftpClient = new FTPClient();
        String rutaFinalFtp = "";

        try {
            ftpClient.connect(server, port);
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                throw new IOException("No se pudo conectar al servidor FTP. Código de respuesta: " + replyCode);
            }

            boolean login = ftpClient.login(user, password);
            if (!login) {
                throw new IOException("Credenciales FTP incorrectas.");
            }

            // Configuración importante para transferencia de archivos binarios (imágenes, pdf, etc.)
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // 1. Crear estructura de directorios: base/idSolicitud/
            String directorioDestino = baseDirSolicitudes + idSolicitud + "/";
            crearDirectorios(ftpClient, directorioDestino);

            // 2. Cambiar al directorio de destino
            ftpClient.changeWorkingDirectory(directorioDestino);

            // 3. Nombre del archivo a guardar (usamos el original)
            String nombreArchivo = file.getOriginalFilename();
            // (Opcional: podrías agregar un UUID al nombre para evitar duplicados si se permite subir múltiples archivos por solicitud)
            // String nombreArchivo = UUID.randomUUID() + "_" + file.getOriginalFilename();

            rutaFinalFtp = directorioDestino + nombreArchivo;

            // 4. Subir el archivo
            try (InputStream inputStream = file.getInputStream()) {
                boolean done = ftpClient.storeFile(nombreArchivo, inputStream);
                if (!done) {
                    throw new IOException("Falló la subida del archivo al FTP.");
                }
            }

        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }

        return rutaFinalFtp;
    }

    // Método auxiliar para crear directorios anidados si no existen
    private void crearDirectorios(FTPClient ftpClient, String rutaPath) throws IOException {
        String[] directorios = rutaPath.split("/");
        if (directorios.length > 0) {
            // Intentar ir al directorio raíz primero si la ruta empieza con /
            if(rutaPath.startsWith("/")) {
                ftpClient.changeWorkingDirectory("/");
            }

            for (String dir : directorios) {
                if (!dir.isEmpty()) {
                    boolean existe = ftpClient.changeWorkingDirectory(dir);
                    if (!existe) {
                        if (!ftpClient.makeDirectory(dir)) {
                            throw new IOException("No se pudo crear el directorio: " + dir);
                        }
                        ftpClient.changeWorkingDirectory(dir);
                    }
                }
            }
        }
    }
}