import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import sigil.UIUtils;

public class HMMGA
{
    private HMM original;
    private HMM [] mutations;
    private Vector prototypes;
    private int nCopies = 8;
    private int nKeep = 1;
    private double maxMutation=1.0, minMutation=0.2, mutationDelta = 0.1, curMutation;
    private boolean exponentialDecay = false;
    private String name;
    private double bestProb;
    private Random rnd;
    private boolean running = true;
    private GADisplayPanel displayPanel; 
    private boolean useViterbi = true;
    private double initialProb;
    private boolean  passedHigh = false;

    private class EvolveThread extends Thread
    {
	private double calculateProb(HMM hmm)
	{
	    double totalProb = 0.0;
	    for(int j=0;j<prototypes.size();j++)
		{
		    int [] intVals = (int [])(prototypes.get(j));
		    if(useViterbi)
			totalProb += hmm.calculateProbabilityViterbi(intVals, intVals.length);
		    else
			totalProb += hmm.calculateProbability(intVals, intVals.length);
		}
	    return totalProb;
	}

	public void run()
	{
	    newPopulation();
	    while(running)
		{
		    try{sleep(15);}catch(InterruptedException ie){}
		    nextGeneration();
		}
	}

	public EvolveThread()
	{
	    super();
	    setPriority(Thread.MIN_PRIORITY);
	}
	
	private void newPopulation()
	{
	    if(!original.isReady())
		original.waitForReestimation();
	    mutations = new HMM[nCopies];
	    for(int i=0;i<nCopies;i++)
		mutations[i] = original.copy();
	    curMutation = maxMutation;
	    bestProb = calculateProb(original);
	    initialProb = bestProb;
	}
	
	private class ProbValue implements Comparable
	{
	    double prob;
	    int index;

	    //Descending sort
	    public int compareTo(Object o)
	    {
		if(o instanceof ProbValue)
		    if(prob<((ProbValue)o).prob)
			return 1;
		    else
			if(prob==((ProbValue)o).prob)
			    return 0;
			else
			    return -1;
		else
		    return 0;
	    }
	    
	}

	private void updateOriginal(int bestIndex)
	{
	    if(!(original.isReady()))
		original.waitForReestimation();
	    original.copyFrom(mutations[bestIndex]);
	}

	private void reEstimateModel(HMM toEstimate)
	{
	    for(int j=0;j<prototypes.size();j++)
		{
		    int [] intVals = (int [])(prototypes.get(j));
		    toEstimate.reEstimateModel(intVals, intVals.length, 1e-6);
		    toEstimate.waitForReestimation();
		}
	}

	
	private void nextGeneration()
	{
	    ProbValue [] probs = new ProbValue[nCopies];
	    
	    for(int i=0;i<nCopies;i++)
		{
		    mutations[i].enforce();
		    probs[i] = new ProbValue();
		    reEstimateModel(mutations[i]);
		    probs[i].prob = calculateProb(mutations[i]);

		    if(Double.isNaN(probs[i].prob))
			probs[i].prob = 0.0;
		    probs[i].index = i;
		}
	    Arrays.sort(probs);

	    double newProb = probs[0].prob;
	    if(displayPanel!=null)
		displayPanel.addPoint(bestProb, curMutation);
	    if(newProb>bestProb)
		{
		    bestProb = newProb;
		    updateOriginal(probs[0].index);
		    passedHigh = false;
		}
	    HMM [] newMutations = new HMM[mutations.length];
	    System.arraycopy(mutations, 0, newMutations, 0, mutations.length);

	    for(int i=0;i<nKeep;i++)
		    newMutations[i] = mutations[probs[i].index].copy();

	    for(int i=nKeep;i<nCopies;i++)
		{
		    int cIndex = probs[i].index;
		    newMutations[cIndex] = mutations[cIndex].copy();
		    newMutations[cIndex].randomizeTables(curMutation);
		}
	    
	    mutations = newMutations;
	    if(!exponentialDecay)
		curMutation = curMutation-mutationDelta;
	    else
		curMutation = curMutation/(1.0+mutationDelta);
	    
	    if(curMutation<=minMutation)
		{
		    if(passedHigh)
			running = false;
		    passedHigh = true;
		    curMutation = maxMutation;
		}
	}
    }


    class GADisplayPanel extends JPanel
    {
	private LinkedList oldProbPoints, oldMutationPoints;
	private int maxLength = 500;
	private int height = 500;
	
	public GADisplayPanel()
	{
	    oldProbPoints = new LinkedList();
	    oldMutationPoints = new LinkedList();
	}
	
	public void addPoint(double probability, double mutation)	    
	{
	    double probPoint = (Math.log(probability)*-10)+100;
	    double mutationPoint = mutation*height;
	    oldProbPoints.addLast(new Double(probPoint));
	    oldMutationPoints.addLast(new Double(mutationPoint));
	    if(oldProbPoints.size()>maxLength)
		oldProbPoints.removeFirst();
	    if(oldMutationPoints.size()>maxLength)
		oldMutationPoints.removeFirst();
	    repaint();
	}

	public void paint(Graphics g)
	{
	    int inc = 4;
	    ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					     RenderingHints.VALUE_ANTIALIAS_ON);
	    Dimension dSize = getSize();
	    maxLength = dSize.width/2;
	    height = dSize.height;
	    g.setColor(Color.black);
	    g.fillRect(0,0,dSize.width,dSize.height);
	    g.setColor(Color.white);
	    g.setFont(new Font("SansSerif", Font.PLAIN, 10));
	    g.drawString("Best probability "+bestProb, 10, 20);
	    g.drawString("Improvement ratio "+(bestProb/initialProb), 10, 33);
	    	    
	    Iterator probIter = oldProbPoints.iterator();
	    Iterator rateIter = oldMutationPoints.iterator();
	    Double oldProb=null, oldRate=null;
	    int x = 0;
	    int y = dSize.height;
	    while(probIter.hasNext() && rateIter.hasNext())
		{
		    Double prob = (Double)(probIter.next());
		    Double rate = (Double)(rateIter.next());
		    if(oldProb!=null && oldRate!=null)
			{
			    g.setColor(Color.yellow);
			    g.drawLine(x-inc, y-oldProb.intValue(), x, y-prob.intValue());
			    g.setColor(Color.blue);
			    g.drawLine(x-inc, y-oldRate.intValue(), x, y-rate.intValue());
			}
		    x+=inc;
		    oldProb = prob;
		    oldRate = rate;
		}
	}

    }


    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.addWindowListener(new WindowAdapter()
	    {
		public void windowClosing(WindowEvent we)
		{
		    running = false;
		}
	    });
	jf.setSize(400,400);
	jf.setTitle("GA for HMM: "+name);
	Container gc = jf.getContentPane();
	displayPanel = new GADisplayPanel();
	JPanel fillPanel = new JPanel(new BorderLayout());
	JPanel cenPanel = UIUtils.addFillers(fillPanel,20);
	cenPanel.add(displayPanel);
	gc.add(fillPanel, BorderLayout.CENTER);
	UIUtils.setColors(gc, Color.white, Color.black);
	jf.show();
    }
    
    public HMMGA(HMM toEvolve, String name, Vector prototypes)
    {
	curMutation = maxMutation;
	original = toEvolve;
	rnd = new Random();
	this.name = name;
	this.prototypes = prototypes;	
	new EvolveThread().start();
	showInterface();
    }


}
