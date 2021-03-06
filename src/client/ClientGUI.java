/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade[14845241] & Sez Prouting[0308852]
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
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import messages.*;

public class ClientGUI extends JPanel {

    private final String CONNECT_MESSAGE = "Connected as: ";
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

    //constructor
    public ClientGUI() {
        super();

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

        clientList = new JList<>(clientListModel);
            clientList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            clientList.setFixedCellWidth(50);

        chatDisplay = new JTextArea();
            chatDisplay.setEditable(false);
            chatDisplay.setLineWrap(true);
            chatDisplay.setWrapStyleWord(true);
        chatInputField = new JTextArea();
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
        add(Box.createRigidArea(new Dimension(0, 20)));
        add(chatPanel);

        connection = new Client(this);
    }
// Panels for differnet sections on GUI
    private class MainInfoPanel extends JPanel {

        public MainInfoPanel() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(new SubInfoPanel());
            add(new ConnectPanel());
        }

    }

    private class SubInfoPanel extends JPanel {

        public SubInfoPanel() {
            super();
            JScrollPane clientScrollPane = new JScrollPane(clientList);
            clientScrollPane.setBorder(BorderFactory.createTitledBorder("Online right now:"));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(connectionStatusLabel);
            add(Box.createRigidArea(new Dimension(0, 10)));
            add(clientScrollPane);

        }
    }

    private class ConnectPanel extends JPanel {

        public ConnectPanel() {
            super();
            add(connectionButton);
        }
    }

    private class ChatDisplayPanel extends JPanel {

        public ChatDisplayPanel() {
            super();
            JScrollPane chatDisplayScrollPane = new JScrollPane(chatDisplay);
            chatDisplayScrollPane.setBorder(BorderFactory.createTitledBorder("Your conversation"));
                chatDisplayScrollPane.setPreferredSize(new Dimension(200, 300));
            JScrollPane chatInputScrollPane = new JScrollPane(chatInputField);
            chatInputScrollPane.setPreferredSize(new Dimension(200, 150));
                chatInputScrollPane.setBorder(BorderFactory.createTitledBorder("Type your message here:"));

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            add(chatDisplayScrollPane);
            add(Box.createRigidArea(new Dimension(0, 20)));
            add(chatInputScrollPane);
            add(new SendMsgPanel());
        }
    }

    private class SendMsgPanel extends JPanel {

        public SendMsgPanel() {
            super();
            setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            add(bcastButton);
            add(sendButton);
        }
    }

    /**
     * This class is used as one location where all listener actions are handled
     */
    private class ListenerGroup implements ActionListener, ListSelectionListener {

        String userName = "";
        String destination;

        /**
         * *******************************************************************************
         * BUTTON EVENTS
	******************************************************************************
         */
        @Override
        public void actionPerformed(ActionEvent e) {
            Object source = e.getSource();

            if (source == connectionButton) {
                
                if (connected) { //If already connected & want to disconnect
                    connectionButton.setText("CONNECT");
                    connectionStatusLabel.setText(CONNECT_MESSAGE + "Disconnected");
                    connection.sendMessage(new DisconnectMessage(userName));
                    JOptionPane.showMessageDialog(null, "You are Disconnected.");

                } else { // if disconnected & want to connect

                    connection.startClient();
                    boolean clientAdded = false;
                    //get users desired name & request it of the server
                    while (!clientAdded && userName != null) {
                        
                        userName = (String) JOptionPane.showInputDialog("Enter your Username");
                        IdMessage idMessage = new IdMessage(userName);

                        if (connection.isConnected()) {
                            connection.sendMessage(idMessage);
                            
                            // Need to pause for server to respond
                            try {
                                Thread.sleep(500); // pauses thread for 500 milliseconds
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }
                            
                            if (connection.checkMessageSuccess()) {
                                JOptionPane.showMessageDialog(null, "You are connected as " + idMessage.getSource() + ".");
                                connectionButton.setText("DISCONNECT");
                                connectionStatusLabel.setText(CONNECT_MESSAGE + userName);
                                setClientName(userName);
                                clientAdded = true;
                            } else {
                                JOptionPane.showMessageDialog(null, "Sorry, " + idMessage.getSource() + "is already taken.\n"
                                        + "Please try another name");
                            }
                        }
                    }
                }
                connected = !connected;
            }

            if (source == sendButton || source == bcastButton) { // actions performed while pressed send or broadcast button
                Message newMessage = null;
                boolean sendMessage = true;  // allows cancellation of message if values are empty
                String msgText = chatInputField.getText();

                if ((msgText != null) && (!msgText.equals(""))) {
                    msgText = "\n\n" + userName + ": " + msgText;

                    if (source == sendButton) {
                        if (clientList.isSelectionEmpty()) {
                            JOptionPane.showMessageDialog(null, "You must select a destination");
                            sendMessage = false;
                        } else {
                            newMessage = new ToMessage(userName, destination, msgText);
                        }
                    } else {
                        newMessage = new BroadcastMessage(userName, msgText);
                    }

                    if (sendMessage) {
                        connection.sendMessage(newMessage);
                        chatDisplay.append(msgText);
                        chatInputField.setText("");
                    }
                }
            }
        }

        /**
         * *******************************************************************************
         * JLIST SELECTION EVENTS
	******************************************************************************
         */
        @Override
        public void valueChanged(ListSelectionEvent e) {
            if (!e.getValueIsAdjusting() && !clientList.isSelectionEmpty()) {
                destination = (String) clientListModel.elementAt(clientList.getSelectedIndex());
            }
        }
    }

    public void updateMessageDisplay(String msg) {
        chatDisplay.append(msg);
    }

    protected void setClientName(String clientName) {
        this.clientName = clientName;
    }

    // method to update users list when connected after disconnected 
    public void updateClientList(String[] newList) {
        String selected = (String) clientList.getSelectedValue();
        clientListModel.clear(); 

        for (String str : newList) { //iterate through new client list after an update
            if (!str.equals(clientName)) {
                clientListModel.addElement(str);
            }
        }
        if (clientListModel.contains(selected)) {
            clientList.setSelectedValue(selected, true);
        }
    }
 // Main  method to execute the program
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kamasez Chat");
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        frame.setLocationRelativeTo(null);
        frame.setSize(screenSize.width / 3, screenSize.height / 2);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ClientGUI());
        frame.pack();
        frame.setLocation((screenSize.width / 2) - (frame.getWidth() / 2),
                (screenSize.height / 2) - (frame.getHeight() / 2));
        frame.setVisible(true);
    }

}
