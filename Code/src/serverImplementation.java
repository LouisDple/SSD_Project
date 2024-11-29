import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.*;
import java.io.*;

public class serverImplementation {
    
    public static void main(String args[]) {
        try{
            //System.setProperty("java.rmi.server.hostname", "192.168.187.221");
            //take input the port number
            InputStreamReader is = new InputStreamReader(System.in);
            BufferedReader br = new BufferedReader(is);
           
            String portname, registryURL; 
            System.out.println("Enter port:");
            //String portNum = (br.readLine()).trim();
            String portNum = "8080";
            //create remote object
            FileManager exportedObj = new FileManager("C:\\Users\\Depelley Louis\\Desktop\\courscarta\\ssd\\SSD_Project\\SSD_Project");
            //generate registry url for rmi registry server
            
            registryURL = "rmi://localhost:" + portNum + "/FileManager";
            //assosiate your object with the url in rmiregistry 
            LocateRegistry.createRegistry(8080);
            Naming.rebind(registryURL, exportedObj);
            System.out.println("ADDserver ready");
        } catch (Exception re){
            System.out.println("Exception in ADDserver: " + re);
        }
    }
}



