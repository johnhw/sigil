package sigil;
import java.util.Vector;
import java.io.Serializable;

/**
 * Header information for a system setup
 *
 * @author John Williamson
 */
public class SignalHeader implements Serializable
{
    /**
     * Annotation for this setup
     */
    public String annotation;

    /**
     * The date at which this system was last saved, or
     * the initialization date if it has not yet been saved
     */
    public String date;

    /**
     * The version number of this setup; increases by one
     * every time the setup is saved
     */
    public int verNo;

}
