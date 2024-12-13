import java.rmi.Naming;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.nio.file.StandardCopyOption;

public class ClientImplementation2 {

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

    public static void main(String[] args) {
        try {
            remoteServer server = (remoteServer) Naming.lookup("rmi://localhost:8080/FileManager");
            String serverFolderPath = "C:\\\\Users\\\\Depelley Louis\\\\Desktop\\\\courscarta\\\\ssd\\\\SSD_Project\\\\SSD_Project\\\\Code\\\\src\\\\testDir\\\\dir1\\\\";
            String localFolderPath = "C:\\\\Users\\\\Depelley Louis\\\\Desktop\\\\courscarta\\\\ssd\\\\SSD_Project\\\\SSD_Project\\\\Code\\\\src\\\\testDir\\\\Nouveau dossier\\\\";

            FolderData folderData = server.getFolderData(serverFolderPath);
            saveFolderData(server, folderData, serverFolderPath, localFolderPath);

            System.out.println("Téléchargement terminé avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
