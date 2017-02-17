package sigil;
import java.util.*;

/**
 * Utilities for handling signals
 *
 * @author John Williamson
 */
public class SignalUtils
{

  /**
   * Merge multiple signals from an input buffer into a single
   * signal
   */
  public static GestureSignal mergeBuffer(Hashtable buffer)
  {

   Enumeration keys = buffer.keys();
   int width=0;

   //Calculate total width
   while(keys.hasMoreElements())
       {
	   Object key = keys.nextElement();
	   GestureSignal gSig = (GestureSignal)(buffer.get(key));
	   width+=gSig.vals.length;
       }


   keys = buffer.keys();

   //Make a new array to hold the output signal
   double [] sig = new double[width];
   int pos = 0;

   //Take each input
   while(keys.hasMoreElements())
   {
       Object key = keys.nextElement();
       GestureSignal gSig = (GestureSignal)(buffer.get(key));

       //Copy into the new array
       System.arraycopy(gSig.vals, 0, sig, pos, gSig.vals.length);
       pos+=gSig.vals.length;
   }

   //Return the new signal
   GestureSignal retVal = new GestureSignal(sig, -1);
   return retVal;
  }


}
