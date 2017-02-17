package sigil;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import java.io.*;

public class VisualElement implements java.io.Serializable
{
    static final long serialVersionUID = 213L;
    private boolean selected = false;
    private GestureElement attatched;
    
    protected Vector connections;
    protected Vector inConnections;
    protected Vector connCentroids;
    protected boolean isGenerator;
    public Rectangle bounds;
   
    protected int arrowRadius = 25;
    protected String label;
    protected static final double connSpace = 3;
    protected static Color groupLabelColor = new Color(60, 60, 60);
    protected static Color connColor = new Color(168,181,240);
    protected static Color connActiveColor = new Color(46,57,119);
    public static Color genColor = new Color(71,82,133);
    public static Color procColor = new Color(38,74,98);
    protected boolean inGroup = false;
    protected Group parentGroup;
    protected Point dynamicPosition;
    protected boolean moving = false;
    protected boolean stoppingNow = false;
    protected Point targetPosition;
    protected double velX, velY;
    protected Point deltaPosition;
    protected Point realPosition;

    public void startMoving(Point target)
    {
	targetPosition = target;
	dynamicPosition = new Point(bounds.x, bounds.y);
	deltaPosition = new Point(bounds.x-target.x, bounds.y-target.y);
	moving = true;
    }


    public void moveOneStep(double velocityFactor)
    {
	int dX = (dynamicPosition.x-targetPosition.x);
	int dY = (dynamicPosition.y-targetPosition.y);

	if(Math.abs(dX)>50 || Math.abs(dY)>50)
	    {
		velX = (double)dX/velocityFactor;
		velY = (double)dY/velocityFactor;
		if(velX<0.0 && velX>-1.0) velX=-1.0;
		if(velX>0.0 && velX<1.0) velX=1.0;
		if(velY<0.0 && velY>-1.0) velY=-1.0;
		if(velY>0.0 && velY<1.0) velY=1.0;
		dynamicPosition.x-=velX;
		dynamicPosition.y-=velY;
		bounds.x = dynamicPosition.x;
		bounds.y = dynamicPosition.y;
		
	    }
	else
	    {
		dynamicPosition.x = targetPosition.x;
		dynamicPosition.y = targetPosition.y;
		bounds.x = dynamicPosition.x;
		bounds.y = dynamicPosition.y;
		stoppingNow = true;
	    }
    }
    
    public Rectangle getBounds()
    {
	return bounds;
    }


    public void addToGroup(Group toAdd)
    {

	if(toAdd.add(this))
	    {
		parentGroup = toAdd;
		inGroup = true;
	    }
    }

    public void removeFromGroup()
    {
	parentGroup.remove(this);
	inGroup = false;
    }

    public void select()
    {
	selected = true;
    }
    
    public void deselect()
    {
	selected = false;
    }




 public boolean isInside(Rectangle r)
 {
  return r.contains(bounds);
 }


 public GestureElement getElement()
 {
  return attatched;
 }


 public VisualElement(GestureElement gElt, int x, int y)
 {

     
     label = gElt.getName();
     connections = new Vector();
     inConnections = new Vector();
     connCentroids = new Vector();
     attatched = gElt;
     isGenerator = gElt.getGenStat();
     bounds = new Rectangle(x,y,80,30);
     bounds.width = label.length()*14;

     realPosition = new Point(bounds.x, bounds.y);
 }

    
 public VisualElement(String label, int x, int y)
 {
     this.label = label;
     connections = new Vector();
     inConnections = new Vector();
     connCentroids = new Vector();
     attatched = null;
     isGenerator = false;
     bounds = new Rectangle(x,y,80,30);
     bounds.width = label.length()*14;
     
     realPosition = new Point(bounds.x, bounds.y);
 }

    public boolean parentExpanded()
    {
	return parentGroup.isExpanded();
    }

    public void contractParent()
    {
	if(inGroup)
	    parentGroup.contract();
    }
    


    public boolean isInGroup()
    {
	return inGroup;
    }

 public void disconnectSelf()
 {
  attatched.deleted();
  while(inConnections.size()>0)
  {
   VisualElement in = ((VisualElement)(inConnections.get(0)));
   in.disconnect(this);
   inConnections.remove(in);
  }

  while(connections.size()>0)
  {
   disconnect((VisualElement)(connections.get(0)));
  }
 }

 public void connected(VisualElement ve)
 {
  inConnections.add(ve);
 }

