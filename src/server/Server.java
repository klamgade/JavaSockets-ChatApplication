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
    protected Map clientList;   //list of clients by name & incomming thread
    
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
System.out.println("Connection made with " + socket.getInetAddress());
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
     * Server tread which handles incoming messages from the client
     */
    private class InwardsMessageThread implements Runnable{
        private Socket socket;
        private boolean threadConnected;    // connection status of this thread
        private boolean clientAdded;
        private ObjectInputStream inStream;
        
        public InwardsMessageThread(Socket s){
            socket = s;
            threadConnected = false;
            clientAdded = false;
            inStream = null;
        }
        
        public void run(){
System.out.println("IN Connection.run(): client-server connction is running in a new thread");
            // Open streams, read incoming messages
            try{
                inStream = new ObjectInputStream(socket.getInputStream());
                threadConnected = true;
System.out.println("ois created");
            }
            catch(IOException e){
                System.out.println(e.getMessage());
            }
            
            while(threadConnected){
                try{
                    Message currentMessage = (Message)inStream.readObject();

                    // ToMessage to be passed to another client
                    if(currentMessage instanceof ToMessage)
                        toMessageHandler(currentMessage);

                    // Broadcast to be passed to ALL clients
                    if(currentMessage instanceof BroadcastMessage)
                        broadcastMessageHandler();

                    // Disconnect to close connection
                    if(currentMessage instanceof DisconnectMessage)
                        disconnectMessageHandler();
                    
                    // IdMessage to create a new client
                    if(currentMessage instanceof IdMessage){
                        clientAdded = newClientHandler(currentMessage);
                        // return clientAdded (success flag) to the client.
                    }

                    inStream.close();
                }
                catch(IOException | ClassNotFoundException e){
                    //System.out.println(e.getMessage());
                }
            }
        }

        private void toMessageHandler(Message inMsg) {
System.out.println("server received a ToMessage");
            ToMessage message = (ToMessage)inMsg;
            if(!clientList.containsKey(message.getSource()))
                clientList.put(message.getSource(), this);
System.out.println("client list entry: " + clientList);
        }

        private void broadcastMessageHandler() {
            System.out.println("server received a BroadcastMessage");
        }

        private void disconnectMessageHandler() {
            System.out.println("server received a DisconnectMessage");
        }

        private boolean newClientHandler(Message newMsg) {
System.out.println("server received an IdMessage");
            //extract client name
            IdMessage msg = (IdMessage)newMsg;
            String clientName = msg.getSource();

            // check if name is already taken
            if(clientList.containsKey(clientName)){
                return false;
            }
            else{
                clientList.put(clientName, this);
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
