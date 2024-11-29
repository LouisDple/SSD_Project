import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class clientImplementation {
    /**
     * @param args
     * @throws NotBoundException
     * @throws MalformedURLException
     */
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        remoteServer t = (remoteServer) Naming.lookup("rmi://localhost:8080/FileManager");
        try{
        t.readTextFile2("C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\SSD_Project\\Code\\src\\testDir\\12.txt");
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}