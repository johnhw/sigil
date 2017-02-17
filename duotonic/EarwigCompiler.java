package duotonic;
import java.util.*;

public class EarwigCompiler
{

    private Vector vms;
    private Hashtable bindings;
    private LinkedList stack;
    private LinkedList evalStack;
   
    private int patch = 0, note = 0, duration = 8, velocity = 127, tempo = 100,  
	octave = 4,  pan = 64;

    public void setBinding(String name, int value)
    {
	if(name!=null)
	    bindings.put(name, new Integer(value));
    }

    public EarwigVM compileExpression(EarwigParser.EarwigElement elt, int channel)
    {
	EarwigVM retVal = new EarwigVM(channel);
	if(elt.type!=EarwigParser.TYPE_SEQUENCE)
	    System.err.println("Parser did not provide a sequence");
	else
	    evaluateSequence(elt.vec, retVal);
	
	return retVal;
    }

    public void evaluateSequence(Vector elts, EarwigVM vm)
    {
	for(int i=0;i<elts.size();i++)
	    {
		EarwigParser.EarwigElement thisElt = (EarwigParser.EarwigElement) 
		    (elts.get(i));

		switch(thisElt.type)
		    {
		    case EarwigParser.TYPE_ASSIGNMENT:
		        setBinding(thisElt.str,evaluateDExpr(thisElt.elt,getBinding(thisElt.str)));
			break;
		    case EarwigParser.TYPE_PAN:
			pan = evaluateDExpr(thisElt.elt, pan);
			vm.addInstruction(EarwigVM.VM_PAN, pan);
			break;
		    case EarwigParser.TYPE_REST:
			vm.addInstruction(EarwigVM.VM_REST, 0);
			break;
		    case EarwigParser.TYPE_NOTE:
			note = thisElt.val;
			vm.addInstruction(EarwigVM.VM_NOTE, note+octave*12);
			break;
		    case EarwigParser.TYPE_NOTE_CHANGE:
			note = evaluateDExpr(thisElt.elt, note);
			vm.addInstruction(EarwigVM.VM_NOTE, note+octave*12);
			break;
		    case EarwigParser.TYPE_OCTAVE:
			octave = evaluateDExpr(thisElt.elt, octave);
			break;
		    case EarwigParser.TYPE_PATCH:
			patch = evaluateDExpr(thisElt.elt, patch);
			vm.addInstruction(EarwigVM.VM_PATCH, patch);
			break;
		    case EarwigParser.TYPE_DURATION:
			duration = evaluateDExpr(thisElt.elt, duration);
			if(duration>0)
			    {

				int halfDur = (1<<(duration>>1));
				int realDur = (duration%2==0) ? halfDur : (halfDur+(halfDur>>1));
				vm.addInstruction(EarwigVM.VM_DURATION, realDur);
			    }
			break;
		    case EarwigParser.TYPE_TEMPO:
			tempo = evaluateDExpr(thisElt.elt, tempo);
			vm.addInstruction(EarwigVM.VM_TEMPO, tempo);
			break;
		    case EarwigParser.TYPE_PUSH:
			push(evaluateExpression(thisElt.elt));
			break;
		    case EarwigParser.TYPE_VELOCITY:
			velocity = evaluateDExpr(thisElt.elt, velocity);
			vm.addInstruction(EarwigVM.VM_VELOCITY, velocity);
			break;
		    case EarwigParser.TYPE_CHORD:
			vm.addInstruction(EarwigVM.VM_START_CHORD, 0);
			evaluateSequence(thisElt.vec, vm);
			vm.addInstruction(EarwigVM.VM_END_CHORD, 0);
			break;
		    }
	    }
    }
    


    private int evaluateDExpr(EarwigParser.EarwigElement elt, 
			      int oldValue)
    {

	if(elt.type==EarwigParser.TYPE_DELTA_UP)
	    oldValue += evaluateExpression(elt.elt);
	else if (elt.type==EarwigParser.TYPE_DELTA_DOWN)
	    oldValue -= evaluateExpression(elt.elt);
	else
	    oldValue = evaluateExpression(elt);
	return oldValue;
    }


