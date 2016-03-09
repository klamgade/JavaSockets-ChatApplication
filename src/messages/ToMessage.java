package messages;

/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * Message populated by a user to be sent from one client and displayed on another.
 * 
 * @author sez
 */

public class ToMessage extends Message{
    
    protected String content;
    
    /**
     * @param from Identifies the source of the message, use -1 for null source
     * @param to Identifies the message destination, use 0 for server or -1 for null destination
     * @param id Unique ID assigned to identify this message
     * @param message The content of the user message
     */
    public ToMessage(int from, int to, String message){
        super(from, to);
        content = message;
    }

    /**
     * @return Returns the message body
     */
    public String getMessageBody() {
        return content;
    }

    /**
     * Allows the message body to be overwritten with new content
     * @param message The new message body
     */
    public void setMessageBody(String message) {
        content = message;
    }
    
    
}
