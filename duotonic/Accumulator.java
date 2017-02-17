package duotonic;
import java.util.*;
import java.io.*;

  class AccEntry implements Comparable, Serializable
  {
   double freq;
   Object val;

   public int compareTo(Object o)
   {
    if(o instanceof AccEntry)
    {
       return (int)(freq-(((AccEntry)o).freq));
    }
    else return 0;
   }

   public AccEntry(double freq, Object val)
   {
    this.freq = freq;
    this.val = val;
   }

  }


public class Accumulator
{


  private Hashtable accumTable;

  public Vector getSortedList()
  {
   Enumeration keys = accumTable.keys();
   Vector retVal = new Vector();
   while(keys.hasMoreElements())
   {
     Object key = keys.nextElement();
     int freq = ((Integer)(accumTable.get(key))).intValue();
     retVal.add(new AccEntry(freq, key));
   }
   Collections.sort(retVal);
   return retVal;
  }

  public Accumulator()
  {
   accumTable = new Hashtable();
  }


  public int get(Object o)
  {
    Integer i = (Integer)(accumTable.get(o));
    if(i==null)
      return 0;
    else
      return i.intValue();
  }

  public void add(Object o)
  {
   Integer i;
   if(accumTable.containsKey(o))
       i = (Integer)(accumTable.get(o));
   else
       i = new Integer(0);
   i = new Integer(i.intValue()+1);
   accumTable.put(o,i);
  }


}
