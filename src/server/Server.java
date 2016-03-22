/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 * 
 * A class which acts as a server, to manage chat messages between various clients
 * @author Kamal & Sez
 */

package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import messages.*;

public class Server {
    public static final int PORT=8889;
    protected final int IN_THREAD=0, OUT_THREAD=1; // array locations for each client thread
    protected ServerSocket serverSocket;
    protected boolean ssConnected;  //connection status of serverSocket
    protected Map<String, Runnable[]> clientList;   //list of clients by name & incomming thread
    
    public Server(){
        ssConnected = false;
        clientList = new ConcurrentHashMap<String, Runnable[]>();
        //FOR DEV: obtain local IP
        try{
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        }
        catch(UnknownHostException e){
            System.out.println(e.getMessage());
        }
    }
    
    /**
     * Creates a server socket & passes incoming clients to new threads
     */
    public void startServer(){
        
        // Establish server socket
        try{
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(30000);
            ssConnected = true;
System.out.println("IN startServer().try#1 : server started, connected = " + ssConnected);
        }
        catch(IOException e){
            System.out.println("Socket Issue: " + e.getMessage());
            System.exit(-1);
        }
        
        // Pass incoming clients to new threads
        while(ssConnected){
            try{
                Socket socket = serverSocket.accept();
//System.out.println("Connection made with " + socket.getInetAddress());
                InputStreamRunnable inStream = new InputStreamRunnable(socket);
                OutputStreamRunnable outStream = new OutputStreamRunnable(socket);
                
                Runnable[] threadArray = new Runnable[]{inStream, outStream};
                inStream.passArray(threadArray);
                
                Thread inThread = new Thread(inStream);
                Thread outThread = new Thread(outStream);
                inThread.start();
                outThread.start();
            }
            catch(IOException e){
                System.out.println("There's a problem accepting the client socket Cx: " + e.getMessage());
            }
        }
    }
    
    /**
     * Used to manage the ObjectInputStream, including opening, closing & inbound message handling
     */
    private class InputStreamRunnable implements Runnable{ //implements Runnable{
        private Socket socket;
        private boolean threadConnected;    // connection status of this thread
        private boolean clientAdded;
        private ObjectInputStream inStream;
        private Runnable[] threadArray;
        
        public InputStreamRunnable(Socket s){
            socket = s;
            threadConnected = false;
            clientAdded = false;
            inStream = null;
            threadArray = new Thread[2];
        }
        
