package sigil;
import java.util.*;

public class ProbabilisticGesture
{

    //List of gestures in this collection
    private Vector gestureList;

    //The highest probability in this list
    private double highestProb;

    //The index of the gesture with highest probability
    private int highestIndex;

    //True if this gesture needs a reflection response from the
    //input device that generated it.
    private boolean needBounce;


    /**
     * Construct a new, empty probabilistic gesture
     */
    public ProbabilisticGesture()
    {
	gestureList = new Vector(); 

	highestProb = 0.0;
	highestIndex = -1;
    }
    
    /**
     * Add the given gesture to the set of 
     * gestures
     */
    public void addGesture(ParameterizedGesture pg)
    {
	gestureList.add(pg);
	if(pg.getProbability()>highestProb)
	    {
		highestProb = pg.getProbability();
		highestIndex = gestureList.size()-1;
	    }
    }

    
    /**
     * True if this gesture needs to be bounced back
     * by the generating input device
     */
    public boolean getBounce()
    {
	return needBounce;
    }
    
    /**
     * Sets whether the receiving input
     * device should bounce gestures back. 
     * Should be reset after the bounce has been performed.
     */
    public void setBounce(boolean bounce)
    {
	needBounce = bounce;
    }

    /**
     * Return the most probable gesture from
     * the current set
     */
    public ParameterizedGesture getMostProbable()
    {
	if(highestIndex>=0 && highestIndex<gestureList.size())
	    return (ParameterizedGesture)(gestureList.get(highestIndex));
	else
	    return null;
    }

    /**
     * Returns the toString of the most probable gesture name
     */
    public String toString()
    {
	if(getMostProbable()!=null)
	    return "[ProbabilisticGesture: most likely gesture = "+getMostProbable().getName()+"]";
	else
	    return null;
    }

    /**
     * Return all of the gestures in the current set, ranked
     * by probability
     */
    public Vector getGestureList()
    {
	Vector retVal = new Vector(gestureList);
	Collections.sort(retVal);
	return retVal;
    }


}
