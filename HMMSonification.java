import sigil.*;
import duotonic.*;
import java.util.*;
import java.awt.*;
import java.io.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SignalDevice: Sonifies the output of a MarkovModel, using
 * a dissonant chord of varying intensity mapped to the various gesture probabilities
 *
 * @author John Williamson
 */
public class HMMSonification extends SProcessorModel
{
    
    private int [] notes;
    private Hashtable gestureTable = new Hashtable();
    private int patch = MIDIUtils.getMatchingPatch("Violin");
    private int baseNote = 60;
    private transient MIDIUtils.PolyChord polyChord;
    private transient MIDIUtils.PolyChannelChord polyChannelChord;

    private transient SpaceSpeaker spaceSpeaker;
    private transient JComboBox patchSelector;
    private boolean reInitNotes = true;
    private boolean useVelocity = true, usePan = false, useTimbre = false, useDissonance = true, 
	useSpeech = false;
    private boolean useSimplePoly = true;
    private int [] permutedPatches;
    
    public static int threadId = 0;

    private class SpaceSpeaker extends Thread implements Serializable
    {
	private long duration;
	private boolean stopped = false;
	private Vector gestureNames;
	private double [] distance;
	private double [] pan;
	private double [] pitch;
	private Speech speaker;

	private int thisID;
	
	public void setPitch(int index, double newPitch)
	{
	    pitch[index] = 1.0+(newPitch-0.5)/5.0;
	}

	public void stopSpeaker()
	{
	    stopped = true;
	}

	public void restartSpeaker()
	{
	    if(stopped)
		    stopped = false;
	}

	public void setPan(int index, double newPan)
	{
	    pan[index] = newPan;
	}

	public void setDistance(int index, double newDistance)
	{
	    distance[index] = 1.0+newDistance;
	}
	

	public SpaceSpeaker(long dur, Vector gestures)
	{
	    duration = dur;
	    gestureNames = gestures;
	    pitch = new double[gestures.size()];
	    distance = new double[gestures.size()];
	    pan = new double[gestures.size()];
	    for(int i=0;i<gestures.size();i++)
		{
		    pitch[i] = 1.0;
		    distance[i] = 1.0;
		    pan[i] = 0.5;
		}
	    speaker = new Speech();
	    thisID = threadId++;
	}