        public void run(){
System.out.println("IN Connection.run(): client-server connction is running in a new thread");
            // Open stream
            try{
                inStream = new ObjectInputStream(socket.getInputStream());
                threadConnected = true;
System.out.println("ois created");
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
            
            //read incoming messages
            while(threadConnected){
                Message currentMessage = null;
                try{
                    currentMessage = (Message)inStream.readObject();
                }
                catch(IOException | ClassNotFoundException e){
                    System.out.println("Error reading message: " + e.getMessage());
                }finally{

                    // Broadcast to be passed to ALL clients
                    if(currentMessage instanceof BroadcastMessage){
                        broadcastMessageHandler((BroadcastMessage)currentMessage);
                    }
                    // ToMessage to be passed to another client
                    else if(currentMessage instanceof ToMessage){
                        toMessageHandler((ToMessage)currentMessage);
                    }

                    // IdMessage to create a new client
                    if(currentMessage instanceof IdMessage){
                        clientAdded = newClientHandler((IdMessage)currentMessage);
                        System.out.println("\tclientAdded boolean = " + clientAdded);
                        
                        // return clientAdded (success flag) to the client.
                        SuccessMessage succMsg = new SuccessMessage(currentMessage.getSource(), clientAdded);
                        OutputStreamRunnable outThread = (OutputStreamRunnable)clientList.get(currentMessage.getSource())[OUT_THREAD];
                        System.out.println("about to send a success msg");
                        outThread.sendMessage(succMsg);
                    }

                    // Disconnect to close connection
                    if(currentMessage instanceof DisconnectMessage){
                        threadConnected = !disconnectMessageHandler((DisconnectMessage)currentMessage);
                    }
                }
            }
            try{
                inStream.close();
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
        }

        /**
         * Passes an array of in- and outstreams to the input stream
         * @param array shall contain an input stream runnable at location 0 and an output stream runnable at location 1
         */
        public void passArray(Runnable[] array){
            threadArray = array;
        }
        
        /**
         * Passes the incoming message to the destination client
         * @param inMsg the ToMessage object which shall be passed to the destination client
         * @return false if the destination client does not exist
         */
        private void toMessageHandler(ToMessage inMsg) {
System.out.println("server received a ToMessage");
            String src = inMsg.getSource();
            String dest = inMsg.getDestination();
            
            if(clientList.containsKey(src)){
                // find outbound thread of destination client & send
                OutputStreamRunnable outThread = (OutputStreamRunnable)clientList.get(dest)[OUT_THREAD];
                //OutputStreamRunnable outThread = (OutputStreamRunnable)clientList.get(src)[OUT_THREAD];  //for testing
                outThread.sendMessage(inMsg);
                System.out.println("\tfrom: "+ src +
                                    "\tto: " + dest +
                                    "\n\tand the message is: " + inMsg.getMessageBody());
            }
        }

        /**
         * Passes the incoming message to all clients which are connected to the server
         * @param msg the BroadcastMessage object which shall be broadcast to all clients
         */
        private void broadcastMessageHandler(BroadcastMessage msg) {
            System.out.println("server received a BroadcastMessage");
        }

        /**
         * Closes ObjectOutputStream for the source client and removes client from the clientList
         * @param disMsg the message containing details of the client to be disconnected
         * @return false if source of disMsg is not in the list, true if the source is removed from clientList and the output stream is closed
         */
        private boolean disconnectMessageHandler(DisconnectMessage disMsg) {
            System.out.println("server received a DisconnectMessage");
            String client = disMsg.getSource();
            
            if(clientList.containsKey(client)){
                OutputStreamRunnable outThread = (OutputStreamRunnable)clientList.get(client)[OUT_THREAD];
                outThread.close();
                clientList.remove(client);
                return true;
            }
            else return false;
        }

        /**
         * Identifies a newly connected client, if the client name does not already exist in the Server's list,
         * the server will add the new client.
         * @param newMsg provides the client name which will be added to the client list
         * @return false if there is already a client by that name in the server's client list
         */
        private boolean newClientHandler(IdMessage newMsg) {
System.out.println("server received an IdMessage");
            //extract client name
            String clientName = newMsg.getSource();

            // check if name is already taken
            if(clientList.containsKey(clientName)){
                System.out.println("we already have that client");
                return false;
            }
            else{
                //add this input thread to the thread appay in the client map
                clientList.put(clientName, threadArray);
                System.out.println("\tthe source of the IdMessage is: " + clientName);
                
                return true;
            }
        }
     
    }
    
    /**
     * Used to manage the OutputObjectStream including opening, writing & closing
     */
    private class OutputStreamRunnable implements Runnable{
        private Socket socket;
        private ObjectOutputStream oos;
        
        public OutputStreamRunnable(Socket s){
            socket = s;
            oos = null;
        }
        
        public void run(){
            
            try{
                oos= new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
    System.out.println("oos created");
            }
            catch(IOException e){
                System.out.println("Error making output stream: " +e.getMessage());
            }
        }
        
        /**
         * Sends message along the output stream
         * @param msg Message to be sent.
         */
        public void sendMessage(Message msg){
            try{
                oos.writeObject(msg);
                oos.flush();
                System.out.println("wrote msg to: " + msg.getDestination());
            }
            catch(IOException e){
                System.out.println("Problem sending message: " +e.getMessage());
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
                System.out.println("Error closing oos: " + e.getMessage());
            }
        }
    }
    
    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }
}
