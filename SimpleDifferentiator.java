import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;     
import javax.swing.*;
import javax.swing.event.*;

public class SimpleDifferentiator extends SProcessorModel
{
 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "SimpleDifferentiator";
 }
 public String getDescription()
 {
  return "Approximates the derivative of an incoming signal";
 }
 public String getDate()
 {
  return "Janurary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


 LinkedList valBuffer;
 int samples = 4;

 public void showInterface()
 {
  JFrame jf = new JFrame(getName());
  Container gc = jf.getContentPane();
  jf.setSize(250, 80);
  gc.setLayout(new BorderLayout());
  final JLabel lab = new JLabel("Samples "+samples);
  gc.add("North", lab);
  JSlider slider = new JSlider(0,20,samples);
  slider.addChangeListener(new ChangeListener(){
     public void stateChanged(ChangeEvent ce)
     {
      samples = ((JSlider)(ce.getSource())).getValue();
      lab.setText("Samples "+samples);
      valBuffer = new LinkedList();}});
  gc.add("Center", slider);
  jf.show();
 }

 public void connectionChange()
 {

 }

 public SimpleDifferentiator()
 {
  super();
  valBuffer = new LinkedList();
 }

 public void processSignal()
 {
   GestureSignal sig = lastSig;
   if(sig.vals.length==sigWidth)
   {
     if(valBuffer.size()>0)
     {
      double [] lastVals = (double [])(valBuffer.getLast());

      active = true;
      if(lastVals.length==sig.vals.length)
	  {
	      double [] nVals = new double[sigWidth];
	      for(int i=0;i<sigWidth;i++)
		  nVals[i] = lastVals[i]-sig.vals[i];
      	      setCurSig(new GestureSignal(nVals,getID()));
	  }
     }
     valBuffer.addFirst(sig.vals);
     if(valBuffer.size()>samples)
      valBuffer.removeLast();
   }
 }

}
