import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ZeroSignature extends SProcessorModel
{
    
    private LinkedList timeQueue;
    private int maxQueueSize = 100;
    private double yScale = 1.1;
    private JFrame gFrame;
    private int differential = 3;
    private double [] [] dSysVals;


 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "ZeroSignature";
 }
 public String getDescription()
 {
  return "Shows a scrolling plot of the zero crossings in an input signal";
 }
 public String getDate()
 {
  return "Feburary 2002";
 }
 public String getAuthor()
 {
  return "John Williamson";
 }


 private Color [] colTable =
 {
     Color.red,
     Color.blue,
     Color.green,
     Color.yellow,
     Color.cyan,
     Color.magenta,
     Color.orange,
     Color.gray,
     Color.white
 };

 public void connectionChange()
 { 
     int width = getInputWidth();
     dSysVals = new double[differential][width];
     for(int i=0;i<differential;i++)
	 dSysVals[i] = new double[width];
	     
     sigWidth = width;
     timeQueue = new LinkedList();
     lastVals = new double[width];
     maxVals = new double[width];
 }


    public void setDynamics(double [] gVals)
    {
	dSysVals[differential-1] = gVals;
    }

    public double [] calculateDynamics()
    {
	int width = getInputWidth();
	double [] retVals = new double[width];
	for(int i=differential-2;i>=0;i--)
	    {
		for(int j=0;j<width;j++)
		    {
			dSysVals[i][j] += dSysVals[i+1][j];
			if(i==0)
			    retVals[j] = (int)(dSysVals[i][j]);
		    }
	    }
	return retVals;
    }

 public void showInterface()
 {
  gFrame.setTitle(getName());  
  gFrame.show();
 }

   
 private class mPanel extends JPanel
 {
     transient Image dblBuffer;
     private final int topGap = 40;
     private final int barSpace = 30;
     private final int posNegSpace = 5;
     private final int barHeight = 20;
     private final int barWidth = 4;

     private void drawSigBars(Graphics g, Dimension dSize)
     {


	 int width = getInputWidth();
	 int curY = topGap;
	 g.setColor(Color.darkGray);
	 for(int i=0;i<width;i++)
	     {
		 
		 g.drawRect(0, curY, dSize.width-1, barHeight);
		 curY+=posNegSpace+barHeight;
		 g.drawRect(0, curY, dSize.width-1, barHeight);
		 curY+=barSpace+barHeight;
	     }
     }

     private void drawSignature(Graphics g, Dimension dSize)
     {
	 
	 int width = getInputWidth();

	 LinkedList temp = (LinkedList)(timeQueue.clone());
	 Iterator iter = temp.iterator();
	
	 long startTime=0;
	 int oldTime=0;
	 int cTime = 0;

	 while(iter.hasNext())
	     {
		 int curY = topGap;
		 int [] zeroVals = (int [])(iter.next());
		 if(zeroVals!=null && (zeroVals.length == width*2))
		     {
			 for(int i=0;i<width;i++)
			     {

				 g.setColor(colTable[i*2]);
				 if(zeroVals[i*2]!=0)
				     {
					 g.fillRect(cTime, curY, barWidth, barHeight);
					 int offHeight = zeroVals[i*2]/4;
					 g.setColor(g.getColor().darker().darker());
					 g.fillRect(cTime, curY-offHeight, barWidth, offHeight);
				     }
				 curY+=posNegSpace+barHeight;
				 
				 g.setColor(colTable[i*2+1]);
				 if(zeroVals[i*2+1]!=0)
				     {
					 g.fillRect(cTime, curY, barWidth, barHeight);
					 int offHeight = zeroVals[i*2+1]/4;
					 g.setColor(g.getColor().darker().darker());
					 g.fillRect(cTime, curY+barHeight, barWidth, offHeight);
				     }
				 curY+=barSpace+barHeight;
			     }
			 oldTime = cTime;
			 cTime++;
		     }
		 
	     }
     }



 public void paint(Graphics gr)
 {
   Dimension dSize = getSize();
   if(dblBuffer==null)
   {
    dblBuffer = createImage(dSize.width, dSize.height);
    if(maxQueueSize!=dSize.width)
      {
       maxQueueSize = dSize.width;
       timeQueue = new LinkedList();
       return;
      }
   }
   Graphics g = dblBuffer.getGraphics();
   g.setColor(Color.black);
   g.fillRect(0,0,dSize.width,dSize.height);

   drawSigBars(g, dSize);
   drawSignature(g, dSize);

  gr.drawImage(dblBuffer,0,0,this);
 }


 }


    
    public void processHetObject(Object o)
    {


    }
    
 public ZeroSignature()
 {
  super();
  gFrame = new JFrame();
  gFrame.setTitle(getName());
  gFrame.getContentPane().add(new mPanel());
  timeQueue = new LinkedList();
  gFrame.setSize(400,420); 
  gFrame.setResizable(false);
 }

    private double [] lastVals;
    private double [] maxVals;

    private int [] calcZeroCrossings(double [] vals)
    {
        int width = getInputWidth();
	int [] retVal = new int[width*2];
	double [] dynamics = new double[width];
        for(int i=0;i<getInputWidth();i++)
        {
	    retVal[i*2] = 0;
	    retVal[i*2+1] = 0;
	    if(lastVals[i]<0 && vals[i]>=0)
		{
		    
		    retVal[i*2] = (int)(maxVals[i]);


		    maxVals[i]=0.0;
		   
		    dynamics[i] = 10;
		}
	    else if (lastVals[i]>=0 && vals[i]<0)
		{
		    retVal[i*2+1]= (int)(maxVals[i]);

		    maxVals[i]=0.0;
		    dynamics[i] = -10;
		}
	    
	    if(Math.abs(vals[i])>maxVals[i])
                maxVals[i]=Math.abs(vals[i]);
	    lastVals[i] = vals[i];
	    setDynamics(dynamics);
        }	
	return retVal;
    }


 public void processSignal()
 {

     GestureSignal sig = lastSig;      
     
     if(getInputWidth()>0)
	 {
	     active = true;
	     int [] zeros = calcZeroCrossings(sig.vals);
	     
	     if(timeQueue.size()>maxQueueSize)
		 timeQueue.removeFirst();
	     timeQueue.addLast(zeros);
	     GestureSignal newSig = new GestureSignal(calculateDynamics(),getID());
	     setCurSig(newSig);
	     gFrame.repaint();
	 }

 }

}
