
package sigil;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * Graphical selector for signal devices
 * Provides a transparent list
 *
 * @author John Williamson
 */
public class DeviceSelector
{

    //Default size
    private static final int defWidth = 230;
    private static final int defHeight = 450;

    //Inset
    private static final int arrowOff = 10;
    
    private Rectangle viewArea;
    private SignalCanvas parent;
    private Hashtable nameHash;
    private Hashtable groupHash;
    private Hashtable groupExpanded;

    //Current scroll position
    private int maxList;

    //Listener for mouse events
    private DevListen dListen;

    //Thread for smooth scrolling
    private Thread scrollThread;

    //True if currently scrolling
    private boolean scrolling;

    private int smoothScrollPos;
    private int maxDevices = 17;


    /**
     * Sets the expanded property of the given group
     */
    private void setExpanded(String groupName, boolean expanded)
    {
	groupExpanded.put(groupName, new Boolean(expanded));
    }

    /**
     * Returns true if the given group is currently expanded
     */
    private boolean isExpanded(String groupName)
    {
	Boolean expanded = ((Boolean)(groupExpanded.get(groupName)));
	if(expanded!=null)
	    return expanded.booleanValue();
	else
	    return false;
    }

    /**
     * Thread which handles continuous scrolling
     */
    private class ScrollThread extends Thread
    {
	private int sign;
	
	public ScrollThread(int sign)
	{
	    super();
	    this.sign = sign;
	}

	public void run()
	{
	    double scrollInc=1.21;
	    while(scrolling && ((sign<0 && smoothScrollPos>0) || (sign>0 && maxList>(maxDevices-1))))
		{
		    try{Thread.sleep(10);} catch(InterruptedException ie) {}
		    smoothScrollPos+=(int)(scrollInc*sign);
		    scrollInc+=0.04;
		    parent.repaint();
		}
	}

    }

    /**
     * Mouse handler, handles dragging the frame and clicking on items
     */
    private class DevListen extends MouseAdapter implements MouseMotionListener
    {
	private int startX, startY;
	private int oldX, oldY;
      

      public void mouseClicked(MouseEvent me)
      {
	  
      }
      
	private void checkName(int x, int y)
	{
	    
	  //Loop through each name/rectangle hash entry
	  Enumeration keyEnum = nameHash.keys();
	  while(keyEnum.hasMoreElements())
	      {
		  //Get the name and rectangle
		  String nextName = (String)(keyEnum.nextElement());
		  Rectangle rect = (Rectangle)(nameHash.get(nextName));

		  //Check if the mouse is in this rectangle
		  if(rect!=null && rect.contains(x,y))
		      {
			 
			  //If it's an arrow at each end, handle scrolling
			  if(nextName.endsWith("_ARROW"))
			      {
				  if(nextName.startsWith("__UP") && smoothScrollPos>0) 
				      {
					  scrollThread = new ScrollThread(-1);
					  scrolling = true;
					  scrollThread.start();
				      }

				      else
                                      if(nextName.startsWith("__DOWN") && 
					 maxList>16)
					  {
					      scrollThread = new ScrollThread(1);
					      scrolling = true;
					      scrollThread.start();
					  }
				  
				  parent.repaint();
			      }
			  else
			      {
				  if(!(nextName.equals("Cancel")))
                                  {
				      //Add the component to the main canvas                                      
				      GestureElement newElement = ModuleLoader.createElement(nextName);
				      if(newElement!=null)
					  parent.add(new VisualElement(newElement,x,y));
				      else
					  {
					      JOptionPane.showMessageDialog(null, "Could not instatiate an instance of "+nextName, 
									    "Instatiation failure", 
									    JOptionPane.ERROR_MESSAGE);
					      return;
					  }
                                  }
				  closeDevices();
			      }
			  return;
		      }
		  
	      }

	}


	private boolean checkGroup(int x, int y)
	{
	    //Check for clicks on groups
	    Enumeration groupEnum = groupHash.keys();
	    while(groupEnum.hasMoreElements())
		{
		    //Get the name and rectangle
		    String nextName = (String)(groupEnum.nextElement());
		    Rectangle rect = (Rectangle)(groupHash.get(nextName));
		    
		    //Check if the mouse is in this rectangle
		    if(rect!=null && rect.contains(x,y))
			{
			    setExpanded(nextName, !isExpanded(nextName));
			    return true;
			}
			
		}
	    return false;
	}
     
