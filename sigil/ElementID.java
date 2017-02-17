package sigil;
/**
 * Used to provide unique ID's for SignalDevices
 *
 * @author John Williamson
 */


public class ElementID
{
    private static int ID=0;
    
    /**
     * Set the current ID (used after deserialization)
     */
    public static void setID(int id)
    {
	ID = id;
    }

    /**
     * Return a new unique id for this session
     */
    public static int getID()
    {
	return ID++;
    }

}
