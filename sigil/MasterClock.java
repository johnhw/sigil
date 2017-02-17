
package sigil;
import java.util.*;
import java.io.*;
import java.text.*;


/**
 * The clock that controls all of the timing for passing
 * signals between devices
 * 
 * @author John Williamson
 */
public class MasterClock extends Thread implements Runnable
{

    //The current clock
    private static MasterClock mClock;

    //True if clock ticks are being generated
    private static boolean running = false;

    //Interval in ms between each clock tick
    private static int clockInterval = 10;

    //Devices registered with the master clock
    private static Vector registeredElts;

    //The global header information
    private static SignalHeader sigHeader;

    //Format for dates for the header info
    public static DateFormat dateForm ;

    //Filename for this setup
    private static String filename = "[untitled]";

    //Number of milliseconds lost in last second
    private static int secondPerformance;

    //Counts the passing of one second
    private static int secondCounter;


    /**
     * Set the filename of this setup
     */
    public static void setFilename(String fName)
    {
	filename = fName;
    }

    /**
     * Get the filename of this setup
     */
    public static String getFilename()
    {
      return filename;
    }

    /**
     * Set the header information for this setup
     */
    public static void setHeader(SignalHeader head)
    {
	sigHeader = head;
    }

    /**
     * Get the header information for this setup
     */
    public static SignalHeader getHeader()
    {
	return sigHeader;
    }
    
    /**
     * Write out the list of registered elements
     */
    public static void writeElts(ObjectOutputStream oe) throws IOException
    {
	oe.writeInt(ElementID.getID());
	oe.writeObject(registeredElts);
    }
    
    /**
     * Restore the list of registered elements
     */
    public static void readElts(ObjectInputStream oe) throws IOException, 
	ClassNotFoundException
    {
	ElementID.setID(oe.readInt());
	registeredElts = (Vector)(oe.readObject());
	
    }
    
    static
    {
	//Initialise to default values
 	registeredElts = new Vector();
	dateForm = new SimpleDateFormat("dd/MM/yy h:mm");
	sigHeader = new SignalHeader();

        sigHeader.annotation = "";
	sigHeader.date = dateForm.format(new Date());
	sigHeader.verNo = 0;
    }
    
    /**
     * Add the given element to the master clock
     */
    public static void registerElement(GestureElement ge)
    {
	registeredElts.add(ge);
    }
    
    /**
     * Remove the given element from the master clock
     */
    public static void removeElement(GestureElement ge)
    {
	registeredElts.remove(ge);
    }
    
    /**
     * Set the inter-tick interval (in ms) for the clock
     */
    public static void setClockInterval(int interval)
    {
	if(interval>1)
	    clockInterval = interval;
    }
    
    /**
     * Get the current inter-tick interval in ms
     */
    public static int getClockInterval()
    {
	return clockInterval;
    }

    /**
     * Return total clock underrun in last second
     */
    public static int getSecondPerformance()
    {
	return secondPerformance;
    }
    
    /**
     * Main execution loop 
     */
    public void run()
    {
	for(;;)
	    {
		long oldSysTime = System.currentTimeMillis();

		//Send a tick all devices
		for(int i=0;i<registeredElts.size();i++)
		    {
			GestureElement gElt = (GestureElement)(registeredElts.get(i));
                        try{
			    if(gElt.synchronous())
				gElt.tick();
                        } catch(Exception E)
                        {
			    String errorMessage = "Device "+gElt.getName()+
				" failed to tick "+E.getMessage();
			    System.err.println(errorMessage);
                            E.printStackTrace();
			}
		    }

		long newTime = System.currentTimeMillis();

		//Calculate sleep time
		long delTime = clockInterval - (newTime-oldSysTime);
		secondCounter+=newTime-oldSysTime;
		if(secondCounter>=1000)
		    {
			secondCounter = 0;
			secondPerformance = 0;
		    }
                if(delTime>=0)
		    {
			//Sleep for a bit
			try{Thread.sleep(delTime);}catch(InterruptedException ie) {}
		    }
		else
		    {
			//Response was too slow
			secondPerformance+=(-delTime);
		    }
	    }
    }
    
    /**
     * Create a new clock, make it the master clock 
     * and start it
     */
    public static void initClock()
    {
	mClock = new MasterClock();
	mClock.start();
    }
    
    /**
     * Start the master clock (after being stopped)
     */
    public static void startClock()
    {
	running = true;
    }
    
    /**
     * Stop the master clock (restart with startClock())
     */
    public static void stopClock()
    {
	running = false;
    }
    
    /**
     * Return true if the clock is currently running
     */
    public static boolean getClockStatus()
    {
	return running;
    }
    
}


