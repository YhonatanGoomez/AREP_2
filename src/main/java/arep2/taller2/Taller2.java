package arep2.taller2;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

public class Taller2 {

    public static void main(String[] args) throws IOException {

        ServerSocket puerto = null;
        puerto = new ServerSocket(35000);
        Socket cliente = null;
        while (!puerto.isClosed()) {

            System.out.println("Aplicacion funciona");
            cliente = puerto.accept();

            PrintWriter salida = new PrintWriter(cliente.getOutputStream(), true);
            BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(
                            cliente.getInputStream()));
            String esperaEntrada, esperaSalida;
            boolean firstLine = true;
            String uriString = "";
            while ((esperaEntrada = entrada.readLine()) != null) {
                if (firstLine) {
                    firstLine = false;
                    uriString = esperaEntrada.split(" ")[1];
                }
                if (!entrada.ready()) {
                    break;
                }
            }
            String responseBody = "";
            if (uriString != null && uriString.equals("/")) {
                responseBody = getIndexResponse();
                esperaSalida = getResponse(responseBody);
            } else if (uriString != null && !getFile(uriString).equals("Not Found")) {
                responseBody = getFile(uriString);
                esperaSalida = getResponse(responseBody);
            } else if (uriString != null && uriString.split("\\.")[1].equals("jpg")
                    || uriString.split("\\.")[1].equals("png")) {
                OutputStream outputStream = cliente.getOutputStream();
                File file = new File("src/main/resources/public/" + uriString);
                try {
                    BufferedImage bufferedImage = ImageIO.read(file);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

                    ImageIO.write(bufferedImage, uriString.split("\\.")[1], byteArrayOutputStream);
                    esperaSalida = getImageResponseHeader("");
                    dataOutputStream.writeBytes(esperaSalida);
                    dataOutputStream.write(byteArrayOutputStream.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                    responseBody = getFile(uriString);
                    esperaSalida = getResponse(responseBody);
                }
            } else {
                esperaSalida = getIndexResponse();
            }
            salida.println(esperaSalida);
            salida.close();
            entrada.close();
        }
        cliente.close();
        puerto.close();
    }

    /**
     * Método para obtener la respuesta del encabezado
     *
     * @param responseBody String de la respuesta dada por el Body
     * @return El encabezado del Body
     */
    private static String getImageResponseHeader(String responseBody) {
        return "HTTP/1.1 200 OK \r\n"
                + "Content-Type: image/jpg \r\n"
                + "\r\n";

    }

    /**
     * Método para obtener un archivo estático
     *
     * @param route String de la ruta para buscar fichero
     * @return los datos del fichero en un String
     */
    public static String getFile(String route) {
        Path file = FileSystems.getDefault().getPath("src/main/resources/public", route);
        String web = "";
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                web += line + "\n";
            }
        } catch (IOException x) {
            web = "Archivo no encontrado";
        }
        return web;
    }

    /**
     * Método para obtener la respuesta de una solicitud
     *
     * @param responseBody String de los datos que trae el Body
     * @return los datos del Body mas un pequeno encabezado
     */
    private static String getResponse(String responseBody) {
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n"
                + responseBody;
    }

    private static String getIndexResponse() {
        return "<!DOCTYPE html>\n"
                + "<html>\n"
                + "    <head>\n"
                + "        <title>Taller 2 AREP</title>\n"
                + "        <meta charset=\"UTF-8\">\n"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "    </head>\n"
                + "    <body>\n"
                + "        <h1>AREP Taller 2</h1>\n"
                + "        <form id=\"redirectForm\">\n"
                + "            <input type=\"text\" id=\"urlInput\" placeholder=\"Nombre y extension del archivo\">\n"
                + "            <button type=\"button\" onclick=\"redirectToURL()\">Ir</button>\n"
                + "        </form>\n"
                + "        <script>\n"
                + "            function redirectToURL() {\n"
                + "                var url = document.getElementById(\"urlInput\").value;\n"
                + "                window.location.href = url;\n"
                + "            }\n"
                + "        </script>\n"
                + "    </body>\n"
                + "</html>";
    }

}
