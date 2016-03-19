package client;

/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 * @author Kamal
 */

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import messages.*;
/**
 *
 * @author carmelsez
 */
public class Client {

    public static final String HOST_NAME = "10.0.0.9";
    public static final int HOST_PORT = 8889; // host port number
    public static final String CLIENT = "Joe";
    public OutputObjectStream oos;
    public Socket socket;
    public boolean connected;
    
    /* CONSTRUCTOR
     */
    public Client() {
        oos = null;
        socket = null;
        connected = false;
    }

    // method to send message from client to server 
    public void startClient() {
       
        // initiating the connection by implemening TCP client.
        try {
            socket = new Socket(HOST_NAME, HOST_PORT);
        } catch (IOException e) {
            System.out.println("client could not make connection: " + e);
            System.exit(-1);
        }
        // obtaining streams from socket and layering with appopriate filtering streams
       try{
            ObjectOutputStream oos= new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            System.out.println("oos created");
            connected = true;
            
       }
       catch(IOException e){
           System.out.println("Client error : " +e);
       }
  }
    // method to get user's name
    String userName = "Joe";
    IdMessage msg = new IdMessage(userName);
    
    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }
}
