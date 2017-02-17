import sigil.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;

/**
 * A view for matrix elements. Uses a fisheye lens to view the matrix 
 *
 * @author John Williamson
 */
public class MatrixView extends JPanel implements java.io.Serializable
{

    //The fisheye lens
    private static MouseLens ml;
    
    //The matrix being drawn
    double [] [] matrix;

    //Mouse handler variables
    private boolean mouseDown = false;
    private int mX, mY;
    
    //The scale factor as the mouse is dragged
    private double inc = 0.01;
    private static final double defInc = 0.01;

    //The size of the view
    int width, height;

    //The size of each matrix element on the view
    int eltWidth, eltHeight;

    //Format for displaying labels
    private static final DecimalFormat dpTwo = new DecimalFormat("0.00");

    /**
     * FileFilter for .mat files
     */
    private  class MATFilter extends javax.swing.filechooser.FileFilter
    {
	public boolean accept(File F)
	{
	    return (F.getName().toLowerCase().endsWith(".mat"));
	}
	
	public String getDescription()
	{
	    return "Signal Gesture matrices (*.mat)";    
	}
    }

    /**
     * Write the matrix out to a file, chosen from a file chooser
     * uses standard MATLAB format, doubles separated by spaces and
     * rows delimted by newlines
     */
    private void saveMatrix()
    {
     	JFileChooser jfc = new JFileChooser(".");
	jfc.setFileFilter(new MATFilter());
	
	if(jfc.showSaveDialog(null)==JFileChooser.APPROVE_OPTION)
	    {
		try{
		File selected = jfc.getSelectedFile();
		PrintWriter ps = new PrintWriter(new BufferedOutputStream(new FileOutputStream(selected)));
		ps.println(width+" "+height);
		for(int i=0;i<width;i++) {
		    for(int j=0;j<height;j++) {
			ps.print(matrix[i][j]+" ");
		    }
		    ps.println("");
		}
		ps.flush();
		ps.close();
		}
	catch(IOException IOE)
		    {
			IOE.printStackTrace();
			JOptionPane.showMessageDialog(null, "Load failed", "IO Error", JOptionPane.ERROR);
		    }
		}
	repaint();           
    }

    /**
     * Read in a matrix from a file.
     * uses standard MATLAB format, doubles separated by spaces and
     * rows delimted by newlines
     */
    private void loadMatrix()
    {
	JFileChooser jfc = new JFileChooser(".");
	jfc.setFileFilter(new MATFilter());
	if(jfc.showOpenDialog(null)==JFileChooser.APPROVE_OPTION)
	    {
		try{
		File selected = jfc.getSelectedFile();
		BufferedReader ps = new BufferedReader(new FileReader(selected));
		String headLine = ps.readLine();
		StringTokenizer headTok = new StringTokenizer(headLine);
		int newWidth = Integer.parseInt(headTok.nextToken());
		int newHeight = Integer.parseInt(headTok.nextToken());
		if(newWidth>width)
		    newWidth = width;
		if(newHeight>height)
		    newHeight = height;

		for(int i=0;i<newWidth;i++) {

		    String inLine = ps.readLine();
		    StringTokenizer strTok = new StringTokenizer(inLine);
		    for(int j=0;j<newHeight;j++) {
			matrix[i][j] = Double.parseDouble(strTok.nextToken());
		    }
		    
		}
		}catch(IOException IOE)
		    {
			IOE.printStackTrace();
			JOptionPane.showMessageDialog(null, "Load failed", "IO Error", JOptionPane.ERROR);
		    }
	    }
	repaint();
    }

    /**
     * Reset the matrix values and repaint
     */
    private void resetMatrix()
    {
	for(int i=0;i<width;i++)
	    for(int j=0;j<height;j++)
		{
		    if(i!=j)
			matrix[i][j] = 0.0;
		    else
			matrix[i][j] = 1.0;
		}
		repaint();

    }

    /**
     * Mouse handler
     */
  private class MouseListen extends MouseAdapter implements MouseMotionListener
  {

            
      private boolean dragging = false;
      private int startY;
      private double oldCellValue;
      private int ox, oy;

      public void mouseMoved(MouseEvent me)
      {
      }

      /**
       * If the mouse dragged, the current element is adjusted
       * by an amount determined by the vertical distance from the mouse down point
       * to the release point
       */
      public void mouseDragged(MouseEvent me)
      {
	  if(dragging)
	      {
		  matrix[mX][mY] = oldCellValue + ((startY-me.getY())/100.0);
		  
		  if(matrix[mX][mY]>1.0)
		      matrix[mX][mY]=1.0;
		  
		  if(matrix[mX][mY]<0.0)
		      matrix[mX][mY]=0.0;
		  repaint();
	      }
	  
      }

