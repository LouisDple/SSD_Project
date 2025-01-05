// Clase principal del servidor
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.io.*;

/**
 * Clase principal del servidor FTP que implementa la interfaz ServidorFTPInterface.
 * Esta clase gestiona las operaciones de transferencia de archivos entre el cliente y el servidor.
 * El servidor utiliza RMI para permitir que los clientes se conecten remotamente y realicen
 * operaciones como listar archivos, subir archivos, descargar archivos y crear carpetas.
 */
public class ServidorFTP implements ServidorFTPInterface {
    private static final String ROOT_FOLDER = "servidor_archivos";

    public ServidorFTP() {
        super();
        crearDirectorioRaiz(ROOT_FOLDER);
    }

    private void crearDirectorioRaiz(String carpetaRaiz) {
        File directorio = new File(carpetaRaiz);
        if (!directorio.exists()) {
            if (!directorio.mkdirs()) {
                System.err.println("Error: No se pudo crear el directorio raíz en " + carpetaRaiz);
            } else {
                System.out.println("Directorio raíz creado correctamente en: " + carpetaRaiz);
            }
        }
    }

    @Override
    public String[] listarArchivos(String carpeta) throws RemoteException {
        File directorio = new File(ROOT_FOLDER + File.separator + carpeta);
        if (directorio.exists() && directorio.isDirectory()) {
            return directorio.list();
        } else {
            return new String[] {"Carpeta no encontrada"};
        }
    }

    @Override
    public boolean subirArchivo(String nombreArchivo, byte[] contenido) throws RemoteException {
        try (FileOutputStream fos = new FileOutputStream(ROOT_FOLDER + File.separator + nombreArchivo)) {
            fos.write(contenido);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] descargarArchivo(String nombreArchivo) throws RemoteException {
        try (FileInputStream fis = new FileInputStream(ROOT_FOLDER + File.separator + nombreArchivo)) {
            byte[] contenido = fis.readAllBytes();
            return contenido;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean crearCarpeta(String nombreCarpeta) throws RemoteException {
        File carpeta = new File(ROOT_FOLDER + File.separator + nombreCarpeta);
        return carpeta.mkdir();
    }

    public static void main(String[] args) {
        try {
            // Configuración de la dirección IP y puerto del servidor
            String direccionIP = (args.length > 0) ? args[0] : "localhost";
            int puerto = (args.length > 1) ? Integer.parseInt(args[1]) : 1099;

            ServidorFTP servidor = new ServidorFTP();
            ServidorFTPInterface stub = (ServidorFTPInterface) UnicastRemoteObject.exportObject(servidor, 0);
            Registry registry = LocateRegistry.createRegistry(puerto);
            registry.rebind("ServidorFTP", stub);
            System.out.println("Servidor FTP listo y ejecutándose en " + direccionIP + ":" + puerto + "...");
        } catch (Exception e) {
            System.err.println("Error en el servidor: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
