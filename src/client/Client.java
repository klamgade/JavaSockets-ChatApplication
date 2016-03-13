package client;

/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 * @author 
 */

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import messages.*;
/**
 *
 * @author carmelsez
 */
public class Client {

    public static final String HOST_NAME = "172.28.22.61";
    public static final int HOST_PORT = 8889; // host port number
    public static final int CLIENT = 2;
    
    /* CONSTRUCTOR
     */
    public Client() {

    }

    // method to send message from client to server 
    public void startClient() {
        Socket socket = null;
        // creating an instance of ToMessage class
        ToMessage message = new ToMessage(Message.SERVER, CLIENT, "Hello!!");
        message.setMessageBody("Hi! I am trying to send this message to the Server");
        System.out.println("fasd"+ message.getMessageBody());
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
       oos.writeObject(message); // sending the message object to server
       }
       catch(IOException e){
           System.out.println("Client error : " +e);
       }
  }
    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }
}
