import sigil.*;
import duotonic.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;

/**
 * SignalDevice: Provides summative audio feedback on completion of gestures
 * Maps incoming heterogenous objects to various types of sound feedback.
 *
 * @author John Williamson
 */
public class SummativeFeedback extends SProcessorModel
{
    
    private transient JTextField wavName, speechText;
    private transient  JTextArea midiText;
    private transient JRadioButton speechButton,  wavButton, midiButton, hiddenButton;
    private Hashtable mappingData;
    private int currentRow;
    private MappingTableModel mapModel;
    private int currentSelection;
    private transient Container sidePanel;
    private transient ButtonGroup radioButtons;
    private transient JPanel sampleDetailsPanel, midiDetailsPanel, speechDetailsPanel;
    private transient JTable feedbackTable;
    private transient JFrame jf;
    private transient boolean justSelected;
    private transient Speech currentSpeaker;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "SummativeFeedback";
    }
    public String getDescription()
    {
	return "Provides summative audio feedback on completion of gestures";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }

    public void processHetObject(Object o)
    {
	if(o instanceof ProbabilisticGesture)
	    {
		ProbabilisticGesture pGest = (ProbabilisticGesture) o;
		
		ParameterizedGesture mostProbable = pGest.getMostProbable();
		if(mostProbable!=null && mappingData!=null)
		    {
			String name = mostProbable.getName();
			SummativeMapping mappedFeedback = (SummativeMapping)(mappingData.get(name));
			if(mappedFeedback!=null)
			    if(mappedFeedback instanceof MIDISummativeMapping)
				((MIDISummativeMapping)mappedFeedback).perform(mostProbable);
			    else
				mappedFeedback.perform();
			else
			    {
				SummativeMapping defaultFeedback = (SummativeMapping)(mappingData.get("[Default]"));
				defaultFeedback.perform();

			    }
			    
			
		    }
	    }
    }


 
    public void connectionChange()
    {
    }

    private class MappingTableModel extends AbstractTableModel implements Serializable
    {
	
	private Vector cols;
	private String [] headings = {"Input", "Mapping"};

	public void addRow()
	{
	    Vector vTemp = new Vector();
	    vTemp.add("");
	    vTemp.add("");
	    cols.add(vTemp);
	    fireTableDataChanged();
	}
	
	public String getColumnName(int col) 
	{ 
	    return headings[col].toString(); 
	}

	public int getRowCount() 
	{ 
	    return cols.size(); 
	}

	public int getColumnCount() 
	{ 
	    return headings.length; 
	}

	public Object getValueAt(int row, int col) 
	{ 
	    return ((Vector)(cols.get(row))).get(col); 
	}

	public void removeRow(int row)
	{
	    if(row>=cols.size())
		return;
	    Object temp;
	    for(int i=row+1;i<cols.size();i++)
		cols.setElementAt(cols.get(i), i-1);
	    Vector emptyVec = new Vector();
	    emptyVec.add("");
	    emptyVec.add("");
	    cols.setElementAt(emptyVec, cols.size()-1);
	    fireTableDataChanged();
	}
	
	public boolean isCellEditable(int row, int col)
	{ 
	    return false;
	}
	
	public boolean isMapped(int row)
	{
	    if(row>=0 && row<cols.size())
		{
		    Vector testVec = (Vector)(cols.get(row));
		    if(testVec.size()>0)
			{
			    String testStr = (String)(testVec.get(0));
			    return (!testStr.equals(""));
			}
		    else return false;
		}
	    else
		return false;
	}
	
	public MappingTableModel()
	{
	    super();
	    cols = new Vector();
	    for(int i=0;i<20;i++)
		{
		    for(int j=0;j<2;j++)
			addRow();
		}
	}
	
	public void setValueAt(Object value, int row, int col) 
	{
	    ((Vector)(cols.get(row))).setElementAt(value, col);
	    fireTableCellUpdated(row, col);
	}
    }


    private class ButtonListener implements ActionListener
    {
	public void actionPerformed(ActionEvent ae)
	{
	    String command = ae.getActionCommand();
	    if(command.equals("Choose..."))
		{
		    FileBrowser fb = new FileBrowser(".wav", ".");
		    String chosenFile = fb.getFilename();
		    wavName.setText(chosenFile);
		}
	    else if (command.equals("Preview"))
		{
		    if(currentSpeaker==null)
			currentSpeaker = new Speech();
		    currentSpeaker.textToSpeech(speechText.getText(), false);
		}
	    else if(command.equals("Test"))
		{
		    try{
			EarwigParser parsed = new EarwigParser(midiText.getText());
			EarwigCompiler compiledSound = new EarwigCompiler();
			compiledSound.compile(parsed);
			compiledSound.play();
		    }
		    catch(EarwigParser.EarwigSyntaxException ese)
			{
			    JOptionPane.showMessageDialog(null, ese.getMessage(),
							  "Parse error", JOptionPane.ERROR_MESSAGE);
			}
		    

		}
	    else if(command.equals("Edit..."))
		{
		    Duotonic speakerEdit = new Duotonic();
		    currentSpeaker = speakerEdit.getFinalSpeaker();
		}
	    else if (command.equals("Play"))
		{
		    File testFile = new File(wavName.getText());
		    if(testFile.isFile())
			{
			    Speech.playSample(wavName.getText(), false);
			}
		}
	    else if (command.equals("Add mapping"))
		{
		    String newMapping;
		    newMapping = JOptionPane.showInputDialog(null, "Please enter name of gesture",
							     "Enter name", 
							     JOptionPane.QUESTION_MESSAGE);

		    if(newMapping!=null && !newMapping.equals(""))
			{
			   
			    mapModel.setValueAt(newMapping, currentRow, 0);
			    feedbackTable.getSelectionModel().setSelectionInterval(currentRow, 
			    							   currentRow);
			    updateSelection();
			    currentRow++;
			    radioButtons.setSelected(hiddenButton.getModel(), true);

			    UIUtils.setEnabled(sidePanel, true);
			    UIUtils.setEnabled(speechDetailsPanel, false);
			    UIUtils.setEnabled(midiDetailsPanel, false);
			    UIUtils.setEnabled(sampleDetailsPanel, false);
			    jf.repaint();
			}
		}
	    else if (command.equals("Remove mapping"))
		{
		    mapModel.removeRow(currentSelection);
		}
	}
    }


    private void updateSelection()
    {
	    SummativeMapping mapping = null;
	    String currentCell="";
	    if(currentSelection>=0 && mapModel.getRowCount()>currentSelection)
		{
		    currentCell = (String)(mapModel.getValueAt(currentSelection, 0));
		    
		    if(speechButton.isSelected() && speechButton.isEnabled())
			{
			    mapping = new SpeechSummativeMapping(speechText.getText());
			    if(currentSpeaker!=null)
				((SpeechSummativeMapping)mapping).setSpeaker(currentSpeaker);
			}
		    if(midiButton.isSelected() && midiButton.isEnabled())
			mapping = new MIDISummativeMapping(midiText.getText());
		    if(wavButton.isSelected() && wavButton.isEnabled())
			mapping = new SampleSummativeMapping(wavName.getText());
		    
		    if(mapping!=null)
			{
			    mapModel.setValueAt(mapping.toString(), currentSelection, 1);
			    mappingData.put(currentCell, mapping);
			}
		}	
	    ListSelectionModel src = feedbackTable.getSelectionModel();
	    currentSelection   = src.getMaxSelectionIndex();
	    
	    wavName.setText("");
	    midiText.setText("");
	    speechText.setText("");

            if(currentSelection<0 || currentSelection>=mapModel.getRowCount())
                return;

	    currentCell = (String)(mapModel.getValueAt(currentSelection, 0));

	    SummativeMapping newMapping = (SummativeMapping)
		(mappingData.get(currentCell));

	    						  
	    if(mapModel.isMapped(currentSelection))
		UIUtils.setEnabled(sidePanel, true);
	    else
		UIUtils.setEnabled(sidePanel, false);

	    radioButtons.setSelected(hiddenButton.getModel(), true);
	    UIUtils.setEnabled(speechDetailsPanel, false);
	    UIUtils.setEnabled(midiDetailsPanel, false);
	    UIUtils.setEnabled(sampleDetailsPanel, false);
	    currentSpeaker = null;

	    if(newMapping!=null)
		{
		    if(newMapping instanceof SpeechSummativeMapping)
			{
			    radioButtons.setSelected(speechButton.getModel(), true);
			    speechText.setText(newMapping.getString());
			    currentSpeaker = ((SpeechSummativeMapping)newMapping).getSpeaker();
			     UIUtils.setEnabled(speechDetailsPanel, true);

			}
		    else if(newMapping instanceof MIDISummativeMapping)
			{
			    radioButtons.setSelected(midiButton.getModel(), true);
			    midiText.setText(newMapping.getString());
			     UIUtils.setEnabled(midiDetailsPanel, true);
			}
		    else if(newMapping instanceof SampleSummativeMapping)
			{
			    radioButtons.setSelected(wavButton.getModel(), true);
			    wavName.setText(newMapping.getString());
			     UIUtils.setEnabled(sampleDetailsPanel, true);
			}   
		}
	

    }

    private class TableSelectionListener implements ListSelectionListener
    {
	public void valueChanged(ListSelectionEvent lse)
	{
	    if(!justSelected)
		{
		    justSelected = true;
		    updateSelection();
		}
	    else
		justSelected = false;
	}

    }



    public Container makeFeedbackPanel()
    {
	Box retVal = Box.createVerticalBox();
	radioButtons = new ButtonGroup();
	hiddenButton = new JRadioButton("Hidden");
	radioButtons.add(hiddenButton);
	hiddenButton.setVisible(false);
	retVal.add(hiddenButton);
	JPanel samplePanel = new JPanel(new FlowLayout());
	sampleDetailsPanel = new JPanel(new BorderLayout());
	wavName = new JTextField(15);
	JButton chooseButton = new JButton("Choose...");
	JButton previewWavButton = new JButton("Play");
	previewWavButton.addActionListener(new ButtonListener());
	JPanel wavButtonPanel = new JPanel(new FlowLayout());
	wavButtonPanel.add(chooseButton);
	wavButtonPanel.add(previewWavButton);
	chooseButton.addActionListener(new ButtonListener());
	sampleDetailsPanel.add(wavName, BorderLayout.NORTH);
	sampleDetailsPanel.add(wavButtonPanel, BorderLayout.SOUTH);
	wavButton = new JRadioButton("Sample");
	radioButtons.add(wavButton);
	wavButton.addActionListener(new RadioListener());
	samplePanel.add(wavButton, BorderLayout.EAST);
	samplePanel.add(sampleDetailsPanel, BorderLayout.WEST);
	samplePanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	retVal.add(samplePanel);

	JPanel speechPanel = new JPanel(new FlowLayout());
	speechDetailsPanel = new JPanel(new BorderLayout());
        speechText = new JTextField(15);
	JButton previewButton = new JButton("Preview");
	previewButton.addActionListener(new ButtonListener());
	JButton editButton = new JButton("Edit...");
	editButton.addActionListener(new ButtonListener());
	JPanel spButtonPanel = new JPanel(new  FlowLayout());
	spButtonPanel.add(previewButton);
	spButtonPanel.add(editButton);
	speechDetailsPanel.add(speechText, BorderLayout.NORTH);
	speechDetailsPanel.add(spButtonPanel, BorderLayout.SOUTH);
	speechButton = new JRadioButton("Speech");
	speechButton.addActionListener(new RadioListener());
	radioButtons.add(speechButton);
	speechPanel.add(speechButton, BorderLayout.EAST);
	speechPanel.add(speechDetailsPanel, BorderLayout.WEST);
	speechPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
	retVal.add(speechPanel);
	JPanel midiPanel = new JPanel(new FlowLayout());
	midiDetailsPanel = new JPanel(new BorderLayout());
	midiText = new JTextArea(10,30);
	midiText.setLineWrap(true);
	midiText.setBorder(new LineBorder(Color.black));
	JPanel buttonPanel = new JPanel(new FlowLayout());
	

	JButton previewMidiButton = new JButton("Test");
	previewMidiButton.addActionListener(new ButtonListener());
	buttonPanel.add(previewMidiButton);
	midiDetailsPanel.add(midiText, BorderLayout.NORTH);
	midiDetailsPanel.add(buttonPanel, BorderLayout.SOUTH);
	midiButton = new JRadioButton("MIDI");
	midiButton.addActionListener(new RadioListener());
	radioButtons.add(midiButton);
	midiPanel.add(midiButton, BorderLayout.EAST);
	midiPanel.add(midiDetailsPanel, BorderLayout.WEST);
	midiPanel.setAlignmentX(JComponent.LEFT_ALIGNMENT);
   
	retVal.add(midiPanel);
        UIUtils.setEnabled(retVal, false);
	return retVal;
    }

    private class RadioListener implements ActionListener
    {

	
	public void actionPerformed(ActionEvent ae)
	{
	    String command = ae.getActionCommand();
	    if(command.equals("Speech"))
		{
		    UIUtils.setEnabled(speechDetailsPanel, true);
		    UIUtils.setEnabled(midiDetailsPanel, false);
		    UIUtils.setEnabled(sampleDetailsPanel, false);
		}
	    else if(command.equals("MIDI"))
		{
		    UIUtils.setEnabled(speechDetailsPanel, false);
		    UIUtils.setEnabled(midiDetailsPanel, true);
		    UIUtils.setEnabled(sampleDetailsPanel, false);
		}
	    else if(command.equals("Sample"))
		{
		    UIUtils.setEnabled(speechDetailsPanel, false);
		    UIUtils.setEnabled(midiDetailsPanel, false);
		    UIUtils.setEnabled(sampleDetailsPanel, true);
		}
	}
    }

    private class CloseListener extends WindowAdapter
    {
	public void windowClosing(WindowEvent we)
	{
	    
	    updateSelection();
	}

    }

    public void showInterface()
    {
	jf = new JFrame();
	jf.setSize(1000,500);
	jf.setTitle(getName());
	Container gc = jf.getContentPane();
	JPanel cenPanel = UIUtils.addFillers(gc, 20);
	cenPanel.setLayout(new BorderLayout());
	if(mapModel==null)
	    {
		mapModel = new MappingTableModel();
		mapModel.addRow();
		mapModel.setValueAt("[Default]",0,0);
		mapModel.setValueAt("MIDI C Major",0,1);
	
	    }
	feedbackTable = new JTable(mapModel);
	feedbackTable.getSelectionModel().addListSelectionListener(new TableSelectionListener());
	currentSelection = 0;
	JScrollPane feedbackScroll = new JScrollPane(feedbackTable);
	JPanel buttonPanel = new JPanel(new FlowLayout());

	JButton addButton = new JButton("Add mapping");
	JButton deleteButton = new JButton("Remove mapping");

	addButton.addActionListener(new ButtonListener());
	deleteButton.addActionListener(new ButtonListener());

	buttonPanel.add(addButton);
	buttonPanel.add(deleteButton);
	JPanel mainPanel = new JPanel(new BorderLayout());
	mainPanel.add(buttonPanel, BorderLayout.SOUTH);
	mainPanel.add(feedbackScroll, BorderLayout.CENTER);
	cenPanel.add(mainPanel, BorderLayout.CENTER);

	sidePanel = makeFeedbackPanel();
	cenPanel.add(sidePanel, BorderLayout.EAST);
	UIUtils.setColors(gc, Color.black, Color.white);
	jf.addWindowListener(new CloseListener());
	jf.show();
    }

    public SummativeFeedback()
    {
	super();
	mappingData = new Hashtable();
	mappingData.put("[Default]", 
			new MIDISummativeMapping("p(organ) (c e g C E)"));
	currentRow = 1;
	setTerminating(true);
    }
    
}





