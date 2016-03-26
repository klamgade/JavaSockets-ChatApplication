/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  14845241 Kamal Lamgade
 *  0308852 Sez Prouting
 *
 * Abstract class to handle client-server messages
 * 
 * @author Sez
 */
package messages;

import java.io.Serializable;

public abstract class Message implements Serializable {
    
    public final static String SERVER = "SERVER";
    protected String from;
    protected String to;
    
/******************************************************************************
 *      CONSTRUCTORS
 ******************************************************************************/
    
    /**
     * @param from - Identifies the client which sent the message
     * @param to - Identifies the message destination, use static variable Message.SERVER 
     */
    public Message(String from, String to){
        this.from = from;
        this.to = to;
    }
    
   public Message(){
        this("" , "");
    }

    
/******************************************************************************
 *  GETTERS & SETTERS
 *******************************************************************************/
    
    /**
     * @return Returns the client which sent this message
     */
    public String getSource() {return from;}

    /**
     * @return Returns the destination client to which this message is addressed
     */
    public String getDestination() {return to;}


    /**
     * Used to Change the destination address
     * @param to - The destination identifier
     */
    public void setDestination(String to) {this.to = to;}
}
