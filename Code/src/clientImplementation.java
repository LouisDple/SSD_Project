import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;

public class clientImplementation {
    /**
     * @param args
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        remoteServer t = (remoteServer) Naming.lookup("rmi://localhost:8080/FileManager");
        try{
        List<String> filesAndDirectories = t.showDirectory("C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\SSD_Project\\Code\\src\\testDir");
        System.out.println("Contenu du répertoire :");
        for (String fileOrDirectory : filesAndDirectories) {
            System.out.println(fileOrDirectory);
        }
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}