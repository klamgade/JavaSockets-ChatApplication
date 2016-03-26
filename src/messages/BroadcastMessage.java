/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * Message populated by a user to be sent from one client and displayed on all others.
 * 
 * @author Sez
 */
package messages;

public class BroadcastMessage extends ToMessage {
    
    /**
     * @param from - Identifies the source of the message
     * @param message - The content of the user message
     */
    public BroadcastMessage(String from, String message){
        super(from, Message.SERVER, message);
    }    
}