    private boolean upLoop(VisualElement ve, VisualElement te)
    {
	if(ve==te || inConnections.contains(te) || inConnections.contains(ve))
	    return true;
	boolean retVal = false;
	for(int i=0;i<inConnections.size();i++)
	    retVal = retVal || ((VisualElement)(inConnections.get(i))).upLoop(ve,te);     
	return retVal;

    }


    private boolean downLoop(VisualElement ve, VisualElement te)
    {
	if(ve==te || connections.contains(te) || connections.contains(ve))
	    return true;
	boolean retVal = false;
	for(int i=0;i<connections.size();i++)
	    retVal = retVal || ((VisualElement)(connections.get(i))).downLoop(ve,te);     
	return retVal;

    }
 public boolean loop(VisualElement ve, VisualElement te)
 {
     return upLoop(ve, te) || downLoop(ve, te);
  
 }

 public boolean connect(VisualElement ve)
 {
  
  if(!loop(ve,this) && !(ve.isGenerator) && !(inConnections.contains(ve)))
  {
   boolean valConn =  attatched.connectTo(ve.attatched);
   if(valConn)
   {
     connections.add(ve);
     ve.connected(this);
   }
   return valConn; 
  }
  else
  return false;
 }

 public boolean disconnect(VisualElement ve)
 {
  connections.remove(ve);
  ve.inConnections.remove(this);
  return attatched.disconnect(ve.attatched);
 }

  public void deleteConnection(int x, int y)
  {
    for(int i=0;i<connCentroids.size();i++)
    {
      Point pt = (Point)connCentroids.get(i);
      if(Math.abs(pt.x-x)<arrowRadius &&
         Math.abs(pt.y-y)<arrowRadius && i<connections.size())
       disconnect((VisualElement)(connections.get(i)));
    }
  }


 public void rename(String name)
 {
  label = name;
  if(attatched!=null)
      attatched.setName(name);
  bounds.width = label.length()*14;
 }

 public void move(int newX, int newY)
 {
     if(!moving)
	 {
	     bounds.x = newX-bounds.width/2;
	     bounds.y = newY-bounds.height/2;
	     realPosition = new Point(bounds.x, bounds.y);
	 }
 }

 public boolean isOver(int x, int y)
 {
  return bounds.contains(x,y);
 }

    private void paintStandardElement(Graphics g, Color tCol)
    {
	Graphics2D gr2d = (Graphics2D)g;
	g.setColor(tCol);
	Paint oldPaint = gr2d.getPaint();
	GradientPaint gradPaint = new GradientPaint(bounds.x, bounds.y, Color.black,
						    bounds.x, bounds.y+bounds.height/2-3, tCol);
	gr2d.setPaint(gradPaint);
	g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height/2);
	
