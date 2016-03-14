/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * 
 * 
 * @author Kamal & Sez
 */
package server;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import messages.*;

public class Server {
    public static final int PORT=8889;
    String ip;
    ServerSocket serverSocket;
    
    public Server(){
        try{
            System.out.println(InetAddress.getLocalHost().getHostAddress());
        }
        catch(UnknownHostException e){
            System.out.println(e.getMessage());
        }
    }
    
    public void startServer(){
        try{
            serverSocket = new ServerSocket(PORT);

            serverSocket.setSoTimeout(30000);
            System.out.println("IN startServer().try#1 : server started");
        }
        catch(IOException e){
            System.out.println("You've got problems with your socket" + e.getMessage());
            System.exit(-1);
        }
        
        try{
            Socket socket = serverSocket.accept();
            System.out.println("IN startServer().try#2 : Cx made with " + socket.getInetAddress());
            Thread t = new Thread(new Connection(socket));
            t.start();
        }
        catch(IOException e){
            System.out.println("There's a problem accepting the client socket Cx: " + e.getMessage());
        }
        
    }
    
    private class Connection implements Runnable{
        private Socket socket;
        
        public Connection(Socket s){
            socket = s;
        }
        
        public void run(){
            System.out.println("IN Connection.run(): client-server connction is running in a new thread");
            try{
                ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
                System.out.println("ois created");
                
                ToMessage msg = (ToMessage)ois.readObject();
                System.out.println("Message = " + msg.getMessageBody());
                
                ois.close();
            }
            catch(IOException | ClassNotFoundException e){
                System.out.println(e.getMessage());
            }
            
        }
    }
    
    public static void main(String[] args) {
        Server s = new Server();
        s.startServer();
    }
}
