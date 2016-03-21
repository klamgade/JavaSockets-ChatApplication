/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * A GUI for the user to chat with other clients. Uses the client class to
 * connect to the server.
 * 
 * @author Sez and Kamal
 */
package client;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import messages.*;

public class ClientGUI extends JPanel {
    
    private final JLabel TITLE;
    private final String CONNECT_MESSAGE="Connected as: ";
    private MainInfoPanel mainInfoPanel;
    private ChatDisplayPanel chatPanel;
    private boolean connected;
    private ListenerGroup listener;
    private JLabel connectionStatusLabel;
    private JButton connectionButton, bcastButton, sendButton;
    private JTextArea chatDisplay;
    private JTextField chatInputField;
    private JList clientList;
    private DefaultListModel<String> clientListModel;
    private Client connection;
    
    public ClientGUI(){
        super();
        //Gets Look and Feel of OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.out.println("Error while setting look & feel: " + e.getMessage());
        }

        //Instantiations
        connected = false;
        listener = new ListenerGroup();
        connectionStatusLabel = new JLabel(CONNECT_MESSAGE + "Disconnected");
        
        connectionButton = new JButton("CONNECT");
        bcastButton = new JButton("Broadcast");
        sendButton = new JButton("SEND");

        clientListModel = new DefaultListModel<String>();
        clientListModel.addElement("Kamal");
        clientListModel.addElement("Sez");
        clientList = new JList<String>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setFixedCellWidth(50);

        
        chatDisplay = new JTextArea();
        chatDisplay.setText("Kamal: Hi! What are you up to");
        chatDisplay.setEditable(false);
        chatDisplay.setWrapStyleWord(true);
        chatInputField = new JTextField("Start typing...");
        chatInputField.selectAll();
        chatInputField.addActionListener(listener);

        // Add listeners
        connectionButton.addActionListener(listener);
        bcastButton.addActionListener(listener);
        sendButton.addActionListener(listener);
        clientList.addListSelectionListener(listener);
        
     
        // Set Look, Feel & Starting characteristics
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
//        setLayout(new BorderLayout());
        TITLE = new JLabel("Kamasez Chat Manager", JLabel.CENTER);
        TITLE.setFont(new Font("Serif", Font.BOLD, 18));
        mainInfoPanel = new MainInfoPanel();
        chatPanel = new ChatDisplayPanel();
        mainInfoPanel.setBorder(BorderFactory.createEtchedBorder());
        chatPanel.setBorder(BorderFactory.createTitledBorder("Your conversation"));

        //Add components
            add(mainInfoPanel);
            add(Box.createRigidArea(new Dimension(0,20)));
            add(chatPanel);
        //add(mainInfoPanel, BorderLayout.CENTER);
        //add(chatPanel, BorderLayout.EAST);
        
        connection = new Client();
    }
    
    
    private class MainInfoPanel extends JPanel{
        
        public MainInfoPanel(){
            super();
            setLayout(new BorderLayout());
            add(new SubInfoPanel(), BorderLayout.CENTER);
            add(new ConnectPanel(), BorderLayout.SOUTH);
        }
        
    }
    
    private class SubInfoPanel extends JPanel{
        public SubInfoPanel(){
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(connectionStatusLabel);
            add(new JScrollPane(clientList));
            
        }
    }
    
    private class ConnectPanel extends JPanel{
        public ConnectPanel(){
            super();
            add(connectionButton);
        }
    }
    
    private class ChatDisplayPanel extends JPanel{
        public ChatDisplayPanel(){
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new JScrollPane(chatDisplay));
            add(Box.createRigidArea(new Dimension(0, 20)));
            add(new JScrollPane(chatInputField));
            add(new SendMsgPanel(), BorderLayout.SOUTH);
        }
    }
    
    private class SendMsgPanel extends JPanel{
        public SendMsgPanel(){
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(bcastButton);
            add(sendButton);
        }
    }
    
    /**
     * This class is used as one location where all listener actions are handled
     */
    private class ListenerGroup implements ActionListener, ListSelectionListener{
         String userName;
         String destination;
        /*********************************************************************************
	BUTTON EVENTS
	*******************************************************************************/
        public void actionPerformed(ActionEvent e)
        {
                Object source = e.getSource();

                if(source == connectionButton){
                    if (connected) { //If already connected & want to disconnect
                        connectionButton.setText("CONNECT");
                        connectionStatusLabel.setText(CONNECT_MESSAGE + "Disconnected");
 System.out.println("sending DisconnectMessage to server");
                        connection.sendMessage(new DisconnectMessage(userName));

                    }
                    else { // if disconnected & want to connect
                        
                        connection.startClient();
                        boolean clientAdded = false;
                        //get users desired name & request it of the server
                        while(!clientAdded && userName!=null){
                            userName = (String) JOptionPane.showInputDialog("Enter your Username");
                            IdMessage idMessage = new IdMessage(userName);

                            if(connection.isConnected()){
   System.out.println("sending IDMessage, client is: " + idMessage.getSource());
                                connection.sendMessage(idMessage);
        // Need to pause for server to respond
        try{
            Thread.sleep(500);
        }
        catch(InterruptedException ex){
            Thread.currentThread().interrupt();
        }
        System.out.println("checkMessageSuccess= " + connection.checkMessageSuccess());
                                if(connection.checkMessageSuccess()){
                                    JOptionPane.showMessageDialog(null, "Yey!! User" + idMessage.getSource() + "is connected.");
                                    connectionButton.setText("DISCONNECT");
                                    connectionStatusLabel.setText(CONNECT_MESSAGE + userName);
                                    clientAdded = true;
                                }
                                else{
                                    JOptionPane.showMessageDialog(null, "Sorry, " + idMessage.getSource() + "is already taken.\n" +
                                                                    "Please try another name");
                                }
                            }  
                        }
                    }
                    connected = !connected;
                }
                
                if(source == sendButton){
                    String str = "Hello, how are you?";
                    ToMessage currentMessage = new ToMessage(userName, destination, str);
                    connection.sendMessage(currentMessage);
                    chatDisplay.setText(userName + ": " + str);
                }
        }
        
        /*********************************************************************************
	JLIST SELECTION EVENTS
	*******************************************************************************/
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting() && !clientList.isSelectionEmpty())
            {
                destination = (String)clientListModel.elementAt(clientList.getSelectedIndex());
                JOptionPane.showMessageDialog(null, "Value was changed to " + destination);
                mainInfoPanel.requestFocusInWindow();
            }
        }
    }
    
    private class Client {

        protected final String HOST_NAME = "172.28.22.61";
        protected final int HOST_PORT = 8889; // host port number
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
                            
                            if(inMessage instanceof ToMessage){
                                ToMessage msg = (ToMessage)inMessage;
                                String displayText = msg.getSource() + ": " + msg.getMessageBody();
                                chatDisplay.setText(displayText);
                            
                            System.out.println("Received a ToMessage\n\tFROM: " +msg.getSource() +
                                                "\n\tTO: " + msg.getDestination() +
                                                "\n\tBODY: " + msg.getMessageBody());
                            }
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
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kamasez Chat");
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        frame.setLocationRelativeTo(null);
        frame.setSize(screenSize.width/3, screenSize.height/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ClientGUI());
        //frame.pack();
        frame.setLocation((screenSize.width/2) - (frame.getWidth()/2),
                (screenSize.height/2) - (frame.getHeight()/2));
        frame.setVisible(true);
    }
            
}
