/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade[14845241] & Sez Prouting[0308852]
 * 
 * A class which acts as a client, to forward messages to and receive messages from the server.
 * Incoming message content is passed to the GUI for display
 * 
 * @author Kamal & Sez
 */
package client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import messages.*;

public class Client {

    protected final String TCP_HOST = "localhost";
    protected final String UDP_HOST = "224.0.0.3"; // multicast group address which allow access to all clients in the chat group.
    protected final int TCP_PORT = 8889; // TCP host port number
    protected final int UDP_PORT = 8888; // UDP host port number
    protected Socket tcpSocket; // socket for tcp layer 
    protected MulticastSocket udpSocket; // a udp datagramsocket for joining "groups" of other multicast hosts on the internet. 
    protected boolean connected, waitingSuccessMsg;
    private InputStreamRunnable inputStream;
    private OutputStreamRunnable outputStream;
    private UDP_Runnable udpStream;
    private ClientGUI clientGUI;

// constructor
    public Client(ClientGUI gui) {
        tcpSocket = null;
        udpSocket = null;
        connected = false;
        waitingSuccessMsg = false;
        inputStream = null;
        outputStream = null;
        udpStream = null;
        clientGUI = gui;
    }

    public void startClient() {

        // open tcpSocket & start input/output stream threads
        try {
            tcpSocket = new Socket(TCP_HOST, TCP_PORT);
            udpSocket = new MulticastSocket(UDP_PORT);
        } 
        catch (IOException e) {
            System.out.println("client could not make connection: " + e.getMessage());
            System.exit(-1);
        }
        inputStream = new InputStreamRunnable(tcpSocket);
        outputStream = new OutputStreamRunnable(tcpSocket);
        udpStream = new UDP_Runnable(udpSocket);

        // used for multiple threads of execution running concurrently
        Thread in = new Thread(inputStream); 
        Thread out = new Thread(outputStream);
        Thread udp = new Thread(udpStream);

        in.start();
        out.start();
        udp.start();

        if(in.isAlive() && out.isAlive() && udp.isAlive())
            connected = true;
        else System.out.println("One or more of the input/output streams did not start");
        
    }

    /**
     * Passes message to output thread for sending. Closes the in/out streams if the message
     * is an instance of DisconnectMessage
     * @param msg - the Message object which is to be sent to the server
     */
    public void sendMessage(Message msg){
        if(connected){
            outputStream.sendMessage(msg);
        }

        if(msg instanceof DisconnectMessage ){
            outputStream.close();
            inputStream.close();
            udpStream.close();
            connected = false;
            
            try{
                tcpSocket.close();
                udpSocket.close();
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
            clientGUI.updateClientList(new String[]{""});
            
        }
    } 

    /**
     * Shows if output stream is open
     * @return true if output stream is open, otherwise false
     */
    public boolean isConnected(){
        return connected;
    }

    /**
     * Checks the inputStream for results of the last SuccessMessage
     * @return the boolean value of the last SuccessMessage received
     */
    public boolean checkMessageSuccess(){
        if(connected){
            return inputStream.getSuccess();
        }
        else return false;
    }
//implementating Runnable interface to share the same object to multiple threads
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
        public void run(){  // object's run method to be called while starting a separately executing thread everytime.
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
                    if(inMessage != null){
                        if((inMessage instanceof SuccessMessage) && waitingSuccessMsg){
                            SuccessMessage succMsg = (SuccessMessage)inMessage;
                            wasSuccessful = succMsg.getSuccess();
                            waitingSuccessMsg = false;
                        }

                        if(inMessage instanceof ToMessage){
                            ToMessage msg = (ToMessage)inMessage;
                            clientGUI.updateMessageDisplay(msg.getMessageBody());
                        }
                }
                    Thread.yield(); // give other threads a chance
            }
            try{
                ois.close();
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }

        /**
         * Closes the InputObjectStream
         */
        public void close(){
            inStreamConnected = false;
        }

        /**
         * 
         * @return the value of wasSuccessful
         */
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
            // obtaining streams from tcpSocket and layering with appopriate filtering streams
            try{
                 oos= new ObjectOutputStream(socket.getOutputStream());
                 oos.flush();
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
            }
            catch(IOException e){
                System.out.println("Error while writing msg: " + e.getMessage());
            }
            // if outbound message is IdMessage, client needs to know if it has been accepted
                //inputStream needs to know whether a success message is expected or not
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
    
    private class UDP_Runnable implements Runnable{
        private MulticastSocket socket;
        private ObjectInputStream udp_ois;
        private boolean udpIsConnected;
        
        public UDP_Runnable(MulticastSocket socket){
            this.socket = socket;
            udpIsConnected = false;
            
            try{
                InetAddress group = InetAddress.getByName(UDP_HOST); // INetAddress representing  an Internet Protocol (IP) address.
                socket.joinGroup(group); // joins a multicast group
            }
            catch(IOException e){
                System.out.println("Could not join group" + e.getMessage());
            }
            udpIsConnected = true;
        }
        
        @Override
        public void run(){
            String[] clientList = null; 
            byte[] buffer = new byte[10000]; // preallocate temporary arrays large enough for DatagramPacket
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while(udpIsConnected){
                try{
                    socket.receive(packet);
                    udp_ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
                    clientList = (String[])udp_ois.readObject();
                }
                catch(IOException | ClassNotFoundException e){
                    System.out.println("Trouble receiving UDP packets"+ e.getMessage());
                }
                    clientGUI.updateClientList(clientList);
                    Thread.yield();
            }
        }
        /**
         * Closes the UDP_Runnable ObjectInputputStream
         */
        public void close(){
            try{
                udp_ois.close();
            }catch(IOException e){
                System.out.println("could not close UDP ois stream: " + e.getMessage());
            }
            udpIsConnected = false;
        }
    }
}
