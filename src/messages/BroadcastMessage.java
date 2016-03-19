/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * Message populated by a user to be sent from one client and displayed on all others.
 * 
 * @author Sez
 */
package messages;

public class BroadcastMessage extends ToMessage {
    
    /**
     * @param from Identifies the source of the message, use "null" for null source
     * @param message The content of the user message
     */
    public BroadcastMessage(String from, String message){
        super(from, Message.SERVER, message);
    }    
}
