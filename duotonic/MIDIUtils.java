package duotonic;
import javax.sound.midi.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.event.*;
import java.awt.*;

/**
 * Utilities for working with Java MIDI
 *
 * @author John Williamson
 */
public class MIDIUtils
{  

    //Synthesizer variables
    private static final String soundBank = "c:\\jdk1.3.1\\jre\\lib\\audio\\soundbank.gm";
    private static final String soundBankFile = "/local/java/j2sdk1_3_1/jre/lib/audio/soundbank.gm";
    private static Synthesizer synth;
    private static Soundbank soundbank;
    private static MidiChannel channels[];


     /**
     * Class for creating chords whose properties (velocity, pan and timbre)
     * be individually updated as required
     * Up to 15 notes can be used in one chord. Note that this will
     * use the entire synthesizer, so nothing else can play at the same time (except for
     * channel 0).
     */
    public static class PolyChannelChord
    {
	private boolean [] notesOn;

	/**
	 * Adjust the note given by the index to play with
	 * the new pitch, velocity, pan and timbre
	 */
	public void adjustProperties(int noteIndex, int newPitch, 
				     int newVelocity, int newPan, int newTimbre)
				     
	{
	    if(noteIndex>=15 || noteIndex<1)
		return;

	    //Don't play the drums!
	    if(noteIndex>=8) 
		noteIndex++;				

	    MidiChannel channel = channels[noteIndex];
	    channel.controlChange(10, newPan);
	   
	    channel.programChange(newTimbre);
	    channel.setChannelPressure(newVelocity);
	    channel.noteOff(newPitch);
            channel.noteOn(newPitch, newVelocity);
	    notesOn[noteIndex] = true;
	       
	}

	/**
	 * Stop the current notes. Can be restarted with adjustProperties for the 
	 * appropriate channels
	 */
	public void stop()
	{
	    for(int i=0;i<16;i++)
		    channels[i].allNotesOff();
	}

	/**
	 * Creates a polychord
	 */
	public PolyChannelChord()
	{
	    notesOn = new boolean[16];
	}

    }



    /**
     * Class for creating chords whose note velocities can
     * be individually updated as required
     */
    public static class PolyChord
    {
	private int [] notes;
	private int [] velocities;
	private MidiChannel channel;
	private int patch;
	private boolean polyChordable = false;

	/**
	 * Adjust the note given by the index to play with
	 * the new velocity
	 */
	public void adjustVelocity(int noteIndex, int newVelocity)
	{
	    velocities[noteIndex] = newVelocity;
	    if(polyChordable)
		channel.setPolyPressure(notes[noteIndex], newVelocity);
	    else
		{
		    channel.noteOff(notes[noteIndex]);
		    channel.noteOn(notes[noteIndex], newVelocity);
		}
	}

	/**
	 * Stop the current notes. Can be restarted with startPolyChord()
	 */
	public void stop()
	{
	    channel.allNotesOff();

	}

	/**
	 * Creates a polychord, using the notes in the notes array,
	 * all of which start with a velocity of 0. The channel and
	 * patch to be used must also be specified
	 */
	public PolyChord(int [] notes, int patch, int channelIndex)
	{
	    this(notes, new int[notes.length], patch, channelIndex);
	}

	/**
	 * Creates a polychord, using the notes in the notes array
	 * with the matching initial velocities. The channel and
	 * patch to be used must also be specified. The chord is automatically started
	 */
	public PolyChord(int [] notes, int [] initialVelocities, int patch, int channelIndex)
	{
	    this.notes = notes;
	    this.patch = patch;
	    this.velocities = initialVelocities;
	    channel = channels[channelIndex];
	    startPolyChord();
	}

	/**
	 * Start playing the polychord
	 */
	public void startPolyChord()
	{
	    /* Set the patch */
	    channel.programChange(patch);
	    for(int i=0;i<notes.length;i++)
		{
		    int noteNumber = notes[i];
		    int velocity = velocities[i];

		    /* Make a noise */
		    channel.noteOn(noteNumber, velocity);

		    if(polyChordable)
			channel.setPolyPressure(noteNumber, velocity);
		}

	}


    }

    /** Stop all notes immediately */
    public static void stopAll()
    {
	    for(int i=0;i<16;i++)
		    channels[i].allNotesOff();
    }

