package sigil;
/**
 * Class encapsulating a single element of a signal; a signals value at one time
 * can have multiple signal paths
 *
 * @author John Williamson
 */

public class GestureSignal implements java.io.Serializable
{

    //The timestamp for this signal
    public final long time;
    
    //The values for this signal
    public double [] vals;

    //The ID of the last device to touch this signal
    public int lastID;

    
    /**
     * Update this signal
     */
    public void update(double [] vals, int lastID)
    {
	this.vals = new double[vals.length];

	//Copy the array to prevent aliasing when signals are multicasted
	System.arraycopy(vals,0,this.vals,0,vals.length);
	this.lastID = lastID;
    }




    /** 
     * Construct a new GestureSignal, taking an array of doubles,
     * a device id and a timestamp
     */
    public GestureSignal(long timeMillis, double [] vals,  int lastID)
    {
	
	this.vals = new double[vals.length];
	
	//Copy the array to prevent aliasing when signals are multicasted
	System.arraycopy(vals,0,this.vals,0,vals.length);
	this.time = timeMillis;
	this.lastID = lastID;
    }

    /** 
     * Construct a new GestureSignal, taking an array of doubles and 
     * a device id
     */
    public GestureSignal(double [] vals, int lastID)
    {
	this.vals = new double[vals.length];
	
	//Copy the array to prevent aliasing when signals are multicasted
	System.arraycopy(vals,0,this.vals,0,vals.length);
	this.time = System.currentTimeMillis();
	this.lastID = lastID;
    }




}
