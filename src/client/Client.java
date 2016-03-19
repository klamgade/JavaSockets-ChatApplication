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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import messages.*;

public class Client {

    public static final String HOST_NAME = "10.0.0.9";
    public static final int HOST_PORT = 8889; // host port number
    public static final String CLIENT = "Joe";
    protected Socket socket;
    protected boolean connected;
    private InputStreamRunnable inputStream;
    private OutputStreamRunnable outputStream;
    
    
    public Client() {
     socket = null;
     connected = false;
     inputStream = null;
     outputStream = null;
    }

     
    public void startClient() {

        // initiating the connection by implemening TCP client.
        try {
            socket = new Socket(HOST_NAME, HOST_PORT);
        } 
        catch (IOException e) {
            System.out.println("client could not make connection: " + e);
            System.exit(-1);
        }
       finally{
            inputStream = new InputStreamRunnable(socket);
            outputStream = new OutputStreamRunnable(socket);
            
            Thread in = new Thread(inputStream);
            Thread out = new Thread(outputStream);
            
            in.start();
            out.start();
            
            if(in.isAlive() && out.isAlive())
                connected = true;
        }
    }
    
    public void sendMessage(Message msg){
        if(connected){
            outputStream.sendMessage(msg);
        }

        if(msg instanceof DisconnectMessage ){
            outputStream.close();
            inputStream.close();
            connected = false;
        }
    } 
    
    /**
     * Shows if output stream is open
     * @return true if output stream is open, otherwise false
     */
    public boolean isConnected(){
        return connected;
    }
    
    private class InputStreamRunnable implements Runnable{
        
        protected ObjectInputStream ois;
        protected Socket socket;
        private boolean inStreamConnected;
        
        public InputStreamRunnable(Socket s){
            ois = null;
            socket = s;
            inStreamConnected = false;
        }
        
        @Override
        public void run(){
            try{
                ois = new ObjectInputStream(socket.getInputStream());
                inStreamConnected = true;
            }
            catch(IOException e){
                System.out.println("ois connection error: " + e.getMessage());
            }
            
            while(inStreamConnected){
                Message inMessage = null;
                try{
                    inMessage = (Message)ois.readObject();
                }
                catch(IOException | ClassNotFoundException e){
                    System.out.println("Error reading message: " +e.getMessage());
                }
                finally{
                    if(inMessage != null){
                        System.out.println("Message details are\n\tFROM: " +inMessage.getSource() +
                            "\n\tTO: " + inMessage.getDestination());
                    }
                }
            }
        }
        
        public void close(){
            try{
                ois.close();
                inStreamConnected = false;
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }
    
    private class OutputStreamRunnable implements Runnable{
        
        protected ObjectOutputStream oos;
        protected Socket socket;
        
        public OutputStreamRunnable(Socket s){
            socket = s;
            oos = null;
        }
        
        @Override
        public void run(){
            // obtaining streams from socket and layering with appopriate filtering streams
            try{
                 oos= new ObjectOutputStream(socket.getOutputStream());
                 oos.flush();
                 System.out.println("oos created");
            }
            catch(IOException e){
                System.out.println("oos connection error : " +e);
            }
        }
        
        public void sendMessage(Message msg){
            try{
                oos.writeObject(msg);
                oos.flush();
                System.out.println("just wrote a msg to the server");
            }
            catch(IOException e){
                System.out.println("Error while writing msg: " + e.getMessage());
            }
        }
        
        public void close(){
            try{
                oos.close();
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        Client client = new Client();
        client.startClient();
    }
}

