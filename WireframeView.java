import sigil.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class WireframeView extends SProcessorModel
{

 public void connectionChange()
 {

 }

    private static final int distance = 200;
    private static final double radConv = Math.PI/180.0;
    private int lastRoll, lastPitch, lastYaw;
    private transient WireframeDisplay wireframe;
    private transient Box3D sensor;
    private boolean wireframeView = true;
   

 public void showInterface()
 {
     
     JFrame jf = new JFrame();
     makeObject();
     wireframe = new WireframeDisplay();
     wireframe.addMouseListener(new ClickListener());
     jf.setTitle(getName());
     jf.setSize(400,400);
     jf.getContentPane().add(wireframe);
     jf.show();
     
 }

    private class ClickListener extends MouseAdapter
    {
	public void mouseReleased(MouseEvent me)
	{
	    wireframeView = !wireframeView;
	    if(wireframe!=null)
		wireframe.repaint();
	}

    }
    
 private class WireframeDisplay extends JPanel
 {

 public void paint(Graphics g)
     {
	 ((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	 Dimension dSize = getSize();
	 g.setColor(Color.black);
	 g.fillRect(0,0,dSize.width,dSize.height);
	 g.setColor(Color.white);
	 Quad3D [] quadsToDraw = sensor.toQuads();
	 for(int i=0;i<6;i++)
	     {
		 Point3D rV1 = Utils3D.rotate3D(quadsToDraw[i].v1, lastRoll*radConv, 
						lastPitch*radConv, lastYaw*radConv);
		 Point3D rV2 = Utils3D.rotate3D(quadsToDraw[i].v2, lastRoll*radConv, 
						lastPitch*radConv, lastYaw*radConv);
		 Point3D rV3 = Utils3D.rotate3D(quadsToDraw[i].v3, lastRoll*radConv, 
						lastPitch*radConv, lastYaw*radConv);
		 Point3D rV4 = Utils3D.rotate3D(quadsToDraw[i].v4, lastRoll*radConv, 
						lastPitch*radConv, lastYaw*radConv);
		 
		 Point drawV1 = Utils3D.project(rV1, dSize.width, distance);
		 Point drawV2 = Utils3D.project(rV2, dSize.width, distance);
		 Point drawV3 = Utils3D.project(rV3, dSize.width, distance);
		 Point drawV4 = Utils3D.project(rV4, dSize.width, distance);
		 Polygon quadView = new Polygon();
		 quadView.addPoint(drawV1.x, drawV1.y);
		 quadView.addPoint(drawV2.x, drawV2.y);
		 quadView.addPoint(drawV3.x, drawV3.y);
		 quadView.addPoint(drawV4.x, drawV4.y);
		 
		 g.setColor(new Color(i*10, 0, i*40));
		 if(wireframeView)
		     g.drawPolygon(quadView);
		 else
		     g.fillPolygon(quadView);
		 if(i==0)
		     {
			 g.setColor(Color.white);
			 g.drawString("Calibrate", drawV1.x, drawV1.y);
		     }
	     }
     }
 }

    
 public WireframeView()
 {
  super();
  setTerminating(true);
 }

 private void makeObject()
 {
     Point3D v1 = new Point3D(-50, -10, -10);
     Point3D v2 = new Point3D(50, 10, 10);
     sensor = new Box3D(v1, v2);
 }

 public void deleted()
 {
 }

 
 public void processSignal()
 {
   GestureSignal sig = getLastSig();      
   if(sig.vals.length>=2 && getInputWidth()>0)
   {
       active = true;
       lastRoll = (int)(sig.vals[0]);
       lastPitch = (int)(sig.vals[1]);
       if(sig.vals.length>=3)
	   lastYaw = (int)(sig.vals[2]);
       if(wireframe!=null)
	   wireframe.repaint();
   }
 }

}
