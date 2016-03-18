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
import java.util.HashMap;
import java.util.Map;
import messages.*;

public class Server {
    public static final int PORT=8889;
    protected ServerSocket serverSocket;
    protected boolean ssConnected;  //connection status of serverSocket
    protected Map<String, Thread> clientList;   //list of clients by name & incomming thread
    
    public Server(){
        ssConnected = false;
        clientList = new HashMap<String, Thread>();
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
                Thread inThread = new Thread(new InwardsMessageThread(socket));
                //Thread outThread = new Thread(new OutwardsMessageThread(socket));
                inThread.start();
                //outThread.start();
            }
            catch(IOException e){
                System.out.println("There's a problem accepting the client socket Cx: " + e.getMessage());
            }
        }
    }
    
    /**
     * Thread which handles incoming messages from the client
     */
    private class InwardsMessageThread extends Thread{ //implements Runnable{
        private Socket socket;
        private boolean threadConnected;    // connection status of this thread
        private boolean clientAdded, msgReceived;
        private ObjectInputStream inStream;
        
        public InwardsMessageThread(Socket s){
            socket = s;
            threadConnected = false;
            clientAdded = false;
            msgReceived = false;
            inStream = null;
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
                    //System.out.println("is this it" + e.getMessage());
                }finally{

                    // Broadcast to be passed to ALL clients
                    if(currentMessage instanceof BroadcastMessage){
                        broadcastMessageHandler();
                    }
                    // ToMessage to be passed to another client
                    else if(currentMessage instanceof ToMessage){
                    
                        msgReceived = toMessageHandler((ToMessage)currentMessage);
                        System.out.println("\tsuccessful toMessage = " + msgReceived);
                    }

                    // IdMessage to create a new client
                    if(currentMessage instanceof IdMessage){
                        clientAdded = newClientHandler((IdMessage)currentMessage);
                        System.out.println("\tclientAdded boolean = " + clientAdded);
                        // return clientAdded (success flag) to the client.
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

        private boolean toMessageHandler(ToMessage inMsg) {
System.out.println("server received a ToMessage");
            String src = inMsg.getSource();
            String dest = inMsg.getDestination();
            
            if(!clientList.containsKey(src)){
                System.out.println("This client doesn't exist");
                return false;
            }
            else{
                //send message here
                System.out.println("\tThe message is from: "+ dest +
                                    "\n\tand the message is: " + inMsg.getMessageBody());
                return true;
            }
        }

        private void broadcastMessageHandler() {
            System.out.println("server received a BroadcastMessage");
        }

        private boolean disconnectMessageHandler(DisconnectMessage disMsg) {
            System.out.println("server received a DisconnectMessage");
            String client = disMsg.getSource();
            if(clientList.containsKey(client)){
                clientList.remove(client);
                return true;
            }
            else return false;
        }

        private boolean newClientHandler(Message newMsg) {
System.out.println("server received an IdMessage");
            //extract client name
            IdMessage msg = (IdMessage)newMsg;
            String clientName = msg.getSource();

            // check if name is already taken
            if(clientList.containsKey(clientName)){
                System.out.println("we already have that client");
                return false;
            }
            else{
                clientList.put(clientName, this);
                System.out.println("\tthe source of the IdMessage is: " + clientName);
                return true;
            }
        }
        
    }
    
 /*   private class OutwardsMessageThread implements Runnable{
        private Socket socket;
        private Message currentMessage;
        private ObjectOutputStream oos;
        
        public OutwardsMessageThread(Socket s){
            socket = s;
            currentMessage = null;
            oos = null;
        }
        
        public void run(){
        }
    }*/
    
    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }
}
