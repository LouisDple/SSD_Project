import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

import java.io.FileReader;

import java.io.IOException;

import java.util.List;
import java.util.stream.Collectors;


public class FileManager extends UnicastRemoteObject implements remoteServer {
    private Path rootDirectory;

    
    public FileManager(String rootPath) throws RemoteException {
        super();
        this.rootDirectory = Paths.get(rootPath).toAbsolutePath().normalize();
        
    }
    
    
    @Override
    public void createDirectory(String directoryPath) {
        try {
            Path newDir = rootDirectory.resolve(directoryPath);
            Files.createDirectories(newDir);
            System.out.println("le fichier a bien été créé");   
        } catch (IOException e) {
            System.err.println("Erreur lors de la création du répertoire : " + e.getMessage());
            // Vous pouvez choisir de relancer l'exception ou de la gérer ici
        }
    }
    @Override
    public void delete(String path) throws IOException {
        try{
            Path fileOrDir = rootDirectory.resolve(path);
            Files.delete(fileOrDir);
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            // Vous pouvez choisir de relancer l'exception ou de la gérer ici
        }
    }
    @Override
    public boolean exists(String path) {
        Path fileOrDir = rootDirectory.resolve(path);
        return Files.exists(fileOrDir);
    }

    public void uploadFileToServer(String filePath, String serverPath) throws RemoteException, IOException {
        try {
            File file = new File(filePath);
            File serverPathFile = new File(serverPath);
    
            FileInputStream in = new FileInputStream(file);
            FileOutputStream out = new FileOutputStream(serverPathFile);
    
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
    
            in.close();
            out.flush();
            out.close();
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        System.out.println("Done writing data...");
    }

   // https://www.codejava.net/java-se/ftp/how-to-download-a-complete-folder-from-a-ftp-server
    
	
	public byte[] downloadFileFromServer(String serverpath) throws RemoteException {
					
		byte [] mydata;	
		
			File serverpathfile = new File(serverpath);			
			mydata=new byte[(int) serverpathfile.length()];
			FileInputStream in;
			try {
				in = new FileInputStream(serverpathfile);
				try {
					in.read(mydata, 0, mydata.length);
				} catch (IOException e) {
					
					e.printStackTrace();
				}						
				try {
					in.close();
				} catch (IOException e) {
				
					e.printStackTrace();
				}
				
			} catch (FileNotFoundException e) {
				
				e.printStackTrace();
			}		
			
			return mydata;
				 
	}
    
    @Override
    public void readTextFile2(String filepath) throws IOException {
        
        
            
            String file =filepath;
            
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String currentLine =reader.readLine();
           
            System.out.println();
            while(currentLine != null){
                System.out.println(currentLine);
                currentLine = reader.readLine();
                
                
            }
            reader.close();
    }
    @Override
    public void copiar(String filename,String localDestinationPath) throws IOException {
        Path ruta1=Paths.get(filename);
        FileChannel in = (FileChannel) Files.newByteChannel(ruta1);
        Path ruta2=Paths.get(localDestinationPath);
        FileChannel out=(FileChannel)
        Files.newByteChannel(ruta2,StandardOpenOption.CREATE,StandardOpenOption.APPEND);
        ByteBuffer buffer = ByteBuffer.allocate(1024*8);
        while(in.read(buffer) != -1) { //Lee del canal in
            buffer.flip(); // Prepara el buffer para escribir en el canal (leer del buffer)
            out.write(buffer); //Escribe en el canal out
            buffer.clear(); // Prepara el buffer para leer de nuevo (escribir en el buffer)
        }
        in.close();
        out.close();
    }   

    
  


    // ...

    @Override
    public List<String> showDirectory(String path) throws RemoteException {
        File directory = new File(path);
        if (!directory.exists() || !directory.isDirectory()) {
            throw new RemoteException("Le chemin spécifié n'est pas un répertoire");
        }

        List<String> filesAndDirectories = new ArrayList<>();
        for (File file : directory.listFiles()) {
            filesAndDirectories.add(file.getName());
        }

        return filesAndDirectories;
    }
    
    public  List<String> filePathsList(String directoryPath) throws IOException, RemoteException{
        List<String> filePaths = new ArrayList<>();
        File directory = new File(directoryPath);
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            for (File file : files) {
                if (file.isFile()) {
                    filePaths.add(file.getAbsolutePath());
                }
            }
        }
        return filePaths;

        
        
    }
    
    

    @Override
    public FolderData getFolderData(String directoryPath) throws RemoteException {
        System.out.println(directoryPath);
        File directory = new File(directoryPath);
        System.out.println(directoryPath);
        if (!directory.isDirectory()) {
            throw new RemoteException("Le chemin spécifié n'est pas un dossier : " + directoryPath);
        }

        return buildFolderData(directory);
    }

    private FolderData buildFolderData(File directory) throws RemoteException {
        FolderData folderData = new FolderData(directory.getName());

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                folderData.addSubFolder(buildFolderData(file));
            } else {
                folderData.addFile(file.getName());
            }
        }

        return folderData;
    }
}


    

    // Méthodes à implémenter
