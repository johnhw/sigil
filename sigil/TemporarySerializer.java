package sigil;
import java.io.*;

/**
 * Serializes an object to a memory buffer
 * which can then be deserialized as necessary
 *
 * @author John Williamson
 */
public class TemporarySerializer
{

  //The buffer holding the current object
  private byte [] buffer;

  /**
   * Serialize the current object and store it
   */
  public TemporarySerializer(Serializable obj) throws IOException
  {
    //Write the data to a byte array
    ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
    ObjectOutputStream objOut = new ObjectOutputStream(byteOut);
    objOut.writeObject(obj);
    objOut.flush();
    objOut.close();

    //Get a copy of the output array
    buffer = byteOut.toByteArray();
  }

    /**
     * Return the byte buffer containing the serialized object
     */
    public byte [] getBuffer()
    {
	return buffer;
    }

  /**
   * Deserialize, and return the original object
   */
  public Object deserialize() throws IOException, ClassNotFoundException
  {
   ObjectInputStream objIn = new ObjectInputStream(new ByteArrayInputStream(buffer));
   Object retVal = objIn.readObject();
   return retVal;
  }


}
