import java.io.*;
import sigil.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.*;
import java.util.*;

public class MousePad extends GeneratorModel 
{
    private int cx, cy;
   
    private class TimedPoint implements Serializable
    {
	public int x,y;
	public long timestamp;
	public TimedPoint(int x, int y)
	{
	    this.x = x;
	    this.y = y;
	    this.timestamp = System.currentTimeMillis();
	}
    }
    private transient LinkedList previousPoints = new LinkedList();
    private int fadeState = 0;
    private static final int maxFade = 20;
    private static final int fadeScale = 12;
    private static final int historySize = 500;
    private long lastTimestamp;
    private long endTimestamp;
    private transient JFrame jf;

    static final long serialVersionUID = 213L;
    public String getGenName()
    {
	return "MousePad";
    }
    public String getDescription()
    {
	return "Transmits the mouse position over the interface window";
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
	return 2;
    }
    

    
    /**
     * Serialize the state of this object
     */
    private void writeObject(ObjectOutputStream out) throws IOException
    {
	out.defaultWriteObject();
    }
    
    /**
     * Deserialize, re-creating histoty information
     */
    private void readObject(ObjectInputStream in) throws IOException
    {
	try{
	    in.defaultReadObject();
	}catch(ClassNotFoundException cfne) {cfne.printStackTrace();}
	
	previousPoints = new LinkedList();
    }

    public void tock()
    {
	double [] sig = new double[2];
	sig[0]=cx;
	sig[1]=cy;
	Dimension dSize;
	if(jf!=null)
	    {
		dSize = jf.getSize();
		previousPoints.addLast(new TimedPoint(cx+dSize.width/2,
						      cy+dSize.height/2));
		if(previousPoints.size()>historySize)
		    previousPoints.removeFirst();
	    }
	GestureSignal gs = new GestureSignal(sig,getID());
	setCurrentSignal(gs);
	if(fadeState>0)
	    fadeState--;
	
	if(jf!=null)
	    jf.repaint();
    }

    public double [] getMaxDimensions()
    {
	
	double [] scales = new double[2];
	Dimension dSize;
	if(jf!=null)
	    {
		dSize = jf.getSize();
		double maxX=0, maxY=0, minX=0, minY=0;
		for(int i=0;i<previousPoints.size();i++)
		    {
			TimedPoint tPoint = (TimedPoint)(previousPoints.get(i));
			int x = tPoint.x-dSize.width/2;
			int y = tPoint.y-dSize.width/2;
			
			if(tPoint.timestamp>lastTimestamp && 
			   tPoint.timestamp<endTimestamp)
			    {
				if(x>maxX)
				    maxX = tPoint.x;
				if(x<minX)
				    minX = tPoint.x;
				if(y>maxY)
				    maxY = tPoint.y;
				if(y<maxY)
				    maxY = tPoint.y;
			    }
		    }
		
		scales[0] = maxX-minX;
		scales[1] = maxY-minY;
	    }
	return scales;
    }

    public void processReverseHetObject(Object o)
    {
	if(o instanceof ProbabilisticGesture)
	    {
		ProbabilisticGesture pGest = (ProbabilisticGesture) o;
		ParameterizedGesture mostProbable = pGest.getMostProbable();
		if(pGest.getBounce())
		    {
			pGest.setBounce(false);
			double [] scales = getMaxDimensions();
			Vector gestureList = pGest.getGestureList();
			for(int i=0;i<gestureList.size();i++)
			    {
				ParameterizedGesture thisGest = (ParameterizedGesture)(gestureList.get(i));
				thisGest.setScales(scales);
			    }
			distributeHetObject(pGest);
		    }
		if(mostProbable!=null)
		    {
			fadeState = maxFade;
			lastTimestamp = mostProbable.getStartTime();
			endTimestamp = mostProbable.getEndTime();
		    }
	    }
    }

    private class ClickListener extends MouseAdapter
    {
	
	public void mousePressed(MouseEvent me)
	{
	    if(MouseUtils.right(me))
		{
		    StateInformation sInfo = new StateInformation();
		    sInfo.setBoolean("Start");
			distributeHetObject(sInfo);
		}
	}

	public void mouseReleased(MouseEvent me)
	{
	    if(MouseUtils.left(me))
		distributeHetObject("MousePad:SEGMENT");
	    if(MouseUtils.right(me))
		{
		    StateInformation sInfo = new StateInformation();
		    sInfo.setBoolean("End");
		    distributeHetObject(sInfo);
		}
	}
	
    }
    private class MouseSignal extends MouseMotionAdapter
    {
        public void mouseMoved(MouseEvent me)
        {
	    Dimension dSize = jf.getSize();
	    cx = me.getX()-dSize.width/2;
	    cy = me.getY()-dSize.height/2;
        }

        public void mouseDragged(MouseEvent me)
        {
	    Dimension dSize = jf.getSize();
	    cx = me.getX()-dSize.width/2;
	    cy = me.getY()-dSize.height/2;
	}
 }



    public MousePad()
    {
	super();

	
    }
    
    private class TraceImager extends JPanel
    {

	private void interpolatePoints(Graphics g, int x1, int y1, int x2, int y2, int width)
	{
	    double dist = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
	    double xInc = (x2-x1)/dist;
	    double yInc = (y2-y1)/dist;
	    double curX = x1, curY = y1;
	    for(int i=0;i<dist+1;i++)
		{
		    g.fillOval((int)(curX-width/2), (int)(curY-width/2), width, width);
		    curX+=xInc;
		    curY+=yInc;
		}
	}

	public void paint(Graphics g)
	{
	    Dimension dSize = getSize();
	    g.setColor(Color.black);
	    g.fillRect(0,0,dSize.width,dSize.height);
	    if(fadeState>0)
		{
		   
		    Color traceCol = new Color(fadeState*fadeScale/2, 
					       fadeState*fadeScale, 
					       fadeState*fadeScale);
		    g.setColor(traceCol);
		    for(int i=0;i<previousPoints.size()-1;i++)
			{
			    TimedPoint tPoint = (TimedPoint)(previousPoints.get(i));
			    TimedPoint nextPoint = (TimedPoint)(previousPoints.get(i+1));
			    if(tPoint.timestamp>lastTimestamp && 
			       tPoint.timestamp<endTimestamp &&
			       nextPoint.timestamp>lastTimestamp && 
			       nextPoint.timestamp<endTimestamp)
				interpolatePoints(g, tPoint.x, tPoint.y, 
						  nextPoint.x, nextPoint.y,8);

			}

		}
	}

    }

 public void showInterface()
 {
     jf = new JFrame(getName());
     jf.setSize(400,400);
     jf.getContentPane().setBackground(Color.black);
     
     jf.addMouseMotionListener(new MouseSignal());
     jf.addMouseListener(new ClickListener());
     jf.setTitle(getName());
     TraceImager tracer = new TraceImager();
     jf.getContentPane().add(tracer);     
     jf.show();
  
 }


}
