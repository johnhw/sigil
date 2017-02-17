package duotonic;
import java.util.*;

public class MarkovVM extends EarwigVM
{

    private Hashtable transitions;
    private Accumulator transitionAccumulation;
    private int length;

    public MarkovVM(int channel)
    {
	super(channel);
    }

    public void addInstruction(int type, int value)
    {
	instructions.add(new EarwigVM.VMInstruction(type, value));
    }

}
