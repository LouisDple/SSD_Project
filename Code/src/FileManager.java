import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.io.BufferedReader;



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
    public List<Path> listDirectory(String directoryPath) throws IOException {
        try{  
            Path dir = rootDirectory.resolve(directoryPath);
            
            return Files.list(dir).collect(Collectors.toList());
        }
        catch(IOException e){
            e.printStackTrace();
            System.err.println("erreur lors de la création de la liste : " + e.getMessage());
            return null;
        }
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
    // Méthodes à implémenter
}