import sigil.*;
import duotonic.MIDIUtils;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.sound.midi.*;
import java.io.*;
/**
 * SignalDevice: Experimental device which creates random melodies and can 
 * be evolved
 *
 * @author John Williamson
 */
public class Melody extends GAProcessorModel implements Cloneable
{


    static final long serialVersionUID = 213L;

    //The random number generator for this device
    Random rnd = new Random();

    //The list of note information
    private Vector noteVector;

    //The timbre for this melody
    private int timbre;

    //Properties
    public String getGenName()
    {
	return "Melody";
    }
    public String getDescription()
    {
	return "Plays random melodies";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }



    /**
     * Create a copy of the note list, and make that the current note list
     */
    private void copyGenotype()
    {
	Vector temp = new Vector();
	for(int i=0;i<noteVector.size();i++)
	    {
		NoteInfo nInfo = (NoteInfo) (noteVector.get(i));
		temp.add(nInfo.copy());
	    }
	noteVector = temp;   
    }

    /**
     * Copy the attributes of another melody
     */
    public void copyFrom(GAElement gaElt)
    {
	if(gaElt instanceof Melody)
	    {
		Melody replacementMelody = (Melody)gaElt;
		noteVector = replacementMelody.noteVector;
		timbre = replacementMelody.timbre;
	    }
    }
    
    /**
     * Modify the melody, changing by mutation factor
     */
    public void mutate(double mutationFactor)
    {
	//Insert a new element
	if(rnd.nextDouble()<mutationFactor/3.0)
	    noteVector.insertElementAt(makeNote(), rnd.nextInt(noteVector.size()));
	
	//Remove an element
	if(rnd.nextDouble()<mutationFactor/3.0 && noteVector.size()>2)
	    noteVector.removeElementAt(rnd.nextInt(noteVector.size()));
	
	if(rnd.nextDouble()<mutationFactor/8.0)
	    timbre = rnd.nextInt(127);

	//Adjust each element by the mutation factor
	for(int i=0;i<noteVector.size();i++)
	    {
		if(rnd.nextDouble()<mutationFactor)
		    {
			NoteInfo nInfo = (NoteInfo)(noteVector.get(i));
			
			int newNote = nInfo.deltaNote+(int)(rnd.nextGaussian()*3*
							    mutationFactor);
			int newVel = nInfo.deltaVel+(int)(rnd.nextGaussian()*20*
							  mutationFactor);
			int newDur = nInfo.dur+(int)(Math.abs(rnd.nextGaussian()*2*
							      mutationFactor));         
			noteVector.setElementAt(new NoteInfo(newNote, newVel, newDur), i);
		    }
	    }
 }

    /**
     * Randomly mix to Melody objects together
     */
    public void breed(GAElement toBreed)
    {
	if(toBreed instanceof Melody)
	    {
		
	    }
	
    }

    /**
     * Returns a deep copy of this element
     */
    public GAElement copy()
    {
	try{
	    Melody cloned = (Melody)clone();
	    cloned.copyGenotype();
	    return cloned;
	} catch(CloneNotSupportedException cnse) { return null; }
    }

    /**
     * Class for each element in the note list
     * holds the change of pitch, the change in velocity (volume) and the duration
     * of the note (in ticks). Note that the actual duration is 2^duration
     */
    private class NoteInfo implements Serializable
    {
	int deltaNote, deltaVel, dur;
	
	public NoteInfo copy()
	{
	    return new NoteInfo(deltaNote, deltaVel, dur);
	}
	
	public NoteInfo(int dNote, int dVel, int dur)
	{
	    deltaNote = dNote;
	    deltaVel = dVel;
	    this.dur = dur;
	}
    }

    
    public void connectionChange()
    {
    }
   

    /**
     * Play the current melody, in a new thread
     */
    private void playMelody()
    {
	new Thread()
	    {
		public void run()
		{
		    
		    int baseNote = 60;
		    int baseVel = 100;
		    int durStep = 10;
		    int patch = timbre;
		    for(int i=0;i<noteVector.size();i++)
			{
			    NoteInfo nInfo = (NoteInfo)(noteVector.get(i));

			    //Adjust pitch and volume
			    baseNote+=nInfo.deltaNote;
			    baseVel+=nInfo.deltaVel;

			    //Limit the velocity
			    if(baseVel<0) baseVel=0;
			    if(baseVel>127) baseVel=127;

			    //Calculate duration
			    int duration = (int)(Math.pow(2,nInfo.dur))*durStep;

			    //Play the note
			    MIDIUtils.playNoteBlocking(0, baseNote, baseVel, 
						       duration, patch); 
			    
			}
		}
	    }.start();
    }
    
    /**
     * Simply plays the current melody
     */
    public void preview()
    {
	playMelody();
    }
    
    /**
     * Simply plays the current melody
     */
    public void showInterface()
    {
	playMelody();
    }
    
    
    /**
     * Create a new element of a melody, with random values
     */
    private NoteInfo makeNote()
    {
	int deltaNote = (int)(rnd.nextGaussian()*7);
	int deltaVel = (int)(rnd.nextGaussian()*40);
	int dur = (int)(Math.abs(rnd.nextGaussian()*4));         
	return new NoteInfo(deltaNote, deltaVel, dur);
     
   }

    /**
     * Create a new sequenece of random melody elements
     */
    private void makeMelody()
    {
       noteVector = new Vector();
       int size = rnd.nextInt(20);
       timbre = rnd.nextInt(127);
       for(int i=0;i<size;i++)
         noteVector.add(makeNote());

    }

    /**
     * Construct a new melody device
     */
    public Melody()
    {
	super();
	setTerminating(true);
        makeMelody();
    }

    public void deleted()
    {
    }


    public void processSignal()
    {
    }

}


