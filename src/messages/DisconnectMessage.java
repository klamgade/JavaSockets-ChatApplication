/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * Message used by the client to tell server the user wants to disconnect.
 * 
 * @author Sez
 */
package messages;

public class DisconnectMessage extends Message {
    
    /**
     * @param from Identifies the source of the message, use "null" for null source
     */
    public DisconnectMessage(String from){
        super(from, Message.SERVER);
    }
}
