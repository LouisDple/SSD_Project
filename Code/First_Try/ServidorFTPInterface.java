// Interfaz del servidor RMI
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaz remota que define los métodos disponibles para la interacción entre el cliente y el servidor FTP.
 * Esta interfaz asegura la comunicación remota mediante RMI al permitir que los métodos definidos sean invocados
 * desde un cliente remoto. Los métodos deben lanzar RemoteException para gestionar errores de red.
 */
public interface ServidorFTPInterface extends Remote {
    String[] listarArchivos(String carpeta) throws RemoteException;
    boolean subirArchivo(String nombreArchivo, byte[] contenido) throws RemoteException;
    byte[] descargarArchivo(String nombreArchivo) throws RemoteException;
    boolean crearCarpeta(String nombreCarpeta) throws RemoteException;
}
