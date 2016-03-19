/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 * 
 * A class which acts as a client by connecting to the server and sending/receiving message objects through
 * the in/out streams.
 * 
 * @author Kamal
 */
package client;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import messages.*;

public class Client {

    public static final String HOST_NAME = "10.0.0.9";
    public static final int HOST_PORT = 8889; // host port number
    public static final String CLIENT = "Joe";
    public ObjectOutputStream oos;
    public Socket socket;
    public boolean connected;
    
    /* CONSTRUCTOR
     */
    public Client() {
     oos = null ;
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
            oos= new ObjectOutputStream(socket.getOutputStream());
            oos.flush();
            System.out.println("oos created");
            connected = true;
            
       }
       catch(IOException e){
           System.out.println("Client error : " +e);
       }
  }
    public void sendMessage(Message msg){
        try{
        if(connected){
        oos.writeObject(msg);
        oos.flush();
        }
        
        if(msg instanceof DisconnectMessage ){
            oos.close();
            connected = false;
        }
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
    } 
    
    /**
     * Shows if output stream is open
     * @return true if output stream is open, otherwise false
     */
    public boolean isConnected(){
        return connected;
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }
}

