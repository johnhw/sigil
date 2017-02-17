import java.util.*;
import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
/**
 * SignalDevice: Finite state machine based recognizer
 *
 * @author John Williamson
 */

public class FSMModel extends SProcessorModel
{
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "FSMModel";
    }
    public String getDescription()
    {
	return "Uses a finite state model of gestures to classify"+
	    "data from a StateRecognizer";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }
    
    private int segmentTime = 100;
    private transient JList gestureList;
    private int fsmState = 0;
    private boolean recognized = false;
    private int recogCode;
    private int alphabetWidth;
    private int nCodes;
    private boolean bounce = false;
    private transient StateVisualisation stateVis;
    private int recogDistance;
    private Vector stateTable;
    private int curMaxState;
    private long lastReset;
    private boolean lastQuiescent = false;
    private long lastQuiescentTime;
    private double probability;
    private double twoWidthSquared;
    private static final double rootTwoPi = Math.sqrt(2*Math.PI);
    private int currentDistance = 1000;
    private int cutoffDistance = 5;
    private double sharpness = 2.0;
    private double gaussianWidth = cutoffDistance/2.0;
    private Vector uniqueNameVector;

    private void updateWidth()
    {
	gaussianWidth = cutoffDistance/sharpness;
	twoWidthSquared = 2.0*gaussianWidth*gaussianWidth;
    }

    private double getProbability(double distance)
    {
	double gaussDist = Math.exp(-distance/twoWidthSquared);
	return gaussDist;
    }

        public int getSignalWidth()
        {
         return 0;
        }

	public void segmentGesture()
	{

	    if(currentDistance<cutoffDistance && recogCode!=0)
		{
		    double probability = getProbability(currentDistance);
		    boolean wasComplete = (currentDistance==0);

		    ProbabilisticGesture pGest = new ProbabilisticGesture();
		    ParameterizedGesture parmGest = new ParameterizedGesture(getGesture(),
									     "FSM",
									     this,
									     lastReset,
									     System.currentTimeMillis(),
									     probability,
									     wasComplete);
		    pGest.setBounce(bounce);
		    pGest.addGesture(parmGest);
		    distributeReverseHetObject(pGest);
		    if(!bounce)
			distributeHetObject(pGest);
		    
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setBoolean("Correct");
		    distributeHetObject(stateInfo);

		}
	    currentDistance = 1000;
	    recognized=false;
	    reset();
	}

    
    public void connectionChange()
    {
    }


  private class WidthListener implements ChangeListener
    {
	private String command;

	public WidthListener(String command)
	{
	    this.command = command;
	}

	public void stateChanged(ChangeEvent ce)
	{
	    JSlider slider = (JSlider)(ce.getSource());
	    if(command.equals("Width"))
		cutoffDistance = slider.getValue();
	    else if(command.equals("Sharpness"))
		sharpness = (double)(slider.getValue())/10.0;
	    else if(command.equals("Segment"))
		segmentTime = slider.getValue();
	    updateWidth();
	}
  }
    
    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(460,540);
	jf.setTitle(getName());
	gestureList = new JList();
	JScrollPane scroller = new JScrollPane(gestureList);
	JPanel southPanel = new JPanel(new BorderLayout());
	southPanel.add(scroller, BorderLayout.CENTER);

	Box sliderPanel = Box.createVerticalBox();

	JCheckBox bounceBox = new JCheckBox("Bounce gestures off input device", bounce);
	bounceBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JCheckBox src = (JCheckBox)(ae.getSource());
		    bounce = src.isSelected();
		}
	    });

	sliderPanel.add(bounceBox);
	
	JSlider widthSlider = new JSlider(1, 12, cutoffDistance);
	JPanel widthPanel = UIUtils.nameSliderLabelled(widthSlider, 
						       "Termination curve width", true);
	sliderPanel.add(widthPanel);
	widthSlider.addChangeListener(new WidthListener("Width"));

	JSlider sharpnessSlider = new JSlider(1, 100, (int)(sharpness*10.0));
	JPanel sharpnessPanel = UIUtils.nameSliderLabelled(sharpnessSlider, 
							   "Termination curve sharpness", true, 0, 0.1);
	sliderPanel.add(sharpnessPanel);
	sharpnessSlider.addChangeListener(new WidthListener("Sharpness"));

	JSlider segmentSlider = new JSlider(1, 400, segmentTime);
	JPanel segmentPanel = UIUtils.nameSliderLabelled(segmentSlider, 
							   "Segmentation period (ms)", true);
	sliderPanel.add(segmentPanel);
	segmentSlider.addChangeListener(new WidthListener("Segment"));


	southPanel.add(sliderPanel, BorderLayout.NORTH);

	stateVis = new StateVisualisation();
	stateVis.setDivs(alphabetWidth);
	JPanel visPanel = new JPanel(new BorderLayout());
	JPanel cenVisPanel = UIUtils.addFillers(visPanel, 20);
	cenVisPanel.add(stateVis, BorderLayout.CENTER);
	Container gc = jf.getContentPane();
	gc.add(visPanel, BorderLayout.CENTER);
	gc.add(southPanel, BorderLayout.SOUTH);
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.show();


    }

    /**
     * Consume StateInformation events
     */
	public void processHetObject(Object o)
	{
	if(o instanceof StateInformation)
	    {
		StateInformation sInfo = (StateInformation)o;
		if(sInfo.getBoolean("Transition"))
		    fsmTransition(sInfo);
		else if(sInfo.getBoolean("Reset") || sInfo.getBoolean("Start"))
		    reset();
		else if(sInfo.getBoolean("End"))
		    segmentGesture();
		else if(sInfo.getBoolean("Quiescent"))
		    setQuiescent();
		else if(sInfo.getBoolean("New Gesture"))
		    addGesture((GesturePrototype)(sInfo.getObject("Gesture")));
	    }
	
    }

    private void setQuiescent()
    {
	lastQuiescent = true;
	lastQuiescentTime = System.currentTimeMillis();
    }


    public void tock()
    {
	checkQuiescent();
    }

    private void checkQuiescent()
    {
		if(lastQuiescent)
		    {
			long duration = System.currentTimeMillis()-lastQuiescentTime;
			if(duration>segmentTime)
			    segmentGesture();
		}
    }

	private void reset()
	{
	    lastQuiescent = false;
            if(fsmState!=0 && !recognized)
		{
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setBoolean("Drop");
		    distributeHetObject(stateInfo);
		}
	    currentDistance++;
	    recognized = false;
	    
	    fsmState = 0;
	    if(stateVis!=null)
		stateVis.setCodeTable((int [] [])(stateTable.get(fsmState)));
	    if(currentDistance>cutoffDistance)
		lastReset = System.currentTimeMillis();
	}
	
	
	private int addState(int code, int state, int recog, int recogDist)
	{
            int [] stateEntry = ((int [] [])(stateTable.get(state)))[code];
            if(stateEntry[0]==0)
		{
                    int newState = curMaxState;
                    int [] [] newStateEntry = new int [nCodes][];
                    for(int j=0;j<nCodes;j++)
			{
			    newStateEntry[j] = new int[4];
			    newStateEntry[j][2] = -1;
			}
                    stateTable.add(newStateEntry);

                    stateEntry[0] = newState;
                    stateEntry[1] = recog;
		    stateEntry[2] = recogDist;
		    stateEntry[3] = recog;
		    curMaxState++;
		    return newState;
		}
            else 
		{
                    if(recog!=0)
			{
			    stateEntry[1] = recog;
			    stateEntry[2] = recogDist;
			    if(stateEntry[3]!=recog)
				stateEntry[3] = 0;
			}
                    return stateEntry[0];
		}
	}

    private void createTable(int codes, int width)
    {
	nCodes = codes;
	
	curMaxState = 1;
	stateTable = new Vector();
	
	//Add initial entry to table
	int [] [] stateEntry = new int [nCodes][];
	for(int j=0;j<nCodes;j++)
	    {
		stateEntry[j] = new int[4];
		stateEntry[j][2] = -1;
	    }
	stateTable.add(stateEntry);
	
	uniqueNameVector = new Vector();
	alphabetWidth = width;
	if(stateVis!=null)
	    stateVis.setDivs(alphabetWidth);
    }
	
	public FSMModel()
	{
	    updateWidth();
	    createTable(3,3);
	}
	
	
        public void addGesture(GesturePrototype proto)
        {
	    String name = proto.getName();
	    if(!uniqueNameVector.contains(name))
		uniqueNameVector.add(name);
           int newState = 0;
           Iterator iter = proto.getData().iterator();
	   int distance = proto.getData().size();
	   int recogCode = uniqueNameVector.indexOf(name);

           while(iter.hasNext())
	       {
		   distance--;
		   int transition = ((Integer)(iter.next())).intValue();
		   newState = addState(transition, newState, recogCode+1, distance);
	       }

	   updateInterface();
        }

	public boolean getRecognized()
	{
	    return recognized;
	}
	
	public String getGesture()
	{
	    String retVal = "UNKNOWN";
	    if(recogCode>0 && recogCode<=uniqueNameVector.size())
                retVal = (String) (uniqueNameVector.get(recogCode-1));
	    return retVal;
	}

	public double [] getProbTable(int [] [] codeTable)
	{
	    double [] retVal = new double[codeTable.length];
	    for(int i=0;i<codeTable.length;i++)
		retVal[i] = codeTable[i][0];
	    return retVal;
	}

    private void updateInterface()
    {
	if(gestureList!=null)
	    gestureList.setListData(getUniqueNameVector());
	
    }



        public Vector getUniqueNameVector()
        {
	    return uniqueNameVector;

        }

	public void fsmTransition(StateInformation sInfo)
	{	    
	    if(nCodes!=sInfo.getAlphabet())
		{
		    nCodes = sInfo.getAlphabet();
		    alphabetWidth = sInfo.getDivisions();
		    createTable(nCodes, alphabetWidth);
		}
	    int trans = sInfo.getTransition();
	    recognized = false;
	    lastQuiescent = false;
            int [] [] codeTable = (int [] [])(stateTable.get(fsmState));
            int [] entry = codeTable[trans];

            int newCode= entry[1];
	    int newDistance = entry[2];
            int oldState = fsmState;
            fsmState = entry[0];

	    currentDistance++;
	    if(newDistance!=-1 && newDistance<currentDistance)
		currentDistance = newDistance;

	    if(stateVis!=null)
		{
		    stateVis.setTransition(trans);
		    stateVis.setCodeTable(codeTable);
		}

	    if(fsmState==0 && currentDistance>cutoffDistance)
		lastReset = System.currentTimeMillis();
	    if(oldState==0 && fsmState!=0)
		{
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setTransition(trans, alphabetWidth, 
					    nCodes);
		    stateInfo.setBoolean("Start");
		    distributeHetObject(stateInfo);
		}

            if(oldState!=0 && fsmState==0)
            {
		StateInformation stateInfo = new StateInformation();
		stateInfo.setTransition(trans, alphabetWidth, nCodes);
		stateInfo.setBoolean("Drop");
		stateInfo.setBoolean("Negative");
		distributeHetObject(stateInfo);
            }
            else if (fsmState!=0)
            {
		
		StateInformation stateInfo = new StateInformation();
		stateInfo.setTransition(trans, alphabetWidth, nCodes);
		stateInfo.setBoolean("Continue");
		stateInfo.setPathInfo(entry[3]);
		distributeHetObject(stateInfo);
            }
	    if(newDistance==0)
		{
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setTransition(trans, alphabetWidth, nCodes);
		    stateInfo.setBoolean("Terminal");
		    distributeHetObject(stateInfo);
		}
	    if(newDistance<cutoffDistance && newDistance!=-1)
		{
		    recognized = true;
		    recogCode = newCode;
		    recogDistance = newDistance;
		}
	}
    }
