import java.rmi.*;
import java.io.IOException;

import java.util.List;


// this is the rmi interface you juste have tu put the methods headers from FileManager class

public interface remoteServer extends Remote {
    
    
    public void createDirectory(String directoryPath)throws IOException;
    public void deleteDirectory(String path) throws IOException;
    public boolean exists(String path)throws IOException; 
    public byte[] downloadFileFromServer(String serverpath) throws RemoteException;
    public void readTextFile2(String filepath) throws IOException;
    public void uploadFileToServer(byte[] mydata, String serverpath, int length) throws RemoteException ;
    public void copiar(String filename,String destinationPath) throws IOException;
    public  List<String> filePathsList(String directoryPath) throws IOException, RemoteException;
    public FolderData getFolderData(String directoryPath) throws RemoteException;
  
    
}

