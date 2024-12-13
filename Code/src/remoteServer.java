import java.rmi.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

import java.util.ArrayList;

public interface remoteServer extends Remote {
    
    
    public void createDirectory(String directoryPath)throws IOException;
    public void delete(String path) throws IOException;
    public boolean exists(String path)throws IOException;
    public void uploadFileToServer(String filePath, String serverPath) throws RemoteException, IOException;
    public byte[] downloadFileFromServer(String serverpath) throws RemoteException;
    public void readTextFile2(String filepath) throws IOException;
    public void copiar(String filename,String destinationPath) throws IOException;
    public List<String> showDirectory(String path) throws RemoteException;
    public  List<String> filePathsList(String directoryPath) throws IOException, RemoteException;
    public FolderData getFolderData(String directoryPath) throws RemoteException;
    
}

