import java.util.*;
import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * SignalDevice: HMM based gesture recognizer
 *
 * @author John Williamson
 */
public class MarkovModel extends SProcessorModel
{
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "MarkovModel";
    }
    public String getDescription()
    {
	return "Uses a hidden markov model of gestures to classify"+
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

    private static final int maxGestureLength = 80;
    private int states;
    private int resynthPos;
    private int segmentTime = 640;
    private double epsilon;
    private int logEpsilon = 22;
    private transient JList gestureList;
       private Vector gestureNames;
    private transient Vector resynthStates, resynthSymbols;
    private boolean resynthesizing = false;
    private boolean ergodic = true;
    private int leftRightDelta = 2;
    private boolean recognized = false;
    private int spaceDivisions;
    private int alphabet;
    private boolean bounce = false;
    private Hashtable hmms;
    private Hashtable prototypeGestures;
    private long lastReset;
    private boolean lastQuiescent = false;
    private long lastQuiescentTime;
    private int [] currentGesture;
    private int gestureIndex;
    private int nGestures = 0;
    private boolean viterbi = true;
    private boolean hardSeg = true;
    private int logThreshold = -30;
    private boolean inGesture = false;

        public int getSignalWidth()
        {
	    return 0;
	}

	public void segmentGesture()
	{
	    
	    Enumeration enum = hmms.keys();
	    Vector orderedNames = new Vector();
	    ProbabilisticGesture pGest = new ProbabilisticGesture();
	    pGest.setBounce(bounce);
	    double sum = 0.0;
	    double [] probTable = new double[nGestures];
	    
	    int index = 0;
	    while(enum.hasMoreElements())
		{
		    String name = (String)(enum.nextElement());
		    orderedNames.add(name);
		    HMM toTest = (HMM)hmms.get(name);
		    if(viterbi)
			probTable[index] = toTest.getProbability();
		    sum += probTable[index];
		    index++;
		}
	   
	    if(sum>Math.exp(logThreshold))
		{
		    
		    for(int i=0;i<nGestures;i++)
			{
			    double probability = probTable[i]/sum;
			    ParameterizedGesture parmGest = new ParameterizedGesture((String)(orderedNames.get(i)),
										     "HMM",
										     this,
										     lastReset,
										     System.currentTimeMillis(),
										     probability,
										     false);
			    pGest.addGesture(parmGest);
			}
		    distributeReverseHetObject(pGest);
		    if(!bounce)
			distributeHetObject(pGest);
		    
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setBoolean("Correct");
		    distributeHetObject(stateInfo);
		}
	    reset();
	    if(hardSeg)
		inGesture = false;
	   
	}

    
    public void connectionChange()
    {
    }


  private class SliderListener implements ChangeListener
    {
	private String command;

	public SliderListener(String command)
	{
	    this.command = command;
	}

	public void stateChanged(ChangeEvent ce)
	{
	    JSlider slider = (JSlider)(ce.getSource());
	    if(command.equals("States"))
		{
		    if(!slider.getValueIsAdjusting())
			{
			    int confirm = JOptionPane.showConfirmDialog(null, "This will invalidate current recognition. Continue?", "Invalidate recognition", JOptionPane.YES_NO_OPTION);
			    if(confirm == JOptionPane.YES_OPTION)
				{
				    states = slider.getValue();		    
				    createModel(alphabet, spaceDivisions, states);
				}
			}
		}
	    else if(command.equals("Epsilon"))
		{
		    if(!slider.getValueIsAdjusting())
			{
			    int confirm = JOptionPane.showConfirmDialog(null, "This will invalidate current recognition. Continue?", "Invalidate recognition", JOptionPane.YES_NO_OPTION);
			    if(confirm == JOptionPane.YES_OPTION)
				{
				    logEpsilon = slider.getValue();
				    if(logEpsilon==0)
					logEpsilon = 1;
				    createModel(alphabet, spaceDivisions, states);
				}
			}
		}
	    else if(command.equals("Segment"))
		segmentTime = slider.getValue();
	    else if(command.equals("Threshold"))
		{
		    logThreshold = slider.getValue()-40;
		    if(logThreshold==0)
			logThreshold=1;
		}
	}
  }


    private void evolveHMM()
    {
	String gestureName = (String)(gestureList.getSelectedValue());
	if(gestureName!=null)
	    {
		HMM gestureModel = (HMM)(hmms.get(gestureName));
		Vector prototypes = (Vector)(prototypeGestures.get(gestureName));
		HMMGA hmmGa = new HMMGA(gestureModel, gestureName, prototypes);
	    }
    }
    
    private void resynthesizeGesture()
    {
	String gestureName = (String)(gestureList.getSelectedValue());
	if(gestureName!=null)
	    {
		HMM gestureModel = (HMM)(hmms.get(gestureName));
		if(gestureModel!=null)
		    {
			resynthesizing = true;
			int resynthPos = 0;
			resynthStates = new Vector();
			resynthSymbols = new Vector();
			gestureModel.simulateSequence(gestureModel.getAvgLength(), 
						      resynthStates, resynthSymbols);
		    }

	    }

    }


    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(460,460);
	jf.setTitle(getName());
	gestureList = new JList(gestureNames);
	JScrollPane scroller = new JScrollPane(gestureList);
	JPanel southPanel = new JPanel(new BorderLayout());
	southPanel.add(scroller, BorderLayout.CENTER);

	JButton resynthesize = new JButton("Resynthesize");
	JButton evolve = new JButton("Evolve HMM...");
	JPanel buttonPanel = new JPanel(new FlowLayout());
	buttonPanel.add(resynthesize);
	buttonPanel.add(evolve);
	southPanel.add(buttonPanel, BorderLayout.SOUTH);
	resynthesize.addActionListener(new ActionListener()
	    {public void actionPerformed(ActionEvent ae){
		    resynthesizeGesture();}});
	evolve.addActionListener(new ActionListener()
	    {public void actionPerformed(ActionEvent ae){
		    evolveHMM();}});

	Box sliderPanel = Box.createVerticalBox();
	
	JCheckBox segBox = new JCheckBox("Use hard segmentation", hardSeg);
        segBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JCheckBox src = (JCheckBox)(ae.getSource());
		    hardSeg = src.isSelected();
		}
	    });
	sliderPanel.add(segBox);

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
	
	JCheckBox ergodicBox = new JCheckBox("Ergodic model", ergodic);
	ergodicBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JCheckBox src = (JCheckBox)(ae.getSource());
		    int confirm = JOptionPane.showConfirmDialog(null, 
								"This will invalidate current recognition. Continue?", 
								"Invalidate recognition", 
								JOptionPane.YES_NO_OPTION);
		    if(confirm == JOptionPane.YES_OPTION)
			{
			    ergodic = src.isSelected();
			    createModel(alphabet, spaceDivisions, states);
			}
		}
	    });
	sliderPanel.add(ergodicBox);
	
	JCheckBox viterbiBox = new JCheckBox("Use viterbi", viterbi);
	viterbiBox.addActionListener(new ActionListener()
	    {
		public void actionPerformed(ActionEvent ae)
		{
		    JCheckBox src = (JCheckBox)(ae.getSource());
		    viterbi = src.isSelected();
		}
	    });
	sliderPanel.add(viterbiBox);
	
	JSlider stateSlider = new JSlider(2, 100, states);
	JPanel statePanel = UIUtils.nameSliderLabelled(stateSlider, 
						       "States per model", true);
	sliderPanel.add(statePanel);
	stateSlider.addChangeListener(new SliderListener("States"));

	JSlider epsilonSlider = new JSlider(1, 100, logEpsilon);
	JPanel epsilonPanel = UIUtils.nameSliderLabelled(epsilonSlider, 
						       "Log epsilon", true, 0, 0.2);
	sliderPanel.add(epsilonPanel);
	epsilonSlider.addChangeListener(new SliderListener("Epsilon"));

	JSlider thresholdSlider = new JSlider(1, 40, logThreshold+40);
	JPanel thresholdPanel = UIUtils.nameSliderLabelled(thresholdSlider, 
						       "Log threshold", true, -40, 1.0);
	sliderPanel.add(thresholdPanel);
	thresholdSlider.addChangeListener(new SliderListener("Threshold"));

	JSlider segmentSlider = new JSlider(1, 2000, segmentTime);
	JPanel segmentPanel = UIUtils.nameSliderLabelled(segmentSlider, 
							   "Segmentation period (ms)", true);
	sliderPanel.add(segmentPanel);
	segmentSlider.addChangeListener(new SliderListener("Segment"));
	southPanel.add(sliderPanel, BorderLayout.NORTH);
	Container gc = jf.getContentPane();
	gc.add(southPanel, BorderLayout.SOUTH);
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.show();


    }

  
	public void processHetObject(Object o)
	{
	if(o instanceof StateInformation)
	    {
		StateInformation sInfo = (StateInformation)o;
		if(sInfo.getBoolean("Transition"))
		    transition(sInfo);
		else if(!hardSeg && sInfo.getBoolean("Reset"))
		    reset();
		else if(sInfo.getBoolean("Start"))
		    reset();
		else if(sInfo.getBoolean("End"))
		    segmentGesture();
		else if(!hardSeg && sInfo.getBoolean("Quiescent"))
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
	if(resynthesizing)
	    {
		int symbol = ((Integer)(resynthSymbols.get(resynthPos))).intValue();
		StateInformation sInfo = new StateInformation();
		sInfo.setTransition(symbol, spaceDivisions, alphabet);
	
		distributeHetObject(sInfo);
		resynthPos++;
		if(resynthPos>=resynthSymbols.size())
		    {
			resynthesizing = false;
			resynthPos = 0;
			
			int centreState = (spaceDivisions/2)*spaceDivisions*spaceDivisions+
			    (spaceDivisions/2)*spaceDivisions+(spaceDivisions/2);
			//Make sure last state is the central one
			StateInformation cenInfo = new StateInformation();
			sInfo.setTransition(centreState, spaceDivisions, alphabet);
			distributeHetObject(cenInfo);
		    }
	    }
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

    private void restart()
    {
	reset();
	gestureIndex = 1;

    }

	private void reset()
	{

	    Enumeration enum = hmms.keys();
	    while(enum.hasMoreElements())
		{
		    HMM toReset = (HMM)(hmms.get(enum.nextElement()));
		    toReset.reset();
		}
	    inGesture = true;
	    gestureIndex = 0;
	    lastQuiescent = false;
	    lastReset = System.currentTimeMillis();
	}
	
    private void createModel(int alphabet, int divisions, int states)
    {
	epsilon = 1.0/Math.exp((((double)(logEpsilon))/5.0));
	nGestures = 0;
	hmms = new Hashtable();
	gestureNames = new Vector();
	this.alphabet = alphabet;
	this.states = states;
	spaceDivisions = divisions;
	gestureIndex = 0;
	prototypeGestures = new Hashtable();
	currentGesture = new int[maxGestureLength];
	updateInterface();
    }
	
	public MarkovModel()
	{
	    states = 14;
	    createModel(27,3,states);
	}
	
    
    public void addGesture(GesturePrototype proto)
    {
	String name = proto.getName();
	Vector prototype;
	if(!hmms.containsKey(name))
	    {
		hmms.put(name, new HMM(states, alphabet, !ergodic, 
				       leftRightDelta, epsilon));
		gestureNames.addElement(name);
		nGestures++;
	    }
	if(prototypeGestures.containsKey(name))
	    prototype = (Vector)(prototypeGestures.get(name));
	else
	    prototype = new Vector();
		    
	prototypeGestures.put(name, prototype);
	
	HMM thisGestureModel = (HMM)(hmms.get(name));
	Iterator iter = proto.getData().iterator();	    
	int [] intVals = new int[proto.getData().size()];
	int count = 0;
	

	while(iter.hasNext())
		intVals[count++]  = ((Integer)(iter.next())).intValue();
	prototype.add(intVals);
	thisGestureModel.reEstimateModel(intVals, intVals.length, 1e-8);
       	updateInterface();
    }

    private void updateInterface()
    {
	if(gestureList!=null)
	    gestureList.setListData(getUniqueNameVector());
	
    }

        public Vector getUniqueNameVector()
        {
	    return new Vector(hmms.keySet());
        }

	public void transition(StateInformation sInfo)
	{	    
	    

	    if(sInfo.getAlphabet()!=alphabet)
		    createModel(sInfo.getAlphabet(), 
				sInfo.getDivisions(), states);
	    if(!inGesture)
		return; 

	    int trans = sInfo.getTransition();

	    double normalizeFactor = 0.0;
	    Enumeration enum = hmms.keys();
	    Vector probs = new Vector();
	    
	    while(enum.hasMoreElements())
		{
		    String hmmName = (String)enum.nextElement();
		    HMM probTest = (HMM)(hmms.get(hmmName));
		    double pOfGest;
		    if(viterbi)
			pOfGest = probTest.probabilityStepViterbi(trans);
		    else
			pOfGest = probTest.probabilityStep(trans);
		    probs.add(new Double(pOfGest));
		    normalizeFactor+=pOfGest;
		}

	    Hashtable symbolProbs = new Hashtable();
	    enum = hmms.keys();
	    int index=0;
	    while(enum.hasMoreElements())
		{
		    String gestName = (String)enum.nextElement();
		    double gestProb = ((Double)(probs.get(index++))).doubleValue();
		    if(normalizeFactor>1e-100)
			symbolProbs.put(gestName, new Double(gestProb/normalizeFactor));
		    else
			symbolProbs.put(gestName, new Double(0.0));
		}
	    StateInformation stateInfo = new StateInformation();
	    stateInfo.setBoolean("Adjusted probabilities");
	    stateInfo.setObject("Probabilities",  symbolProbs);
	    distributeHetObject(stateInfo);

	    if(gestureIndex>=currentGesture.length)
		gestureIndex = 0;
	    currentGesture[gestureIndex++] = trans;
	    lastQuiescent = false;
	}
    }
