import java.rmi.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;



public interface remoteServer extends Remote {
    
    public List<Path> listDirectory(String directoryPath) throws IOException;
    public void createDirectory(String directoryPath)throws IOException;
    public void delete(String path) throws IOException;
    public boolean exists(String path)throws IOException;
    public void readTextFile2(String filepath) throws IOException;
    public void copiar(String filename,String destinationPath) throws IOException;
    public List<String> showDirectory(String path) throws RemoteException;
}