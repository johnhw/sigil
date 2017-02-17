import duotonic.*;
import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

/**
 * SignalDevice: Quantizes incoming signals
 * and outputs a series of discrete states representing
 * the incoming signal
 *
 * @author John Williamson
 */
public class Quantizer extends SProcessorModel
{

    
    private int oldTransition;
    private transient Vector divisionVector;
    private long oldTime, allSmallTime = 0;
    private static Color recColor = new Color(88,48,18);
    private int divisions = 3;
    private int divSize = 20;
    private static final double quiescentThreshold = 1.0;
    private static final int resetTime = 200;
     private static final int recognizeTime = 40;
    private boolean recording = false;
    private GesturePrototype recordPrototype;
    private boolean wasAllSmall = false;
    private boolean threeD = true;

    public String getGenName()
    {
	return "Quantizer";
    }
    public String getDescription()
    {
	return " Quantizes incoming signals"
	    +" and outputs a series of discrete states representing"
	    +" the incoming signal";
    }
    public String getDate()
    {
	return "March 2002";
    }
    public String getAuthor()
    {
	return "John Williamson";
    }


    public int getSignalWidth()
    {
	return 0;
    }
    

 static final long serialVersionUID = 213L;

    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
        createDivisions();
    }

   public Color getColor()
   {
        if(!recording)
	    return super.getColor();
	else
	    return recColor;
   }


    
    public void newGesture(String gestName)
    {
	recordPrototype.setName(gestName);
	StateInformation gestInfo = new StateInformation();
	gestInfo.setBoolean("New Gesture");
	gestInfo.setObject("Gesture", recordPrototype);
	distributeHetObject(gestInfo);
    }

    public void processHetObject(Object o)
    {
	if(o instanceof String && ((String)o).startsWith("NamedGesture:"))
	    {
		recording = !recording;
		String name = ((String)o);
		name = name.substring(name.indexOf(":")+1);
		if(!recording)
		    newGesture(name);
		else
		    recordPrototype = new GesturePrototype();
	    }
     if(o instanceof String && ((String)o).endsWith("SEGMENT"))
     {
          recording = !recording;
          if(!recording)
          {
                  String gestName = JOptionPane.showInputDialog(null, 
								"Enter gesture name", 
								"Gesture name", 
								JOptionPane.QUESTION_MESSAGE);

                  if(gestName!=null)
		      newGesture(gestName);
	  }
          else
          {
	      recordPrototype = new GesturePrototype();
          }
     }
    }

 

 public void connectionChange()
 {
     if(getInputWidth()>2 && !threeD)
	     threeD=true;
     else if(getInputWidth()==2 && threeD)
	     threeD=false;

 }

 private void createDivisions()
 {
     int offset = ((divisions)*divSize)/2;
     divisionVector = new Vector();
     for(int i=0;i<divisions;i++)
	 for(int j=0;j<divisions;j++)
	     for(int k=0;k<divisions;k++)
		 {
		     int x = (i*divSize) - offset;
		     int y = (j*divSize) - offset;
		     int z = (k*divSize) - offset;
		     Box3D newBox = new Box3D(new Point3D(x,y,z),
					      new Point3D(x+divSize, y+divSize, z+divSize));
		     divisionVector.add(newBox);
		 }
 }


    private class SliderListener implements ChangeListener
    {
	private String command;
	
	public void stateChanged(ChangeEvent ce)
	{
	    JSlider slider = (JSlider)(ce.getSource());
	    if(command.equals("Divisions") && divisions!=slider.getValue() &&
	       !slider.getValueIsAdjusting())
		{
		    int confirm = JOptionPane.showConfirmDialog(null, 
								"Continue? This will invalidate current recognition!");
		    if(confirm == JOptionPane.OK_OPTION)
			{
			    divisions = slider.getValue();
			    createDivisions();
			    
			}
		    else
			slider.setValue(divisions);
		}
	    else if(command.equals("Scale"))
		{
		    divSize = slider.getValue();
		    createDivisions();

		}
	}
	public SliderListener(String cmd)
	{
	    command = cmd;
	}
    }
    
    
    
    
    private JPanel makeInterface()
    {
	JPanel cen = new JPanel(new BorderLayout());
	JPanel northPanel = new JPanel(new BorderLayout());
	JSlider divSlider = new JSlider(2,8,divisions);
	JSlider scaleSlider = new JSlider(2,200,divSize);
	
	divSlider.addChangeListener(new SliderListener("Divisions"));
	scaleSlider.addChangeListener(new SliderListener("Scale"));
	
	JPanel scalePanel = UIUtils.nameSliderLabelled(scaleSlider, "Scale", true);
	JPanel divPanel = UIUtils.nameSliderLabelled(divSlider, "Divisions", true);
	northPanel.add(divPanel, BorderLayout.NORTH);
	northPanel.add(scalePanel, BorderLayout.CENTER);
	JPanel buttonPanel = new JPanel(new FlowLayout());
	
	cen.add(northPanel, BorderLayout.NORTH);
	return cen;

 }

    public void showInterface()
    {
	JFrame jf = new JFrame();
	jf.setSize(450,140);
	jf.setTitle("State system");
	JPanel mainPanel = makeInterface();
	jf.getContentPane().add(mainPanel);
	UIUtils.setColors(jf.getContentPane(), Color.black, Color.white);
	jf.show();
    }

    public Quantizer()
    {
	super();
	createDivisions();
    }

   private int getSegment(double [] vals)
   {
    int x=0, y=0, z=0;
    x = (int)vals[0];
    if(vals.length>1)
      y = (int)vals[1];
    if(vals.length>2)
      z = (int)vals[2];
    int maxSize = (divisions*divSize/2)-1;

    if(x>maxSize) x = maxSize;
    if(x<-maxSize) x = -maxSize;

    if(y>maxSize) y = maxSize;
    if(y<-maxSize) y = -maxSize;

    if(z>maxSize) z = maxSize;
    if(z<-maxSize || !threeD) z = -maxSize;

    Point3D testPt = new Point3D(x,y,z);
    
    int segment=0;

    for(int i=0;i<divisionVector.size();i++)
    {
     Box3D thisDiv = (Box3D)(divisionVector.get(i));

     if(thisDiv.contains(testPt))
     {
        segment = i;
        break;
     }
    }
    return segment;
   }

    public void processSignal()
    {
	GestureSignal sig = lastSig;
	
	if(sig.vals.length==sigWidth && sigWidth>1 && sigWidth<4)
	    {
		boolean allSmall = true;
                int spaceSegment = getSegment(sig.vals);
		
                for(int i=0;i<sig.vals.length;i++)
                {
		   
                 if(!(Math.abs(sig.vals[i])<quiescentThreshold))
                 {
                      allSmall = false;
                      break;
                 }
                }

		

                if(oldTransition!=spaceSegment)
		    {
			oldTransition = spaceSegment;
			StateInformation stateInfo = new StateInformation();
			if(!threeD)
			    {
				spaceSegment/=divisions;
				stateInfo.setTransition(spaceSegment, divisions, 
							divisions*divisions);
				stateInfo.setBoolean("Transition");
                                
			    }
			    else
			    stateInfo.setTransition(spaceSegment, divisions, 
						    divisions*divisions*divisions);
			distributeHetObject(stateInfo);
                        if(recording)
			    recordPrototype.addData(spaceSegment, sig.vals);
                       
		    }

		if(allSmall)
		    allSmallTime = System.currentTimeMillis()-oldTime;

		if(allSmall && !wasAllSmall)
		    {		
			StateInformation stateInfo = new StateInformation();
			stateInfo.setBoolean("Quiescent");
			distributeHetObject(stateInfo);
			wasAllSmall = true;
		    }
		else if(!allSmall)
		    {
			oldTime = System.currentTimeMillis();
			wasAllSmall = false;
		    }

                   if(allSmallTime>resetTime)
		    {
			StateInformation stateInfo = new StateInformation();
			stateInfo.setBoolean("Reset");
			distributeHetObject(stateInfo);
			oldTime = System.currentTimeMillis();
		    }



	    }   
    }

}





