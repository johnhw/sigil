import java.awt.*;
import java.util.*;

public class Utils3D
{

 public static void depthSort(Vector pts)
 {
   Collections.sort(pts);
 }
 
 public static Point project(Point3D pt, double scale, double dist)
 {
   double x,y;
   x = (pt.x/(pt.z+dist))*scale+scale/2;
   y = (pt.y/(pt.z+dist))*scale+scale/2;
   return new Point((int)x,(int)y);
 }

    public static Point project(double ix, double iy, double iz, double scale, double dist)
    {
	double x,y;
	x = (ix/(iz+dist))*scale+scale/2;
	y = (iy/(iz+dist))*scale+scale/2;
	return new Point((int)x,(int)y);


    }

 public static Quad3D rotateQuad(Quad3D quad,double roll, double pitch, double yaw)
 {
  quad.v1 = rotate3D(quad.v1, roll, pitch, yaw);
  quad.v2 = rotate3D(quad.v2, roll, pitch, yaw);
  quad.v3 = rotate3D(quad.v3, roll, pitch, yaw);
  quad.v4 = rotate3D(quad.v4, roll, pitch, yaw);
  return quad;
 }


 public static Point3D rotate3D(Point3D pt, double roll, double pitch, double yaw)
 {
        Point3D retVal = new Point3D();
        double rx,ry,rz, tz, kx, ky;

        rx=Math.cos(yaw)*pt.x-Math.sin(yaw)*pt.z;
        rz=Math.sin(yaw)*pt.x+Math.cos(yaw)*pt.z;

        ry=Math.cos(pitch)*pt.y-Math.sin(pitch)*rz;
        tz=Math.sin(pitch)*pt.y+Math.cos(pitch)*rz;

        kx=Math.cos(roll)*rx-Math.sin(roll)*ry;
        ky=Math.sin(roll)*rx+Math.cos(roll)*ry;
        
        retVal.x=kx;
        retVal.y=ky;
        retVal.z=tz;
        return retVal;       
 }


}
