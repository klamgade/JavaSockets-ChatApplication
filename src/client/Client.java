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

    public static final String HOST_NAME = "172.28.22.61";
    public static final int HOST_PORT = 8889; // host port number
    public static final String CLIENT = "Joe";
    protected Socket socket;
    protected boolean connected, waitingSuccessMsg;
    private InputStreamRunnable inputStream;
    private OutputStreamRunnable outputStream;
    
    
    public Client() {
     socket = null;
     connected = false;
     waitingSuccessMsg = false;
     inputStream = null;
     outputStream = null;
    }

     
    public void startClient() {

        // open socket & start input/output stream threads
        try {
            System.out.println("start client entered");
            socket = new Socket(HOST_NAME, HOST_PORT);
            System.out.println("new socket made");
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
    
    /**
     * Passes message to output thread for sending. Closes the in/out streams if the message
     * is an instance of DisconnectMessage
     * @param msg the Message object which is to be sent to the server
     */
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
    
    public boolean checkMessageSuccess(){
        if(connected){
            return inputStream.getSuccess();
        }
        else return false;
    }
    
    private class InputStreamRunnable implements Runnable{
        
        protected ObjectInputStream ois;
        protected Socket socket;
        private boolean inStreamConnected, wasSuccessful;
        
        public InputStreamRunnable(Socket s){
            ois = null;
            socket = s;
            inStreamConnected = false;
            wasSuccessful = false;
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
                        if((inMessage instanceof SuccessMessage) && waitingSuccessMsg){
                            SuccessMessage succMsg = (SuccessMessage)inMessage;
                            wasSuccessful = succMsg.getSuccess();
       System.out.println("wasSuccessful = " + wasSuccessful);
                            waitingSuccessMsg = false;
                        }
                        System.out.println("Message details are\n\tFROM: " +inMessage.getSource() +
                            "\n\tTO: " + inMessage.getDestination());
                    }
                }
            }
        }
        
        /**
         * Closes the InputObjectStream
         */
        public void close(){
            try{
                ois.close();
                inStreamConnected = false;
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }
        
        public boolean getSuccess(){
            return wasSuccessful;
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
        
        /**
         * Sends a Message to the server
         * @param msg Message object which is written to the ObjectOutputStream
         */
        public void sendMessage(Message msg){
            try{
                oos.writeObject(msg);
                oos.flush();
                System.out.println("just wrote a msg to the server");
            }
            catch(IOException e){
                System.out.println("Error while writing msg: " + e.getMessage());
            }
            // if outbound message is IdMessage, client needs to know if it has been accepted
                // by the server, inputStream needs to know whether a success message is expected or not
            if(msg instanceof IdMessage){
                waitingSuccessMsg = true;
            }
        }
        
        /**
         * Closes the ObjectOutputStream
         */
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

