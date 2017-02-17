/**
 * General class for applying "fisheye" distortion when
 * rendering AWT components.
 *
 * @author John Williamson
 */
package sigil;
import java.awt.*;
import java.awt.event.*;


public class MouseLens implements MouseMotionListener
{
    public static final int LENS_FISHEYE = 1;
    public static final int LENS_SPHERICAL = 2;
    public static final int LINEAR_NORMAL = 1;
    public static final int LINEAR_X_ONLY = 2;
    public static final int LINEAR_Y_ONLY = 4;
    public static final int LINEAR_COMPOSITE = 8;

    public static final int INTERP_LINEAR = 0;
    public static final int INTERP_COSINE = 1;
   
    private double xScale, yScale;
    private int xDivs, yDivs;
    private int currentX=0, currentY=0;
    private Point [] [] distortionMatrix;
    private Point [] [] overlayMatrix;
    private double size, expon;
    private Component parent;
    private boolean distorting = true;
    private Dimension oldSize;
    private boolean autoRedraw = true;
    private int lensType = LENS_FISHEYE;
    private int linearMode = LINEAR_NORMAL;
    private boolean distLock = false;
    
    public void lockDistort()
    {
	distLock = true;

    }

    
    public void unlockDistort()
    {
	distLock = false;
    }


  
    private boolean planarMode = false;

    private Point dPlanarPoint;
    private Point planarPoint;

    private double planarScaleX;
    private double planarScaleY;
    private boolean maintainAspect = false;
    private int interpMode = INTERP_COSINE;
    private static final int ift = 10;

    /**
     * Set the interpolation mode. Can be one of:
     * INTERP_LINEAR Linear interpolation
     * INTERP_COSINE Cosine interpolation (default)
     */
    public void setInterpolationMode(int iMode)
    {
	interpMode = iMode;
    }
    

    /**
     * Return the interpolation mode
     */
    public int getInterpolationMode()
    {
	return interpMode;
    }

    /**
     * Disable planar mode
     */
    public void disablePlanarMode()
    {
	planarMode = false;
    }

    /**
     * Set to true if the aspect ratio
     * of rectangles should be preserved
     */
    public void setAspect(boolean aspect)
    {
	maintainAspect = aspect;
    }


    /**
     * Return the current status of the aspect ratio.
     * If true, rectangles will maintain their aspect ratio
     */
    public boolean getAspect()
    {
	return maintainAspect;
    }



    /**
     * Enable planar mode, centered at (x,y)
     * This will maintain constant distortion across the
     * entire area, until planar mode is disabled again
     */
    public void enablePlanarMode(int x, int y, int width, int height)
    {	
	planarPoint = new Point(x,y);
	Rectangle r = new Rectangle(x,y,width, height);
	distortSemanticRectangle(r,-50,50);

	dPlanarPoint = new Point(r.x, r.y) ;
	
	planarScaleX = r.width/(double)width;
	planarScaleY = r.height/(double)height;

	planarMode = true;
	
    }

    /**
     * Set the autoredraw. If true (default)
     * component will be painted every time the 
     * mouse moves over it
     */
    public void setAutoRedraw(boolean auto)
     {
	autoRedraw = auto;
    }

    /**
     * Get the autoredraw status 
     */
    public boolean getAutoRedraw()
    {
	return autoRedraw;
    }

    /**
     * Get the distortion status (true = distortion on)
     */
    public boolean getDistorting()
    {
	return distorting;
    }


    /**
     * Turn distortion on and off
     */
    public void setDistorting(boolean distort)
    {
	distorting = distort;
    }



    public void mouseDragged(MouseEvent me)
    {
    if(!distLock)
    {
	checkScale();
	setCentre(me.getX(), me.getY());

	if(autoRedraw)
	    parent.repaint();

       }
    }


    public void mouseMoved(MouseEvent me)
    {
     if(!distLock)
     {
	checkScale();
	setCentre(me.getX(), me.getY());
	
	if(autoRedraw)
	    parent.repaint();
            }
    }

    private void initLens()
    {

	parent.addMouseMotionListener(this);
	checkScale();
	recreateMatrix();
    }

    /**
     * Set the lens type. 
     * Can have any of LENS_SPHERICAL or LENS_FISHEYE bits set
     */
    public void setLensType(int spherical)
    {
	lensType = spherical;
	recreateMatrix();
    }