      public void mousePressed(MouseEvent me)
      {
	  //Close if click outside
	  if(!viewArea.contains(me.getX(), me.getY()))
	      closeDevices();
	  
	  //Get mouse position for dragging
	  startX = me.getX();
	  startY = me.getY();
	  oldX = viewArea.x;
	  oldY = viewArea.y;
	  
	  //Do nothing if not LMB or not yet initialised.
          if(nameHash==null || groupHash==null || !MouseUtils.left(me))
	      return;

	  boolean groupClicked = checkGroup(me.getX(), me.getY());

	  if(!groupClicked)
	      checkName(me.getX(), me.getY());
	 
      }

	public void mouseReleased(MouseEvent me)
	{
	    scrolling = false;
	}
      
      
      public void mouseMoved(MouseEvent me)
      {
	  
      }
      

      //Drag the window around
      public void mouseDragged(MouseEvent me)
      {
	  
	  viewArea.x = oldX+(me.getX()-startX);
	  viewArea.y = oldY+(me.getY()-startY);
	  parent.repaint();
      }

  }

    /**
     * Return true if the specified point is in the window
     *
     * @return true if point was over
     */
    public boolean isOver(int x, int y)
    {
	return viewArea.contains(x,y);
    }

    
    /**
     * Close the window
     */
    public void closeDevices()
    {
	parent.removeMouseListener(dListen);
	parent.removeMouseMotionListener(dListen);
	parent.closeDevice();
    }


    /**
     * Construct a new device window
     *
     * @param parent The SignalCanvas this is connected to
     * @param cx  The x position of the window
     * @param cy  The y position of the window
     */
  public DeviceSelector(SignalCanvas parent, int cx, int cy)
  {
    this.parent = parent;    
    groupExpanded = new Hashtable();
    dListen = new DevListen();
    parent.addMouseListener(dListen);
    parent.addMouseMotionListener(dListen); 
    
    viewArea = new Rectangle(cx-defWidth/2, cy-defHeight/2,
                             defWidth, defHeight);
  }

    /**
     * Draw a triangle fitting in the given rectangle, facing
     * up or down as specified by the last parameter
     */
    private void fillTriangle(Graphics g, int x, int y, int width, int height, boolean up)
    {

	Polygon triangle = new Polygon();
	if(!up)
	    {
		triangle.addPoint(x,y);
		triangle.addPoint(x+width,y);
		triangle.addPoint(x+width/2,y+height);        
	    }
        else
	    {
		triangle.addPoint(x,y+height);
		triangle.addPoint(x+width,y+height);
		triangle.addPoint(x+width/2,y);        
	    }
	g.fillPolygon(triangle);
    }

    /**
     * Draw an arrow for each end of the list
     * takes a bounding rectangle, and a boolean that is true
     * if the arrow should point upwards
     */
    private void drawArrow(Graphics g, int x, int y, int width, int height, boolean up)
    {  
	if(SignalSystem.useAlpha)
	    g.setColor(new Color(80,80,80,100));
	else
	    g.setColor(new Color(40,40,40));
	
	//Draw outside rectangle
	g.fillRect(x, y,
		   width, height);
	
	g.drawRect(x, y,
                  width, height);

	//Draw the triangle
	Polygon triangle = new Polygon();
	int woff = width/4;
	g.setColor(Color.black);
	fillTriangle(g, x+woff/2, y+3, width-woff, height-6, up);
  }

  /**
   * Draw one label onto the graphics, filling the given area, and
   * writing the text into it using the specified color, font type and size
   * and offset
   */
    private void drawLabel(Graphics g, Rectangle area, String text, int xOff, 
			   int yOff, int fontType, int ptSize, boolean arrow,
			   boolean arrowUp)
  {

      //Draw the background
      g.fillRect(area.x, area.y, area.width, area.height);             
      g.drawRect(area.x, area.y, area.width, area.height);
      g.setFont(new Font("SansSerif", fontType, ptSize));
      
      //Draw the text
      g.setColor(Color.white);
      g.drawString(text, area.x+xOff, area.y+yOff);                                   
      
      //Draw an expansion arrow if required
      if(arrow)
	  {
	      g.setColor(new Color(38,74,98));
	      fillTriangle(g, area.x+4, area.y+5, 12, area.height-10, arrowUp);
	  }
  }


