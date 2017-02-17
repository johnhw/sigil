import sigil.*;
import javax.sound.midi.*;
import java.io.*;
import java.util.*;
/**
 * Simple MIDI synthesizer, implementing the SynthContoller interface
 *
 * @author John Williamson
 */

public class MIDISynth extends Thread implements SynthController, Runnable
{
    //Java MIDI settings
    public static final String soundBank = "c:\\jdk1.3.1\\jre\\lib\\audio\\soundbank.gm";
    private static final String soundBankFile = "/local/java/j2sdk1_3_1/jre/lib/audio/soundbank.gm";
    private static Synthesizer synth;
    private static Soundbank soundbank;
    private static MidiChannel channels[];

    //True is synth makes a noise
    private boolean enabled = false;

    //Current instrument
    private int patch = 0;

    //Number of channels used so far
    private static int chanCnt = 0;

    //The hashtable for parameter names and values
    private Hashtable synthParms = new Hashtable();

    //Hashtable of maximum values for synth parameters
    private static Hashtable synthMaxValues;

    //Hashtable of default values for synth parameters
    private static Hashtable synthDefValues;

    /**
     * Returns the current patch for this device
     * Uses General MIDI patch numbers
     */
    public int getPatch()
    {
     return patch;
    }

    /**
     * Sets the current patch for this device
     * Uses General MIDI patch numbers
     */
    public void setPatch(int patchNo)
    {
        patch=patchNo;
    }
    
    /**
     * Starts executing the main thread, which plays a sequence
     * of notes continuously, checking the parameters each time
     */
    public void run()
    {
        if(chanCnt>14)
               chanCnt = 0;

        if(chanCnt==9)
                chanCnt++;

        MidiChannel mChannel = synth.getChannels()[chanCnt++];
        
	int delTime;
        Random r = new Random();
	while(true)
	    {
                
		delTime = ((Integer)(synthParms.get("Tempo"))).intValue();
                if(delTime<=10)
                  delTime = 10;
		delTime = (60*1000)/delTime;
		try{Thread.sleep(delTime);} catch(InterruptedException ie) {}
                if(enabled)
                {
                playNote(mChannel);
                }
	    }

    }

    /**
     * Turn sound output on
     */
    public void enableSynth()
    {
	enabled = true;
    }

    /**
     * Turn sound output off
     */
    public void disableSynth()
    {
	enabled = false;

    }

    /**
     * Returns true if this device is producing sound
     */
    public boolean getEnabledState()
    {
	return enabled;
    }

    /*
     * Get the name of this synthesizer
     */
    public String getSynthName()
    {
	return "Java Sound MIDI synthesizer";
    }

    /**
     * Return all of the parameter names for this device
     */
    public Enumeration getParameterNames()
    {
      	return synthParms.keys();
    }


    /**
     * Get the named parameter, in the range 0.0 to 1.0
     * which will be expanded to the maximum range of the device
     */
    public void setNormalizedParameter(String name, double par)
    {

	setParameter(name, normalizeParm(name, par));
	
    }



    /**
     * Get the named parameter, in the range 0.0 to 1.0
     * which will be expanded to the maximum range of the device
     */
    public double getNormalizedParameter(String name)
    {
	return unNormalizeParm(name, (((Integer)synthParms.get(name))).intValue());

	
    }


    /**
     * Set the named parameter
     */
    private void setParameter(String name, int value)
    {
	synthParms.put(name, new Integer(value));

    }

