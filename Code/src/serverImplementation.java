import java.rmi.*;

import java.rmi.registry.LocateRegistry;

import java.io.*;
// it is the code that will run the registry server for basic tests there is not much to customize
public class serverImplementation {
    
    public static void main(String args[]) {
        try{
            //System.setProperty("java.rmi.server.hostname", "192.168.187.221");
            String registryURL; 
            
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



