import sigil.*;
import java.util.*;
/**
 * Class to take various synthesizer parameters and map them to appropriate output
 * synthesizers
 */

public class SynthMapper
{

    private Hashtable parameterMapper;
    private Vector synthNames;
    private Hashtable synths;
    
   
    public SynthMapper()
    {
        synths = new Hashtable();
	synthNames = new Vector();
        parameterMapper = new Hashtable();
    }


    public void disableSynth(String name)
    {	
        SynthController theController = (SynthController)(synths.get(name));
	if(theController!=null)
	    {
		theController.disableSynth();
	    }
    }


    public void enableSynth(String name)
    {


        SynthController theController = (SynthController)(synths.get(name));
	if(theController!=null)
	    {
		theController.enableSynth();
		
	    }


    }



    /** Add a synth controller to this mapper */
    public void addSynthController(SynthController toAdd)
    {
        String synthName = toAdd.getSynthName();
	synthNames.add(synthName);

	Enumeration parms = toAdd.getParameterNames();
        while(parms.hasMoreElements())
		parameterMapper.put(parms.nextElement(), synthName);
		
	synths.put(synthName, toAdd);
    }

    /** Remove a synth controller from this mapper */
    public void removeSynthController(SynthController toRemove)
    {
       
        synths.remove(toRemove.getSynthName());
        synthNames.remove(toRemove.getSynthName());
	Enumeration parms = toRemove.getParameterNames();
        while(parms.hasMoreElements())
		parameterMapper.remove(parms.nextElement());

    }

    public void setParameter(String name, double normValue)
    {
	String synthName = (String)(parameterMapper.get(name));
	if(synthName!=null)
	    {

		SynthController theController = (SynthController)(synths.get(synthName));
		if(theController!=null)
		    {
			theController.setNormalizedParameter(name, normValue);
			
		    }
	    }
    }

    public void setPatch(String name, int patchNo)
    {
        SynthController theController = (SynthController)(synths.get(name));
		if(theController!=null)
		    {
                        theController.setPatch(patchNo);
			
		    }
    }


    public double getParameter(String name)
    {
	String synthName = (String)(parameterMapper.get(name));
	if(synthName!=null)
	    {

		SynthController theController = (SynthController)(synths.get(synthName));
		if(theController!=null)
		    {
			return theController.getNormalizedParameter(name);
			
		    }
             }
        return 0.0;
    }


    public int getPatch(String name)
    {
        SynthController theController = (SynthController)(synths.get(name));

		if(theController!=null)
		    {
                        return theController.getPatch();             
		    }
        return -1;                        
    }


    public Enumeration getParameterList()
    {
	return parameterMapper.keys();
    }

    public Enumeration getSynthNames()
    {
	return synthNames.elements();

    }

}

