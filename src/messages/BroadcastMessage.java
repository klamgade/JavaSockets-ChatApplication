package messages;

/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * Message populated by a user to be sent from one client and displayed on another.
 * 
 * @author sez
 */
public class BroadcastMessage extends ToMessage {
    
    /**
     * @param from Identifies the source of the message, use -1 for null source
     * @param message The content of the user message
     */
    public BroadcastMessage(int from, String message){
        super(from, Message.SERVER, message);
    }    
}