    /**
     * Get the lens type
     */
    public int getLensType()
    {
	return lensType;

    }

    /**
     * Create a mouse lens for the given component, with
     * the default parameters
     */
    public MouseLens(Component parent)
    {
	xDivs = 60;
	yDivs = 60;
	this.parent = parent;
	setLens(60, 1.35);
	initLens();
    }

    

    /**
     * Create a mouse lens for the given component,
     * dividing the distortion matrix into xDivs by yDivs
     * Distortion amount is controlled by size and expon
     */
    public MouseLens(Component parent, int xDivs, int yDivs,
		     double size, double expon)
    {
	this.xDivs = xDivs;
	this.yDivs = yDivs;
	this.parent = parent;
	this.expon = expon;
	this.size = size;
	initLens();
    }

    /**
     * Set the lens distortion parameters
     * Note that the parameters will probably require
     * experimentation to obtain good results.
     * @param expon Changes the exponent of distortion
     * @param size Changes the size of distortion
     */
    public void setLens(double size, double expon)
    {
	this.expon = expon;
	this.size = size;
	recreateMatrix();
    }
    
    private void recreateMatrix()
    {
	overlayMatrix = new Point[xDivs*2][yDivs*2];
	distortionMatrix = new Point[xDivs][yDivs];
	int cx, cy;
	for(int i=0;i<xDivs;i++)
	    for(int j=0;j<yDivs;j++)
		{
		    cx = (int)(i*xScale);
		    cy = (int)(j*yScale);
		    distortionMatrix[i][j] = new Point(cx, cy);		    
		}
	
	calculateOverlayLens(size, expon);
    }


    /**
     * Set the subdivisions of the distortion grid
     * more = slower but more accurate
     */
    public void setDivisions(int xDivs, int yDivs)
    {
	this.xDivs = xDivs;
	this.yDivs = yDivs;

    }


    private void calculateOverlayLens(double div, double expon)
    {

	calculateDistortion(distortionMatrix, overlayMatrix, 
			    xDivs/2, yDivs/2, div, expon, true);
    }


    /**
     * Return the current distortion type. Can be LINEAR_NORMAL (default)
     * or LINEAR_X_ONLY or LINEAR_Y_ONLY or LINEAR_COMPOSITE
     */
    public int getLinearMode()
    {
	return linearMode;
    }

    
    /**
     * Set the current distortion type. Can be LINEAR_NORMAL (default)
     * or LINEAR_X_ONLY or LINEAR_Y_ONLY or LINEAR_COMPOSITE
     */
    public void setLinearMode(int linearMode)
    {
	this.linearMode = linearMode;
	recreateMatrix();
    }

    public Point calculateLens(double x, double y, double cx, double cy)
    {
	double centreDist;
	return null;
    }

    private void calculateDistortion(Point [] [] from, Point [] [] to, int cx, int cy, 
				     double divConst, double expon,
				     boolean overlay)
    {
	double centreDist, yNorm, xNorm;

	currentX = xDivs/2;
	currentY = yDivs/2;
	for(int i=-xDivs/2;i<(xDivs+xDivs/2);i++)
	    {
		for(int j=-xDivs/2;j<(yDivs+yDivs/2);j++)
		    {
			Point cPoint = new Point((int)(i*xScale), (int)(j*yScale));

			calculateLens(i,j,cPoint.x, cPoint.y);

			if(linearMode == LINEAR_X_ONLY)
			    {
				centreDist = Math.sqrt((i-cx)*(i-cx));
				
			    }
			else if (linearMode == LINEAR_Y_ONLY)
			    {
				centreDist = Math.sqrt((j-cy)*(j-cy));
			    }
			else if (linearMode == LINEAR_COMPOSITE)
			    {
				
				centreDist = Math.sqrt((j-cy)*(j-cy)) + 
				    Math.sqrt((i-cx)*(i-cx));
			    }
			    else
				{
				    centreDist = Math.sqrt((i-cx)*(i-cx)+(j-cy)*(j-cy));
				}


			int newX = cPoint.x;
			int newY = cPoint.y;
		       			
			
			double fcentreDist = (-divConst/Math.pow(expon, centreDist));
			double scentreDist = (centreDist*centreDist)/(divConst*25);
			
			centreDist = 0;

			if((lensType&LENS_FISHEYE)!=0)
			    centreDist += fcentreDist;

			if((lensType&LENS_SPHERICAL)!=0)
			    centreDist += scentreDist;
			
			xNorm = ((i-currentX)*centreDist)/1;
			yNorm = ((j-currentY)*centreDist)/1; 
						
			xNorm+=0.01;
			yNorm+=0.01;
			
			if(overlay)
			    to[i+xDivs/2][j+xDivs/2] = new Point((int)xNorm, (int)yNorm);
			else
			    to[i+xDivs/2][j+xDivs/2] = new Point((int)(cPoint.x-xNorm), 
								 (int)(cPoint.y-yNorm));
			
		    }

	    }
    }

