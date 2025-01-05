// Cliente RMI
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.io.*;

/**
 * Clase ClienteFTP que permite la conexión remota al servidor FTP mediante RMI.
 * Esta clase proporciona funcionalidades para interactuar con el servidor, como listar archivos,
 * subir archivos y descargar archivos. El cliente se conecta al servidor a través de una dirección IP
 * y un puerto específicos, y realiza las operaciones utilizando los métodos definidos en la interfaz remota.
 */
public class ClienteFTP {
    private ClienteFTP() {}

    public static void main(String[] args) {
        try {
            // Configuración de la dirección IP y puerto del servidor
            String direccionIP = (args.length > 0) ? args[0] : "localhost";
            int puerto = (args.length > 1) ? Integer.parseInt(args[1]) : 1099;

            Registry registry = LocateRegistry.getRegistry(direccionIP, puerto);
            ServidorFTPInterface stub = (ServidorFTPInterface) registry.lookup("ServidorFTP");

            // Listar archivos en el servidor
            String[] archivos = stub.listarArchivos("");
            System.out.println("Archivos disponibles en el servidor:");
            for (String archivo : archivos) {
                System.out.println(archivo);
            }

            // Subir un archivo al servidor
            String nombreArchivoSubida = "archivo.txt";
            byte[] contenidoArchivo = "Contenido del archivo de prueba".getBytes();
            boolean resultadoSubida = stub.subirArchivo(nombreArchivoSubida, contenidoArchivo);
            System.out.println("Subida de archivo: " + (resultadoSubida ? "Exitosa" : "Fallida"));

            // Descargar un archivo del servidor
            String nombreArchivoDescarga = "archivo.txt";
            byte[] contenidoDescargado = stub.descargarArchivo(nombreArchivoDescarga);
            if (contenidoDescargado != null) {
                System.out.println("Archivo descargado: " + nombreArchivoDescarga);
                System.out.println(new String(contenidoDescargado));
            } else {
                System.out.println("Error al descargar el archivo.");
            }
        } catch (Exception e) {
            System.err.println("Error en el cliente: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