    /**
     * The names of all 127 General MIDI instruments
     */
    public static final String [] patchNames = {
	"Acoustic Grand Piano",	"Bright Acoustic Piano",
	"Electric Grand Piano",	"Honky-tonk Piano",
	"Rhodes Piano", "Chorused Piano",
	"Harpsichord",	"Clavinet",
	"Celesta", 
	"Glockenspiel", "Music Box",
	"Vibraphone", "Marimba",
	"Xylophone", "Tubular Bells",
	"Dulcimer", "Hammond Organ",
	"Percussive Organ", "Rock Organ",
	"Church Organ", "Reed Organ",
	"Accordion", "Harmonica",
	"Tango Accordion", "Acoustic Guitar (nylon)",
	"Acoustic Guitar (steel)", "Electric Guitar (jazz)",
	"Electric Guitar (clean)", "Electric Guitar (muted)",
	"Overdriven Guitar", "Distortion Guitar", 
	"Guitar Harmonics", "Acoustic Bass",
	"Electric Bass (finger)", "Electric Bass (pick)",
	"Fretless Bass", "Slap Bass 1",
	"Slap Bass 2", "Synth Bass 1", 
	"Synth Bass 2", "Violin",
	"Viola", "Cello",
	"Contrabass", "Tremolo Strings",
	"Pizzicato Strings", "Orchestral Harp",
	"Timpani", "String Ensemble 1",
	"String Ensemble 2", "SynthStrings 1",
	"SynthStrings 2", "Choir Aahs",
	"Voice Oohs", "Synth Voice",
	"Orchestra Hit", "Trumpet",
	"Trombone", "Tuba",
	"Muted Trumpet", "French Horn",
	"Brass Section", "Synth Brass 1",
	"Synth Brass 2", "Soprano Sax",
	"Alto Sax", "Tenor Sax",
	"Baritone Sax", "Oboe",
	"English Horn", "Bassoon",
	"Clarinet", "Piccolo",
	"Flute", "Recorder",
	"Pan Flute", "Bottle Blow",
	"Shakuhachi", "Whistle",
	"Ocarina", "Lead 1 (square)",
	"Lead 2 (sawtooth)", "Lead 3 (calliope lead)",
	"Lead 4 (chiff lead)", "Lead 5 (charang)",
	"Lead 6 (voice)", "Lead 7 (fifths)",
	"Lead 8 (bass + lead)", "Pad 1 (new age)",
	"Pad 2 (warm)", "Pad 3 (polysynth)",
	"Pad 4 (choir)", "Pad 5 (bowed)",
	"Pad 6 (metallic)", "Pad 7 (halo)",
	"Pad 8 (sweep)", "FX 1 (rain)",
	"FX 2 (soundtrack)", "FX 3 (crystal)",
	"FX 4 (atmosphere)", "FX 5 (brightness)",
	"FX 6 (goblins)", "FX 7 (echoes)",
	"FX 8 (sci-fi)", "Sitar",
	"Banjo", "Shamisen",
	"Koto", "Kalimba",
	"Bagpipe", "Fiddle",
	"Shanai", "Tinkle Bell", 
	"Agogo", "Steel Drums",
	"Woodblock", "Taiko Drum",
	"Melodic Tom", "Synth Drum",
	"Reverse Cymbal", "Guitar Fret Noise",
	"Breath Noise", "Seashore",
	"Bird Tweet", "Telephone Ring",
	"Helicopter", "Applause",
	"Gunshot"
    };  

    /**
     * Sleep for the given duration
     * and attempt to correct for any timing errors that occur
     */
     public static void safeSleep(long duration)
    {
	long startTime = System.currentTimeMillis();
	int div = 10;

	if(duration<div*4)
	    try{Thread.sleep(duration);} catch(InterruptedException ie){}
	else
	    {
		long divDuration = duration/div;
		long currentTime, endTime = startTime+duration;

		currentTime=System.currentTimeMillis();

		while(currentTime-startTime<duration)
		    {
			if(duration-(currentTime-startTime)<divDuration)
			    try{Thread.sleep(duration-(currentTime-startTime));}  catch(InterruptedException ie){}
			else
			    try{Thread.sleep(divDuration);}  catch(InterruptedException ie){}
			currentTime=System.currentTimeMillis();
		    }
	    }
    }


