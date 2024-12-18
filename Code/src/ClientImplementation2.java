import java.rmi.Naming;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;


public class clientImplementation2 {

    public static void saveFolderData(remoteServer server, FolderData folderData, String serverFolderPath, String localPath) throws Exception {
        Files.createDirectories(Paths.get(localPath));

        for (String fileName : folderData.getFiles()) {
            byte[] fileData = fileName.getBytes();
            Files.write(Paths.get(localPath, fileName), fileData);
        }

        for (FolderData subFolder : folderData.getSubFolders()) {
            saveFolderData(server, subFolder, serverFolderPath + File.separator + subFolder.getName(), localPath + File.separator + subFolder.getName());
        }
    }

    public static void uploadFileHandler(remoteServer server , byte[] fileData,String clientPath, String serverPath) throws Exception{
        File clientpathfile = new File(clientPath);
				byte [] mydata=new byte[(int) clientpathfile.length()];
				FileInputStream in=new FileInputStream(clientpathfile);	
					System.out.println("uploading to server...");		
				 in.read(mydata, 0, mydata.length);					 
				 server.uploadFileToServer(mydata, serverPath, (int) clientpathfile.length());
				 
				 in.close();
    }

    public static FolderData buildFolderDataLocal(String directoryPath) {
        FolderData folderData = new FolderData(new File(directoryPath).getName());

        for (File file : new File(directoryPath).listFiles()) {
            if (file.isDirectory()) {
                folderData.addSubFolder(buildFolderDataLocal(file.getAbsolutePath()));
            } else {
                folderData.addFile(file.getName());
            }
        }

        return folderData;
    }
    
    
    public static FolderData buildFolderDataLocal(File directory) throws Exception {
        
        FolderData folderData = new FolderData(directory.getName());

        for (File file : directory.listFiles()) {
            if (file.isDirectory()) {
                folderData.addSubFolder(buildFolderDataLocal(file));
            } else {
                folderData.addFile(file.getName());
            }
        }

        return folderData;
    }

    public static void uploadFolderHandler(remoteServer server , FolderData folderData, String clientPath, String serverPath) throws Exception{
        System.out.println("ici 1");
        Files.createDirectories(Paths.get(serverPath));
        
        try{
            System.out.println("ici i");
            for (String fileName : folderData.getFiles()) {
                
                
                System.out.println("ici ii");
                String clientFilePath = clientPath + File.separator + fileName;
                String serverFilePath = serverPath + File.separator + fileName;

                byte[] fileData = Files.readAllBytes(Paths.get(clientFilePath));
                System.out.println("uploading to server...");
                server.uploadFileToServer(fileData, serverFilePath, fileData.length);
                System.out.println("ici 2");
            }

            for (FolderData subFolder : folderData.getSubFolders()) {
                uploadFolderHandler(server, subFolder, clientPath + File.separator + subFolder.getName(), serverPath + File.separator + subFolder.getName());
            }
        }catch(Exception e){
            
            e.printStackTrace();
        }

        

        
    }
        

    public static void downloadFileHandler(remoteServer server,String clientPath, String serverPath) throws Exception{
        byte [] mydata = server.downloadFileFromServer(serverPath);
				System.out.println("downloading...");
				File clientpathfile = new File(clientPath);
				FileOutputStream out=new FileOutputStream(clientpathfile);				
	    		out.write(mydata);
				out.flush();
		    	out.close();
    }

    public static void printFolderData(FolderData folderData) {
        System.out.println("Nom du dossier : " + folderData.getName());
        System.out.println("Fichiers :");
        for (String file : folderData.getFiles()) {
            System.out.println(" - " + file);
        }
        System.out.println("Sous-dossiers :");
        for (FolderData subFolder : folderData.getSubFolders()) {
            System.out.println(" - " + subFolder.getName());
            printFolderData(subFolder); // appel récursif pour afficher les sous-dossiers
        }
    }
        


    public static void main(String[] args) {
        try {
            remoteServer server = (remoteServer) Naming.lookup("rmi://localhost:8080/FileManager");
            String serverFolderPath = "C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\Code";
            String localFolderPath = "C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\Code\\src\\dosstest";
            
            
            //to upload a whole folder to server/////////////////////
            File directory = new File(localFolderPath);
            FolderData folderData = buildFolderDataLocal(directory);
            
            server.createDirectory(serverFolderPath + File.separator + folderData.getName());
            serverFolderPath = serverFolderPath + File.separator + folderData.getName();
            uploadFolderHandler(server,folderData,localFolderPath,serverFolderPath);
            ///////////////////////////////////////////////////////////////
            
            // to download a whole folder from server://////////////:
            FolderData folderData2 = server.getFolderData(serverFolderPath);
            saveFolderData(server, folderData2, serverFolderPath, localFolderPath);
            //////////////////////////////////////////////////////////////////////
            System.out.println("Téléchargement terminé avec succs.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
