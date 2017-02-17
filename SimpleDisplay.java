import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SimpleDisplay extends SProcessorModel
{

 public void connectionChange()
 {

 }

 private int lastX, lastY;
 mFrame mf;

 public void showInterface()
 {
  mf.setTitle(getName());
  mf.show();

 }
 private class mFrame extends JFrame
 {

 public void paint(Graphics g)
 {
   Dimension dSize = getSize();
   g.setColor(Color.black);
   g.fillRect(0,0,dSize.width,dSize.height);
   g.setColor(Color.red);
   g.drawOval(lastX-2,lastY-2,4,4);

 }


 }


 public SimpleDisplay()
 {
  super();
  setTerminating(true);
  mf = new mFrame();
  mf.setSize(400,400);
 }

 public void deleted()
 {
 }

 public void processSignal()
 {
   GestureSignal sig = lastSig;      
   if(sig.vals.length>=2 && getInputWidth()>0)
   {
       active = true;
       lastX = (int)(sig.vals[0]);
       lastY = (int)(sig.vals[1]);
       mf.repaint();
   }
 }

}
