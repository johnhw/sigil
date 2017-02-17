
public class Quad3D implements Comparable
 {
  Point3D v1, v2, v3, v4;

  public int compareTo(Object o)
  {
   if(o instanceof Quad3D)
      return (int)(v1.z - ((Quad3D)o).v1.z);
   else
      return 0;
  }
     public String toString()
     {
	 return "["+v1+", "+v2+", "+v3+", "+v4+"]";

     }

  public Quad3D(Point3D v1, Point3D v2, Point3D v3, Point3D v4)
  {
   this.v1 = v1;
   this.v2 = v2;
   this.v3 = v3;
   this.v4 = v4;
  }

  public Quad3D()
  {

  }
 }
