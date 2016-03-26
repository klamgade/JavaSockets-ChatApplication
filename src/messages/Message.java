/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
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
     * @param from Identifies the source of the message, use "null" for null source
     * @param to Identifies the message destination, use static variable Message.SERVER for server or "null" for null destination 
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
     * @return Returns the ID of the application or object which sent this message
     */
    public String getSource() {return from;}

    /**
     * @return Returns the ID of the application or object to which this message is addressed
     */
    public String getDestination() {return to;}


    /**
     * Used to Change the destination address
     * @param to The destination identifier
     */
    public void setDestination(String to) {this.to = to;}
    

    
}
