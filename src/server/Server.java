/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * A class which acts as a server, to manage chat messages between various clients
 *
 * @author Kamal & Sez
 */
package server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import messages.*;

public class Server {

    public static final int PORT = 8889; // TCP port number
    protected final String UDP_HOST = "224.0.0.3"; // multicast group address which allow access to all clients in the chat group.
    protected final int BC_WAIT_TIME = 1000; // pause time before rebroadcast
    protected ServerSocket serverSocket;
    protected UdpServer udpServer;
    protected boolean ssConnected;  //connection status of serverSocket
    protected Map<String, OutputStreamRunnable> clientMap; //used to access output thread from the input thread
  
    // constructor
    public Server() {
        ssConnected = false;
        clientMap = new ConcurrentHashMap<>();
    }

    /**
     * Creates a TCP ServerSocket, a UDP DatagramSocket & passes incoming TCP clients to new threads
     */
    public void startServer() {

        // Establish TCP ServerSocket
        try {
            serverSocket = new ServerSocket(PORT);
            serverSocket.setSoTimeout(30000);
            ssConnected = true;
        } catch (IOException e) {
            System.out.println("Socket Issue: " + e.getMessage());
            System.exit(-1);
        }
        
        // Establish UDP DatagramSocket
        try{
            udpServer = new UdpServer(new DatagramSocket());
            Thread udpThread = new Thread(udpServer);
            udpThread.start();        
        }
        catch(SocketException se){
            System.out.println("trouble making datagram socket: " + se.getMessage());
        }

        // Pass incoming clients to new threads
        while (ssConnected) {
            try {
                Socket socket = serverSocket.accept();
                
                InputStreamRunnable inStream = new InputStreamRunnable(socket);
                OutputStreamRunnable outStream = new OutputStreamRunnable(socket);
                inStream.passOutputThread(outStream); // so input stream can add output stream to the clientMap

                // used for multiple threads of execution running concurrently
                Thread inThread = new Thread(inStream); 
                Thread outThread = new Thread(outStream);
                inThread.start();
                outThread.start();
                
            } catch (IOException e) {
                System.out.println("Cannot accept new client: " + e.getMessage());
            }
        }
        
        // on server close:
        try{
            serverSocket.close();
            udpServer.close();
        }
        catch(IOException e){
            System.out.println("Cannot close one or more server sockets" + e.getMessage());
        }
    }
    
    /**
     * Closes safely closes the user socket only if there are no clients connected. Determined by entries in the clientMap
     */
    public void close(){
        if(!clientMap.isEmpty())
            ssConnected = false;
    }

    /**
     * Used to manage the ObjectInputStream, including opening, closing &
     * inbound message handling
     */
    private class InputStreamRunnable implements Runnable {

        private Socket socket;
        private boolean threadConnected;    // connection status of this thread
        private boolean clientAdded;
        private ObjectInputStream inStream;
        private OutputStreamRunnable outputThread;

        public InputStreamRunnable(Socket s) {
            socket = s;
            threadConnected = false;
            clientAdded = false;
            inStream = null;
            outputThread = null;
        }

