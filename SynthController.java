import sigil.*;
import java.util.Enumeration;
import java.io.*;
/**
 * Interface for objects that control synthesizers (by whatever means).
 * Parameters for the synthesizers can be set and retrieved
 */

class SynthParameter
{
    int min, max;
    int value;
}

public interface SynthController extends java.io.Serializable
{

    /** Disable this synth */
    public void disableSynth();

    /** Enable this synth */
    public void enableSynth();

    /** Set a parameter back to the default */
    public void restoreDefault(String parmName);

    /** Set all parameters back to the default */
    public void restoreDefaults();

    /** Return the state of this synth */
    public boolean getEnabledState();

    /** Return a (synth-specific) instrument number */
    public int getPatch();

    /** Set the (synth-specific) instrument number */
    public void setPatch(int patchNo);

    /** Get the name of this synth controller */
    public String getSynthName();

    /** Return the names of valid parameters for this synth */
    public Enumeration getParameterNames();
    


    /** Set a parameter with a value normalized to 0.0-1.0. 
        Paramaters are linearly scaled to fit the range */
    public void setNormalizedParameter(String name, double par);

    
    /** Get a parameter with a value normalized to 0.0-1.0. 
        Paramaters are linearly scaled to fit the range */
    public double getNormalizedParameter(String name);




}
