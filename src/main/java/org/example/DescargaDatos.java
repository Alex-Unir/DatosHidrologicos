package org.example;
import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DescargaDatos {

    private static final String URL_ZIP = "https://www.miteco.gob.es/content/dam/miteco/es/agua/temas/evaluacion-de-los-recursos-hidricos/boletin-hidrologico/Historico-de-embalses/BD-Embalses.zip";

    static {
        // Desactivar la validación del certificado SSL
        disableSSLVerification();
    }

    public static void carpetas(String[] args) {
        try {
            // Obtener la fecha actual
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            String fechaActual = dateFormat.format(new Date());

            // Crear carpeta con fecha actual
            String nombreCarpeta = fechaActual + "_embalses";

            // Obtener la ruta del directorio actual
            Path rutaActual = Paths.get("").toAbsolutePath();

            // Subir 1 nivel
            Path rutaDestino = rutaActual.getParent();

            // Añadir la carpeta "Datoshigrologicos/FicherosMDB/" y el nombre de la carpeta con fecha
            rutaDestino = rutaDestino.resolve("Datoshigrologicos/FicherosMDB/" + nombreCarpeta);

            // Crear la carpeta si no existe
            if (!Files.exists(rutaDestino)) {
                Files.createDirectories(rutaDestino);
            }

            // Ruta completa del archivo ZIP descargado
            Path rutaZip = rutaDestino.resolve("BD-Embalses.zip");

            // Descargar + descomprimir ZIP
            descargarArchivo(URL_ZIP, rutaZip);
            descomprimirArchivoZip(rutaZip, rutaDestino);

            // Borrar solo el archivo ZIP
            if (Files.exists(rutaZip)) {
                Files.delete(rutaZip);
            }

            System.out.println("Proceso completado exitosamente.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static void descargarArchivo(String urlStr, Path destino) throws IOException {
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, destino, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void descomprimirArchivoZip(Path archivoZip, Path destino) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(archivoZip.toFile()))) {
            ZipEntry entrada;
            while ((entrada = zis.getNextEntry()) != null) {
                Path rutaNueva = destino.resolve(entrada.getName());
                if (entrada.isDirectory()) {
                    Files.createDirectories(rutaNueva);
                } else {
                    Files.createDirectories(rutaNueva.getParent());
                    try (OutputStream fos = Files.newOutputStream(rutaNueva)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

    private static void disableSSLVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = (hostname, session) -> true;
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
