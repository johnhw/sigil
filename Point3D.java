
 public class Point3D implements Comparable
 {
  double x,y,z;

  public int compareTo(Object o)
  {
    if(o instanceof Point3D)
    {
        Point3D pt = (Point3D) o;
        return (int)(pt.z-this.z);
    }
    else
    return 0;

  }
     public Point3D(double x, double y, double z)
     {
	 this.x = x;
	 this.y = y;
	 this.z = z;

     }

     public String toString()
     {
      return "("+x+", "+y+", "+z+")";

     }
     public Point3D()
     {
	 this.x = 0;
	 this.y = 0;
	 this.z = 0;


     }
 }
