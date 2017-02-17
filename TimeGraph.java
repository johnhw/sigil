import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TimeGraph extends SProcessorModel
{

 private LinkedList timeQueue;
 private int maxQueueSize = 100;
 private double yScale = 1.1;
 private JFrame gFrame;

 static final long serialVersionUID = 213L;
 public String getGenName()
 {
  return "TimeGraph";
 }
 public String getDescription()
 {
  return "Provides a scalable, scrolling plot of incoming signals";
 }
 public String getDate()
 {
  return "Janurary 2002";
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
   timeQueue = new LinkedList();
 }


 public void showInterface()
 {
  gFrame.setTitle(getName());  
  gFrame.show();
 }

 private class ZoomListener extends MouseAdapter implements MouseMotionListener 
 {
  private boolean dragging = false;
  private int startY;
  private double oldYScale;

  public void mousePressed(MouseEvent me)
  {
   if(!dragging)
   {
     dragging = true;
     oldYScale = yScale;
     startY = me.getY();
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
     yScale = oldYScale + ((startY-me.getY())/50.0);
     if(yScale<1.1)
      yScale = 1.1;
     gFrame.repaint();
   }

  }

  public void mouseMoved(MouseEvent me)
  {

  }


 }

 private class mPanel extends JPanel
 {
 transient Image dblBuffer;
 
 private void drawAxis(Graphics g, Dimension dSize)
 {
   g.setColor(new Color(12,118,36));
   int axisCentre = dSize.width-10;
   g.drawLine(axisCentre, 10, axisCentre, dSize.height-10);
   g.drawLine(axisCentre-20, 10, axisCentre, 10);
   g.drawLine(axisCentre-20, dSize.height-10, axisCentre, dSize.height-10);

   double scalVal = Math.pow(10, Math.round(Math.log(yScale)/Math.log(10)));

   double minTick = scalVal*10;
   double majTick = minTick*10;


   double maxVal = (dSize.height-20)*(yScale/2);
   g.setColor(g.getColor().brighter());

   for(double i=0;i>-maxVal;i-=minTick)
   {
    int yVal = (int)(i/yScale)+dSize.height/2;
    g.drawLine(axisCentre-5, yVal, axisCentre-1, yVal);
   }

   for(double i=0;i<maxVal;i+=minTick)
   {
    int yVal = (int)(i/yScale)+dSize.height/2;
    g.drawLine(axisCentre-5, yVal, axisCentre-1, yVal);
   }

   g.setColor(g.getColor().brighter());

   for(double i=0;i>-maxVal;i-=majTick)
   {
    int yVal = (int)(i/yScale)+dSize.height/2;
    g.drawLine(axisCentre-10, yVal, axisCentre-1, yVal);
   }

   for(double i=0;i<maxVal;i+=majTick)
   {
    int yVal = (int)(i/yScale)+dSize.height/2;
    g.drawLine(axisCentre-10, yVal, axisCentre-1, yVal);
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

   drawAxis(g, dSize);

   LinkedList temp = (LinkedList)(timeQueue.clone());
   Iterator iter = temp.iterator();
   GestureSignal oldSig=null;
   long startTime=0;
   int oldTime=0;
   int cTime = 0;
   while(iter.hasNext())
   {
    GestureSignal gSig = (GestureSignal)(iter.next());
     if(oldSig!=null && (oldSig.vals.length == gSig.vals.length))
     {
    for(int i=0;i<gSig.vals.length;i++)
    {

     g.setColor(colTable[i % colTable.length]);
       g.drawLine(oldTime,(int)(-oldSig.vals[i]/yScale+dSize.height/2),
                cTime, (int)(-gSig.vals[i]/yScale+dSize.height/2));
    }
     oldTime = cTime;
     cTime++;
      }

     oldSig = gSig;   
   }
  gr.drawImage(dblBuffer,0,0,this);
 }


 }

 public TimeGraph()
 {
  super();
  setTerminating(true);
  gFrame = new JFrame();
  gFrame.setTitle(getName());
  gFrame.getContentPane().add(new mPanel());
  timeQueue = new LinkedList();
  gFrame.setSize(400,420);
  ZoomListener zl = new ZoomListener();
  gFrame.addMouseListener(zl);
  gFrame.addMouseMotionListener(zl);

  gFrame.setResizable(false);
 }


 public void processSignal()
 {

     GestureSignal oneSig = lastSig;
     if(timeQueue.size()>maxQueueSize)
      timeQueue.removeFirst();

     timeQueue.addLast(oneSig);
     setCurSig(oneSig);
     gFrame.repaint();
 }

}
