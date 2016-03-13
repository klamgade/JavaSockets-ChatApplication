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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import messages.*;

public class ClientGUI extends JPanel {
    
    private MainInfoPanel mainInfoPanel;
    private ChatPanel chatPanel;
    private final JLabel TITLE;
    private final String CONNECT_MESSAGE="You are connected as: ";
    private JLabel connectionStatusLabel;
    private JButton connectionButton, bcastButton, sendButton;
    private JTextField tester1, tester2;
    
    public ClientGUI(){
        super();
        //Gets Look and Feel of OS
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.out.println("Error while setting look & feel: " + e.getMessage());
        }

        //Instantiations
        connectionStatusLabel = new JLabel(CONNECT_MESSAGE + "Disconnected");
        connectionButton = new JButton("CONNECT");
        bcastButton = new JButton("Broadcast");
        sendButton = new JButton("SEND");
        tester1 = new JTextField("Online");
        tester2 = new JTextField("Chat here");
        
        
        // Set Look, Feel & Starting characteristics
        setLayout(new BorderLayout());
        TITLE = new JLabel("Kamasez Chat Manager", JLabel.CENTER);
        TITLE.setFont(new Font("Serif", Font.BOLD, 18));
        mainInfoPanel = new MainInfoPanel();
        chatPanel = new ChatPanel();
        
        //Add components
        add(mainInfoPanel, BorderLayout.WEST);
        add(chatPanel, BorderLayout.CENTER);
        
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
            add(tester1);
            
        }
    }
    
    private class ConnectPanel extends JPanel{
        public ConnectPanel(){
            super();
            add(connectionButton);
        }
    }
    
    private class ChatPanel extends JPanel{
        public ChatPanel(){
            super();
            setLayout(new BorderLayout());
            add(tester2, BorderLayout.CENTER);
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
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("Kamasez Chat");
        Toolkit tk = Toolkit.getDefaultToolkit();
        Dimension screenSize = tk.getScreenSize();
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(new ClientGUI());
        frame.pack();
        frame.setLocation((screenSize.width / 2) - (frame.getWidth() / 2),
                (screenSize.height / 2) - (frame.getHeight() / 2));
        frame.setVisible(true);
    }
            
}
