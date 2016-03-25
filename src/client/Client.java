/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
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

    //protected final String TCP_HOST = "172.28.117.89";
    protected final String TCP_HOST = "localhost";
    protected final String UDP_HOST = "224.0.0.3";
    protected final int TCP_PORT = 8889; // TCP host port number
    protected final int UDP_PORT = 8888; // UDP host port number
    protected Socket tcpSocket;
    protected MulticastSocket udpSocket;
    protected boolean connected, waitingSuccessMsg;
    private InputStreamRunnable inputStream;
    private OutputStreamRunnable outputStream;
    private UDP_Runnable udpStream;
    private ClientGUI clientGUI;


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
            System.out.println("start client entered");
            tcpSocket = new Socket(TCP_HOST, TCP_PORT);
            System.out.println("tcp socket created");
            udpSocket = new MulticastSocket(UDP_PORT);
            System.out.println("udp socket made");
        } 
        
        catch (IOException e) {
            System.out.println("client could not make connection: " + e.getMessage());
            System.exit(-1);
        }
        inputStream = new InputStreamRunnable(tcpSocket);
        outputStream = new OutputStreamRunnable(tcpSocket);
        udpStream = new UDP_Runnable(udpSocket);

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
                    if(inMessage != null){
                        if((inMessage instanceof SuccessMessage) && waitingSuccessMsg){
                            SuccessMessage succMsg = (SuccessMessage)inMessage;
                            wasSuccessful = succMsg.getSuccess();
   System.out.println("wasSuccessful = " + wasSuccessful);
                            waitingSuccessMsg = false;
                        }

                        if(inMessage instanceof ToMessage){
                            ToMessage msg = (ToMessage)inMessage;
                            //String displayText = msg.getSource() + ": " + msg.getMessageBody();
                            //chatDisplay.setText(displayText);
                            clientGUI.updateMessageDisplay(msg.getMessageBody());
                            
                        
                        System.out.println("Received a ToMessage\n\tFROM: " +msg.getSource() +
                                            "\n\tTO: " + msg.getDestination() +
                                            "\n\tBODY: " + msg.getMessageBody());
                        }
                }
                    Thread.yield();
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
            // obtaining streams from tcpSocket and layering with appopriate filtering streams
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
                InetAddress group = InetAddress.getByName(UDP_HOST);
                socket.joinGroup(group);
            }
            catch(IOException e){
                System.out.println("Could not join group" + e.getMessage());
            }
            udpIsConnected = true;
        }
        
        @Override
        public void run(){
            System.out.println("udp thread is running");
            String[] clientList = null;
            byte[] buffer = new byte[10000];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            
            while(udpIsConnected){
                System.out.println("udp is connected, about to try reception");
                try{
 System.out.println("udp client is ready to Rx");
                    socket.receive(packet);
 System.out.println("a udp packet has been received");
                    udp_ois = new ObjectInputStream(new ByteArrayInputStream(buffer));
                    System.out.println("number of udp bytes available: " + udp_ois.available());
                    clientList = (String[])udp_ois.readObject();
 System.out.println("\nlist of clients received by Client.java:");
 for(String s : clientList)                   
 System.out.println(s);
                }
                catch(IOException | ClassNotFoundException e){
                    System.out.println("Trouble receiving UDP packets"+ e.getMessage());
                }
                    clientGUI.updateClientList(clientList);
                    Thread.yield();
            }
        }
        
        public void close(){
            try{
                udp_ois.close();
            }catch(IOException e){
                System.out.println("could not close UDP ois stream: " + e.getMessage());
            }
            finally{
                udpIsConnected = false;
            }
        }
    }
}