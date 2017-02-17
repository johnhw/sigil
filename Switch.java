import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Switch extends MProcessorModel
{

 private boolean [] filteredInputs;
 private int activeOutputs;

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "Switch";
 }
 public String getDescription()
 {
  return "Switches between individual elements of multiple input signals";
 }
 public String getDate()
 {
  return "Janurary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }



 public int getSignalWidth()
 {
    return activeOutputs;
 }

 public Switch()
 {
  super();
 }

 public void processSignalBuffer()
 {
   double [] vals = new double [getSignalWidth()];
   int count = 0, filtCount = 0;
   for(int i=0;i<generators.size();i++)
   {
    GestureSignal oneSig = (GestureSignal)(signalBuffer.get(generators.get(i)));
    for(int j=0;j<oneSig.vals.length;j++)
    {
     if(filtCount<filteredInputs.length && !filteredInputs[filtCount])
      vals[count++]=oneSig.vals[j];
     filtCount++;
    }
   }
   GestureSignal gSig = new GestureSignal(vals, getID());
   setCurSig(gSig);
 }

 private void calculateActiveOutputs()
 {
  activeOutputs = filteredInputs.length;
  for(int i=0;i<filteredInputs.length;i++)
   if(filteredInputs[i])
        activeOutputs--;
  propogateWidthChanges();

 }

 public void connectionChange()
 {
  filteredInputs = new boolean[getInputWidth()];
  calculateActiveOutputs();
 }
               

 private class TopListener implements ActionListener
 {
  Vector controlledButtons;

  public TopListener(Vector cont)
  {
   controlledButtons = cont;
  }

  public void actionPerformed(ActionEvent ae)
  {
   boolean activate = ((JToggleButton)(ae.getSource())).isSelected();
   for(int i=0;i<controlledButtons.size();i++)
   {
    
    JToggleButton jTog = (JToggleButton)(controlledButtons.get(i));
    if(activate)
    {
            if(!jTog.isSelected())
              jTog.doClick();
            jTog.setEnabled(false);
    }
    else
    {
              jTog.setEnabled(true);
            if(jTog.isSelected())
               jTog.doClick();
    }
   }
  }
 }

 private class SwitchListener implements ActionListener
 {
  private int index;

  public void actionPerformed(ActionEvent ae)
  {
   boolean selState = ((JToggleButton)(ae.getSource())).isSelected();
   filteredInputs[index] = selState;
   calculateActiveOutputs();
  }

  public SwitchListener(int in)
  {
   index = in;
  }

 }


 public void showInterface()
 {
  JFrame jf = new JFrame();
  JPanel butPan = new JPanel(new BorderLayout());
  JScrollPane scroll = new JScrollPane(butPan);
  jf.setBackground(Color.black);
  jf.setSize(280,400);
  jf.setTitle(getName());
  Container gc = jf.getContentPane();
  Box vert = Box.createVerticalBox();
  int count = 0;
  for(int i=0;i<generators.size();i++)
  {
   JPanel genPan = new JPanel();
   GestureGenerator gGen = (GestureGenerator)(generators.get(i));
   int nVars = gGen.getSignalWidth();
   genPan.setLayout(new GridLayout(nVars+1,1));
   JToggleButton topBut = new JToggleButton(gGen.getName());
   topBut.setForeground(Color.blue);
   genPan.add(topBut);
   Vector topVec = new Vector();
   for(int j=0;j<nVars;j++)
   {
          
           JToggleButton inBut = new JToggleButton("Control "+j);
           if(filteredInputs[count])
                inBut.setSelected(true);

           inBut.addActionListener(new SwitchListener(count));
           topVec.add(inBut);
           genPan.add(inBut);
           count++;
   }
   topBut.addActionListener(new TopListener(topVec));

   vert.add(genPan);
  }
  butPan.add("North",vert);
  gc.add(scroll);
  jf.show();
 }


}