	gradPaint = new GradientPaint(bounds.x, bounds.y+bounds.height/2+3, tCol,
				      bounds.x, bounds.y+bounds.height, Color.black);
	gr2d.setPaint(gradPaint);
	g.fillRect(bounds.x, bounds.y+bounds.height/2, bounds.width, bounds.height/2);
	gr2d.setPaint(oldPaint);
	g.setColor(Color.white);
	g.drawString(label,bounds.x+5, bounds.y+(bounds.height-12)/2+12);
    }

  

 public void paint(Graphics g)
 {

     if(inGroup && !parentGroup.isExpanded())
	 return;

     if(attatched!=null)
	 g.setColor(attatched.getColor());
     Color tCol = g.getColor();

     if(inGroup)
	 {
	     
	     g.setFont(new Font("SansSerif", Font.PLAIN, 9));
	     String parentLabel = parentGroup.label;
	     int labelWidth = g.getFontMetrics().stringWidth(parentLabel);
	     
	     g.setColor(groupLabelColor.darker().darker());
	     g.fillRect(bounds.x-20, bounds.y-16, labelWidth+10, 15);

	     g.setColor(groupLabelColor);
	     g.drawRect(bounds.x-20, bounds.y-16, labelWidth+10, 15);
	     g.setColor(Color.white);
	     g.drawString(parentLabel, bounds.x-16, bounds.y-5);
	 }

     
     g.setFont(new Font("SansSerif", Font.PLAIN, 13));
     bounds.width = g.getFontMetrics().stringWidth(label)+15;
     if(bounds.width<50)
	 bounds.width = 50;

     paintStandardElement(g, tCol);


     if(attatched!=null && attatched.isTerminating())
	 {
	     g.setColor(tCol.darker().darker());
	     g.fillRect(bounds.x+1, bounds.y+bounds.height-6, bounds.width-2, 5);
	     g.fillRect(bounds.x+1, bounds.y+1, bounds.width-2, 5);
	 }
     
     if(selected)
	 {
	     g.setColor(new Color(100,50,50,90));
	     g.fillRect(bounds.x-7, bounds.y-7, bounds.width+12, bounds.height+12);
	 }
 }

 public void paintConnections(Graphics g)
 {
  connCentroids = new Vector();
  double space = connSpace;
   for(int i=0;i<connections.size();i++)
   {
    VisualElement vElt = ((VisualElement)(connections.get(i)));
    GestureElement gElt = vElt.attatched;

    int nConn = attatched.getSignalWidth();

    double startX, startY, endX, endY;
    if(inGroup && !parentGroup.isExpanded())
	{
	    Group topGroup = parentGroup.getExpandedParent();
	    startX = topGroup.bounds.x+topGroup.bounds.width/2;
	    startY = topGroup.bounds.y+topGroup.bounds.height/2;
	}
    else
	{
	    startX = bounds.x+bounds.width/2;
	    startY = bounds.y+bounds.height/2;
	}

    if(vElt.inGroup && !vElt.parentGroup.isExpanded())
	{
	    
	    Group topGroup = vElt.parentGroup.getExpandedParent();
	    endX = topGroup.bounds.x+topGroup.bounds.width/2;
	    endY = topGroup.bounds.y+topGroup.bounds.height/2;
	}
    else
	{
	    endX = vElt.bounds.x+vElt.bounds.width/2;
	    endY = vElt.bounds.y+vElt.bounds.height/2;
	}
    
    if(startX==endX && startY==endY)
	continue;
	
    connCentroids.add(new Point((int)(startX+endX)/2,(int)(startY+endY)/2));

    double nVecX = (endX-startX);
    double nVecY = (endY-startY);
    double ang = Math.atan2(nVecY, nVecX);
    ang-=0.5*Math.PI;

    startX=startX-(nConn*space*Math.cos(ang))/2.0;
    startY=startY-(nConn*space*Math.sin(ang))/2.0;
    endX=endX-(nConn*space*Math.cos(ang))/2.0;
    endY=endY-(nConn*space*Math.sin(ang))/2.0;
    g.setColor(connColor);

    if(nConn==0)
    {
     nConn=1;
     g.setColor(Color.gray);
    }

    for(int j=0;j<nConn;j++)
    {
     g.drawLine((int)startX, (int)startY, (int)endX, (int)endY);
     double centrePointX = (startX+endX)/2;
     double centrePointY = (startY+endY)/2;
     double arrowLeftX = centrePointX - space*2*Math.cos(ang);
     double arrowLeftY = centrePointY - space*2*Math.sin(ang);

     double arrowRightX = centrePointX + space*2*Math.cos(ang);
     double arrowRightY = centrePointY + space*2*Math.sin(ang);

     double arrowForwardX = centrePointX + space*2*Math.cos(ang+Math.PI*0.5);
     double arrowForwardY = centrePointY + space*2*Math.sin(ang+Math.PI*0.5);

     if(j==0)
     {
       g.drawLine((int)arrowLeftX, (int)arrowLeftY, (int)arrowForwardX, (int)arrowForwardY);
       g.drawLine((int)arrowLeftX, (int)arrowLeftY, (int)centrePointX, (int)centrePointY);

     }

     if(j==nConn-1)
     {
       g.drawLine((int)arrowRightX, (int)arrowRightY, (int)arrowForwardX, (int)arrowForwardY);
       g.drawLine((int)arrowRightX, (int)arrowRightY, (int)centrePointX, (int)centrePointY);

     }
     startX+=space*Math.cos(ang);
     startY+=space*Math.sin(ang);

     endX+=space*Math.cos(ang);
     endY+=space*Math.sin(ang);

    }    
   }
 }

 public void paintDrag(Graphics g, Point dragPt)
 {

    if(attatched.isTerminating())
     return;

    int nConn = attatched.getSignalWidth();


    int startX = bounds.x+bounds.width/2-((int)nConn*(int)connSpace)/2;
    int startY = bounds.y+bounds.height/2;

    int endX = dragPt.x-((int)nConn*(int)connSpace)/2;
    int endY = dragPt.y;
    g.setColor(Color.gray);
    if(nConn<1)
	nConn=1;
    for(int j=0;j<nConn;j++)
    {     
     g.drawLine((int)startX, (int)startY, (int)endX, (int)endY);
     startX+=connSpace;
     endX+=connSpace;
    }    
 }


}
