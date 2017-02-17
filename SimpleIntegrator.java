import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class SimpleIntegrator extends SProcessorModel
{

 double [] vals;
 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "SimpleIntegrator";
 }
 public String getDescription()
 {
  return "Approximates the integral of a signal";
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
     JFrame jf = new JFrame();
     jf.setTitle(getName());
     jf.setSize(150,80);
     Container gc = jf.getContentPane();
     JButton resetButton = new JButton("Reset");
     resetButton.addActionListener(new ActionListener()
	 {
	     public void actionPerformed(ActionEvent ae)
	     {
		 for(int i=0;i<vals.length;i++)
		     vals[i]=0.0;
	     }
	 });
     gc.add(resetButton);
     jf.show();
 }

 public void connectionChange()
 {
  vals = new double[getInputWidth()];
 }

 public SimpleIntegrator()
 {
  super();
 }

 public void processSignal()
 {
   GestureSignal sig = lastSig;
   if(sig.vals.length==sigWidth)
   {
      for(int i=0;i<sigWidth;i++)
	  vals[i] += sig.vals[i];
      
      setCurSig(new GestureSignal(vals,getID()));
    }

  }

}
