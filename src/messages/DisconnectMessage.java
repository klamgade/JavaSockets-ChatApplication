/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * Message used by the client to tell server the user wants to disconnect.
 * 
 * @author Sez
 */
package messages;

public class DisconnectMessage extends Message {
    
    /**
     * @param from - Identifies the source of the message
     */
    public DisconnectMessage(String from){
        super(from, Message.SERVER);
    }
}
