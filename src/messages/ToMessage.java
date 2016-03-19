/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * Message populated by a user to be sent from one client and displayed on another.
 * 
 * @author Sez
 */
package messages;

public class ToMessage extends Message{
    
    protected String content;
    
    /**
     * @param from Identifies the source of the message, use "null" for null source
     * @param to Identifies the message destination, use static variable Message.SERVER for server
     * @param message The content of the user message
     */
    public ToMessage(String from, String to, String message){
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
