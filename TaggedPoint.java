     public class TaggedPoint implements Comparable
     {
      Point3D pt;
      int tag;

      public int compareTo(Object o)
      {
       if(o instanceof TaggedPoint)
        return pt.compareTo(((TaggedPoint)o).pt);
       else
        return 0;
      }

      public TaggedPoint(Point3D pt, int tag)
      {
       this.pt = pt;
       this.tag = tag;
      }

     }
