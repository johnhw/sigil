import java.util.*;
import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;

/**
 * SignalDevice:String matching based recognizer
 *
 * @author John Williamson
 */
public class BestMatchModel extends SProcessorModel
{
    
    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "BestMatchModel";
    }
    public String getDescription()
    {
	return "Uses a string matching model of gestures to classify"+
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
    
    private int segmentTime = 600;
    private transient JList gestureList;
    private long lastReset;
    private boolean bounce;
    private boolean hardSeg = true;
    private boolean lastQuiescent = false;
    private long lastQuiescentTime;
    private double probability;
    private int maxDistance = 8;
    private double sharpness = 7.0;
    private double gaussianWidth;
    private Vector currentGesture;
    private Hashtable uniqueNameMappings;
    private double twoWidthSquared;
    private Hashtable matchers;
    private boolean inGesture = false;
    private int divs, alphabet, dims;
    private void updateWidth()
    {
	gaussianWidth = maxDistance/sharpness;
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
	   

	    boolean foundMatch = false;
	    int bestMatchDistance = 1000;
	    String bestMatch="UNKNOWN";
	    Enumeration enum = uniqueNameMappings.keys();
	    int gestLen = currentGesture.size();
	    ProbabilisticGesture pGest = new ProbabilisticGesture();
	    System.out.println(currentGesture);
	    while(enum.hasMoreElements())
		{
		    String name = (String)(enum.nextElement());
		    Vector prototypes = (Vector)(uniqueNameMappings.get(name));
		    for(int i=0;i<prototypes.size();i++)
			{
			    Vector proto = (Vector)(prototypes.get(i));
			    int len = proto.size();
			    if(Math.abs(len-gestLen)<=maxDistance)
				{
				    int difference = StringMatching.computeDifference(proto, currentGesture);
				    if(difference<=maxDistance)
					{
					    foundMatch = true;
					    double probability = getProbability(difference);
					    boolean wasComplete = (difference==0);
					    long curTime = System.currentTimeMillis();
					    ParameterizedGesture parmGest = new ParameterizedGesture(name,
												     "BestMatch",
												     this,
												     lastReset,
												     curTime,
												     probability,
												     wasComplete);
					    pGest.setBounce(bounce);
					    pGest.addGesture(parmGest);
					}
				}
			}
		}


	    if(foundMatch)
		{
		   
		    
		    distributeReverseHetObject(pGest);
		    if(!bounce)
			distributeHetObject(pGest);
		    StateInformation stateInfo = new StateInformation();
		    stateInfo.setBoolean("Correct");
		    distributeHetObject(stateInfo);
		}
	   
	     lastQuiescent = false;
	     lastQuiescentTime = System.currentTimeMillis();
	     
	     reset();
	     inGesture = false;
	     
	     clearMatchings();
	}

    private void clearMatchings()
    {
	Enumeration enum = matchers.keys();
	while(enum.hasMoreElements())
		{
		    String mappingName = (String)enum.nextElement();
		    Vector probTesters = (Vector)(matchers.get(mappingName));
		    for(int i=0;i<probTesters.size();i++)
			{
			    StringMatching probTest = (StringMatching)(probTesters.get(i));
			    probTest.reset();
			}
		}
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
	    if(command.equals("Max distance"))
		maxDistance = slider.getValue();
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

	JSlider widthSlider = new JSlider(1, 12, maxDistance);
	JPanel widthPanel = UIUtils.nameSliderLabelled(widthSlider, 
						       "String distance limit", true);
	sliderPanel.add(widthPanel);
	widthSlider.addChangeListener(new SliderListener("Max distance"));

	JSlider sharpnessSlider = new JSlider(1, 200, (int)(sharpness*10.0));
	JPanel sharpnessPanel = UIUtils.nameSliderLabelled(sharpnessSlider, 
							   "Distance curve sharpness", true, 0, 0.1);
	sliderPanel.add(sharpnessPanel);
	sharpnessSlider.addChangeListener(new SliderListener("Sharpness"));

	JSlider segmentSlider = new JSlider(1, 2000, segmentTime);
	JPanel segmentPanel = UIUtils.nameSliderLabelled(segmentSlider, 
							   "Segmentation period (ms)", true);
	sliderPanel.add(segmentPanel);
	segmentSlider.addChangeListener(new SliderListener("Segment"));


	southPanel.add(sliderPanel, BorderLayout.NORTH);
	Container gc = jf.getContentPane();
	gc.add(southPanel, BorderLayout.CENTER);
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
		    {
			if(divs!=sInfo.getDivisions() || alphabet!=sInfo.getAlphabet())
			    {
				divs = sInfo.getDivisions();
				alphabet = sInfo.getAlphabet();
				dims = (alphabet == divs*divs) ? 2 : 3;
			    }
			transition(sInfo.getTransition());
			if(!inGesture && !hardSeg)
			    {
				reset();
				inGesture = true;
			    }
		    }
		else if(sInfo.getBoolean("Start"))
		    reset();
		else if(sInfo.getBoolean("End"))
		    {			
			segmentGesture();
			inGesture = false;
		    }
		else if(!hardSeg && sInfo.getBoolean("Quiescent"))
		    setQuiescent();
		else if(sInfo.getBoolean("New Gesture"))
		    addGesture((GesturePrototype)(sInfo.getObject("Gesture")));
	    }
    }

    private void setQuiescent()
    {
	lastQuiescent = true;

	//inGesture = false;
    }


    public void transition(int trans)
    {
	if(!inGesture)
	    return;

	currentGesture.add(new Integer(trans));
	lastQuiescent = false;
	lastQuiescentTime = System.currentTimeMillis();
	double normalizeFactor = 0.0;
	Enumeration enum = matchers.keys();
	Vector probs = new Vector();
	    
	    while(enum.hasMoreElements())
		{
		    String mappingName = (String)enum.nextElement();
		    Vector probTesters = (Vector)(matchers.get(mappingName));
		    double pOfGest = 0.0;
		    for(int i=0;i<probTesters.size();i++)
			{
			    StringMatching probTest = (StringMatching)(probTesters.get(i));
			    pOfGest += getProbability(probTest.computeDifferenceStep(new Integer(trans)));
			}
		    probs.add(new Double(pOfGest));
		    normalizeFactor+=pOfGest;
		}

	    Hashtable symbolProbs = new Hashtable();
	    enum = matchers.keys();
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
    }

    public void tock()
    {
	checkQuiescent();
    }

    private void checkQuiescent()
    {
	if(lastQuiescent && !hardSeg)
	    {
		long duration = System.currentTimeMillis()-lastQuiescentTime;
		if(duration>segmentTime)
		    {
			segmentGesture();
			reset();
			inGesture = false;
		    }
	    }
    }

	private void reset()
	{

	    currentGesture = new Vector();
	    //lastQuiescent = false;
	    lastReset = System.currentTimeMillis();
	    inGesture = true;
	    clearMatchings();
	    Hashtable symbolProbs = new Hashtable();
	    Enumeration enum = matchers.keys();
	    while(enum.hasMoreElements())
		{
		    String gestName = (String)enum.nextElement();
		    symbolProbs.put(gestName, new Double(0.0));
		}
	    StateInformation stateInfo = new StateInformation();
	    stateInfo.setBoolean("Adjusted probabilities");
	    stateInfo.setObject("Probabilities",  symbolProbs);
	    distributeHetObject(stateInfo);
	}

    	private void restart()
	{
	    inGesture = true;
	    Object firstElt = null;
	    if(currentGesture!=null && currentGesture.size()>0)
		firstElt = currentGesture.get(0);
	    
	    currentGesture = new Vector();
	    if(firstElt!=null)
		currentGesture.add(firstElt);
	    lastQuiescent = false;
	    lastQuiescentTime = System.currentTimeMillis();
	    lastReset = System.currentTimeMillis();
	}
	
		
	public BestMatchModel()
	{
	    updateWidth();
	    uniqueNameMappings = new Hashtable();
	    matchers = new Hashtable();
	}
	
	
        public void addGesture(GesturePrototype proto)
        {
	    String name = proto.getName();


	    Vector currentMappings;
	    if(!uniqueNameMappings.containsKey(name))
		{
		    currentMappings = new Vector();
		    
		}
	    else
		currentMappings = (Vector)(uniqueNameMappings.get(name));

	    Vector data = proto.getData();
	    for(int i=0;i<currentMappings.size();i++)
		if(StringMatching.computeDifference(data, 
						    (Vector)(currentMappings.get(i)))<=maxDistance>>1)
		    return;

		currentMappings.add(data);
	    uniqueNameMappings.put(name, currentMappings);


	    Vector currentMatchers;
	    if(!matchers.containsKey(name))
		currentMatchers = new Vector();
	    else
		currentMatchers = (Vector)(matchers.get(name));
	    currentMatchers.add(new StringMatching(proto.getData()));
	    matchers.put(name, currentMatchers);
	}

	
        public Vector getUniqueNameVector()
        {
	    return new Vector(uniqueNameMappings.keySet());

        }

    }
