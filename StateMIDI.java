import duotonic.*;
import sigil.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class StateMIDI extends SProcessorModel
{
    public static final int MUSIC_SCALE = 0;
    public static final int MUSIC_MELODY = 1;
    public int musicType = MUSIC_SCALE; 
    private transient Hashtable melodyTable;
    private transient EarwigCompiler currentMelody;
    private transient JCheckBox panningGest, timbreGest, playMusic, playTerm, playNeg, playCont, ascendingBox;
    private boolean terminalPlayed = true;
    private boolean negativePlayed = true;
    private boolean continuePlayed = true;
    private boolean panningTied = false;
    private int currentVel = 0;
    private int currentPitch = 0;
    private int pitchInc = 1;
    private int velInc = 30;
    private int patchContinue = 0;
    private int patchSuccess = 74;
    private int patchTerminal = 83;    
    private int patchDrop = 118;
    private int lastPan = 64;
    private boolean timbreTied = false;
    private boolean useShepard = false;
    private int lastTimbre;
    private int [] permutedPatches;
    private boolean ascending = true;
 

    static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "StateMIDI";
 }
 public String getDescription()
 {
  return "Provides MIDI audio feedback on StateRecognizer output";
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

 private class CheckListener implements ActionListener
 {
     private String command;
     private JComboBox melodyBox;

  public void actionPerformed(ActionEvent ae)
  {
      boolean val = ((JToggleButton)(ae.getSource())).isSelected();
      if(command.equals("Terminal"))
	  {
	      terminalPlayed = val;
	  }
      else if(command.equals("Continuous"))
	  {
	      continuePlayed = val;
	      if(melodyBox!=null && melodyBox.isEnabled() && !continuePlayed)
		  melodyBox.setEnabled(false);
	  }
      else if(command.equals("Drop"))
	  {
	      negativePlayed = val;
	  }

      else if(command.equals("Ascending"))
	  {
	      ascending = val;
	  }
      else if(command.equals("Panning"))
	  {
	      panningTied = val;
	  }
      else if(command.equals("Melody"))
	  {
	      musicType = (val) ? MUSIC_MELODY : MUSIC_SCALE;
	      melodyBox.setEnabled(val);
	      ascendingBox.setEnabled(!val);
	  }
      else if(command.equals("Timbre"))
	  {
	      timbreTied = val;
	  }
      else if(command.equals("Shepard"))
	  {
	      useShepard = true;
	      panningGest.setEnabled(false);
	      timbreGest.setEnabled(false);
	  }
      else if(command.equals("MIDI"))
	  {
	      useShepard = false;
	      panningGest.setEnabled(true);
	      timbreGest.setEnabled(true);
	  }
  }

  public CheckListener(String cmd, JComboBox mBox)
  {
    command = cmd;
    melodyBox = mBox;
  }
 }
    
    private class ComboListener implements ItemListener
    {
	public void itemStateChanged(ItemEvent ie)
	{
	    JComboBox jCombo = (JComboBox)(ie.getSource());
	    String selectedMelody = (String)(jCombo.getSelectedItem());
	    currentMelody = (EarwigCompiler)(melodyTable.get(selectedMelody));
	}
    }
	
    public void showInterface()
    {
     JPanel cen = UIUtils.addFillers(new JPanel(new BorderLayout()), 20, 20);
     Box eastPanel = Box.createVerticalBox();
     JRadioButton midiButton = new JRadioButton("Use MIDI");
     JRadioButton shepardButton = new JRadioButton("Use Shepard tones");
     midiButton.setSelected(!useShepard);
     shepardButton.setSelected(useShepard);
     ButtonGroup synthGroup = new ButtonGroup();
     synthGroup.add(midiButton);
     synthGroup.add(shepardButton);
     midiButton.addActionListener(new CheckListener("MIDI",null));
     shepardButton.addActionListener(new CheckListener("Shepard",null));

     panningGest = new JCheckBox("Tie panning to gestures", panningTied);
     timbreGest = new JCheckBox("Tie timbre to gestures", timbreTied);
     playTerm = new JCheckBox("Play terminal audio", terminalPlayed);
     playCont = new JCheckBox("Play continuous audio", continuePlayed);
     playNeg = new JCheckBox("Play negative feedback audio", negativePlayed);
     playMusic = new JCheckBox("Melody", (musicType==MUSIC_MELODY));
     ascendingBox = new JCheckBox("Ascending", ascending);

     JComboBox melodyBox = new JComboBox();
     Enumeration enum = melodyTable.keys();
     while(enum.hasMoreElements())
	 {
	     String melodyName = (String)(enum.nextElement());
	     melodyBox.addItem(melodyName);
	 }

     melodyBox.addItemListener(new ComboListener());
     timbreGest.addActionListener(new CheckListener("Timbre", null));
     panningGest.addActionListener(new CheckListener("Panning", null));
     playTerm.addActionListener(new CheckListener("Terminal", null));
     playCont.addActionListener(new CheckListener("Continuous", melodyBox));
     playNeg.addActionListener(new CheckListener("Drop", null));
     if(musicType!=MUSIC_MELODY)
       melodyBox.setEnabled(false);
     playMusic.addActionListener(new CheckListener("Melody", melodyBox));

     eastPanel.add(midiButton);
     eastPanel.add(shepardButton);
     eastPanel.add(ascendingBox);
     eastPanel.add(timbreGest);
     eastPanel.add(panningGest);
     eastPanel.add(playTerm);
     eastPanel.add(playCont);
     eastPanel.add(playNeg);
     eastPanel.add(playMusic);
     eastPanel.add(melodyBox);
     cen.add(eastPanel, BorderLayout.EAST);
     JFrame jf = new JFrame();
     jf.getContentPane().add(cen);
     UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
     jf.setSize(250,300);
     jf.setTitle(getName());
     jf.show();
    }


    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	openMelodies();
    }

    private void openMelodies()
    {
	try{
	BufferedReader reader = Library.getReader("melodies.ewg");
	String inLine = reader.readLine();
	melodyTable = new Hashtable();
	while(inLine!=null)
	    {
		String name = inLine;
		inLine = reader.readLine();
		String toParse = inLine;
		inLine = reader.readLine();
		if(toParse!=null)
		    {
			try{
			    EarwigParser tempParser = new EarwigParser(toParse);
			    EarwigCompiler newCompiled = new EarwigCompiler();
			    newCompiled.compile(tempParser);
			    melodyTable.put(name, newCompiled);
			} catch(EarwigParser.EarwigSyntaxException ese) {}
		    } 
	    }
	}
	catch(IOException ioe) {ioe.printStackTrace();}
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

    public StateMIDI()
    {	
	openMelodies();
	permutePatches();
    }

   public void processHetObject(Object o)
   {
       if(o instanceof StateInformation)
	   {
	       StateInformation stateInfo = (StateInformation) o;
	       if(stateInfo.getBoolean("Terminal"))
		   playTerminalFeedback();
	       if(stateInfo.getBoolean("Drop"))
		   playDropFeedback(stateInfo.getBoolean("Negative"));
	       if(stateInfo.getBoolean("Continue"))
		   playContinueFeedback(stateInfo);
	   }
       else if(o instanceof ProbabilisticGesture)
	   {
	       ProbabilisticGesture pGest = (ProbabilisticGesture) o;
	       ParameterizedGesture mostProbable = pGest.getMostProbable();
		if(mostProbable!=null)
		    {
			playCorrectFeedback(mostProbable);
		    }
	   }
   }

   private void playCorrectFeedback(ParameterizedGesture gesture)
     {

	 if(timbreTied)
	     patchSuccess = lastTimbre;
	 else
	     patchSuccess = 74;

	 int vel = (int)(gesture.getProbability()*60.0)+20;
       if(terminalPlayed)
              {
		  MIDIUtils.playNote(0, 48, vel, 500, patchSuccess, lastPan);
		  MIDIUtils.playNote(0, 55, vel, 500, patchSuccess, lastPan);
		  MIDIUtils.playNote(0, 60, vel, 500, patchSuccess, lastPan);
		  MIDIUtils.playNote(0, 64, vel, 500, patchSuccess, lastPan);
		  MIDIUtils.playNote(0, 67, vel, 500, patchSuccess, lastPan);
		  if(gesture.getComplete())
		      {
			  MIDIUtils.playNote(0, 72, vel, 500, patchSuccess, lastPan);
			  MIDIUtils.playNote(0, 84, vel, 500, patchSuccess, lastPan);
		      }
		}
	}

        private  void playTerminalFeedback()
	{
	    if(terminalPlayed)
		{
                            MIDIUtils.playNote(0, 60, 127, 100, patchTerminal);
                            MIDIUtils.playNote(0, 64, 127, 100, patchTerminal);
		}
	}
        
        private void playDropFeedback(boolean wasNegative)
	{
	    if(negativePlayed && wasNegative)
		{
                            MIDIUtils.playNote(0, 50, currentVel, 100, patchDrop);
                            MIDIUtils.playNote(0, 53, currentVel, 100, patchDrop);
		}
		    currentVel = 0;
		    currentPitch = 0;
		    if(currentMelody!=null)
			currentMelody.reset();
	}


        private void playContinueFeedback(StateInformation stateInfo)
	{
	    if(currentVel+velInc<127)
                currentVel+=velInc;

	    if(ascending)
		currentPitch+=pitchInc;
	    else
		currentPitch-=pitchInc;

	    if(timbreTied)
		{
		    patchContinue = permutedPatches[stateInfo.getPathInfo()];
		    lastTimbre = patchContinue;
		}

	    if(panningTied)
		lastPan = MIDIUtils.getPan(stateInfo.getPathInfo());
	    else
		lastPan = 64;


	    if(continuePlayed)
		{
		    if(!useShepard)
			{
			    if(musicType == MUSIC_SCALE)
				{
				    MIDIUtils.playNote(0, 50+currentPitch, currentVel, 100, 
						       patchContinue, lastPan);
				}
			    else if(musicType == MUSIC_MELODY)
				{
				    if(currentMelody!=null)
					currentMelody.playStep();
				}
			}
		    else
			{
			    ParadoxSynthesizer.playNote(currentPitch, 300, false);

			}
		}


	}

   
}
