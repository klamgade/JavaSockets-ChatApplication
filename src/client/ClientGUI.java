package client;

/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * A GUI for the user to chat with other clients. Uses the client class to
 * connect to the server.
 * 
 * @author Sez Prouting
 */

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    private DefaultListModel clientListModel;
    
    public ClientGUI(){
        super();
        //Gets Look and Feel of OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error while setting look & feel: " + e.getMessage());
        }

        //Instantiations
        connected = false;
        listener = new ListenerGroup();
        connectionStatusLabel = new JLabel(CONNECT_MESSAGE + "Disconnected");
        
        connectionButton = new JButton("CONNECT");
        bcastButton = new JButton("Broadcast");
        sendButton = new JButton("SEND");
        
        clientListModel = new DefaultListModel();
            clientListModel.addElement("Kamal");
            clientListModel.addElement("Sez");
        clientList = new JList(clientListModel);
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
    
    private class ListenerGroup implements ActionListener, ListSelectionListener{

        /*********************************************************************************
	BUTTON EVENTS
	*******************************************************************************/
        public void actionPerformed(ActionEvent e)
        {
                Object source = e.getSource();

                if(source == connectionButton){
                    if(connected){
                        connectionButton.setText("CONNECT");
                        connectionStatusLabel.setText(CONNECT_MESSAGE + "Disconnected");
                    }
                    else {
                        connectionButton.setText("DISCONNECT");
                        connectionStatusLabel.setText(CONNECT_MESSAGE + "Joe");
                    }
                    connected = !connected;
                }
                
                if(source == sendButton){
                    chatDisplay.setText("From Kamal: Hi! What are you up to?" +
                                        "\n\nTo Kamal: not much" +
                                        "\n\nTo ALL: everyone stop!!");
                   // repaint();
                }
        }
        
        /*********************************************************************************
	JLIST SELECTION EVENTS
	*******************************************************************************/
        public void valueChanged(ListSelectionEvent e)
        {
            if (!e.getValueIsAdjusting() && !clientList.isSelectionEmpty())
            {
                String str = (String)clientListModel.elementAt(clientList.getSelectedIndex());
                JOptionPane.showMessageDialog(null, "Value was changed to " + str);
                mainInfoPanel.requestFocusInWindow();
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