    /**
     * Distorts a rectangle, pinned at the centre 
     */
    public Rectangle distortRectangle(Rectangle r)
    {
	return distortRectangle(r.x, r.y, r.width, r.height);

    }


    /**
     *
     *  Distort the given rectangle, changing it's values directly.
     *  Return true if the associated scale is between minScale and maxScale
     *  false otherwise
     */
    public boolean distortSemanticRectangle(Rectangle r, double minScale, double maxScale)
    {

	Point dPoint1 = distort(r.x,r.y);
	Point dPoint2 = distort(r.x+r.width, r.y+r.height);
	int oldWidth = r.width;
	int oldHeight = r.height;
	r.x = dPoint1.x;
	r.y = dPoint1.y;
	
	r.width = dPoint2.x-dPoint1.x;
	r.height = dPoint2.y-dPoint1.y;
	
        double scale = ((((double)r.width/(double)oldWidth)+((double)r.height/(oldHeight)))/2.0);

	if(maintainAspect)
	    {
		
		r.width = (int)(oldWidth*scale);
		r.height = (int)(oldHeight*scale);
	    }



	
	return (scale>minScale  && scale<maxScale);
    }


   
    /**
     * Distorts a rectangle, pinned at the centre 
     */
    public Rectangle distortRectangle(int x, int y, int width, int height)
    {
	Rectangle retVal;
	Point dPoint1 = distort(x,y);
	Point dPoint2 = distort(x+width,y+height);
	int nwidth = dPoint2.x-dPoint1.x;
	int nheight = dPoint2.y-dPoint1.y;

	
        double scale = ((((double)nwidth/(double)width)+((double)nheight/((double)height)))/2.0);

	if(maintainAspect)
	    {
		
		nwidth = (int)(width*scale);
		nheight = (int)(height*scale);
	    }

	return new Rectangle(dPoint1.x, dPoint1.y, nwidth, nheight);

    }



    private void setCentre(int xCentre, int yCentre)
    {
	
	currentX = (int)(xCentre/xScale);
	currentY = (int)(yCentre/yScale);
	if(currentX<=0)
	    currentX = 1;

	if(currentX>=xDivs)
	    currentX = xDivs;

	if(currentY<=0)
	    currentY = 1;

	if(currentY>=yDivs)
	    currentY = yDivs;

    }
    
    /**
     * set the matrix subdivisions to x by y
     */
    public void setDivs(int x, int y)
    {
	xDivs = x;
	yDivs = y;
	recreateMatrix();
    }


    private void setScale(int x, int y)
    {
	xScale = (double)x/(double)xDivs;
	yScale = (double)y/(double)yDivs;
	recreateMatrix();
    }

    /**
     * Distort the given point, returning the new distorted point
     *
     * @returns The distorted point
     */
    public Point distort(Point p)
    {
	return distort(p.x, p.y);
    }


    private double cubicInterpolate(double x, double a, double b, double y, double val)
    {
	double tau = (b-a) - (a-x);
	double gamma = (y-b) - (b-a);
	double alpha = tau-gamma*0.25;
	double beta = gamma-tau*0.25;
	double invVal = 1-val;
	double retVal = val*b + invVal*a + 
	    (beta*(val*val*val-val) + 
	     alpha*(invVal*invVal*invVal - invVal))/3.75;
	return retVal;

    }

