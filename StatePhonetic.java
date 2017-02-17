import duotonic.*;
import sigil.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

public class StatePhonetic extends SProcessorModel
{

    private Vector stateTable;
    private int nCodes;
    private transient Speech speaker;
    private transient MarkovPhonetic phoneticModel;
    private transient JButton addSpeech, clearSpeech;
    private Vector speechPhonemes = new Vector(); 
    private Vector markovPhonemes = new Vector();
    private transient DefaultListModel speechList = new DefaultListModel();
    private boolean usePath = false, markov = true;
    private int state;
    private String currentPrefix;
    private int curMaxState;
    private int index = 0;
    private int oldPath = 0;

    private class StateElement implements Serializable
    {
	private Hashtable transitions;
	private String phoneme;

	public boolean hasTransition(int transition)
	{
	    return transitions.containsKey(new Integer(transition));
	}

	public StateElement(String phoneme)
	{
	    transitions = new Hashtable();
	    this.phoneme = phoneme;
	}

	public void addTransition(int transition, int state)
	{
	    if(!transitions.containsKey(new Integer(transition)))
		transitions.put(new Integer(transition), new Integer(state));
	}

	public String getPhoneme()
	{
	    return phoneme;
	}

	public int getTransition(int transition)
	{
	    Integer newState = (Integer)(transitions.get(new Integer(transition)));
	    if(newState!=null)
		return newState.intValue();
	    else
		return 0;
	}
    }

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "StatePhonetic";
    }
    public String getDescription()
    {
	return "Provides speech feedback on StateRecognizer output, using a tied markov model";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }


    public void createTable(int size)
    {
	int nCodes = size*size*size;
	stateTable = new Vector();
	StateElement stateElt = new StateElement("_");
	stateTable.add(stateElt);
	currentPrefix = phoneticModel.getInitialState();
	state = 0;
	curMaxState = 1;
    }

    

  public void connectionChange()
  {

  }


    private class RadioListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    String command = ae.getActionCommand();
	    if(command.equals("Markov state-based"))
		{
		    markov = true;
		    usePath = false;
		    addSpeech.setEnabled(false);
		    clearSpeech.setEnabled(false);
		}
	    else if(command.equals("Markov path-based"))
		{
		    markov = true;
		    usePath = true;
		    addSpeech.setEnabled(false);
		    clearSpeech.setEnabled(false);
		}
	    else if(command.equals("Speech path-based"))
		{
		    markov = false;
		    usePath = true;		
    		    addSpeech.setEnabled(true);
		    clearSpeech.setEnabled(true);
		}

	    else if(command.equals("Add speech"))
		{
		    String newSpeech = JOptionPane.showInputDialog(null, "Please enter new sequence", 
								   "New speech", JOptionPane.QUESTION_MESSAGE);
		    if(newSpeech!=null)
			{
			    speechPhonemes.add(textToPhonemeVector(newSpeech));
			    speechList.addElement(newSpeech);
			}
		    
		}
	    
	    else if(command.equals("Clear speech"))
		{

		    speechPhonemes = new Vector();
		    speechList.removeAllElements();
		}
	}


    }

    
    public void showInterface()
    {
     JFrame jf = new JFrame();
     
     UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
     jf.setSize(200,300);
     jf.setTitle(getName());
     Container gc = jf.getContentPane();
     Box mainPanel = Box.createVerticalBox();
     JRadioButton markovButton = new JRadioButton("Markov state-based");
     JRadioButton markovPathButton = new JRadioButton("Markov path-based");
     JRadioButton speechButton = new JRadioButton("Speech path-based");
     addSpeech = new JButton("Add speech");
     addSpeech.setEnabled(!markov);
     clearSpeech = new JButton("Clear speech");
     clearSpeech.setEnabled(!markov);
     if(markov && !usePath)
	 markovButton.setSelected(true);
     else if(markov)
	 markovPathButton.setSelected(true);
     else
	 speechButton.setSelected(true);
     ButtonGroup feedbackGroup = new ButtonGroup();
     feedbackGroup.add(markovButton);
     feedbackGroup.add(markovPathButton);
     feedbackGroup.add(speechButton);
     markovButton.addActionListener(new RadioListener());
     markovPathButton.addActionListener(new RadioListener());
     speechButton.addActionListener(new RadioListener());
     addSpeech.addActionListener(new RadioListener());
     clearSpeech.addActionListener(new RadioListener());
     mainPanel.add(markovButton);
     mainPanel.add(markovPathButton);
     mainPanel.add(speechButton);
     mainPanel.add(Box.createVerticalStrut(5));
     mainPanel.add(addSpeech);
     mainPanel.add(clearSpeech);
     mainPanel.add(Box.createVerticalStrut(5));

     JList speechListing = new JList(speechList);
     mainPanel.add(new JScrollPane(speechListing));
     gc.add(mainPanel);
     UIUtils.setColors(gc, Color.black, Color.white);
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
        speaker = new Speech();
	phoneticModel = new MarkovPhonetic("englishModel");
	speechPhonemes = new Vector();
	speechList = new DefaultListModel();
    }

    private Vector textToPhonemeVector(String text)
    {
	String phonemes = TextPhoneme.textToPhonemes(text);
	Vector retVal = new Vector();
	StringTokenizer tok = new StringTokenizer(phonemes,"/", false);
	while(tok.hasMoreTokens())
	    retVal.add(tok.nextToken());
	System.out.println(retVal);
	return retVal;
    }

    public StatePhonetic()
    {
	speaker = new Speech();
	phoneticModel = new MarkovPhonetic("englishModel");
	createTable(3);
    }

   public void processHetObject(Object o)
   {
       if(o instanceof StateInformation)
	   {
	       StateInformation stateInfo = (StateInformation) o;
	       if(stateInfo.getBoolean("Correct"))
		   dropFeedback();
	       else if(stateInfo.getBoolean("Drop"))
		   dropFeedback();
	       else if(stateInfo.getBoolean("Continue"))
		   playContinueFeedback(stateInfo.getTransition(), stateInfo.getPathInfo());
	       	else if(stateInfo.getBoolean("Alphabet Size Change"))
		    createTable(((Integer)(stateInfo.getObject("Alphabet Size"))).intValue());
	   }
   }
        
    private void dropFeedback()
    {
	state = 0;
	index = 0;
	currentPrefix = phoneticModel.getInitialState();
    }
    
    private void playMarkovState(int transition)
    {
	StateElement stateElt = (StateElement)(stateTable.get(state));
		String nextPhoneme = "_";
		if(!stateElt.hasTransition(transition))
		    {
			stateElt.addTransition(transition, curMaxState++);
			nextPhoneme = phoneticModel.markovSimulate(currentPrefix);
			while(nextPhoneme.charAt(0)=='_')
			    {
				currentPrefix=phoneticModel.getInitialState();
				nextPhoneme = phoneticModel.markovSimulate(currentPrefix);
			    }
			stateTable.add(new StateElement(nextPhoneme));
			state = curMaxState-1;
		    }
		else
		    {
			nextPhoneme = stateElt.getPhoneme();
			state = stateElt.getTransition(transition);
		    }
		currentPrefix = currentPrefix.substring(1)+nextPhoneme;
		speaker.say(MarkovPhonetic.translatePhoneme(nextPhoneme), false);
    }

    private void playMarkovPath(int path)
    {
	if(path<speechPhonemes.size())
	    {
		Vector phonemeVec = (Vector)(markovPhonemes.get(path));
		
		if(phonemeVec!=null && index<phonemeVec.size())
		    {
			String newPhoneme = (String)(phonemeVec.get(index));
			speaker.say(MarkovPhonetic.translatePhoneme(newPhoneme), false);
			index++;
		    }
		else
		    {
			int len = phonemeVec.size();
			String lastPhoneme = (String)(phonemeVec.get(len-1));
			String secondLastPhoneme = "_";
			if(len>1)
			    secondLastPhoneme = (String)(phonemeVec.get(len-2));
			String newPhoneme = phoneticModel.markovSimulate(secondLastPhoneme+
									 lastPhoneme);
			phonemeVec.add(newPhoneme);
			speaker.say(MarkovPhonetic.translatePhoneme(newPhoneme));
		    }
	    }
	else
	    {
		Vector newPath = new Vector();
		newPath.add(phoneticModel.markovSimulate(phoneticModel.getInitialState()));
		markovPhonemes.add(newPath);
	    }
    }

    private void playSpeechPath(int path)
    {
	if(path<speechPhonemes.size())
	    {
		Vector phonemeVec = (Vector)(speechPhonemes.get(path));
		
		if(phonemeVec!=null && index<phonemeVec.size())
		    {
			String newPhoneme = (String)(phonemeVec.get(index));
			speaker.say(newPhoneme, false);
			index++;
		    }
		else
		    index = 0;
	    }
    }
    
    private void playContinueFeedback(int transition, int path)
    {
	if(path!=oldPath)
	    index = 0;

	if(phoneticModel!=null && !usePath)
	    playMarkovState(transition);
	    
	else if(markov && usePath)
	    playMarkovPath(path);
	else
	    playSpeechPath(path);

	  
	oldPath = path;
    }
}
