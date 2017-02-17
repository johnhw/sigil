import java.util.Arrays;

 public class Box3D
 {
   Point3D point1, point2;

   public boolean contains(Point3D pt)
   {
     boolean xInside = pt.x>point1.x && pt.x<=point2.x;
     boolean yInside = pt.y>point1.y && pt.y<=point2.y;
     boolean zInside = pt.z>point1.z && pt.z<=point2.z;
     return(xInside && yInside && zInside);
   }

   public Quad3D [] toQuads()
   {
     Quad3D [] faces = new Quad3D[6];
     Point3D bxayaz = new Point3D(point2.x, point1.y, point1.z);
     Point3D bxaybz = new Point3D(point2.x, point1.y, point2.z);
     Point3D bxbyaz = new Point3D(point2.x, point2.y, point1.z);
     Point3D axbyaz = new Point3D(point1.x, point2.y, point1.z);
     Point3D axbybz = new Point3D(point1.x, point2.y, point2.z);
     Point3D axaybz = new Point3D(point1.x, point1.y, point2.z);
     faces[0] = new Quad3D(point1, bxayaz, bxaybz, axaybz);
     faces[1] = new Quad3D(point1, axbyaz, axbybz, axaybz);
     faces[2] = new Quad3D(point1, bxayaz, axbybz, axbyaz);
     faces[3] = new Quad3D(point2, axbybz, axbyaz, bxbyaz);
     faces[4] = new Quad3D(point2, axbybz, axaybz, bxaybz);
     faces[5] = new Quad3D(point2, bxbyaz, bxayaz, bxaybz);
     Arrays.sort(faces);
     return faces;
   }


   public Box3D rotate3D(double roll, double pitch, double yaw)
   {
    return new Box3D(Utils3D.rotate3D(point1, roll,pitch,yaw),
                     Utils3D.rotate3D(point2, roll,pitch,yaw));

   }

   public Box3D(Point3D a, Point3D b)
   {
        point1 = a;
        point2 = b;
   }

   public String toString()
   {
    return "["+point1+" <-> "+point2+"]";
   }
 }
