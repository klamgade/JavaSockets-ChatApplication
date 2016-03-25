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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Position;
import messages.*;

public class ClientGUI extends JPanel {
    private final String CONNECT_MESSAGE="Connected as: ";
    private String clientName;
    private MainInfoPanel mainInfoPanel;
    private ChatDisplayPanel chatPanel;
    private boolean connected;
    private ListenerGroup listener;
    private JLabel connectionStatusLabel;
    private JButton connectionButton, bcastButton, sendButton;
    private JTextArea chatDisplay, chatInputField;
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
        clientName = "";
        listener = new ListenerGroup();
        connectionStatusLabel = new JLabel(CONNECT_MESSAGE + "Disconnected");
        
        connectionButton = new JButton("CONNECT");
        bcastButton = new JButton("Broadcast");
        sendButton = new JButton("SEND");

        clientListModel = new DefaultListModel<>();
        //clientListModel.addElement("Kamal");
        //clientListModel.addElement("Sez");
        clientList = new JList<>(clientListModel);
        clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        clientList.setFixedCellWidth(50);

        
        chatDisplay = new JTextArea();
            chatDisplay.setText("Kamal: Hi! What are you up to");
            chatDisplay.setEditable(false);
            chatDisplay.setLineWrap(true);
            chatDisplay.setWrapStyleWord(true);
        chatInputField = new JTextArea("Start typing...");
            chatInputField.selectAll();
            chatInputField.setLineWrap(true);
            chatInputField.setWrapStyleWord(true);
            chatInputField.setEditable(true);

        // Add listeners
        connectionButton.addActionListener(listener);
        bcastButton.addActionListener(listener);
        sendButton.addActionListener(listener);
        clientList.addListSelectionListener(listener);
        
     
        // Set Look, Feel & Starting characteristics
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        mainInfoPanel = new MainInfoPanel();
        chatPanel = new ChatDisplayPanel();
        mainInfoPanel.setBorder(BorderFactory.createEtchedBorder());

        //Add components
            add(mainInfoPanel);
            add(Box.createRigidArea(new Dimension(0,20)));
            add(chatPanel);
        
        connection = new Client(this);
    }
    
    
    private class MainInfoPanel extends JPanel{
        
        public MainInfoPanel(){
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new SubInfoPanel());
            add(new ConnectPanel());
        }
        
    }
    
    private class SubInfoPanel extends JPanel{
        public SubInfoPanel(){
            super();
            JScrollPane clientScrollPane = new JScrollPane(clientList);
                clientScrollPane.setBorder(BorderFactory.createTitledBorder("Online right now:"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(connectionStatusLabel);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(clientScrollPane);
            
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
            JScrollPane chatDisplayScrollPane = new JScrollPane(chatDisplay);
                chatDisplayScrollPane.setBorder(BorderFactory.createTitledBorder("Your conversation"));
                chatDisplayScrollPane.setPreferredSize(new Dimension(200, 300));
            JScrollPane chatInputScrollPane = new JScrollPane(chatInputField);
                chatInputScrollPane.setPreferredSize(new Dimension(200, 100));
                
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(chatDisplayScrollPane);
            add(Box.createRigidArea(new Dimension(0, 20)));
            add(chatInputScrollPane);
            add(new SendMsgPanel());
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
         String userName = "";
         String destination;
        /*********************************************************************************
	BUTTON EVENTS
	*******************************************************************************/
         @Override
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
                                    setClientName(userName);
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
                
                if(source == sendButton || source == bcastButton){
                    Message newMessage;
                    String msgText = chatInputField.getText();
                    
                    if((msgText != null) && (!msgText.equals(""))){
                        msgText = "\n" + userName + ": " + msgText;
                        
                        if(source == sendButton){
                            if(clientList.isSelectionEmpty())
                                JOptionPane.showMessageDialog(null, "You must select a destination");
                            newMessage = new ToMessage(userName, destination, msgText);
                        }
                        else newMessage = new BroadcastMessage(userName, msgText);
                        
                        System.out.println("about to send msg to: " + destination);
                        connection.sendMessage(newMessage);
                        chatDisplay.append(msgText);
                        chatInputField.setText("");
                    }
                }
        }
        
        
        /*********************************************************************************
	JLIST SELECTION EVENTS
	*******************************************************************************/
         @Override
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting() && !clientList.isSelectionEmpty())
            {
                destination = (String)clientListModel.elementAt(clientList.getSelectedIndex());
              //  JOptionPane.showMessageDialog(null, "Value was changed to " + destination);
              //  mainInfoPanel.requestFocusInWindow();
            }
        }
    }
    
    
    public void updateMessageDisplay(String msg){
        chatDisplay.append(msg);
    }
    
    protected void setClientName(String clientName){
        this.clientName = clientName;
    }
    
    public void updateClientList(String[] newList){
        //clientListModel = new DefaultListModel<>();
        String selected = (String)clientList.getSelectedValue();
        clientListModel.clear();
        
        for(String str : newList){
            if(!str.equals(clientName))
                clientListModel.addElement(str);
        }
        if (clientListModel.contains(selected))
            clientList.setSelectedValue(selected,true);
        //clientList = new JList<>(clientListModel);
        //clientList.repaint();
        //repaint();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kamasez Chat");
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        frame.setLocationRelativeTo(null);
        frame.setSize(screenSize.width/3, screenSize.height/2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ClientGUI());
        frame.pack();
        frame.setLocation((screenSize.width/2) - (frame.getWidth()/2),
                (screenSize.height/2) - (frame.getHeight()/2));
        frame.setVisible(true);    
    }
            
}
