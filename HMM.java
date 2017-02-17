import java.util.Random;
import java.util.Vector;
import java.io.*;
import java.text.*;

public class HMM implements Serializable, Cloneable
{
    static final long serialVersionUID = 213L;
    private double [] [] transitions;
    private double [] [] symbolDistribution;
    private double [] stateDistribution;
    private int states, alphabet;
    private double epsilon = 0.01;
    private static final int maxIter = 1000;
    private transient Random rnd = new Random();
    
    private transient double [] [] alpha;
    private transient double [] [] beta;
    private transient double [] [] gamma;
    private transient double [] [] [] xi;
    private static NumberFormat numForm = new DecimalFormat("0.00");
    private boolean leftRight;
    private int leftRightDelta;
    private transient Thread estimationThread;

    private transient double [] previousProbs;
    private double oldSum;
    private int totalTrainingLength=0, reEstimations=0;
    private boolean needInit = true;

    private boolean currentlyReestimating = false;

    private transient double scale [];

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

	rnd = new Random();
    }

    
    public double [] [] copy2DArray(double [] [] toCopy)
    {
	double [] [] retVal = new double [toCopy.length] [];
	for(int i=0;i<toCopy.length;i++)
	    retVal[i] = copyArray(toCopy[i]);
	return retVal;
    }

    public double [] copyArray(double [] toCopy)
    {
	double [] retVal = new double[toCopy.length];
	System.arraycopy(toCopy, 0, retVal, 0, toCopy.length);
	return retVal;
    }

    public void copyFrom(HMM toCopyFrom)
    {
	waitForReestimation();
	HMM copy = toCopyFrom.copy();
	symbolDistribution = copy.symbolDistribution;
	transitions = copy.transitions;
	stateDistribution = copy.stateDistribution;
    }

    public HMM copy()
    {
	try{
	HMM clone = (HMM)(this.clone());

	clone.stateDistribution = copyArray(stateDistribution);
	clone.transitions = copy2DArray(transitions);
	clone.symbolDistribution = copy2DArray(symbolDistribution);
	return clone;
	} catch(CloneNotSupportedException cnse) {return null;} 
    }

    public int getAvgLength()
    {
	return (int)((double)totalTrainingLength/(double)reEstimations);
    }


    public double getEpsilon()
    {
	return epsilon;
    }

    public double getProbability()
    {
	return oldSum;
    }


    public double probabilityStep(int symbol)
    {
	if(currentlyReestimating)
	    return 0.0;
	if(needInit)
	    {
		previousProbs = new double [states];
		double totalSum=0.0;
		for(int i=0;i<states;i++)
		    {
			previousProbs[i] = stateDistribution[i]*
			    symbolDistribution[i][symbol];
			totalSum+=previousProbs[i];
		    }
		normalize(previousProbs);
		oldSum = Math.exp(totalSum);
		needInit = false;

		return oldSum;
	    }
	else
	    {
		double [] newProb = new double[states];
		double totalSum = 0.0;
		for(int j=0;j<states;j++)
		    {
			double sum = 0.0;
			
			for(int i=0;i<states;i++)
				sum += previousProbs[i]*transitions[i][j];

			sum *=  symbolDistribution[j][symbol];
			newProb[j] = sum;
			totalSum += sum;
		    }
		normalize(previousProbs);
		previousProbs = newProb;
		oldSum*=totalSum;
		return oldSum;
	    }
    }

    public double probabilityStepViterbi(int symbol)
    {
	if(currentlyReestimating)
	    return 0.0;
	if(needInit)
	    {
		previousProbs = new double [states];
		double totalSum=0.0;
		for(int i=0;i<states;i++)
			previousProbs[i] = stateDistribution[i]*
			    symbolDistribution[i][symbol];
	
		oldSum = totalSum;
		needInit = false;
		return oldSum;
	    }
	else
	    {
		double [] newProb = new double[states];
		double totalMax = 0.0;
		
		for(int j=0;j<states;j++)
		    {
			double max = 0.0, val;			
			for(int i=0;i<states;i++)
			    {
				val = previousProbs[i]*transitions[i][j];
				if(val>max)
				    max = val;
			    }
			newProb[j] = max*symbolDistribution[j][symbol];
			if(newProb[j]>totalMax)
			    totalMax = newProb[j];
		    }
	
		previousProbs = newProb;
		oldSum = totalMax;
		return oldSum;
	    }
    }


    public void reset()
    {
	if(currentlyReestimating)
	    return;
	needInit = true;
    }

    public int [] viterbi(int [] observed, int len)
    {
	double [] dummy = new double[1];
	return viterbi(observed, len, dummy);
    }

    public int [] viterbi (int [] observed, int len, double [] outProb)
    {
	if(currentlyReestimating)
	    return null;
	int [] [] psi = new int[states][]; 
	double [] [] delta = new double [states][];
	for(int i=0;i<states;i++)
	    {
		delta[i] = new double[len];
		psi[i] = new int[len];
		delta[i][0] = stateDistribution[i]*
		    symbolDistribution[i][observed[0]];
		psi[i][0] = 0;
	    }
	for(int t=1;t<len;t++)
	    {
		for(int j=0;j<states;j++)
		    {
			double maxDelta = 0;
			int maxIndex = 0;
			for(int i=0;i<states;i++)
			    {
				double newDelta = delta[i][t-1]*
				    transitions[i][j];
				if(newDelta>maxDelta)
				    {
					maxDelta = newDelta;
					maxIndex = i;
				    }
			    }
			delta[j][t] = maxDelta*symbolDistribution[j][observed[t]];
			psi[j][t] = maxIndex;
		    }

	    }
	double maxP = 0;
	int maxQ = 0;
	
	for(int i=0;i<states;i++)
	    {
		if(delta[i][len-1]>maxP)
		    {
			maxP = delta[i][len-1];
			maxQ = psi[i][len-1];
		    }
	    }
	
	int [] backtracked = new int[len];
	backtracked[len-1] = maxQ;
	for(int t=len-2;t>=0;t--)
		backtracked[t] = psi[backtracked[t+1]][t+1];
	
	outProb[0] = maxP;
	return backtracked;
    }

    
    public double calculateProbabilityViterbi(int [] observeSeq, int length)
    {
	if(currentlyReestimating)
	    return 0.0;

	if(length<1)
	    return 0.0;
	
	double [] probVal = new double[1];
	viterbi(observeSeq, length, probVal);
	double prob = probVal[0];
	//prob = prob*Math.pow(Math.sqrt(1.0/epsilon),length);
	return prob;
    }

    public double calculateProbability(int [] observeSeq, int length)
    {
	if(currentlyReestimating)
	    return 0.0;

	if(length<1)
	    return 0.0;
	alpha = new double [states] [];
	scale = new double [length];
	for(int i=0;i<states;i++)
	    alpha[i] = new double[length];
	double prob = Math.exp(forward(observeSeq, length-1));
	//prob = prob*Math.pow(Math.sqrt(1.0/epsilon),length);
	return prob;
    }


    

    /**
     * Forward recursion, with scale
     */
    private double forward(int [] observeSeq, int time)
    {

	scale[0] = 1.0;
	for(int i=0;i<states;i++)
	    {
		alpha[i][0] = stateDistribution[i]*
		    symbolDistribution[i][observeSeq[0]];
		scale[0] += alpha[i][0];
	    }
	for(int i=0;i<states;i++)
	    alpha[i][0] /= scale[0];
	
	for(int t=1;t<=time;t++)
	    {
		scale[t] = 0.0;
		    for(int j=0;j<states;j++)
			{
			    double sum = 0.0;
			    for(int i=0;i<states;i++)
			      sum += alpha[i][t-1]*transitions[i][j];
			    sum *=  symbolDistribution[j][observeSeq[t]];
			    alpha[j][t] = sum;
			    scale[t] += alpha[j][t];
			}
		for(int j=0;j<states;j++)
		    alpha[j][t] /= scale[t];
	    }
	
	double p = 0.0;
	for(int t=0;t<=time;t++)
	    p+=Math.log(scale[t]);
	return p;
    }

    /**
     * Backward, with scale
     */
    private void backward(int [] observeSeq, int time)
    {

	for(int i=0;i<states;i++)
	    beta[i][time] = 1.0/scale[time];

	for(int t=time-1;t>=0;t--)
	    {
		for(int i=0;i<states;i++)
			{
			    double sum = 0.0;
			    for(int j=0;j<states;j++)
			      sum += beta[j][t+1]*transitions[i][j]*
				  symbolDistribution[j][observeSeq[t+1]];
			    beta[i][t] = sum/scale[t];
			}
	    }
    }
    

    private void initialiseAlphaBetaXiGamma(int length)
    {

	alpha = new double [states] [];
	beta = new double [states] [];
	xi = new double [states] [] [];
	gamma = new double[states] [];
	scale = new double [length];

	for(int i=0;i<states;i++)
	    {
		alpha[i] = new double[length];
		beta[i] = new double[length];
		gamma[i] = new double[length];
		xi[i] = new double[states][];
		for(int j=0;j<states;j++)
		    xi[i][j] = new double[length];
	    }
    }

    

    private void xi(int [] observeSeq, int time)
    {
	for(int t=0;t<=time-1;t++)
	    {
		double normalize = 0.0;
		for(int i=0;i<states;i++)
		    {
			for(int j=0;j<states;j++)
			    {
				xi[i][j][t] = alpha[i][t]*beta[j][t+1];
				xi[i][j][t] *= transitions[i][j];
				xi[i][j][t] *= symbolDistribution[j][observeSeq[t+1]];

				normalize+=xi[i][j][t];
			    }
		    }
		for(int i=0;i<states;i++)
		    for(int j=0;j<states;j++)
			xi[i][j][t]/=normalize;
	    }
    }

    private double gamma(int [] observeSeq, int time)
    {
	double sum = 0.0;
	for(int t=0;t<=time;t++)
	    {
		double normalize = 0.0;
		for(int j=0;j<states;j++)
		    {
			gamma[j][t] = alpha[j][t]*beta[j][t];
			normalize+=gamma[j][t];
		    }
		for(int j=0;j<states;j++)
		    gamma[j][t] /= normalize;
		    
	    }
	return sum;
    }

    private double newPi(int [] observeSeq, int i)
    {
	return epsilon+(1-epsilon)*gamma[i][0];
    }
    
    private void newTransitions(int [] observeSeq, int time)
			
    {
	double num, denom, denomSym, numSym;
	for(int i=0;i<states;i++)
	    {
		denom = 0;
		for(int t=0;t<=time-1;t++)
		    denom+=gamma[i][t];
		for(int j=0;j<states;j++)
		    {
			num = 0.0;
			for(int t=0;t<time-1;t++)
			    num += xi[i][j][t];
			transitions[i][j] =epsilon+(1-epsilon)*(num/denom);
		    }
	        denomSym = denom+gamma[i][time];
		for(int k=0;k<alphabet;k++)
		    {
			numSym = 0.0;
			for(int t=0;t<=time;t++)
			    numSym += (observeSeq[t]==k) ? gamma[i][t] : 0;
			symbolDistribution[i][k] = (epsilon) + (1-epsilon)*(numSym/denomSym);
			
		    }
	    }
    }

    public void reEstimateModel(final int [] observeSeq, final int length, final double minDelta)
    {
	if(length<3 || currentlyReestimating)
	    return;
	currentlyReestimating = true;
	
	estimationThread = new Thread(){
	    public void run()
	    {
		initialiseAlphaBetaXiGamma(length);
		int time = length-1;;
		double oldProb = forward(observeSeq, time);
		double delta;
		backward(observeSeq, time);
		gamma(observeSeq, time);
		xi(observeSeq, time);
		int iter = 0;
		do
		    {
			for(int j=0;j<states;j++)
			    stateDistribution[j] = newPi(observeSeq, j);
			
			newTransitions(observeSeq, time);
			double newProb = forward(observeSeq, time);
			backward(observeSeq, time);
			gamma(observeSeq, time);
			xi(observeSeq, time);
			delta = oldProb - newProb;
			oldProb = newProb;
			if(leftRight)
			    enforceLeftRight(leftRightDelta);
			iter++;
			
		    } while(Math.abs(delta)>minDelta && iter<maxIter);
		reEstimations++;
		totalTrainingLength += length;
		currentlyReestimating = false;
	    }
	};
	estimationThread.start();
    }

    public void dumpTables()
    {
	if(currentlyReestimating)
	    return;
	System.out.println("State distribution");
	for(int i=0;i<states;i++)
	    System.out.print(numForm.format(stateDistribution[i])+" ");

	System.out.println("\n");
	System.out.println("Transition probabilities");
	for(int i=0;i<states;i++)
	    {
		for(int j=0;j<states;j++)
		    System.out.print(numForm.format(transitions[i][j])+" ");
		System.out.println("");
	    }
	
	System.out.println("");
	System.out.println("Symbol distribution");
	for(int i=0;i<states;i++)
	    {
		for(int k=0;k<alphabet;k++)
		    System.out.print(numForm.format(symbolDistribution[i][k])+" ");
		System.out.println("");
	    }
    }

    private double[][] initialiseTable(int width, int height)
    {
	double [] [] table = new double [width] [];
	for(int i=0;i<table.length;i++)
	    table[i] = new double[height];
       	return table;
    }

    private void normalize(double [] toNorm)
    {
	double sum = 0.0;
	for(int i=0;i<toNorm.length;i++)
	    sum+=toNorm[i];
	if(sum!=0.0)
	    for(int i=0;i<toNorm.length;i++)
		toNorm[i]/=sum;
    }
    
    public void randomizeTables(double randFactor)
    {
	for(int i=0;i<states;i++)
	    {
		stateDistribution[i] = (1.0-randFactor)*
		    stateDistribution[i]+randFactor*rnd.nextDouble();
		normalize(stateDistribution);
		for(int j=0;j<states;j++)
		    transitions[i][j] = (1.0-randFactor)*
			transitions[i][j]+randFactor*rnd.nextDouble();
		normalize(transitions[i]);
		for(int a=0;a<alphabet;a++)
		    symbolDistribution[i][a] = (1.0-randFactor)*
			symbolDistribution[i][a]+
			randFactor*rnd.nextDouble();
		normalize(symbolDistribution[i]);
	    }
    }


    public HMM(int states, int alphabet)
    {
	this(states, alphabet, false, 0, 0.01);
    }

    private void enforceLeftRight(int delta)
    {
	for(int i=0;i<states;i++)
	    {
		stateDistribution[i] = (i==0) ? 1.0 : 0.0;
		for(int j=0;j<states;j++)
		    {
			if(j<i)
			    transitions[i][j] = epsilon;
			if(j>i+delta)
		           transitions[i][j] = epsilon;
		    }
	    }

    }

    public void enforce()
    {
	if(leftRight)
	    enforceLeftRight(leftRightDelta);
    }

    private void initialize()
    {
    	stateDistribution = new double[states];
	symbolDistribution = initialiseTable(states, alphabet);
	transitions = initialiseTable(states, states);
	randomizeTables(1.0);
       	if(leftRight)
	    enforceLeftRight(leftRightDelta);
    }

    public HMM(int states, int alphabet, 
	       boolean leftRight, int leftRightDelta, double epsilon)
    {
	this.states = states;
	this.alphabet = alphabet;
	this.leftRight = leftRight;
	this.leftRightDelta = leftRightDelta;
	this.epsilon = epsilon;
	initialize();
    }

    public void printViterbi(int [] observeSeq, int length)
    {
	if(currentlyReestimating)
	    return;
	int [] result = viterbi(observeSeq, length);
	System.out.println("Viterbi");
	for(int i=0;i<result.length;i++)
	    System.out.print(result[i]+" ");
	System.out.println("");
    }
    
    private int probabilisticTransition(double [] stateDist)
    {
	double randVal = Math.random();
	double currentProb = 0.0;
	for(int i=0;i<stateDist.length;i++)
	    {
		currentProb += stateDist[i];
		if(randVal<currentProb)
		    return i;
	    }
	return 0;
    }

    public void simulateSequence(int length, Vector states, Vector symbols)
    {
	if(currentlyReestimating)
	    return;
	int state = probabilisticTransition(stateDistribution);
	int symbol;
	states.add(new Integer(state));
        for(int i=0;i<length;i++)
	    {
		symbol = probabilisticTransition(symbolDistribution[state]);
		state = probabilisticTransition(transitions[state]);
		states.add(new Integer(state));
		symbols.add(new Integer(symbol));
	    }
    }

    public boolean isReady()
    {
	return !currentlyReestimating;
    }

    public void waitForReestimation()
    {
	if(estimationThread!=null && currentlyReestimating)
	    try{estimationThread.join();}catch(InterruptedException ie){}
    }

    public static void main(String args[])
    {

	HMM test = new HMM(8,3,true,2,0.01);
	test.dumpTables();
	int [] testSeq = {0,1,1,2,1,1,2,1,1,2,0,0,0,1,1,1,2,2,2,1,1,1,0,0,0};
	test.printViterbi(testSeq, testSeq.length);
	System.out.println("P of seq ="+test.calculateProbability(testSeq, testSeq.length));
	System.out.println("Pv of seq ="+test.calculateProbabilityViterbi(testSeq, testSeq.length));
	test.reEstimateModel(testSeq, testSeq.length,1e-8);
	test.waitForReestimation();
	test.dumpTables();
	test.printViterbi(testSeq, testSeq.length);  
        System.out.println("Pf of seq ="+test.calculateProbability(testSeq, testSeq.length));
	System.out.println("Pv of seq ="+test.calculateProbabilityViterbi(testSeq, testSeq.length));
	Vector states = new Vector();
	Vector symbols = new Vector();
	test.simulateSequence(10, states, symbols);
	System.out.println("States "+states);
	System.out.println("Symbols "+symbols);
	Vector prototypes = new Vector();
	prototypes.add(testSeq);
	int [] testSeq2 = {0, 1,1,2,1,1,2,1,1,2,0,0,1,0,1,1,1,0};
	prototypes.add(testSeq2);
	HMMGA hmmGa = new HMMGA(test, "Test", prototypes);
	while(true)
	    {
		try{Thread.sleep(2000);}catch(InterruptedException ie) {}
		
	    }
	
    }

}
