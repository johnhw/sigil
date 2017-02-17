import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class Sum extends MProcessorModel
{

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Sum";
 }
 public String getDescription()
 {
  return "Adds multiple input streams together";
 }
 public String getDate()
 {
  return "Feburary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


 public void connectionChange()
 {

 }

 public void processSignalBuffer()
 {
   Collection vals = signalBuffer.values();
   if(vals.size()<2)
        return;
   Iterator iter = vals.iterator();
   GestureSignal [] sigs = new GestureSignal[vals.size()];
   int size=1000;
   for(int j=0;j<vals.size();j++)
   {
    sigs[j] = (GestureSignal) (iter.next());
     if(sigs[j].vals.length<size)
         size = sigs[j].vals.length;        
   }

   double [] newVals = new double[size];

   for(int i=0;i<size;i++)
     for(int j=0;j<sigs.length;j++)
        newVals[i] += sigs[j].vals[i];
   GestureSignal gSig = new GestureSignal(newVals, getID());
   setCurSig(gSig);
 }

 public Sum()
 {
  super();
 }

 public void showInterface()
 {

 }


}
