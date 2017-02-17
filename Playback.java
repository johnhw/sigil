import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

/**
 * SignalDevice: Plays back a recorded signal file
 * stored in standard ASCII format. Uses multiple takes
 * of one gesture, written to disk as files like
 * ecurve_1.dat,  ecurve_2.dat, etc.
 *
 * @author John Williamson
 */
public class Playback extends GeneratorModel
{

    //Color device changes when playback in progress
    private static Color recColor = new Color(88,48,18);
    private boolean segment = false, segmentRecord = false;
    private static final int waitTicks = 30;
    private int curWait;
    private String oldLine;
    
    //Properties...                                                  
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "Playback";
    }
    public String getDescription()
    {
	return "Plays back a recorded signal file";
    }
    public String getDate()
    {
	return "Janurary 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    /**
     * Return the current color (changes during playback)
     */
    public Color getColor()
    {
	if(!playing)
	    return super.getColor();
	else
	    return recColor;
    }


    //True if playback paused in the middle of a file
    private boolean paused = false;

    //Base name of the current gesture
    private String currentName = "";

    //Reader for the current file
    private transient BufferedReader inReader;

    //Button for toggling the play control
    private JToggleButton playButton;


    //ID number of particular example of current gesture
    private String currentNum;

    //Combo box for selecting which example of the gesture to play back
    private JComboBox numBox;

    //True if a playback is in progress
    private boolean playing = false;

    //Time in ms since epoch at which playback of the current
    //gesture began
    private long startTime = 0;

    //True if the input file was just opened, but not yet read from
    private boolean justOpened = false;

    //The current output signal width
    private int signalWidth;

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
	
	  playing = false;
	  paused = false;
	  if(playButton!=null)
	      playButton.setSelected(false);
	  propogateWidthChanges();
    }

    /**
      * Read in a list of all the files ending in .dat in the current directory
      * which are of the form "<something>_<num>.dat" and stores the list
      * of base names found in a hashtable
      * Each name maps to a vector of example numbers (the num part in the filename)
      *
      * @return Hashtable mapping names to vectors of integers
      */
    private Hashtable getBaseNames()
    {
        //Read directory
	File thisDir = new File(".");
	File [] files = thisDir.listFiles();
	int maxInt = 0;
	Hashtable fileList = new Hashtable();

        //For each file
	for(int i=0;i<files.length;i++)
	    {
		String fname = files[i].getName();

                //Check file is of correct format
		if(fname.endsWith(".dat"))
		    {
			int startChop = fname.indexOf("_");
			int endChop = fname.indexOf(".");

                        //Ensure that the underscore is present
			if(startChop>-1 && endChop>-1)
			    {
                                //Slice out number part of name
				String name = fname.substring(0, startChop);
				String num = fname.substring(startChop+1, endChop);

				Vector numVec;
				int thisNum;

                                //Add a new hashtable entry, linking this
                                //basename to the available number
				try
				    {
					thisNum = Integer.parseInt(num);

                                        //Add to hashtable vector
					if(fileList.containsKey(name))
					    numVec = (Vector)(fileList.get(name));
					else
					    numVec = new Vector();
					numVec.add(new Integer(thisNum));

                                        //Store in hashtable
					fileList.put(name, numVec);

				    } catch(NumberFormatException nfe) {}
			    }
		 }
	 }
	return fileList;
 }

    /**
     * Listener for when the user changes the current
     * selection in the name combo box
     */
    private class NameChangeListener implements ItemListener
    {
	private Hashtable keyNum;

	public void  itemStateChanged(ItemEvent e)
	{
            //Get the currently selected item
	    JComboBox src = (JComboBox)(e.getSource());
	    String curSel = src.getSelectedItem().toString();

            //Look up the hashtable of names to numbers and copy
            //the vector for the currently selected name
            //Note that the name is guaranteed to be in the hashtable
            Vector firstNums = new Vector((Vector)(keyNum.get(curSel)));

	    if(firstNums!=null)
		{
                    //Change the contents of the number combo box
                    numBox.removeAllItems();
		    for(int i=0;i<firstNums.size();i++)
			    numBox.addItem(firstNums.get(i).toString());
                   
		}
	}

        /**
          * Creates a new listener, taking the hashtable
          * that maps names to number vectors
          */
        public NameChangeListener(Hashtable kNum)
	{
	    keyNum = kNum;
	}
 
    }

    /**
     * Return the current signal width; always one if
     * not currently playing
     */
    public int getSignalWidth()
    {
	if(!playing || paused)
	    {
		return 1;
	    }
	else
	    return signalWidth;

    }

 /**
  * Show the interface
  */
 public void showInterface()
 {
  //Update file list   
  Hashtable baseNames = getBaseNames();

 

  //Create the name combo box from hashtable data
  Enumeration keys = baseNames.keys();
  final JComboBox nameBox = new JComboBox();
  while(keys.hasMoreElements())
      	  nameBox.addItem(keys.nextElement());

  //Get the first element of the hashtable and fill the
  //number combobox with those numbers
  keys = baseNames.keys();
  Vector firstNums = (Vector)(baseNames.get(keys.nextElement())); 
  numBox = new JComboBox(firstNums);  
  nameBox.addItemListener(new NameChangeListener(baseNames));

  //Layout components
  JPanel setPanel = new JPanel(new BorderLayout());
  setPanel.add(nameBox, BorderLayout.EAST);
  setPanel.add(numBox, BorderLayout.WEST);
  final JComboBox nBox = numBox;

  //Create the segmentation options
  JPanel segmentPanel = new JPanel(new FlowLayout());
  final JCheckBox segmentBox = new JCheckBox("Segment on start/stop", segment);
  final JCheckBox recordBox = new JCheckBox("Send record signals", segmentRecord);
  recordBox.setEnabled(segment);
  segmentPanel.add(segmentBox);
  segmentPanel.add(recordBox);

  segmentBox.addActionListener(new ActionListener()
      {
	  public void actionPerformed(ActionEvent ae)
	  {
	      boolean val = ((JCheckBox)(ae.getSource())).isSelected();
	      segment = val;
	      recordBox.setEnabled(val);
	  }
      });


  recordBox.addActionListener(new ActionListener()
      {
	  public void actionPerformed(ActionEvent ae)
	  {
	      boolean val = ((JCheckBox)(ae.getSource())).isSelected();
	      segmentRecord = val;
	  }
      });

  //Add listener for play/pause control
  playButton = new JToggleButton("Play", playing);
  playButton.addActionListener(new ActionListener()
      {
	  public void actionPerformed(ActionEvent ae)
	  {
	      currentName = nameBox.getSelectedItem().toString();
              currentNum = nBox.getSelectedItem().toString();
	      if(playing)
		  pausePlaying();
	      else
		  startPlaying();
	  }

      });

  //Add a stop control
  JButton stopButton  = new JButton("Stop");
  stopButton.addActionListener(new ActionListener()
      {
	  public void actionPerformed(ActionEvent ae)
	  {

	      stopPlaying();
	  }
      });

  //Layout components
  JPanel lowerPanel = new JPanel(new BorderLayout());
  JPanel controlPanel = new JPanel(new FlowLayout());
  controlPanel.add(playButton);
  controlPanel.add(stopButton);
  lowerPanel.add(segmentPanel, BorderLayout.NORTH);
  
  lowerPanel.add(controlPanel, BorderLayout.SOUTH);
  JPanel mainPanel = new JPanel(new BorderLayout());
  mainPanel.add(lowerPanel, BorderLayout.SOUTH);
  mainPanel.add(setPanel, BorderLayout.NORTH);

  //Make a frame and add the components to it
  JFrame jf;
  jf = new JFrame();
  jf.setSize(400,130);
  jf.getContentPane().add(mainPanel, BorderLayout.CENTER);  
  jf.setTitle(getName());
  jf.show();

 }

 public void deleted()
 {
 }

 /**
  * Open the file specified by the current selection
  * and set the playback flags
  */
 private void startPlaying()
 {
     if(!currentName.equals("") && !paused)
	 {

	     startTime = System.currentTimeMillis();
	     try
		 {
		     inReader = new BufferedReader(new FileReader(currentName+"_"+currentNum+".dat"));
		 }catch(IOException ioe) { ioe.printStackTrace(); }
	 }
     curWait = waitTicks;
     justOpened = true;
     playing = true;
     paused = false;
     
	  
 }

 /**
  * Stop the playback, and reset the file position
  */
 public void stopPlaying()
 {
  playing = false;
  paused = false;
  playButton.setSelected(false);
  if(segment)
      {
	  if(segmentRecord)
	      distributeHetObject("NamedGesture:"+currentName);
	  else
	      {
		  StateInformation stateInfo = new StateInformation();
		  stateInfo.setBoolean("End");
		  distributeHetObject(stateInfo);
	      }
      }


  try{
  inReader.close();
  }catch(IOException ioe) { ioe.printStackTrace(); }

  //Update the width
  propogateWidthChanges();
 }

    /**
     * Pause playing (stop, but do not reset file position)
     */
    public void pausePlaying()
    {
	playing = false;
	paused = true;
    }

    /**
     * Playback next element from file, if currently playing
     */
    public void tock()
    {
	if(playing)
	    {
		String newLine = null;
		if(segment && curWait == 1)
		    {
			if(segmentRecord)
			    distributeHetObject("NamedGesture:"+currentName);
			else
			    {
				StateInformation stateInfo = new StateInformation();
				stateInfo.setBoolean("Start");
				distributeHetObject(stateInfo);
			    }
		    }
		if(curWait<waitTicks && curWait>0)
		    newLine = oldLine;
		else
		    {
			try{
			    newLine = inReader.readLine();
			}catch(IOException ioe) { ioe.printStackTrace(); }
			oldLine = newLine;
		    }
		if(newLine!=null)
		    {

                        //Break up the input line
			StringTokenizer numTok = new StringTokenizer(newLine);
			int tokCnt = numTok.countTokens();
			double [] valArr = new double[tokCnt-1];

                        //Change signal width
			signalWidth = tokCnt-1;

                        //Update width, if this is the first time
                        //the file has been read from
                        if(justOpened)
                        {
                                propogateWidthChanges();
				
                                justOpened = false;
                        }
			
			int index = 0;
                        numTok.nextToken();  //Skip time stamp

                        //Read each signal value
			while(numTok.hasMoreTokens())
			    {
				double newVal;
				try{
				    newVal = Double.parseDouble(numTok.nextToken());
				    
				}catch(NumberFormatException nfe) {newVal = 0;}

                                //Store in signal array                         
				valArr[index++]=newVal;
			    }

                        //Propogate the new signal
			setCurrentSignal(new GestureSignal(valArr, getID()));
		    }
		else
		    {
			stopPlaying();			
		    }
	    }
		curWait--;
    }

    
}
