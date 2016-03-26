/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * Message from the server to a client indicating if an action was successful or not.
 * 
 * @author Sez
 */
package messages;

public class SuccessMessage extends Message{
    
    protected boolean success;
    
    /**
     * @param to - Identifies the message destination, use static variable Message.SERVER for server
     * @param success - shall be true if server operation was successful, otherwise false
     */
    public SuccessMessage(String to, boolean success){
        super(Message.SERVER, to);
        this.success = success;
    }

    /**
     * @return true if server operation was successful, otherwise false
     */
    public boolean getSuccess() {
        return success;
    }
}