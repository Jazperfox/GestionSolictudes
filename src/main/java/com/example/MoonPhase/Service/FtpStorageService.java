package com.example.MoonPhase.Service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.io.ByteArrayOutputStream;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import java.io.ByteArrayOutputStream;


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

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            String directorioDestino = baseDirSolicitudes + idSolicitud + "/";
            crearDirectorios(ftpClient, directorioDestino);

            ftpClient.changeWorkingDirectory(directorioDestino);

            String nombreArchivo = file.getOriginalFilename();

            rutaFinalFtp = directorioDestino + nombreArchivo;

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


    private void crearDirectorios(FTPClient ftpClient, String rutaPath) throws IOException {
        if (rutaPath.startsWith("/")) {
            ftpClient.changeWorkingDirectory("/");
        }

        String[] directorios = rutaPath.split("/");

        for (String dir : directorios) {
            if (!dir.isEmpty()) {
                boolean existe = false;
                try {
                    existe = ftpClient.changeWorkingDirectory(dir);
                } catch (IOException e) {
                    System.out.println("Aviso: El servidor lanzó error al buscar directorio '" + dir + "': " + e.getMessage());
                    existe = false;
                }

                if (!existe) {
                    System.out.println("El directorio '" + dir + "' no existe. Intentando crear...");
                    if (!ftpClient.makeDirectory(dir)) {
                        throw new IOException("No se pudo crear el directorio: " + dir + ". Verifique permisos del usuario FTP.");
                    }
                    if (!ftpClient.changeWorkingDirectory(dir)) {
                        throw new IOException("Se creó el directorio '" + dir + "' pero no se pudo acceder a él.");
                    }
                }
            }
        }
    }


    public Resource descargarArchivo(String rutaFtp) throws IOException {
        FTPClient ftpClient = new FTPClient();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            if(!ftpClient.retrieveFile(rutaFtp, outputStream)) {
                throw new IOException("No se pudo descargar el archivo: " + rutaFtp);
            }

            return new ByteArrayResource(outputStream.toByteArray());

        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
            outputStream.close();
        }
    }


    public void eliminarArchivo(String rutaFtp) throws IOException {
        if (rutaFtp == null || rutaFtp.isEmpty()) {
            return;
        }

        FTPClient ftpClient = new FTPClient();
        try {
            ftpClient.connect(server, port);
            ftpClient.login(user, password);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            boolean deleted = ftpClient.deleteFile(rutaFtp);

            if (deleted) {
                System.out.println("Archivo eliminado del FTP: " + rutaFtp);
            } else {
                System.out.println("No se pudo borrar el archivo (tal vez no existía): " + rutaFtp);
            }

        } finally {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        }
    }

}