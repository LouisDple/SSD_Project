
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface FileServerInterface extends Remote {
    String uploadFileBlock(String fileName, byte[] data, int blockNumber) throws RemoteException;
    byte[] downloadFileBlock(String fileName, int blockNumber) throws RemoteException, IOException;
    int getFileBlockCount(String fileName) throws RemoteException, IOException;
    String createDirectory(String path) throws RemoteException, IOException;
    String delete(String path) throws RemoteException, IOException;
    public String uploadDirectoryTree(String localPath, String serverPath) throws RemoteException;
    List<String> downloadDirectoryTree(String path) throws RemoteException, IOException;
}
