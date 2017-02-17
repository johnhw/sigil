import sigil.MouseUtils;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

 class StateVisualisation extends JPanel
 {
     private double yaw, pitch;
     private int divs, scale=20, currentTransition;
     private int distance = 180;
     private int [] [] codeTable;
     private static final Color edgeColor = new Color(90,90,90);
     private static final Color sphereColor = new Color(180,47,40);
     private static final Color darkSphereColor = new Color(50,57,150);
     private static final Color terminateColor = new Color(170,47,170);
     private static final Color overStateColor = new Color(190,40,40);
    
    public void setDivs(int divs)
    {
     this.divs = divs+1;
     repaint();
    }


    public StateVisualisation()
    {
        SpinListener sListener = new SpinListener();
	
        addMouseListener(sListener);
        addMouseMotionListener(sListener);
    }

    public void setTransition(int trans)
    {
     currentTransition = trans;

     repaint();
    }

    public void setCodeTable(int [] [] codeTable)
    {
      this.codeTable = codeTable;
      repaint();
    }

    private class SpinListener extends MouseAdapter implements MouseMotionListener 
    {
	private boolean dragging = false;
        private int startY, startX, oldDistance;
	private double oldPitch, oldYaw;
        private boolean rightDrag;
	
	public void mousePressed(MouseEvent me)
	{
	    if(!dragging)
		{
                    rightDrag = MouseUtils.right(me);                       
		    dragging = true;
		    oldPitch = pitch;
                    oldDistance = distance;
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
                  if(rightDrag)
                  {
                     distance = oldDistance+(me.getY()-startY);
                     if(distance<60)
                       distance = 60;
                  }
                  else
                  {
		    pitch = oldPitch+((me.getY()-startY)/80.0);
		    yaw = oldYaw+((me.getX()-startX)/80.0);
                  }
                    repaint();
		}
	}
	
	public void mouseMoved(MouseEvent me)
	{
	    
	}
		
    }



     private void drawSphere(Graphics g, Point3D pt, int size,
                          int distance, int yOff, int width, Color col)
     {
         Point drawPt = Utils3D.project(pt, width, distance);

         g.setColor(col.darker());
         g.fillOval(drawPt.x-(size>>1)+1, drawPt.y-(size>>1)+yOff+1, size, size);
         g.setColor(col);
         g.fillOval(drawPt.x-(size>>1), drawPt.y-(size>>1)+yOff, size, size);
         g.setColor(Color.white);
         g.fillOval((drawPt.x-(size>>1))+2, (drawPt.y-(size>>1))+yOff+2, 1, 1);

     }

     private void drawCube(Graphics g, int x, int y, int z, int size,
                          int distance, int yOff, int width, Color col,
                          boolean dX, boolean dY, boolean dZ)
     {
         Point3D newPt = new Point3D(x,y,z);
         Point3D rotatePt = Utils3D.rotate3D(newPt, 0, pitch, yaw);
         g.setColor(col);

         Point drawPt = Utils3D.project(rotatePt, width, distance);
         Point newDrawPt;
        if(dX)
        {
         newPt = new Point3D(x+size, y, z);
         rotatePt = Utils3D.rotate3D(newPt, 0, pitch, yaw);
         newDrawPt = Utils3D.project(rotatePt, width, distance);
         g.drawLine(drawPt.x, drawPt.y+yOff, newDrawPt.x, newDrawPt.y+yOff);
        }
        if(dY)
        {
         newPt = new Point3D(x, y+size, z);
         rotatePt = Utils3D.rotate3D(newPt, 0, pitch, yaw);
         newDrawPt = Utils3D.project(rotatePt, width, distance);
         g.drawLine(drawPt.x, drawPt.y+yOff, newDrawPt.x, newDrawPt.y+yOff);
        }
        if(dZ)
        {
         newPt = new Point3D(x, y, z+size);
         rotatePt = Utils3D.rotate3D(newPt, 0, pitch, yaw);
         newDrawPt = Utils3D.project(rotatePt, width, distance);
         g.drawLine(drawPt.x, drawPt.y+yOff, newDrawPt.x, newDrawPt.y+yOff);
        }

     }


     public void paint(Graphics g)
     {
      ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Dimension dSize = getSize();
      g.setColor(Color.black);
      g.fillRect(0,0,dSize.width,dSize.height);

      int offset = (scale*(divs-1))/2;

      Vector sphereList = new Vector();
      int count = 0;

      for(int i=0;i<divs;i++)
       for(int j=0;j<divs;j++)
        for(int k=0;k<divs;k++)
        {
         int x = (i*scale) - offset;
         int y = (j*scale) - offset;
         int z = (k*scale) - offset;

         boolean drawX=true, drawY=true, drawZ=true;
         drawX = (i!=divs-1);
         drawY = (j!=divs-1);
         drawZ = (k!=divs-1);

         if(drawX && drawY && drawZ)
         {
          Point3D newPt = new Point3D(x+scale/2,y+scale/2,z+scale/2);
          Point3D rotatePt = Utils3D.rotate3D(newPt, 0, pitch, yaw);
          TaggedPoint tagPt = new TaggedPoint(rotatePt, count);
          sphereList.add(tagPt);
          count++;
         }
         
         drawCube(g, x, y, z, scale, distance, -80, dSize.width, edgeColor, drawX, drawY, drawZ);
        }
      

     Collections.sort(sphereList);
     Iterator iter = sphereList.iterator();
     while(iter.hasNext())
     {
          TaggedPoint tPt = (TaggedPoint)iter.next();
            int radius = 10;
            Color useColor = sphereColor;
            count = tPt.tag;

            if(count==currentTransition)
            {
               radius = 15;
	       if(codeTable!=null && codeTable[count][0]!=0)
		   useColor = overStateColor;
	       else
		   useColor = Color.white;
            }
            if(codeTable==null)
              useColor = darkSphereColor;
            else
            {
             if(count>=codeTable.length || (codeTable[count][0]==0 &&
                                            count!=currentTransition))
               useColor = darkSphereColor;
             if(count<codeTable.length && codeTable[count][2]==0)
             {
               useColor = terminateColor;
               radius = 15;
             }
            }

            drawSphere(g, tPt.pt, radius, distance, -80, dSize.width, useColor);
     }
  }
 }