      /**
       * Start a drag, locking the lens distortion
       */
      public void mousePressed(MouseEvent me)
      {
	  if(eltWidth<1 || eltHeight<1)
	      return;
          
	  
	  if((me.getModifiers()&MouseEvent.BUTTON1_MASK)!=0)
	      {
		  ml.lockDistort();
		  dragging = true;
		  mouseDown = true;
		  mX = me.getX()/eltWidth;
		  mY = me.getY()/eltHeight;
		  startY = me.getY();
		  oldCellValue = matrix[mX][mY];
	      }
      }

      
      /**
       * Bring up the load/save/reset menu at the given point
       */
      public void popupMenu(int x, int y)
      {
	       JPopupMenu jMen = new JPopupMenu();
	       jMen.add(new AbstractAction("Save...")
		   {
		       public void actionPerformed(ActionEvent ae){
			   saveMatrix();
		       }
		   });
	       
	       jMen.add(new AbstractAction("Load...")
		   {
		       public void actionPerformed(ActionEvent ae){
			   loadMatrix();
		       }
		   });


	       jMen.add(new AbstractAction("Reset")
		   {
		       public void actionPerformed(ActionEvent ae){
			   resetMatrix();
		       }
		   });

	       jMen.show(MatrixView.this, x, y);

      }

      /**
       * Handle right clicks to bring up the popup menu
       */
      public void mouseClicked(MouseEvent me)
      {
	  if(me.getClickCount()==1 && (me.getModifiers()&MouseEvent.BUTTON3_MASK)!=0)
	      {
		  popupMenu(me.getX(), me.getY());
	      }
      }

      /**
       * Unlock distortion after a drag
       */
      public void mouseReleased(MouseEvent me)
      {
	  ml.unlockDistort();
	  mouseDown = false;
	  dragging = false;
      }
  }

    /** 
     * Set the matrix being edited. This will cause the given matrix
     * to be DIRECTLY MODIFIED!
     */
    public void setMatrix(double [] [] theMat)
    {
	this.matrix = theMat;
    }

    /**
     * Set the current display height
     */
    public void setHeight(int height)
    {
	this.height = height;
    }

    /**
     * Set the current display width
     */
    public void setWidth(int width)
    {
      this.width = width;
    }

    /** 
     * Create a new matrix view
     */
    public MatrixView()
    {
	ml = new MouseLens(this);
	ml.setLens(40, 1.2);
	ml.setAspect(true);
	MouseListen mListen = new MouseListen();
	addMouseListener(mListen);
	addMouseMotionListener(mListen);
    }
    
    public void update(Graphics g)
    {
	paint(g);
    }
    
    /**
     * Return a color given a matrix value
     */
    public Color getMatrixColor(double matValue)
    {
	Color retCol;
	matValue = 1-matValue;
	if(matValue>=0.0)
	    retCol = new Color((float)matValue, (float)matValue, (float)matValue);
	else
	    retCol = new Color(255,255,0);
	return retCol;
    }

    /**
     * Paint the view
     */
    public void paint(Graphics og)
    {
        int mWidth = width;
        int mHeight = height;

	//Check view is valid
        if(matrix==null || width==0 || height==0)
	    return;
        
	Dimension dSize = getSize();
        Graphics gr = og;

	//Clear
	gr.setColor(Color.black);
	gr.fillRect(0,0,dSize.width, dSize.height);

	//Setup the distorted graphics
        DGraphics g = new DGraphics(ml,gr, true);
        eltWidth = dSize.width/mWidth;
        eltHeight = dSize.height/mHeight;
        g.setSemanticZoom(-50.0);

	//For each element
        for(int i=0;i<mWidth;i++)
              for(int j=0;j<mHeight;j++)
		      {
			  //Get a color and draw the element
			  g.setColor(getMatrixColor(matrix[i][j]));
			  
			  g.fillRect(i*eltWidth, j*eltHeight,
				     (eltWidth), (eltHeight));
			  g.setColor(new Color(100,120,100));
			  g.drawRect(i*eltWidth, j*eltHeight,
				     (eltWidth), (eltHeight));

                       }

	//Draw an outline around the current element
	//if the mouse is being dragged
	if(mouseDown)
	    {
		g.setFont(new Font("SansSerif", Font.PLAIN, 7));
		FontMetrics fMet = g.getFontMetrics();
		int i = mX, j = mY;
		
		int sHeight = fMet.getHeight();
		String nString = dpTwo.format(matrix[i][j]);
		int sWidth = fMet.stringWidth(nString);
		
		g.setColor(new Color(150,220,150));
		g.drawRect(i*eltWidth+1, j*eltHeight+1,
			   (eltWidth-2), (eltHeight-2));
		
		ml.enablePlanarMode(i*eltWidth+eltWidth/2-10, j*eltHeight+eltHeight/2-4, sWidth+20, sHeight+8);
		
		g.setColor(new Color(80,120,80,190));
		g.fillRect(i*eltWidth+eltWidth/2-10, j*eltHeight+eltHeight/2-sHeight+2,
			   sWidth+20,sHeight);
		g.setColor(Color.darkGray);
		g.drawRect(i*eltWidth+eltWidth/2-10, j*eltHeight+eltHeight/2-sHeight+2,
			   sWidth+20,sHeight);
		g.setColor(Color.green);
		g.drawString(nString, i*eltWidth+eltWidth/2, j*eltHeight+eltHeight/2);
		ml.disablePlanarMode();
        }
  }

}
