package sigil;
/**
 * Abstract class for genetically evolvable processor devices
 * only supports single input devices
 * All methods for genetic interfaces are empty by default
 *
 * @author John Williamson
 */

public abstract class GAProcessorModel extends SProcessorModel implements GAElement
{

    public abstract void copyFrom(GAElement gaElt);
   
    public void mutate(double mutFactor)
    {
	
    }

    public  void breed(GAElement toBreed)
    {

    }

    public abstract GAElement copy();

    public  void preview()
    {

    }
}
