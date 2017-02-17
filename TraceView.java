import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;


public class TraceView extends SProcessorModel
{
 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "TraceView";
 }
 public String getDescription()
 {
  return "Provides a rotatble 3D view of an incoming signal";
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

    private double [] lastX;
    private double [] lastY;
    private double [] lastZ;
    private int markIndex = 0;
    private int maxIndex = 30;
    private double yaw, pitch;
    private boolean frozen = false;

    mPanel mp;

    public void freeze()
    {
	frozen = true;

    }

    public void processHetObject(Object o)
    {
/*     if(o instanceof String)
     {
      String s = (String)o;
      if(s.endsWith("SEGMENT"))
        if(frozen)
           unfreeze();
        else
           freeze();
     }*/
    }

    public void unfreeze()
    {
	frozen = false;
    }


    private void push(double [] vals)
    {
	if(!frozen)
	    {
		lastX[markIndex] = vals[0];
		if(vals.length>1)
		    lastY[markIndex] = vals[1];
		else
		    lastY[markIndex] = 0.0;
		if(vals.length>2)
		    lastZ[markIndex] = vals[2];
		else
		    lastZ[markIndex] = 0.0;
		
		markIndex++;
		if(markIndex>=maxIndex)
		    markIndex = 0;
	    }
    }

    private class SpinListener extends MouseAdapter implements MouseMotionListener 
    {
	private boolean dragging = false;
	private int startY, startX;
	private double oldPitch, oldYaw;
	
	public void mousePressed(MouseEvent me)
	{
	    if(!dragging)
		{
		    dragging = true;
		    oldPitch = pitch;
		    oldYaw = yaw;
		    startY = me.getY();
		    startX = me.getX();
		}
	}
	
	public void mouseReleased(MouseEvent me)
	{
	    dragging = false;
	}
	
	public void mouseDragged(MouseEvent me)
	{
	    if(dragging)
		{
		    pitch = oldPitch+((me.getY()-startY)/80.0);
		    yaw = oldYaw+((me.getX()-startX)/80.0);
		    mp.repaint();
		}
	}
	
	public void mouseMoved(MouseEvent me)
	{
	    
	}
		
    }
    

    private void modifyBuffer(int mIndex)
    {

	
	double [] newX = new double[mIndex];
	System.arraycopy(lastX,0,newX,0,Math.min(mIndex,lastX.length));

	double [] newY = new double[mIndex];
	System.arraycopy(lastY,0,newY,0,Math.min(mIndex,lastY.length));
	
	double [] newZ = new double[mIndex];
	System.arraycopy(lastZ,0,newZ,0,Math.min(mIndex,lastZ.length));

	lastX = newX;
	lastY = newY;
	lastZ = newZ;
	maxIndex = mIndex;
	if(markIndex>=maxIndex)
	    markIndex = 0;

    }

 private class LengthChange implements ChangeListener
 {

     public void stateChanged(ChangeEvent ce)
     {
	 JSlider source = (JSlider)(ce.getSource());
	 int newMax = source.getValue();
	 modifyBuffer(newMax);

     }

 }


 public void showInterface()
 {
     JFrame jf = new JFrame();
     JSlider lengthSlider = new JSlider(0,200,30);
     lengthSlider.addChangeListener(new LengthChange());
     lengthSlider.setValue(maxIndex);
     jf.getContentPane().setLayout(new BorderLayout());
     jf.getContentPane().add(mp, BorderLayout.CENTER);
     SpinListener spinListen = new SpinListener();
     mp.addMouseListener(spinListen);
     mp.addMouseMotionListener(spinListen);
     JPanel controlPanel = new JPanel(new BorderLayout());
     JButton freezeButton = new JButton("Freeze");
     freezeButton.addActionListener(new ActionListener(){
	     public void actionPerformed(ActionEvent ae)
	     {
		 if(frozen)
		     unfreeze();
		 else
		     freeze();
	     }

	 });
     controlPanel.add(lengthSlider, BorderLayout.CENTER);
     controlPanel.add(freezeButton, BorderLayout.SOUTH);

     jf.getContentPane().add(controlPanel, BorderLayout.SOUTH);
     jf.setSize(400,400);
     jf.setTitle(getName());
     jf.show();

 }
 private class mPanel extends JPanel
 {

 public void paint(Graphics g)
 {
   Dimension dSize = getSize();
   g.setColor(Color.black);
   g.fillRect(0,0,dSize.width,dSize.height);
   int ageVal = 0;

   Point3D axisX = Utils3D.rotate3D(new Point3D(-150,0,0),0,pitch,yaw);
   Point3D axisY = Utils3D.rotate3D(new Point3D(0,-150,0),0,pitch,yaw);
   Point3D axisZ = Utils3D.rotate3D(new Point3D(0,0,-150),0,pitch,yaw);
   Point zeroPt = Utils3D.project(0, 0, 0, dSize.width, 500);

   Point axisXPt = Utils3D.project(axisX, dSize.width, 500);
   Point axisYPt = Utils3D.project(axisY, dSize.width, 500);
   Point axisZPt = Utils3D.project(axisZ, dSize.width, 500);
   g.setColor(Color.red);
   g.drawLine(zeroPt.x, zeroPt.y, axisXPt.x, axisXPt.y);
   g.setColor(Color.green);
   g.drawLine(zeroPt.x, zeroPt.y, axisYPt.x, axisYPt.y);
   g.setColor(Color.blue);
   g.drawLine(zeroPt.x, zeroPt.y, axisZPt.x, axisZPt.y);


   Point oldPt = null;
   for(int i=markIndex;i<maxIndex;i++)
       {
	   
	   Point3D vP = new Point3D(lastX[i], lastY[i], lastZ[i]);
	   Point3D rP = Utils3D.rotate3D(vP,0, pitch, yaw); 
	   Point drawPoint = Utils3D.project(rP, dSize.width, 500);
           g.setColor(new Color(0,0,(int)(((double)ageVal/(double)maxIndex)*200.0)+30));
	   g.fillOval(drawPoint.x-4, drawPoint.y-4, 8, 8);
	   if(oldPt!=null)
	       g.drawLine(oldPt.x, oldPt.y, drawPoint.x, drawPoint.y);
	   ageVal++;
	   oldPt = drawPoint;
       }
   oldPt = null;

   for(int i=0;i<markIndex;i++)
       {	   
	   Point3D vP = new Point3D(lastX[i], lastY[i], lastZ[i]);
	   Point3D rP = Utils3D.rotate3D(vP,0, pitch, yaw); 
	   Point drawPoint = Utils3D.project(rP, dSize.width, 500);
	   g.setColor(new Color(0,0,(int)(((double)ageVal/(double)maxIndex)*200.0)+30));
	   g.fillOval(drawPoint.x-4, drawPoint.y-4, 8, 8);
	   if(oldPt!=null)
	       g.drawLine(oldPt.x, oldPt.y, drawPoint.x, drawPoint.y);
	   ageVal++;
	   oldPt = drawPoint;
       }

 }


 }


 public TraceView()
 {
  super();
  setTerminating(true);
  mp = new mPanel();
  lastX = new double[maxIndex];
  lastY = new double[maxIndex];
  lastZ = new double[maxIndex];

 }

 public void deleted()
 {
 }

 public void processSignal()
 {
   GestureSignal sig = lastSig;      
   if(sig.vals.length>=1 && getInputWidth()>0)
   {
       active = true;
       push(sig.vals);       
       mp.repaint();
   }
 }

}

