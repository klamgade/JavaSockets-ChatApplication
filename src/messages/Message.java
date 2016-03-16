package messages;

import java.io.Serializable;

/**
 *  DMS S1 2016 ASSIGNMENT 1
 *  Kamal Lamgade & Sez Prouting
 *
 * Abstract class to handle client-server messages
 * 
 * @author sez
 */

public abstract class Message implements Comparable<Message>, Serializable {
    
    public final static String SERVER = "SERVER";
    protected String from;
    protected String to;
    protected int msgID;
    
/******************************************************************************
 *      CONSTRUCTORS
 ******************************************************************************/
    
    /**
     * @param from Identifies the source of the message, use -1 for null source
     * @param to Identifies the message destination, use 0 for server or -1 for null destination 
     */
    public Message(String from, String to){
        this.from = from;
        this.to = to;
        msgID = -1;
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
     * @return Returns the unique ID of this message
     */
    public int getMessageID() {return msgID;}

    /**
     * @param msgID Should be a sequential id set by the message handler
     */
    public void setMessageID(int msgID) {this.msgID = msgID;}

    /**
     * Used to Change the destination address
     * @param to The destination identifier
     */
    public void setDestination(String to) {this.to = to;}
    
/******************************************************************************
 *  COMPARATOR
 ******************************************************************************/    
    
    /**
     * Compares message IDs (x, y) such that x.compareTo(y) <= 0 Assumes ID values are set by message handler in sequential order
     * @param other The message which is to be compared
     * @return Value < 0 if (this < other), or  > 0 if (this > other) and 0 if (this == other)
     */
    @Override
    public int compareTo(Message other){
        return (this.msgID - other.msgID);
    }
    
}