	public void run()
	{
	    while(true)
		{
		    try{sleep(100);}catch(InterruptedException ie)
			{}
		    while(!stopped)
			{
			    for(int i=0;i<gestureNames.size();i++)
				{
				    try{sleep(duration);}catch(InterruptedException ie){}
				    String toSay = (String)(gestureNames.get(i));
				    if(useVelocity)
					speaker.setDistance(distance[i]);
				    else
					speaker.setDistance(0.0);
				    if(usePan)
					speaker.setPan(pan[i]);
				    else
					speaker.setPan(0.5);
				    if(useDissonance)
					speaker.setPitch(pitch[i]);
				    else
					speaker.setPitch(1.0);
				    speaker.textToSpeech(toSay, false);
				}
			}
		}
	}

    }


    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "HMMSonification";
    }
    public String getDescription()
    {
	return "Sonifies MarkovModel output using a dissonanct chord of varying intensity mapped to the various gesture probabilities";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    public void connectionChange()
    {
    }
    
    public void processHetObject(Object o)
    {
	if(o instanceof StateInformation)
	    {
		StateInformation sInfo = (StateInformation)o;
		if(sInfo.getBoolean("Adjusted probabilities"))
		   newProbabilities((Hashtable)(sInfo.getObject("Probabilities")));
		if(sInfo.getBoolean("End") || sInfo.getBoolean("Drop") 
		   || sInfo.getBoolean("Correct"))
		    stopAll();
	    }
    }


    private void stopAll()
    {
	MIDIUtils.stopAll();
	if(polyChord!=null)
	    polyChord.stop();
	if(polyChannelChord!=null)
	    polyChannelChord.stop();
	stopSpeakers();
    }

    
    private class ComboListener implements ItemListener
    {
	public void itemStateChanged(ItemEvent ie)
	{
	    JComboBox src = (JComboBox)(ie.getSource());
	    patch = src.getSelectedIndex();
	    reInitNotes = true;
	}
    }
    

    private void permutePatches()
    {
	permutedPatches = new int[128];
	Random rnd = new Random();
	for(int i=0;i<128;i++)
	    permutedPatches[i] = i;
	    
	for(int i=0;i<2000;i++)
	    {
		int a = rnd.nextInt(128);
		int b = rnd.nextInt(128);
		int t = permutedPatches[a];
		permutedPatches[a] = permutedPatches[b];
		permutedPatches[b] = t;
	    }
    }

    private void stopSpeakers()
    {
	if(spaceSpeaker!=null)
	    spaceSpeaker.stopSpeaker();

    }

    private void newSpeakers(int num, Vector gestures)
    {
	stopSpeakers();
	if(spaceSpeaker==null)
	    {
		spaceSpeaker = new SpaceSpeaker(120, gestures);
		spaceSpeaker.start();
	    }
	else
	    spaceSpeaker.restartSpeaker();
    }

    private void initializeNotes(int length, Vector gestureNames)
    {
	notes = new int[length];
	if(useDissonance)
	    for(int i=0;i<length;i++)
		notes[i] = baseNote+i;
	else
	     for(int i=0;i<length;i++)
		notes[i] = baseNote;

	stopAll();
	if(useSimplePoly && !useSpeech)
	    polyChord = new MIDIUtils.PolyChord(notes, patch, 7);
	else if(!useSpeech)
	    polyChannelChord = new MIDIUtils.PolyChannelChord();
	if(useSpeech)
		newSpeakers(length, gestureNames);
	adjustProbabilities(length);
	reInitNotes = false;
    }

    private void adjustProbabilities(int length)
    {
	Enumeration enum = gestureTable.keys();
	int noteIndex = 0;
	while(enum.hasMoreElements())
	    {
		Double probability = (Double)(gestureTable.get(enum.nextElement()));

		if(!useSpeech)
		    {
			int newVelocity = (int)(Math.sqrt(probability.doubleValue())*127.0);
			
			if(useVelocity && useSimplePoly && !useSpeech)
			    polyChord.adjustVelocity(noteIndex, newVelocity);
			else if(!useSimplePoly)
			    {
				int newPan = (int)(((double)noteIndex/(double)length)*127.0);
				int newTimbre = permutedPatches[noteIndex];
				int newPitch;
				if(!usePan)
				    newPan = 64;
				if(!useTimbre)
				    newTimbre = patch;
				if(!useVelocity)
				    newVelocity = 80;
				if(!useDissonance)
				    newPitch = baseNote;
				else
				    newPitch = baseNote+noteIndex;
				polyChannelChord.adjustProperties(noteIndex+1, newPitch, newVelocity, 
								  newPan, newTimbre);
			    }
		    }
		else
		    {
			spaceSpeaker.setPan(noteIndex, 1.0-((double)noteIndex/(double)length));
			spaceSpeaker.setPitch(noteIndex, 1.0-((double)noteIndex/(double)length));
			spaceSpeaker.setDistance(noteIndex, 1.0-probability.doubleValue());
			spaceSpeaker.restartSpeaker();
		    }
		noteIndex++;
	    }
    }

    public void newProbabilities(Hashtable probTable)
    {
	
	Enumeration enum = probTable.keys();
	int length  = 0;
	Vector gestureNames = new Vector();
	while(enum.hasMoreElements())
	    {
		String name = (String)(enum.nextElement());
		gestureNames.add(name);
		if(!gestureTable.containsKey(name))
		    reInitNotes = true;
		length++;
	    }
	gestureTable = probTable;
	if(!reInitNotes && ((useSimplePoly && polyChord!=null) || 
			    (!useSimplePoly && polyChannelChord!=null) || useSpeech))
	    adjustProbabilities(length);
	else
	    initializeNotes(length, gestureNames);
    }


    private class CloseListener extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    stopAll();
	}
    }

    private class CheckListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    JCheckBox src = ((JCheckBox)(ae.getSource()));
	    boolean val = src.isSelected();
	    String command = ae.getActionCommand();
	    if(polyChord!=null)
		polyChord.stop();
	    if(polyChannelChord!=null)
		polyChannelChord.stop();
	    polyChord = null;
	    polyChannelChord = null;
	    if(command.equals("Map velocity"))
		useVelocity = val;
	    if(command.equals("Map timbre"))
		{
		    useTimbre = val;
		    patchSelector.setEnabled(!val);
		}
	    if(command.equals("Map panning"))
		usePan = val;
	    if(command.equals("Use speech"))
		useSpeech = val;
	    if(command.equals("Use dissonance"))
		useDissonance = val;
	    useSimplePoly = (!useTimbre && !usePan);
	    reInitNotes = true;
	}

    }

    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(300,250);
	jf.setTitle(getName());
	jf.addWindowListener(new CloseListener());
	Box controlBox = Box.createVerticalBox();
	patchSelector = MIDIUtils.getPatchSelector(patch);
	patchSelector.setEnabled(!useTimbre);
	controlBox.add(patchSelector);
	JCheckBox speechBox = new JCheckBox("Use speech", useSpeech);
	JCheckBox velocityBox = new JCheckBox("Map velocity", useVelocity);
	JCheckBox panBox = new JCheckBox("Map panning", usePan);
	JCheckBox timbreBox = new JCheckBox("Map timbre", useTimbre);
	JCheckBox dissonanceBox = new JCheckBox("Use dissonance", useDissonance);
	controlBox.add(speechBox);
	controlBox.add(velocityBox);
	controlBox.add(panBox);
	controlBox.add(timbreBox);
	controlBox.add(dissonanceBox);
	speechBox.addActionListener(new CheckListener());
	panBox.addActionListener(new CheckListener());
	timbreBox.addActionListener(new CheckListener());
	velocityBox.addActionListener(new CheckListener());
	dissonanceBox.addActionListener(new CheckListener());

	patchSelector.addItemListener(new ComboListener());
	jf.getContentPane().add(controlBox, BorderLayout.NORTH);
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.show();
    }

    
    
    public HMMSonification()
    {
	super();
	permutePatches();
    }

}





