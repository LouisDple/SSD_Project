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



public class FileManager extends UnicastRemoteObject implements remoteServer {
    private Path rootDirectory;

    
    public FileManager(String rootPath) throws RemoteException {
        super();
        this.rootDirectory = Paths.get(rootPath).toAbsolutePath().normalize();
        
    }
    
    
    //this method creates a directory o the severside 
    @Override
    public void createDirectory(String directoryPath) {
        try {
            Path newDir = rootDirectory.resolve(directoryPath);
            Files.createDirectories(newDir);
            System.out.println("folder created");   
        } catch (IOException e) {
            System.err.println("eror during folder creation: " + e.getMessage());
            // Vous pouvez choisir de relancer l'exception ou de la gérer ici
        }
    }
    
    //this method deletes a directory on the serverside 
    @Override
    public void deleteDirectory(String path) throws IOException {
        try{
            Path fileOrDir = rootDirectory.resolve(path);
            Files.delete(fileOrDir);
        } catch (IOException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
            
        }
    }
    //checks if a file or directory exists on the serverside 
    @Override
    public boolean exists(String path) {
        Path fileOrDir = rootDirectory.resolve(path);
        return Files.exists(fileOrDir);
    }

    //uploads a file on the server given an array of bytes of the file
    public void uploadFileToServer(byte[] mydata, String serverpath, int length) throws RemoteException {
			
    	try {
    		File serverpathfile = new File(serverpath);
    		FileOutputStream out=new FileOutputStream(serverpathfile);
    		byte [] data=mydata;
			
    		out.write(data);
			out.flush();
	    	out.close();
	 
		} catch (IOException e) {
			
			e.printStackTrace();
		}
    	
    	System.out.println("Done writing data...");
		
	}

   // https://www.codejava.net/java-se/ftp/how-to-download-a-complete-folder-from-a-ftp-server
    
	
	
   //returns an array of bytes to the client so it can download file from the server , the array needs to ba hanfled on the client implementation
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
    //this methode reads a file on the serverside
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
    
    //this method copies a file to another folder on the serverside
    @Override 
    public void copiar(String filePath,String destinationPath) throws IOException {
        Path ruta1=Paths.get(filePath);
        FileChannel in = (FileChannel) Files.newByteChannel(ruta1);
        Path ruta2=Paths.get(destinationPath);
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
    //this method shows all 
    
    
    //this method creates an array of strings showing the paths of a chosen directory on server side
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
            return filePaths;
        }
        
        else{
            System.out.println("this is not a directory");
            return null;

        }

        
        
    }
    
    

    @Override
    // this is the function you call in the client implementation to use  buildfolder data (you use it only for directories in the server side )
    public FolderData getFolderData(String directoryPath) throws RemoteException {
        
        File directory = new File(directoryPath);
        
        if (!directory.isDirectory()) {
            throw new RemoteException("Sepecified path is not a directory : " + directoryPath);
        }

        return buildFolderData(directory);
    }

    
    //this function encapsulate and returns all the paths of files in a directory and of its subdirectories in a recusrive way  (ton use for the server side)
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