    private double interpolate(double a, double b, double val)
    {
	double fVal;
	if(interpMode == INTERP_COSINE)
	    fVal =  (1-Math.cos(val*Math.PI))*0.5;
	else
	    fVal = val;
	return a*(1-fVal)+fVal*b;
    }

    private void checkScale()
    {
	Dimension dSize = parent.getSize();
	if(oldSize==null || oldSize.width!=dSize.width || 
	   oldSize.height!=dSize.height)
	    {
		oldSize = dSize;
		setScale(oldSize.width, oldSize.height);
	    }
    }


    private Point planarDistort(int x, int y)
    {

	
	y-=planarPoint.y;

	x-=planarPoint.x;
	x*=planarScaleX;
	x+=dPlanarPoint.x;
	
	y*=planarScaleY;

	y+=dPlanarPoint.y;
	return new Point(x,y);
    }

    /**
     * Distort the given point, returning the new distorted point
     *
     * @returns The distorted point
     */
    public Point distort(int x, int y)
    {
	if(!distorting)
	    return new Point(x,y);

	if(planarMode)
	    return planarDistort(x,y);

	int mappedX = (int)(Math.floor(x/xScale));
	int mappedY = (int)(Math.floor(y/yScale));
	double interpX = (x/xScale)-mappedX;
	double interpY = (y/yScale)-mappedY;


	int offX, offY;


	offX = -currentX+xDivs;
	offY = -currentY+yDivs;
	
	
	if(mappedX>=0 && mappedY>=0 && mappedX<xDivs*2-2 && mappedY<yDivs*2-2
	   && mappedX+offX>=0 && mappedX+offX<xDivs*2-2 && mappedY+offY>=0
	   && mappedY+offY<yDivs*2-2)
	    {
		Point v1 = new Point((int)(mappedX*xScale), (int)(mappedY*yScale));
		Point v2 = new Point((int)((mappedX+1)*xScale), (int)(mappedY*yScale));
		Point v3 = new Point((int)(mappedX*xScale), (int)((mappedY+1)*yScale));
		Point v4 = new Point((int)((mappedX+1)*xScale), (int)((mappedY+1)*yScale));

		int tx, ty;
		
		tx = mappedX+offX;
		ty = mappedY+offY;
		
	       

		int v1X=v1.x-overlayMatrix[tx][ty].x;
		int v1Y=v1.y-overlayMatrix[tx][ty].y;
		
		int v2X=v2.x-overlayMatrix[tx+1][ty].x;
		int v2Y=v2.y-overlayMatrix[tx+1][ty].y;
		
		int v3X=v3.x-overlayMatrix[tx][ty+1].x;
		int v3Y=v3.y-overlayMatrix[tx][ty+1].y;
		
		int v4X=v4.x-overlayMatrix[tx+1][ty+1].x;
		int v4Y=v4.y-overlayMatrix[tx+1][ty+1].y;
		double interpXPoint, interpYPoint;

		double interpXPoint1 = interpolate(v1X, v2X, interpX);
		double interpXPoint2 = interpolate(v1X, v2X, interpX);
		
		interpXPoint = interpolate(interpXPoint1, interpXPoint2, 
					   interpY);
		
		double interpYPoint1 = interpolate(v1Y, v3Y, interpY);
		double interpYPoint2 = interpolate(v2Y, v4Y, interpY);
		interpYPoint = interpolate(interpYPoint1, interpYPoint2, 
					   interpX);

                if(linearMode == LINEAR_Y_ONLY)
		    interpXPoint = x;
		
		return new Point((int)(interpXPoint), (int)(interpYPoint));

	    }
	else
	    {
		
		return new Point(x,y);
	    }
    }

    /**
     * Distort the given point, returning the appropriate
     * scale factor for that point
     *
     * @return The distorted scale factor
     */
    public double getScale(int x, int y)
    {
	if(!distorting)
	    return 1.0;

	if(planarMode)
	    return (planarScaleX+planarScaleY)/2.0;

	Point v1 = distort(x-ift, y-ift);
	Point v2 = distort(x+ift, y+ift);


	double dValueX = Math.abs((v1.x-v2.x)/((double)ift*2.0));
	double dValueY = Math.abs((v1.y-v2.y)/((double)ift*2.0));
	return (dValueX+dValueY)/2.0;

    }

    

}
