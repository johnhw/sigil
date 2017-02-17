
package sigil;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/**
 * Displays textual information in a transparent panel
 * Uses a field/value format.
 *
 * @author John Williamson
 */
public class InfoDisplay
{

    //Default size
    private static final int defWidth = 300;
    private static final int defHeight = 260;
    
    private Rectangle viewArea;
    private SignalCanvas parent;
    
    //Listener for mouse events
    private InfoListen dListen;
    
    //The area of the close button
    private Rectangle closeButton;
    
    private Vector fields, values;

    private static final Color boxColor = new Color(100,50,50,100);
    private static final Color titleColor = new Color(120,120,120,150);
    private static final Color headingColor = new Color(200, 200, 200);
    private static final Color textColor = Color.white;

    private String title;



    /**
     * Mouse handler, handles dragging the frame and clicking on items
     */
    private class InfoListen extends MouseAdapter implements MouseMotionListener
  {
      private int startX, startY;
      private int oldX, oldY;
      

      public void mouseClicked(MouseEvent me)
      {
	  
      }
      

      public void mousePressed(MouseEvent me)
      {
	  //Close if click outside
	  if(!viewArea.contains(me.getX(), me.getY()) || 
	     closeButton.contains(me.getX(), me.getY()))
	      closeInfo();

	  
	  //Get mouse position for dragging
	  startX = me.getX();
	  startY = me.getY();
	  oldX = viewArea.x;
	  oldY = viewArea.y;
	  
	

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
    public void closeInfo()
    {
	parent.removeMouseListener(dListen);
	parent.removeMouseMotionListener(dListen);
	parent.closeInfo();
    }


      /**
       * Construct a new device window
       *
       * @param parent The SignalCanvas this is connected to
       * @param cx  The x position of the window
       * @param cy  The y position of the window
       */
  public InfoDisplay(SignalCanvas parent, int cx, int cy, Vector fields, Vector values, String title)
  {
    this.parent = parent;    
    this.fields = fields;
    this.values = values;
    this.title = title;
    dListen = new InfoListen();
    parent.addMouseListener(dListen);
    parent.addMouseMotionListener(dListen); 
    
    viewArea = new Rectangle(cx-defWidth/2, cy-defHeight/2,
                             defWidth, defHeight);

  }


    /**
     * Draw the given text in the given rectangle,
     * wrapping as required
     */
     private void drawTextInBox(Graphics g, String str,  int x, int y, int width, int height)
    {

	Shape tempR = g.getClip();
	//g.setClip(new Rectangle(x,y,width,height));
       
	FontMetrics fMet = g.getFontMetrics();
	String curLine = "";
	int curPos = 0;
	int lineHeight = fMet.getHeight()+fMet.getAscent()+fMet.getDescent();
	int currentY = y+lineHeight;
	
	while(curPos<str.length())
	    {
		curLine = "";
                while(fMet.stringWidth(curLine)<width-10 && curPos<str.length())
		    {
                      if(str.charAt(curPos)!='\n')                        
                          curLine=curLine+str.charAt(curPos);              
			curPos++;
		    }
		g.drawString(curLine, x, currentY);
		currentY+=lineHeight/2;
	    }
	
	g.setClip(tempR); 
    }


      
      /**
       * Paint each of the field value pairs onto 
       * the window, each field having a line and then
       * a box with the value text inside.
       *
       */
      private void drawFieldInfo(Graphics g)
      {
	  int xInset = 20;
	  int yInset = 20;
	  int closeButtonSize = 20;
	  

	  closeButton = new Rectangle(viewArea.x+viewArea.width-closeButtonSize, 
				      viewArea.y,
				      closeButtonSize, 
				      closeButtonSize);
	  g.setColor(titleColor);
	  g.fillRect(viewArea.x, viewArea.y,
		     viewArea.width, closeButtonSize);
	  g.fillRect(closeButton.x, closeButton.y,
		     closeButton.width, closeButton.height);
	  g.setColor(Color.black);
	  g.drawLine(closeButton.x, closeButton.y, 
		     closeButton.x+closeButton.width,
		     closeButton.y+closeButton.height);
	  g.drawLine(closeButton.x+closeButton.width, closeButton.y, 
		     closeButton.x,
		     closeButton.y+closeButton.height);
	  g.setColor(Color.white);
	  g.setFont(new Font("SansSerif", Font.PLAIN, 14));
	  g.drawString(title, viewArea.x+10, viewArea.y+closeButtonSize-5);

	  g.setFont(new Font("SansSerif", Font.PLAIN, 12));
	  FontMetrics fMet = g.getFontMetrics();
	  int lineHeight = fMet.getHeight()+fMet.getAscent()+fMet.getDescent()-3;
	  int currentY = viewArea.y+yInset+lineHeight;
	  if(fields!=null && values!=null && fields.size()==values.size())
	      {
		  for(int i=0;i<fields.size();i++)
		      {
			  String heading = fields.get(i).toString();
			  String text = values.get(i).toString();

			  g.setColor(headingColor);
			  g.drawString(heading, viewArea.x+xInset, currentY);
			  currentY+=lineHeight*2-6;
			  int nLines = g.getFontMetrics().stringWidth(text)/(viewArea.width-xInset*2);
			  g.setColor(textColor);
			  drawTextInBox(g, text, viewArea.x+xInset, currentY-2*lineHeight-lineHeight/2,
					  viewArea.width-xInset*2, nLines*lineHeight);
			  currentY+=(nLines-1)*lineHeight+4;
		      }
	      }


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
	    shade = new Color(80,80,80,188);
	else
	    shade = new Color(80,80,80);
	    
	g.setColor(shade);
	g.fillRect(viewArea.x, viewArea.y, viewArea.width, viewArea.height);

	//Draw an outline
	if(SignalSystem.useAlpha)
	    g.setColor(new Color(120,120,120,150));
	else
	    g.setColor(new Color(120,120,120));
	g.drawRect(viewArea.x, viewArea.y, viewArea.width, viewArea.height);

	//Draw the labels inside the rectangle
	Shape tempR = g.getClip();
	g.setClip(viewArea);
	drawFieldInfo(g);
	g.setColor(temp);
	g.setClip(tempR); 

  }




}