    /**
     * Draw each of the labels in the list 
     */
    private void drawLabels(Graphics g)
    {
	nameHash = new Hashtable();

	groupHash = new Hashtable();

	int curMaxList = 0;

	//List of groups
	Enumeration groups = ModuleLoader.getGroupNames();

	//Offsets for drawing each list element
	int xInset = 20;
	int yInset = 25;
	int yHeight = 19;	
	int yGap = 1;
	
	int currentY = yInset;
	int maxHeight = viewArea.height-yInset-3*yHeight-yGap;

	if(smoothScrollPos>0)
	    {
		//Draw the up arrow
		drawArrow(g, viewArea.x+xInset,
			  viewArea.y+currentY,
			  viewArea.width-xInset*2, yHeight, true);
		nameHash.put("__UP_ARROW", new Rectangle(viewArea.x+xInset, viewArea.y+currentY,
							 viewArea.width-xInset*2, yHeight));
	    }

	
	
	currentY+=yHeight;

	int topList = currentY;

	Color groupCol, eltCol;
	if(SignalSystem.useAlpha)
	    {
		groupCol = new Color(38,74,98,100);
		eltCol = new Color(71,82,133,100);
	    }
	else
	    {
		groupCol = new Color(18,35,48);
		eltCol = new Color(30,40,70);
	    }

	int curPos = 0;

	boolean firstDevice = true;
	int height;
	//For each generator that fits in the window
	while(groups.hasMoreElements() && currentY<maxHeight)
	    {
		String groupName = (String)(groups.nextElement());
		boolean expanded = isExpanded(groupName);
		
		height = yHeight;
		
		if(curPos*yHeight>=smoothScrollPos)
		    {
			g.setColor(groupCol);
			curMaxList++;
			
			Rectangle rect = new Rectangle(viewArea.x+xInset, viewArea.y+currentY,
                                                       viewArea.width-xInset*2, height-yGap);

			if(height!=yHeight)
			    groupName = "";
			else
			    //Update the position rectangle hashtable
			    groupHash.put(groupName, rect);

                        drawLabel(g, rect, groupName, 22, height-5, Font.PLAIN, 12, true, 
				  expanded);


			//Update the position
			currentY+=height;
			firstDevice = false;
		    }
		curPos++;

		//Draw the group elements, if the group is expanded
		if(expanded)
			    {
				Vector deviceNames = ModuleLoader.getGroupDevices(groupName);
				if(deviceNames!=null)
				    {
					int curIndex = 0;

					//Draw each device label
					while(curIndex<deviceNames.size() && currentY<maxHeight)
					    {
						height = yHeight;
						if(curPos*yHeight>=smoothScrollPos)
						    {

							String name = (String)(deviceNames.get(curIndex));
							g.setColor(eltCol);
						
							//Draw the label
							Rectangle rect = new Rectangle(viewArea.x+xInset, 
									     viewArea.y+currentY,
									     viewArea.width-xInset*2,
									     height-yGap);

							if(height!=yHeight)
							    name = "";
							else
							    //Update the position rectangle hashtable
							nameHash.put(name, rect);
							
							drawLabel(g, rect, name, 8, height-5, 
								  Font.PLAIN, 10, false, false);
							

							
							//Update the position
							currentY+=height;
							firstDevice = false;
							curMaxList++;
						    }
						curPos++;
						curIndex++;
					    }

				    }
			    }

     }

	//Gap between generator and processor list
	currentY += 5;

	maxList = curMaxList;
	if(maxList<maxDevices)
	    currentY+=yHeight*(maxDevices-maxList);

	if(maxList>=maxDevices-1)
	    {
		//Draw the down arrow
		drawArrow(g, viewArea.x+xInset,
			  viewArea.y+currentY,
			  viewArea.width-xInset*2, yHeight, false);
		nameHash.put("__DOWN_ARROW", new Rectangle(viewArea.x+xInset, viewArea.y+currentY,
							   viewArea.width-xInset*2, yHeight));
	    }
     currentY += yHeight+yGap;

     currentY+=5;

     //Draw the cancel button
     //with a red background
       if(SignalSystem.useAlpha)
               g.setColor(new Color(200,0,0,100));
        else
               g.setColor(new Color(200,0,0));



       Rectangle rect = new Rectangle(viewArea.x+xInset, viewArea.y+currentY,
                                      viewArea.width-xInset*2, yHeight-yGap);

       nameHash.put("Cancel", rect);
       drawLabel(g, rect, "Cancel", arrowOff, yHeight-5, Font.PLAIN, 10, false, false);
       currentY+=yHeight;


  }

    /**
     * Paint this window
     */
    public void paint(Graphics g)
    {
	Color temp = g.getColor();
	Color shade;

	//shade the background
	if(SignalSystem.useAlpha)
	    shade = new Color(40,40,40,188);
	else
	    shade = new Color(40,40,40);
	    
	g.setColor(shade);
	g.fillRect(viewArea.x, viewArea.y, viewArea.width, viewArea.height);

	//Draw an outline
	if(SignalSystem.useAlpha)
	    g.setColor(new Color(120,120,120,188));
	else
	    g.setColor(new Color(120,120,120));
	g.drawRect(viewArea.x, viewArea.y, viewArea.width, viewArea.height);

	//Draw the labels inside the rectangle
	Shape tempR = g.getClip();
	g.setClip(viewArea);
	drawLabels(g);
	g.setColor(temp);
	g.setClip(tempR); 

  }




}
