package sigil;
import java.io.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class FileBrowser extends JPanel
{
    private Hashtable fileRects;
    private File curDir = new File(".");
    private String extension;
    private int fileWidth = 550, fileHeight = 18, xInset = 20, yInset = 100;
    private Color fileColor = new Color(20, 80, 130);
    private Color dirColor = new Color(20, 40, 80);
    private Color cancelColor = new Color(20, 20, 110);
    //The fisheye lens
    private static MouseLens ml;
    private JDialog mainDialog;
    private String returnedFile;
    private Rectangle cancelRect;
    private Rectangle overRect; 
    private File overFile;

    public String getFilename()
    {
	
	return returnedFile;
    }


    private class MoveListener extends MouseMotionAdapter
    {
	public void mouseMoved(MouseEvent me)
	{
	    int x = me.getX();
	    int y = me.getY();
	    
	    overRect = null;
	    overFile = null;
	    try{
		Enumeration files = fileRects.keys();
		while(files.hasMoreElements())
		    {
			File thisFile = (File)(files.nextElement());
			Rectangle testRect = (Rectangle)(fileRects.get(thisFile));
			if(testRect.contains(x,y))
			    {
				overRect = testRect;
				overFile = thisFile;
				break;
			    }
		    }
	    } catch(ConcurrentModificationException cme) {}
	}
    }

    private class ClickListener extends MouseAdapter
    {
	public void mouseReleased(MouseEvent me)
	{
	    int x = me.getX();
	    int y = me.getY();
	    if(cancelRect.contains(x,y))
		{
		    returnedFile = null;
		    mainDialog.dispose();
		}
	    if(overRect!=null)
		{
		    if(overFile.isDirectory())
			{
			    String dirName = curDir.getPath();
			    int lastSlash = dirName.indexOf(File.separator); 
			    if(overFile.getName()=="..")
				if(lastSlash!=-1)
				    curDir = new File(dirName.substring(0, lastSlash)); 
				else
				    curDir = new File(".");
			    else
				curDir = new File(curDir.getPath()+File.separator+
						  overFile.getName());
			    readFiles();
			}
		    else
			{
			    returnedFile = curDir.getPath()+File.separator+
				overFile.getName();
			    mainDialog.dispose();
			}
		    
		 
		}
	}
    }

    public FileBrowser(String extension, String path)
    {
	super();
	mainDialog = new JDialog();
	mainDialog.setModal(true);
	mainDialog.setSize(600, fileHeight*26+yInset*2);
	mainDialog.setTitle("File browser");
        curDir = new File(path);
        this.extension = extension;
	ml = new MouseLens(this);
	ml.setLens(40, 1.2);
	ml.setAspect(true);
	ml.setLinearMode(MouseLens.LINEAR_Y_ONLY);
	addMouseListener(new ClickListener());
	addMouseMotionListener(new MoveListener());
	readFiles();
	mainDialog.getContentPane().add(this);
	mainDialog.show();
    }
    
    private void readFiles()
    {
	fileRects = new Hashtable();
	String [] fileNames = curDir.list();
	int currentY = yInset;
	Arrays.sort(fileNames);
	for(int i=0;i<fileNames.length;i++)
	    {
		File fileTest = new File(curDir+File.separator+fileNames[i]);
		if((fileNames[i].endsWith(extension)  && fileTest.isFile()) || 
		   fileTest.isDirectory())
		    {
			Rectangle fileRect = new Rectangle(xInset, currentY, fileWidth, 
							   fileHeight);
			fileRects.put(fileTest, fileRect);
		
			currentY+=fileHeight;
		    }
	    }
	if(!curDir.getPath().equals("."))
	    {
		Rectangle fileRect = new Rectangle(xInset, currentY, fileWidth, fileHeight);
		fileRects.put(new File(".."), fileRect);
	    }
	cancelRect = new Rectangle(450, 600, 130, 20);
	repaint();
    }
    
    private void drawTextInBox(Graphics g, String str,  int x, int y, int width, int height)
    {

	FontMetrics fMet = g.getFontMetrics();
	String curLine = "";
	int curPos = 0;
	int lineHeight = 6;  
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
		currentY+=lineHeight;
	    }
    }

    public void paint(Graphics gr)
    {
	Dimension dSize = getSize();
	//((Graphics2D)gr).setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
	//				  RenderingHints.VALUE_ANTIALIAS_ON);
	gr.setColor(Color.black);
	gr.fillRect(0,0,dSize.width, dSize.height);
	gr.setColor(cancelColor.darker().darker());
	gr.fillRect(cancelRect.x, cancelRect.y, cancelRect.width, cancelRect.height);
	gr.setColor(cancelColor);
	gr.drawRect(cancelRect.x, cancelRect.y, cancelRect.width, cancelRect.height);
	gr.setColor(Color.white);
	gr.drawString("Cancel", cancelRect.x+40, cancelRect.y+15);
	
	DGraphics g = new DGraphics(ml, gr, true);

	
	Enumeration files = fileRects.keys();
	while(files.hasMoreElements())
	    {

		g.setSemanticZoom(-50.0);
		File thisFile = (File)(files.nextElement());
		
		boolean isDir = thisFile.isDirectory();
		Rectangle drawRect = (Rectangle)(fileRects.get(thisFile));
		String name = thisFile.getName();
		if(isDir)
		    {
			name = name+File.separator;
			g.setColor(dirColor.darker().darker());
		    }
		else
		    g.setColor(fileColor.darker().darker());
		if(drawRect==overRect)
		    g.setColor(g.getColor().brighter());
		g.fillRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
		if(isDir)
		    g.setColor(dirColor);
		else
		    g.setColor(fileColor);

		if(drawRect==overRect)
		    g.setColor(g.getColor().brighter());
		g.drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
		ml.enablePlanarMode(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
		
		g.setColor(Color.white);
		
		g.setFont(new Font("SansSerif", Font.PLAIN, 10));
		g.drawString(name, drawRect.x+5, drawRect.y+drawRect.height-8);
		
                if(!isDir && extension.equals(".sgc"))
		{
		    SignalHeader sigHead = PersistentState.skimHeader(curDir.getName()+
								      File.separator+
                                                                      thisFile.getName());
		    if(sigHead!=null)
			{
			    g.setSemanticZoom(1.5);
			    g.setFont(new Font("SansSerif", Font.PLAIN, 4));
			    g.drawString("Last modified "+sigHead.date,
					 drawRect.x+5, 
					 drawRect.y+drawRect.height+2);
			    
			    g.drawString(" Version:"+sigHead.verNo, 
					 drawRect.x+4, 
					 drawRect.y+drawRect.height+8);

			    Rectangle testRect = new Rectangle(drawRect.x, drawRect.y, 
							       drawRect.width, drawRect.height);
			    if(ml.distortSemanticRectangle(testRect, 2.0, 200.0))
				{
				  
				    drawTextInBox(g, sigHead.annotation,  drawRect.x+80, 
						  drawRect.y+1, 
						  140, drawRect.height-3);
				}
			}
		}

		ml.disablePlanarMode();
	    }
    }
    

}
