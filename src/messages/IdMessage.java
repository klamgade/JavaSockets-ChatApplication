/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * A Message used to inform the server of the client's desired name
 * 
 * @author Kamal
 */
package messages;

public class IdMessage extends Message {

    /**
     * @param from - is the name the client is requesting to be known by.
     */
    public IdMessage(String from) {
        super(from, Message.SERVER);
    }
}
