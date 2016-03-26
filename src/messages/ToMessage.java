/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * Message populated by a user to be sent from one client and displayed on another.
 * 
 * @author Sez
 */
package messages;

public class ToMessage extends Message{
    
    protected String content;
    
    /**
     * @param from - Identifies the source of the message
     * @param to - Identifies the message destination, use static variable Message.SERVER for server
     * @param message - The content of the user message
     */
    public ToMessage(String from, String to, String message){
        super(from, to);
        content = message;
    }

    /**
     * Used to access the content of the message
     * @return Returns the message body
     */
    public String getMessageBody() {
        return content;
    }

    /**
     * Allows the message body to be overwritten with new content
     * @param message - The new message content
     */
    public void setMessageBody(String message) {
        content = message;
    }
}
