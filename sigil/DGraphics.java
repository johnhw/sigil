/**
 * Subclass of graphics which provides distortion functionality
 * in conjunction with MouseLens
 *
 * @author John Williamson
 */
package sigil;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.text.AttributedCharacterIterator;

public class DGraphics extends Graphics
{
    private MouseLens thisLens;
    private Graphics realGraphics;
    private boolean highQuality = false;
    private double semZoomMin = 0.0;
    private double semZoomMax = 500.0;

    /**
     * Set the minimum scale level at which components appear
     */

    public void setSemanticZoom(double minScale, double maxScale)
    {
	semZoomMin = minScale;
	semZoomMax = maxScale;
    }

    
    public void setSemanticZoom(double minScale)
    {
	semZoomMin = minScale;
	semZoomMax = 500.0;

    }

    public double getSemanticZoom()
    {
	return semZoomMin;
    }

    public DGraphics(MouseLens thisLens, Graphics realGraphics)
    {
	this.thisLens = thisLens;
	this.realGraphics = realGraphics;

    }

    
    public void copyArea(int x, int y, int width, int height, int dx, int dy) 
    {
	Point distPt = thisLens.distort(x,y);
	double scale = thisLens.getScale(x+(width/2), y+(height/2));
	realGraphics.copyArea(distPt.x, distPt.y, (int)(width*scale), 
			      (int)(height*scale), (int)(dx*scale), (int)(dy*scale));
	

    }


    public void setColor(Color c)
    {
	realGraphics.setColor(c);

    }


    

