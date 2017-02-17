import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class SimpleHFilter extends SProcessorModel
{
 private double smooth = 0.65;
 private FilterCascade [] filters;

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "SimpleFilter";
 }
 public String getDescription()
 {
  return "Applies simple filtering to a signal";
 }
 public String getDate()
 {
  return "Janurary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }



 public void showInterface()
 {
  JFrame jf = new JFrame(getName());
  Container gc = jf.getContentPane();
  jf.setSize(250, 80);
  gc.setLayout(new BorderLayout());
  final JLabel lab = new JLabel("Smooth "+smooth);
  gc.add("North", lab);
  JSlider slider = new JSlider(0,100,(int)(smooth*100));
  slider.addChangeListener(new ChangeListener(){
     public void stateChanged(ChangeEvent ce)
     {
	 smooth = ((JSlider)(ce.getSource())).getValue()/100.0;
	 if(filters!=null)
	     for(int i=0;i<filters.length;i++)
		 filters[i].setCoefficient(smooth);
	        
      lab.setText("Smooth "+smooth);
     }
    });
  gc.add("Center", slider);
  jf.show();
 }

 public void connectionChange()
 {
   filters = new FilterCascade[getInputWidth()];
   for(int i=0;i<filters.length;i++)
       filters[i] = new FilterCascade(8, smooth);
 }

 public SimpleHFilter()
 {
  super();
 }

 public void processSignal()
 {
   GestureSignal sig = lastSig;
   if(sig.vals.length==sigWidth)
   {
      active = true;
      double [] newVals = new double[sig.vals.length];
      for(int i=0;i<sigWidth;i++)
      	  newVals[i] = sig.vals[i] - filters[i].filter(sig.vals[i]);
      
      setCurSig(new GestureSignal(newVals,getID()));
    }
   
 }

}
