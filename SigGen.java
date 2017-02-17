import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class SigGen extends GeneratorModel
{
    private int currentTick = 0;
    private double phase = 0;
    private double amplitude = 100;
    private double period = 25;
    private double pulseWidth = 0.5;
    private String sigType = "Sine";
    private double waveVal = 0;
    private double oldVal;




    private JSlider phaseSlider, amplitudeSlider, frequencySlider, pulseWidthSlider;
    private ButtonGroup typeGroup;

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "SigGen";
 }
 public String getDescription()
 {
  return "Creates signal test patterns (sine, saw, triangle, square)";
 }
 public String getDate()
 {
  return "Feburary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


    private void writeObject(ObjectOutputStream out) throws IOException
    {    
     out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException
    {
    try{
     in.defaultReadObject();
     }catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
        
    }

    private boolean generating = true;

    public int getSignalWidth()
    {
         return 1;
    }


 private class SignalChange implements ActionListener
 {
  private String cmd;
  public SignalChange(String cmd)
  {
   this.cmd = cmd;
  }

  public void actionPerformed(ActionEvent ae)
  {
     sigType = cmd;
     oldVal = -1.0;
   }

 }


 private class SliderMoved implements ChangeListener
 {

  public void stateChanged(ChangeEvent ce)
  {
   phase = ((double)phaseSlider.getValue()/100.0)*Math.PI*2;
   amplitude = amplitudeSlider.getValue()*10;
   period = 1.0/((double)frequencySlider.getValue()/2000.0);
  }

 }
 public void showInterface()
 {
  JFrame jf;

  jf = new JFrame();
  jf.setSize(400,200);
  
  jf.setTitle(getName());
  Container gc = jf.getContentPane();
  gc.setLayout(new BorderLayout());

  JPanel sliderPanel = new JPanel(new GridLayout(3,2));

  sliderPanel.add(new JLabel("Frequency"));
  frequencySlider = new JSlider(0,100,50);
  ChangeListener cl = new SliderMoved(); 
  frequencySlider.addChangeListener(cl);
  sliderPanel.add(frequencySlider);

  sliderPanel.add(new JLabel("Phase"));
  phaseSlider = new JSlider(0,100,0);
  phaseSlider.addChangeListener(cl);
  sliderPanel.add(phaseSlider);

  sliderPanel.add(new JLabel("Amplitude"));
  amplitudeSlider = new JSlider(0,100,10);
  amplitudeSlider.addChangeListener(cl);
  sliderPanel.add(amplitudeSlider);



  gc.add(sliderPanel, BorderLayout.CENTER);
  typeGroup = new ButtonGroup();
  JRadioButton sine = new JRadioButton("Sine");
  JRadioButton saw = new JRadioButton("Saw");
  JRadioButton square = new JRadioButton("Square");
  JRadioButton triangle = new JRadioButton("Triangle");
  sine.setSelected(true);
  sine.addActionListener(new SignalChange("Sine"));
  saw.addActionListener(new SignalChange("Saw"));
  square.addActionListener(new SignalChange("Square"));
  triangle.addActionListener(new SignalChange("Triangle"));

  typeGroup.add(sine);
  typeGroup.add(saw);
  typeGroup.add(square);
  typeGroup.add(triangle);
  JPanel buttonPanel = new JPanel(new FlowLayout());
  buttonPanel.add(sine);
  buttonPanel.add(saw);
  buttonPanel.add(square);
  buttonPanel.add(triangle);



  gc.add(buttonPanel, BorderLayout.NORTH);

  jf.show();
 }



 public void deleted()
 {
 }


 public void startPlaying()
 {
   generating = true;          
 }

 public void stopPlaying()
 {
     
  generating = false;
 }


 private void makeSig(double [] valArr)
 {
   double val = 0.0;
   val = Math.sin(phase+(double)(currentTick/period)*2*Math.PI);
   double tempVal = val;
   
   if(sigType.equals("Square"))
   {
           if(tempVal>0.0) val=1.0;
           if(tempVal<0.0) val=-1.0;
   }

   if(sigType.equals("Saw"))
   {
          if(tempVal<0.0 && oldVal>=0.0)
                     waveVal = -1.0;
            else
            {
              waveVal+=(2.0/period);
              val=waveVal;
            }
   }

   if(sigType.equals("Triangle"))
   {
          if(tempVal<0.0 && oldVal>=0.0)
                     waveVal = 1.0;

          if(tempVal<0.0)
          {
              waveVal-=(4.0/period);
              val=waveVal;

          }                     
            else
            {
              waveVal+=(4.0/period);
              val=waveVal;
            }
   }


   oldVal = tempVal;
   val*=amplitude;
   currentTick++;
   valArr[0] = val;
 }

    public void tock()
    {
        if(generating)
	    {
               double [] valArr = new double[1];
               makeSig(valArr);
               setCurrentSignal(new GestureSignal(valArr, getID()));
            }
    }

    
}