    public void drawRect(int x, int y, int width, int height)
    {


	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		
		if(highQuality)
		    {
			Point v1, v2, v3, v4;
			v1 = thisLens.distort(x,y);
			v2 = thisLens.distort(x+width,y);
			v3 = thisLens.distort(x+width,y+height);
			v4 = thisLens.distort(x,y+height);
			if(thisLens.getLinearMode()==MouseLens.LINEAR_Y_ONLY)
			    {
				v1.x = x;
				v4.x = x;
			    }
			
			Polygon newQuad = new Polygon();
			newQuad.addPoint(v1.x, v1.y);
			
			newQuad.addPoint(v2.x, v2.y);
			newQuad.addPoint(v3.x, v3.y);
			newQuad.addPoint(v4.x, v4.y);
			realGraphics.drawPolygon(newQuad);
		    }
		else
		    {
			realGraphics.drawRect(distRect.x, distRect.y, distRect.width,
					      distRect.height);
		    }
	    }
    }

    public void fillRect(int x, int y, int width, int height)
    {
	

	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		if(highQuality)
		    {
			Point v1 = thisLens.distort(x,y);
			Point v2 = thisLens.distort(x+width,y);
			Point v3 = thisLens.distort(x+width,y+height);
			Point v4 = thisLens.distort(x,y+height);
			Polygon newQuad = new Polygon();
			newQuad.addPoint(v1.x, v1.y);
			
			newQuad.addPoint(v2.x, v2.y);
			newQuad.addPoint(v3.x, v3.y);
			newQuad.addPoint(v4.x, v4.y);
			realGraphics.fillPolygon(newQuad);
		    }
		else
		    {
			
			realGraphics.fillRect(distRect.x, distRect.y, distRect.width,
	       		      distRect.height);
		    }
	    
	    }
    }


    
    public void drawLine(int x1, int y1, int x2, int y2)
    {
	double scale = thisLens.getScale((x1+x2)/2, (y1+y2)/2);
	if(scale<semZoomMin || scale>semZoomMax)
	    return;
	Point distPt1 = thisLens.distort(x1,y1);
	Point distPt2 = thisLens.distort(x2,y2);
	realGraphics.drawLine(distPt1.x, distPt1.y, distPt2.x, distPt2.y);
    }

    public void drawOval(int x, int y, int width, int height)
    {
	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.drawOval(distRect.x, distRect.y, distRect.width, distRect.height);
	    }
    }

    public void clearRect(int x, int y, int width, int height)
    {	

	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.clearRect(distRect.x, distRect.y, distRect.width, distRect.height);
	    }
    }

    public Graphics create()
    {
	return realGraphics.create();
    }

    public Graphics create(int x, int y, int width, int height)
    {
	return realGraphics.create(x, y, width, height);
    }



    public void clipRect(int x, int y, int width, int height)
    {
	Rectangle distRect = thisLens.distortRectangle(x,y,width,height);
	realGraphics.clipRect(distRect.x, distRect.y, distRect.width, distRect.height);

    }
    
    public void dispose()
    {
	realGraphics.dispose();
    }

     public void drawArc(int x, int y, int width, int height, int startAngle, int endAngle)
    {
	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.drawArc(distRect.x, distRect.y, distRect.width, distRect.height,
				     startAngle, endAngle);
	    }
    }

    
    public void fillArc(int x, int y, int width, int height, int startAngle, int endAngle)
    {

	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.fillArc(distRect.x, distRect.y, distRect.width, distRect.height,
				     startAngle, endAngle);
	    }
    }

    public boolean drawImage(Image i, int x, int y, int width, int height, Color bgcolor,
			  ImageObserver obs)
    {
	

	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		return realGraphics.drawImage(i, distRect.x, distRect.y, distRect.width, 
					      distRect.height, 
					      bgcolor, obs);
	    }
	else return false;
    }

    public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2, 
			     int sx1, int sy1, int sx2, int sy2,
			     Color bgcolor,
			     ImageObserver obs)
    {
	Point distPt1 = thisLens.distort(dx1,dy1);

	int width = (int)((dx2-dy1));
	int height = (int)((dy2-dy1));


	
	Rectangle distRect = new Rectangle(dx1,dy1,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {

		return realGraphics.drawImage(i, distRect.x, distRect.y, 
					      distRect.x+distRect.width, distRect.y+distRect.height,
					      sx1,sy1,sx2,sy2,
					      bgcolor, obs);
	    }
	else
	    return false;
    }
    public boolean drawImage(Image i, int dx1, int dy1, int dx2, int dy2, 
			     int sx1, int sy1, int sx2, int sy2,
			    
			     ImageObserver obs)
    {
	
	int width = (int)((dx2-dy1));
	int height = (int)((dy2-dy1));
	
	Rectangle distRect = new Rectangle(dx1,dy1,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		return realGraphics.drawImage(i, distRect.x, distRect.y, 
					      distRect.x+distRect.width, distRect.y+distRect.height,
					      sx1,sy1,sx2,sy2,
					      obs);
	    }
	else
	    return false;

    }

    public void  drawPolyline(int[] xPoints, int[] yPoints, int nPoints) 
    {
	
	
	int [] newXPoints = new int[nPoints];
	int [] newYPoints = new int[nPoints];

	for(int i=0;i<nPoints;i++)
	    {
		Point distPt = thisLens.distort(xPoints[i], yPoints[i]);
		newXPoints[i] = distPt.x;
		newYPoints[i] = distPt.y;
	    }

	realGraphics.drawPolyline(newXPoints, newYPoints, nPoints);
    }

    public Shape getClip() 
    {
	return realGraphics.getClip();
    } 

    public Rectangle getClipBounds()
    {

	return realGraphics.getClipBounds();
    }


    public Rectangle getClipBounds(Rectangle r) 
    {
	return realGraphics.getClipBounds(r);
    }

    public Rectangle getClipRect()
    {
	return realGraphics.getClipRect();
    }
    
    public Color getColor()
    {
	return realGraphics.getColor();
    }

    public Font getFont()
    {
	return realGraphics.getFont();
    }

    public FontMetrics getFontMetrics()
    {
	return realGraphics.getFontMetrics();
    }

    
    public FontMetrics getFontMetrics(Font f)
    {
	return realGraphics.getFontMetrics(f);
    }

    public boolean hitClip(int x, int y, int width, int height)
	{
	    
	    Rectangle distRect = new Rectangle(x,y,width,height);
	    distRect = thisLens.distortRectangle(distRect);
	    return realGraphics.hitClip(distRect.x, distRect.y, distRect.width,
					distRect.height);	
		
	    
	}
    
    public void setClip(int x, int y, int width, int height)
    {
	
	    Rectangle distRect = new Rectangle(x,y,width,height);
	    distRect = thisLens.distortRectangle(distRect);

	    realGraphics.setClip(distRect.x, distRect.y, 
				 distRect.width, distRect.height);	
    }

    public void setClip(Shape s)
    {
	realGraphics.setClip(s);

    }

    public void setFont(Font f)
    {
	realGraphics.setFont(f);
    }

    
    public void setPaintMode()
    {
	realGraphics.setPaintMode();
    }

    public void setXORMode(Color cl)
    {
	realGraphics.setXORMode(cl);
    }

    
    public void translate(int x, int y)
    {
	Point distPt = thisLens.distort(x,y);
	realGraphics.translate(distPt.x, distPt.y);
    }


    public void finalize()
    {
	realGraphics.finalize();
    }

    public boolean drawImage(Image i, int x, int y, int width, int height,
			  ImageObserver obs)
    {
	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		return realGraphics.drawImage(i, distRect.x, distRect.y, distRect.width, distRect.height,
					      obs);
	    }
	else
	    return false;
    }

    
    public boolean drawImage(Image i, int x, int y, ImageObserver obs)
	{
	    
	int width = 0, height = 0;
	try{
	    width = i.getWidth(null);
	    height = i.getHeight(null);
	} catch(NullPointerException ne) {System.err.println("Couldn't get image size....");}
	    Rectangle distRect = new Rectangle(x,y,width,height);
	    if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
		{
		    return realGraphics.drawImage(i, distRect.x, distRect.y, distRect.width, distRect.height, obs);
		}
	    else
		return false;
    }

    public boolean drawImage(Image i, int x, int y, Color bgcolor, ImageObserver obs)
    {

	int width = 0, height = 0;
	try{
	width = i.getWidth(null);
	 height = i.getHeight(null);
	} catch(NullPointerException ne) {System.err.println("Couldn't get image size....");}
	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		return realGraphics.drawImage(i, distRect.x, distRect.y, distRect.width, distRect.height, bgcolor, 
					      obs);
	    }
	else
	    return false;


    }



    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {


	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.drawRoundRect(distRect.x, distRect.y, distRect.width, distRect.height,
					   arcWidth, arcHeight);
	    }
    }
    
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
    {


       	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.fillRoundRect(distRect.x, distRect.y, distRect.width, distRect.height,
					   arcWidth, arcHeight);
	    }
    }

    
    public void fillOval(int x, int y, int width, int height)
    {
	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		realGraphics.fillOval(distRect.x, distRect.y, distRect.width, distRect.height);
	    }
    }

    public void draw3DRect(int x, int y, int width, int height, boolean raised)
    {

	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		if(highQuality)
		    {
			Point v1 = thisLens.distort(x,y);
			Point v2 = thisLens.distort(x+width,y);
			Point v3 = thisLens.distort(x+width,y+height);
			Point v4 = thisLens.distort(x,y+height);
			Polygon newQuad = new Polygon();
			newQuad.addPoint(v1.x, v1.y);
			
			newQuad.addPoint(v2.x, v2.y);
			newQuad.addPoint(v3.x, v3.y);
			newQuad.addPoint(v4.x, v4.y);
			realGraphics.drawPolygon(newQuad);
		    }    
		else
		    {
		
			realGraphics.draw3DRect(distRect.x, distRect.y, distRect.width,
						distRect.height, raised);
		    }
	    }
    }


    public void drawPolygon(Polygon p)
    {
	drawPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    
    public void drawString(String s, int x, int y)
    {
	
	Font cFont = realGraphics.getFont();
	FontMetrics cfMet = Toolkit.getDefaultToolkit().getFontMetrics(cFont);
	int oldWidth = cfMet.stringWidth(s);
	int oldHeight = cfMet.getHeight()+(cfMet.getHeight()/4);

	Rectangle distRect = new Rectangle(x,y,oldWidth, oldHeight);
	
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {

		int newFontSize = (int)(cFont.getSize()*((double)distRect.width/(double)oldWidth));
		Font nFont = new Font(cFont.getName(), cFont.getStyle(), newFontSize);
		FontMetrics nfMet = Toolkit.getDefaultToolkit().getFontMetrics(nFont);
	
		realGraphics.setFont(nFont);
		realGraphics.drawString(s,distRect.x,
					distRect.y);
		realGraphics.setFont(cFont);
	}

    }

    public void drawPolygon(int [] xpoints, int [] ypoints, int npoints)
    {
	double scale = thisLens.getScale(xpoints[0], ypoints[0]);
	if(scale<semZoomMin || scale>semZoomMax)
	    return;

	Polygon newPoly = new Polygon();
	for(int i=0;i<npoints;i++)
	    {
		Point distPt = thisLens.distort(xpoints[i], ypoints[i]);
		newPoly.addPoint(distPt.x, distPt.y);
	    }
	realGraphics.drawPolygon(newPoly);
    }

    
    public void fillPolygon(int [] xpoints, int [] ypoints, int npoints)
    {
	double scale = thisLens.getScale(xpoints[0], ypoints[0]);
	if(scale<semZoomMin || scale>semZoomMax)
	    return;

	Polygon newPoly = new Polygon();
	for(int i=0;i<npoints;i++)
	    {
		Point distPt = thisLens.distort(xpoints[i], ypoints[i]);
		newPoly.addPoint(distPt.x, distPt.y);
	    }
	realGraphics.fillPolygon(newPoly);
    }

    public void fillPolygon(Polygon p)
    {
	fillPolygon(p.xpoints, p.ypoints, p.npoints);
    }

    public void fill3DRect(int x, int y, int width, int height, boolean raised)
    {
	
	Rectangle distRect = new Rectangle(x,y,width,height);
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		if(highQuality)
		    {
			Point v1 = thisLens.distort(x,y);
			Point v2 = thisLens.distort(x+width,y);
			Point v3 = thisLens.distort(x+width,y+height);
			Point v4 = thisLens.distort(x,y+height);
			Polygon newQuad = new Polygon();
			newQuad.addPoint(v1.x, v1.y);
			
			newQuad.addPoint(v2.x, v2.y);
			newQuad.addPoint(v3.x, v3.y);
			newQuad.addPoint(v4.x, v4.y);
			realGraphics.fillPolygon(newQuad);
		    }    
		else
		    {
		
			realGraphics.fill3DRect(distRect.x, distRect.y, distRect.width,
						distRect.height, raised);
		    }
	    }



    }
    

    

    public void drawBytes(byte[] data, int offset, int length, int x, int y) 
    {

	Font cFont = realGraphics.getFont();
	FontMetrics cfMet = Toolkit.getDefaultToolkit().getFontMetrics(cFont);
	int oldWidth = cfMet.stringWidth(new String(data, offset, length));
	int oldHeight = cfMet.getHeight()+(cfMet.getHeight()/4);
	
	
	Rectangle distRect = new Rectangle(x,y,oldWidth, oldHeight);
	
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	{
	    int newFontSize = (int)(cFont.getSize()*((double)distRect.width/(double)oldWidth));
	    
	    Font nFont = new Font(cFont.getName(), cFont.getStyle(), newFontSize);
	    FontMetrics nfMet = Toolkit.getDefaultToolkit().getFontMetrics(nFont);
	    
	    realGraphics.setFont(nFont);
	    realGraphics.drawBytes(data,offset,length, distRect.x, 
				   distRect.y);
	    

	    realGraphics.setFont(cFont);
	}

    }


    

    

    public void drawChars(char[] data, int offset, int length, int x, int y) 
    {

	Font cFont = realGraphics.getFont();
	FontMetrics cfMet = Toolkit.getDefaultToolkit().getFontMetrics(cFont);
	int oldWidth = cfMet.stringWidth(new String(data, offset, length));
	int oldHeight = cfMet.getHeight()+(cfMet.getHeight()/4);
		Rectangle distRect = new Rectangle(x,y,oldWidth, oldHeight);
	
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	{

	       int newFontSize = (int)(cFont.getSize()*((double)distRect.width/(double)oldWidth));

	       Font nFont = new Font(cFont.getName(), cFont.getStyle(), newFontSize);
	       FontMetrics nfMet = Toolkit.getDefaultToolkit().getFontMetrics(nFont);
	       
	       realGraphics.setFont(nFont);
	       realGraphics.drawChars(data,offset,length, distRect.x, 
				      distRect.y);
	       realGraphics.setFont(cFont);
	}


    }

    public void drawString(AttributedCharacterIterator iterator, int x, int y)
    {

	Font cFont = realGraphics.getFont();
	FontMetrics cfMet = Toolkit.getDefaultToolkit().getFontMetrics(cFont);
	int oldWidth = 50;
	int oldHeight = cfMet.getHeight()+(cfMet.getHeight()/4);
	
	Rectangle distRect = new Rectangle(x,y,oldWidth, oldHeight);
	
	if(thisLens.distortSemanticRectangle(distRect, semZoomMin, semZoomMax))
	    {
		
		int newFontSize = (int)(cFont.getSize()*((double)distRect.width/(double)oldWidth));
		Font nFont = new Font(cFont.getName(), cFont.getStyle(), newFontSize);
		FontMetrics nfMet = Toolkit.getDefaultToolkit().getFontMetrics(nFont);
		
		
		realGraphics.setFont(nFont);
		
		realGraphics.drawString(iterator, distRect.x, 
					distRect.y);
		
		realGraphics.setFont(cFont);
	}


    }



    public DGraphics(MouseLens thisLens, Graphics realGraphics, boolean highQuality)
    {
	this.thisLens = thisLens;
	this.realGraphics = realGraphics;
	this.highQuality = highQuality;
    }

    public boolean getHighQuality()
    {
	return highQuality;
    }

    public void setHighQuality(boolean quality)
    {
	highQuality = quality;
    }




}
