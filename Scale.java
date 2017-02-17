import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class Scale extends SProcessorModel
{
    private double factor = 1.0;
    private int dbFactor = 80;

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Scale";
 }
 public String getDescription()
 {
  return "Scales a signal by a given amount";
 }
 public String getDate()
 {
  return "Feburary 2002";
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
  final JLabel lab = new JLabel("Scale "+(dbFactor-80)+"dB");
  gc.add("North", lab);

  JSlider slider = new JSlider(0,160,dbFactor);
  slider.addChangeListener(new ChangeListener(){
     public void stateChanged(ChangeEvent ce)
     {
      dbFactor = ((JSlider)(ce.getSource())).getValue();
      double logVal = (dbFactor-80.0)/20.0;
      factor = Math.pow(10.0, logVal);
      lab.setText("Scale "+(dbFactor-80)+"dB");
     }
    });
  gc.add("Center", slider);
  jf.show();
 }

 public void connectionChange()
 {
 }

 public Scale()
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
      for(int i=0;i<sig.vals.length;i++)
          newVals[i] = factor*sig.vals[i];
      
      setCurSig(new GestureSignal(newVals,getID()));
    }
   
 }

}