    /**
     * Play a note on the given channel, using
     * the currently set parameters
     */
    private void playNote(final MidiChannel channel)
    {
	new Thread()
	    {
		public void run()
		{
                    int pitchBend = ((Integer)(synthParms.get("Pitch bend"))).intValue();
		    int pan = ((Integer)(synthParms.get("Pan"))).intValue();
		    int noteNumber = ((Integer)(synthParms.get("Pitch"))).intValue();
		    int velocity = ((Integer)(synthParms.get("Velocity"))).intValue();
		    int duration = ((Integer)(synthParms.get("Duration"))).intValue();

                        for(int i=0;i<31;i++)
                        {
                         String ccName = "CC7-"+i;
                         int ccVal = ((Integer)(synthParms.get(ccName))).intValue();
                         if(ccVal!=-1)
                         {
                          channel.controlChange(i+64, ccVal);
                          synthParms.put(ccName, new Integer(-1));
                         }

                         ccName = "CC14-"+i;
                         ccVal = ((Integer)(synthParms.get(ccName))).intValue();
                         if(ccVal!=-1)
                         {
                          channel.controlChange(i, (ccVal>>7));
                          channel.controlChange(32, (ccVal&127));
                          synthParms.put(ccName, new Integer(-1));
                         }

                        }

                    /* Set the patch */
                    channel.programChange(patch);

		    /* Set the pitch bend for the fractional part */
                    channel.setPitchBend(pitchBend);
		    
		    /* Set the pan */
		    channel.controlChange(10,pan);

		    /* Make a noise */
		    channel.noteOn(noteNumber, velocity);
		    
		    try
			{
			    sleep(duration);
			}
		    catch(InterruptedException exception){}
		    
		    channel.noteOff(noteNumber);

                    try
			{
                            sleep(50);
			}
		    catch(InterruptedException exception){}
		    
		    /* reset pitch bend */
                    channel.setPitchBend(8192);
		    
		    /* Re-center the pan*/
		    channel.controlChange(10,63);
		}
	    }.start();
		
    }


    /**
     * Take a name and a value from 0.0 to 1.0, and scale the value
     * to the maximum range for that parameter
     * @return The scaled paramater
     */
    private int normalizeParm(String name, double val)
    {
	int maxVal = ((Integer)synthMaxValues.get(name)).intValue();
	val *= maxVal;
	return (int)val;

    }

    
    /**
     * Take a name and a value, and scale the value
     * back from range for that parameter to a number between
     * 0.0 and 1.0
     * @return The scaled paramater
     */
    private double unNormalizeParm(String name, int val)
    {
	int maxVal = ((Integer)synthMaxValues.get(name)).intValue();
	double nval = (double)val/(double)maxVal;
	return nval;

    }




  static
  {

      //Initialize midi
	try
	    {
		synth = MidiSystem.getSynthesizer();
		synth.open();
	    }
	catch(MidiUnavailableException mue)
	    {
		System.err.println("MIDI not present");
	    }
	try{
	soundbank = MidiSystem.getSoundbank(new File(soundBank));
	}
	catch(InvalidMidiDataException imde) {}
	catch(IOException ie) {}
	channels = synth.getChannels();

	//Create the hashtables
        synthDefValues = new Hashtable();
        synthMaxValues = new Hashtable();


	//Initialize continuous controllers
        for(int i=0;i<31;i++)
        {
         String ccName = "CC7-"+i;
         synthDefValues.put(ccName, new Integer(-1));
         synthMaxValues.put(ccName, new Integer(127));
         ccName = "CC14-"+i;
         synthDefValues.put(ccName, new Integer(-1));
         synthMaxValues.put(ccName, new Integer(16384));
        }

	//Initialize all the maximum values
	synthMaxValues.put("Pitch", new Integer(255));
	synthMaxValues.put("Velocity", new Integer(127));
        synthMaxValues.put("Pitch bend", new Integer(16384));
	synthMaxValues.put("Pan", new Integer(127));
        synthMaxValues.put("Tempo", new Integer(4000));
	synthMaxValues.put("Duration", new Integer(2000));


	//Intialize all the default values
        synthDefValues.put("Pitch", new Integer(60));
        synthDefValues.put("Velocity", new Integer(127));
        synthDefValues.put("Pitch bend", new Integer(8192));
        synthDefValues.put("Pan", new Integer(64));
        synthDefValues.put("Tempo", new Integer(660));
        synthDefValues.put("Duration", new Integer(200));

  }

    /**
     * Restore the default values for the named parameter
     */
    public void restoreDefault(String parmName)
    {
     if(synthDefValues.containsKey(parmName))
       synthParms.put(parmName, synthDefValues.get(parmName));
    }

    /**
     * Restore the defaults for all of the parameters
     */
    public void restoreDefaults()
    {
     Enumeration parms = synthDefValues.keys();
     while(parms.hasMoreElements())
      restoreDefault((String)(parms.nextElement()));
    }
    
    /**
     * Create a new synthesizer with all of the default
     * parameters, and start it running
     */
    public MIDISynth()
    {
        restoreDefaults();
        start();
    }


}