    private int getBinding(String name)
    {
	if(name==null)
	    return 0;

	if(name.equals("oct"))
	    return octave;
	if(name.equals("patch"))
	    return patch;
	if(name.equals("tempo"))
	    return tempo;
	if(name.equals("vel"))
	    return velocity;
	if(name.equals("note"))
	    return note;
	if(name.equals("dur"))
	    return duration;	
	Integer bound = (Integer)(bindings.get(name));
	if(bound!=null)
	    return bound.intValue();
	else
	    return 0;
    }
     
    private void push(int val)
    {
	stack.addLast(new Integer(val));
    }

    
    private void pushEval(int val)
    {
	evalStack.addLast(new Integer(val));
    }

    private int pop()
    {

	    if(stack.size()>0)
		{
		    Integer popped = (Integer)stack.removeLast();
		    return popped.intValue();
		}
	    else
		return 0;
    }

    private int popEval()
    {
	    if(evalStack.size()>0)
		{
		    Integer popped = (Integer)evalStack.removeLast();
		    return popped.intValue();
		}
	    else
		return 0;
    }


    



    private int evaluateExpression(EarwigParser.EarwigElement elt)
    {
	evalStack = new LinkedList();
	evalExpr(elt);
	if(evalStack.size()>0)
	    return popEval();
	else
	    return 0;
    }

    private void evalExpr(EarwigParser.EarwigElement elt)
    {
	if(elt.type==EarwigParser.TYPE_MULTI_EXPR)
	    {
		Vector exprs = elt.vec;
		for(int i=0;i<exprs.size();i++)
		    {
			EarwigParser.EarwigElement nextElt = (EarwigParser.EarwigElement) (exprs.get(i));
			evalExpr(nextElt);
		    }
	    }

	if(elt.type==EarwigParser.TYPE_POP)
	    pushEval(pop());

	if(elt.type==EarwigParser.TYPE_NUMBER)
	    pushEval(elt.val);

	else if (elt.type==EarwigParser.TYPE_VARIABLE)
	    pushEval(getBinding(elt.str.substring(1)));
	else if(elt.type==EarwigParser.TYPE_ADD)
	    {
		int a = popEval();
		int b = popEval();
		pushEval(a+b);
	    }
	else if(elt.type==EarwigParser.TYPE_MULT)
	    {
		int a = popEval();
		int b = popEval();
		pushEval(a*b);
	    }

	else if(elt.type==EarwigParser.TYPE_SUB)
	    {
		int a = popEval();
		int b = popEval();
		pushEval(b-a);
	    }
	
	else if(elt.type==EarwigParser.TYPE_DIV)
	    {
		int a = popEval();
		int b = popEval();
		if(a!=0)
		    pushEval(b/a);
		else
		    pushEval(0);
	    }

    }

    	public void compile(EarwigParser toPlay)
	    {
		Vector sequences = toPlay.getSequences();
		for(int i=0;i<sequences.size();i++)
		    {
			EarwigParser.EarwigElement elt = (EarwigParser.EarwigElement)(sequences.get(i));
			vms.add(compileExpression(elt,i));
		    }
	    }



    public EarwigCompiler()
    {
	vms = new Vector();
	bindings = new Hashtable();
	stack = new LinkedList();
    }

    public void playStep()
    {
	if(vms.size()>0)
	    {
		EarwigVM vm = (EarwigVM)(vms.get(0));
		vm.playStepNoBlock();
	    }

    }

    public void reset()
    {
	if(vms.size()>0)
	    {
		EarwigVM vm = (EarwigVM)(vms.get(0));
		vm.reset();
	    }
    }

    public void play()
    {
	for(int i=0;i<vms.size();i++)
	    {
		EarwigVM vm = (EarwigVM)(vms.get(i));
		vm.start();
	    }

    }

    


}