    /**
     * Return a JComboBox with the patch names 
     * and select the first element
     */
    public static JComboBox getPatchSelector()
    {
	JComboBox retVal = new JComboBox(patchNames);
	retVal.setSelectedIndex(0);
	return retVal;
    }

    /**
     * Return a JComboBox with the patch names 
     * and select the patch passed
     */
    public static JComboBox getPatchSelector(int currentPatch)
    {
	JComboBox retVal = new JComboBox(patchNames);
	if(currentPatch>=0 && currentPatch<128)
	    retVal.setSelectedIndex(currentPatch);
	return retVal;
    }


    /**
     * Return the name of the specified patch number
     * or an empty string if number is out of range
     *
     */
    public static String numberToName(int number)
    {
	if(number>=0 && number<128)
	    return patchNames[number];
	else
	    return "";
    }

    


    /**
     * Return the first patch index whose name
     * contains the given string
     */
    public static int getMatchingPatch(String name)
    {
	
	name = name.toLowerCase();
	for(int i=0;i<patchNames.length;i++)
	    {
		if(patchNames[i].toLowerCase().indexOf(name)>=0)
		    return i;
	    }
	return 0;

    }

    /**
     * Take a patch name and return the closest matching
     * patch number. Return 0 if name not found.
     */
    public static int nameToNumber(String name)
    {
	for(int i=0;i<patchNames.length;i++)
	    {
		if(name.toLowerCase().startsWith(patchNames[i].toLowerCase()))
		    return i;
	    }
	return 0;
    }

    
    /**
     * Recursively compute the panning value for a given
     * path
     */
    private static int recursePan(int curPath, int val)
    {
	if(curPath==0)
	    return 0;
	if((curPath%2)==0)
	    return val+recursePan(curPath>>1, val>>1);
	else
	    return -val+recursePan(curPath>>1, val>>1);
    }

    /**
     * Given an integer, return a panning position, such
     * that consecutive positions are well distributed within
     * the stereo space.
     */
    public static int getPan(int path)
    {
	if(path==0)
	    return 64;
	return 64+recursePan(path, 32);
    }

    /**
     * Play a note, given a channel and a set of note parameters:
     * Pitch, velocity, duration and an instrument name
     * Uses the piano if an invalid name is specified.
     */
    public static void playNote(final int channelIndex, final int pitch,
    final int vel, final int dur, final String patchName)
    {
	int patch = getMatchingPatch(patchName);
	
	playNote(channelIndex, pitch, vel, dur, patch);
    }



    /**
     * Play a note, given a channel and a set of note parameters:
     * Pitch, velocity, duration and a patch (instrument) number
     */
    public static void playNote(final int channelIndex, final int pitch,
    final int vel, final int dur, final int pat)
    {
	new Thread()
	    {
		public void run()
		{
		    playNoteBlocking(channelIndex, pitch, vel, dur, pat);
		}
	    }.start();
    }

        /**
     * Play a note, given a channel and a set of note parameters:
     * Pitch, velocity, duration and a patch (instrument) number
     */
    public static void playNote(final int channelIndex, final int pitch,
    final int vel, final int dur, final int pat, final int pan)
    {

	new Thread()
	    {
		public void run()
		{
		    playNoteBlocking(channelIndex, pitch, vel, dur, pat, pan);
		}
	    }.start();
    }

    /**
     * Play a note, given a channel and a set of note parameters:
     * Pitch, velocity, duration and a patch (instrument) number
     * Block until the note has finished playing
     */
    public static void playNoteBlocking(final int channelIndex, final int pitch,
					final int vel, final int dur, final int pat)
    {
	playNoteBlocking(channelIndex, pitch, vel, dur, pat, 64);
    }


    /**
     * Play a note, given a channel and a set of note parameters:
     * Pitch, velocity, duration, pan and a patch (instrument) number
     * Block until the note has finished playing
     */
    public static void playNoteBlocking(final int channelIndex, final int pitch,
    final int vel, final int dur, final int pat, final int pan)
    {

		    MidiChannel channel = channels[channelIndex];
	
		    int noteNumber = pitch;
		    int velocity = vel;
		    int duration = dur;
		    int patch = pat;

		    channel.controlChange(10, pan);

                    /* Set the patch */
                    channel.programChange(patch);

		    /* Make a noise */
		    channel.noteOn(noteNumber, velocity);
		    safeSleep(duration);
		    
                    channel.noteOff(noteNumber);

    }

    static
    {
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

    }
    
}