        /**
         * Opens input streams and passes incoming messages to their various type handlers
         */
        @Override
        public void run() {
            // Open stream
            try {
                inStream = new ObjectInputStream(socket.getInputStream());
                threadConnected = true;
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            //read incoming messages until this thread is stopped
            while (threadConnected) {
                Message currentMessage = null;
                
                try {
                    currentMessage = (Message) inStream.readObject();
                } 
                catch (IOException | ClassNotFoundException e) {
                    System.out.println("Error reading message: " + e.getMessage());
                }

                // Broadcast to be passed to ALL clients
                if (currentMessage instanceof BroadcastMessage) {
                    broadcastMessageHandler((BroadcastMessage) currentMessage);
                } 
                // ToMessage to be passed to another client
                else if (currentMessage instanceof ToMessage) {
                    toMessageHandler((ToMessage) currentMessage);
                }

                // IdMessage to create a new client
                if (currentMessage instanceof IdMessage) {
                    clientAdded = newClientHandler((IdMessage) currentMessage);

                    // return clientAdded (success flag) to the client.
                    SuccessMessage succMsg = new SuccessMessage(currentMessage.getSource(), clientAdded);
                    clientMap.get(currentMessage.getSource()).sendMessage(succMsg);
                }

                // Disconnect message will close the connection
                if (currentMessage instanceof DisconnectMessage) {
                    disconnectMessageHandler((DisconnectMessage) currentMessage);
                }
            }
            
            
            try {
                inStream.close();
                socket.close();
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }

        /**
         * Passes an array of in- and outstreams to the input stream
         *
         * @param osr - the output thread which is running on the same socket as this input thread
         */
        public void passOutputThread(OutputStreamRunnable osr) {
            outputThread = osr;
        }

        /**
         * Passes the incoming message to the destination client
         *
         * @param inMsg - the ToMessage object which shall be passed to the
         * destination client
         * @return false if the destination client does not exist
         */
        private void toMessageHandler(ToMessage inMsg) {
            String dest = inMsg.getDestination();

            if (clientMap.containsKey(dest)) {
                // find outbound thread of destination client & send
                clientMap.get(dest).sendMessage(inMsg);
            }
        }

        /**
         * Passes the incoming message to all clients which are connected to the
         * server, except the client which originated the message
         *
         * @param msg - the BroadcastMessage object which shall be broadcast to
         * all clients
         */
        private void broadcastMessageHandler(BroadcastMessage bcMsg) {
            ToMessage toMsg = null;
            Set<String> clientSet = clientMap.keySet(); // stores list of clients as a set with no duplicate elements
            String source = bcMsg.getSource();
            String msgBody= bcMsg.getMessageBody();
            
            for(String client : clientSet){
                if(!client.equals(source)) {
                    toMsg = new ToMessage(source, client, msgBody );
                    toMessageHandler(toMsg);
                }
            }
        }

        /**
         * Closes client streams and removes client from the clientMap
         *
         * @param disMsg - the message containing details of the client to be
         * disconnected
         * @return false if source of disMsg is not in the list, true if the
         * source is removed from clientList and the output stream is closed
         */
        private void disconnectMessageHandler(DisconnectMessage disMsg) {
            String client = disMsg.getSource();

            if (clientMap.containsKey(client)) {                
                clientMap.get(client).close();
                threadConnected = false;
                clientMap.remove(client);
            }
        }

        /**
         * Identifies a newly connected client, if the client name does not
         * already exist in the Server's list, the server will add the new
         * client.
         *
         * @param newMsg - provides the client name which will be added to the
         * client list
         * @return false if there is already a client by that name in the
         * server's client list
         */
        private boolean newClientHandler(IdMessage newMsg) {
            //extract client name
            String clientName = newMsg.getSource();

            // check if name is already taken
            if (clientMap.containsKey(clientName)) {
                return false;
            } else {
                //add this input thread to the thread appay in the client map
                clientMap.put(clientName, outputThread);
                return true;
            }
        }

    }

    /**
     * Used to manage the OutputObjectStream including opening, writing &
     * closing
     */
    private class OutputStreamRunnable implements Runnable {

        private Socket socket;
        private ObjectOutputStream oos;

        public OutputStreamRunnable(Socket s) {
            socket = s;
            oos = null;
        }

        /**
         * Opens output streams and sends messages to server
         */
        public void run() {

            try {
                oos = new ObjectOutputStream(socket.getOutputStream());
                oos.flush();
            } catch (IOException e) {
                System.out.println("Error making output stream: " + e.getMessage());
            }
        }

        /**
         * Sends any message which extends the Message class to the server
         *
         * @param msg - Message to be sent.
         */
        public void sendMessage(Message msg) {
            try {
                oos.writeObject(msg);
                oos.flush();
            } catch (IOException e) {
                System.out.println("Problem sending message: " + e.getMessage());
            }
        }

        /**
         * Closes the ObjectOutputStream
         */
        public void close() {
            try {
                oos.close();
            } catch (IOException e) {
                System.out.println("Error closing oos: " + e.getMessage());
            }
        }
    }

    /**
     * Broadcasts the list of currently connected clients
     */
    private class UdpServer implements Runnable {
        
        private DatagramSocket socket;
        private boolean udpInConnection;
        private InetAddress group;

        /**
         * 
         * @param socket - UDP socket on which the client list will be broadcast
         */
        public UdpServer(DatagramSocket socket) {
            this.socket = socket;
            udpInConnection = false;
            
            try{
                group = InetAddress.getByName(UDP_HOST);
            }
            catch(IOException e) {
                System.out.println(e.getMessage());
            }
            udpInConnection = true;
        }
        
        /**
         * Initiates socket shutdown
         */
        public void close(){
            udpInConnection = false;
        }

        /**
         * Opens output streams and sends regular broadcasts of client list
         */
        @Override
        public void run() {
            String[] clientArray= null;
            DatagramPacket packet;
            ObjectOutputStream udp_oos = null;
            ByteArrayOutputStream baos = null;
            Set<String> keySet;
            
            while (udpInConnection) {
                keySet = clientMap.keySet();
                clientArray =keySet.toArray(new String[keySet.size()]);
                
                if(clientArray != null ){
                    try {
                        // preallocate temporary array and stream
                        baos = new ByteArrayOutputStream();
                        udp_oos = new ObjectOutputStream(baos);
                        udp_oos.writeObject(clientArray);// send list of clients to baos
                        udp_oos.flush(); 
                        
                        // convert clientList to byte[]
                        byte[] data = baos.toByteArray();
                        baos.reset();  // reseting for another stream of byteArray
                        
                        // create & send packet
                        packet =new DatagramPacket(data, data.length, group, 8888);
                        socket.send(packet);// sending the packet in from server socket
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
                
                try {
                    Thread.sleep(BC_WAIT_TIME);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    Thread.yield(); // give other threads a chance
                }
            }
            
            //shutdown
            try{
                if(baos != null) baos.close();
                if(udp_oos != null) udp_oos.close();
                socket.close();
            }
            catch(IOException e){
                System.out.println("could not close UDP streams: " + e.getMessage());
            }
        }
    }
    
    /**
     * starts server
     * @param args - not used
     */
    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }
}
