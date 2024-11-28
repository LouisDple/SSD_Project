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
        FileManager t = (FileManager) Naming.lookup("rmi://localhost:5670/FileManager");
        System.out.println("Hola server...= " + t.sayHello("Pepe") + "Hora actual=" + t.getTime());
    }
}