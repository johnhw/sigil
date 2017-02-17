package sigil;
/**
 * Class encapsulating a recognized gesture, with some
 * parameterization.
 *
 * @author John Williamson
 */
public class ParameterizedGesture implements Comparable
{

    //Name of recognizer that created this instance
    private String generatorName;

    //Recognizer that created this gesture
    private ProcessorModel source;

    //Name of gesture
    private String name;

    //Start and end of gestures
    private long startTime, endTime;

    //Probability that this gesture was recognized correctly
    private double probability;

    //The difference between the maximum extents in each of the dimensions
    //of the gesture
    private double [] scales;

    //True if complete gesture was recognized.
    private boolean wasComplete;

  
    /**
     * Compares two gestures, returning the
     * most probable
     */
    public int compareTo(Object o)
    {
	if(o instanceof ParameterizedGesture)
	    {
		ParameterizedGesture pg = (ParameterizedGesture) o;
		return (int)(getProbability()*100.0-pg.getProbability()*100.0);
	    }
	else
	    return 0;
    }

    public String toString()
    {
	return "[Parameterized Gesture \""+name+"\" created by ("+generatorName+
	    ") p="+probability+" from "+startTime+" to "+endTime+" duration "+
	    getDuration()+" completed: "+wasComplete+"]";
    }


    /**
     * Create a new gesture with the given name.
     * startTime = endTime = time of call.
     * probability of recognition is 1.0.
     * Scales will remain null.
     * Gesture will be considered complete.
     */
    public ParameterizedGesture(String name, String generatorName, ProcessorModel source)
    {
	this.name = name;
	this.source = source;
	this.generatorName = generatorName;
	this.startTime = System.currentTimeMillis();
	this.endTime = this.startTime;
	this.probability = 1.0;
	this.wasComplete = true;
	this.scales = null;
    }


    /**
     * Create a new gesture with the given name and time stamps
     * probability of recognition is 1.0.
     * Scales will remain null.
     * Gesture will be considered complete.
     */
    public ParameterizedGesture(String name, String generatorName, 
				ProcessorModel source, long startTime, long endTime)
    {
	this.name = name;
	this.source = source;
	this.generatorName = generatorName;
	this.startTime = startTime;
	this.endTime = endTime;
	this.probability = 1.0;
	this.wasComplete = true;
	this.scales = null;
    }

    
    /**
     * Create a new gesture with the given name and time stamps
     * probability and completeness state
     * Scales will remain null.
     */
    public ParameterizedGesture(String name, String generatorName, 
				ProcessorModel source, long startTime, 
				long endTime, double probability, boolean wasComplete)
    {
	this.name = name;
	this.generatorName = generatorName;
	this.source = source;
	this.startTime = startTime;
	this.endTime = endTime;
	this.probability = probability;
	this.wasComplete = wasComplete;
	this.scales = null;
    }

    /**
     * Return the name of this gesture
     */
    public String getName()
    {
	return name;
    }


    /**
     * Return the name of the recognizer that generated this gesture
     */
    public String getGeneratorName()
    {
	return generatorName;
    }
    
    
    /**
     * Set the maximum size in each of the dimensions in 
     * which the gesture was traced. 
     */
    public void setScales(double [] scales)
    {
	this.scales = scales;
    }


    /**
     * Return the maximum size in each of the dimensions in 
     * which the gesture was traced. Can be null if this information not known.
     */
    public double [] getScales()
    {
	return scales;
    }

    /**
     * Return the average of the maximum extents of this gesture
     */
    public double getAverageScale()
    {
	if(scales!=null)
	    {
		double sum = 0.0;
		for(int i=0;i<scales.length;i++)
		    sum+=scales[i];
		sum/=scales.length;
		return sum;
	    }
	    else
		return 0.0;
    }

    /**
     * Return the device that generated this gesture.
     * This can then be used for recieveHetObject() calls, etc
     */
    public ProcessorModel getSource()
    {
	return source;
    }
     

    /**
     * Returns the probability that this gesture was recognized correctly
     */
    public double getProbability()
    {
	return probability;
    }

    /**
     * Return true if this gesture was completely recognized 
     * (i.e not auto-completed)
     */
    public boolean getComplete()
    {
	return wasComplete;
    }

    /**
     * Return the time at which this gesture finished
     * This should be adjusted after segmentation to be the actual
     * end point, not including any quiescent period
     */
    public long getEndTime()
    {
	return endTime;
    }

    /**
     * Returns the time at which the gesture started, or
     * an approximation thereof if the actual start time is not known
     */
    public long getStartTime()
    {
	return startTime;
    }

    /**
     * Returns the duration of the gesture.
     * Equivalent  to getEndTime()-getStartTime()
     */
    public long getDuration()
    {
	return endTime - startTime;
    }




}
