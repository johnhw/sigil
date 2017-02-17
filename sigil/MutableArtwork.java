package sigil;
import java.util.*;
import java.awt.*;

  public class MutableArtwork implements Cloneable
  {
   private Vector genotype;
   private static Random gen = new Random();

   public MutableArtwork doClone()
   {
    MutableArtwork ma =  new MutableArtwork(this);
    ma.genotype = new Vector();
    for(int i=0;i<genotype.size();i++)
     ma.genotype.add(((ArtworkElement)(genotype.get(i))).doClone());
    return ma;
   }

   private class ArtworkElement implements Cloneable
   {
     double divRatio;
     Color c1, c2;

     public ArtworkElement doClone()
     {
           ArtworkElement ae = new ArtworkElement(divRatio,
           new Color(c1.getRed(), c1.getGreen(), c1.getBlue()),
           new Color(c2.getRed(), c2.getGreen(), c2.getBlue()));
           return ae;
     }

     public ArtworkElement(double d, Color a, Color b)
     {
      divRatio = d;
      c1 = a;
      c2 = b;
     }
   }

   private void addElement()
   {
        double divRatio = gen.nextDouble();
        int r1 = gen.nextInt(255);
        int g1 = gen.nextInt(255);
        int b1 = gen.nextInt(255);
        int r2 = gen.nextInt(255);
        int g2 = gen.nextInt(255);
        int b2 = gen.nextInt(255);


        Color c1 = new Color(r1,r1,r1);
        Color c2 = new Color(r2,r2,r2);        
        genotype.add(new ArtworkElement(divRatio, c1, c2));

   }

   public void initGenotype()
   {
     genotype = new Vector();     
     int numElts = gen.nextInt(15)+10;
     for(int i=0;i<numElts;i++)
        addElement();
   }

   public MutableArtwork(Object baseObject)
   {
    initGenotype();
   }

   public void mutate(double factor)
   {
        if(gen.nextDouble()<factor && genotype.size()>2)
          genotype.remove(0);

        if(gen.nextDouble()<factor)
          addElement();

        for(int i=0;i<genotype.size();i++)
        {
         ArtworkElement elt = (ArtworkElement)(genotype.get(i));
         elt.divRatio += ((gen.nextDouble()*factor)-(factor/2.0));
         if(elt.divRatio<0.0)
          elt.divRatio =0.0;
          if(elt.divRatio>1.0)
          elt.divRatio=1.0;
         elt.c1 = mutateColor(elt.c1,factor);
         elt.c2 = mutateColor(elt.c2,factor);
        }
   }

   private Color mutateColor(Color c, double factor)
   {
    int r = c.getRed() + (int)((factor*255.0*(gen.nextDouble()-0.5)));
    int g = c.getGreen()+ (int)((factor*255.0*(gen.nextDouble()-0.5)));
    int b = c.getBlue() +(int)((factor*255.0*(gen.nextDouble()-0.5)));
    if(r<0) r=0;
    if(r>255) r=255;
    if(g<0) g=0;
    if(g>255) g=255;
    if(b<0) b=0;
    if(b>255) b=255;
    return new Color(r,g,b);
   }

   public void breed(MutableArtwork ma)
   {

   }


   public void paint(Graphics g, int x, int y, int width, int height)
   {
      Vector rectList = new Vector();

      ArtworkElement elt = (ArtworkElement)(genotype.get(0));
      Rectangle rectA = new Rectangle(x, y, (int)(elt.divRatio*width), height);
      Rectangle rectB = new Rectangle(x+(int)(elt.divRatio*width), y, (int)((1.0-elt.divRatio)*width), height);
      rectList.add(rectA);
      rectList.add(rectB);
      Vector horizVec = new Vector();
      horizVec.add(new Boolean(true));
      horizVec.add(new Boolean(true));


      int curPos = 0;
      for(int i=1;i<genotype.size();i++)
      {
        elt = (ArtworkElement)(genotype.get(i));
        double dRat = elt.divRatio;
        Rectangle oR = (Rectangle)(rectList.get(curPos));
        boolean hDiv = ((Boolean)horizVec.get(curPos)).booleanValue();
        if(i%28==0)
        {
          rectA = new Rectangle(x, y, (int)(elt.divRatio*width), height);
          rectB = new Rectangle(x+(int)(elt.divRatio*width), y, (int)((1.0-elt.divRatio)*width), height);
          horizVec.add(new Boolean(true));
          horizVec.add(new Boolean(true));

        }
        else if(!hDiv)
        {
          rectA = new Rectangle(oR.x, oR.y, (int)(dRat*oR.width), oR.height);
          rectB = new Rectangle(oR.x+(int)(dRat*oR.width), oR.y, (int)((1.0-dRat)*oR.width), oR.height);
          horizVec.add(new Boolean(true));
          horizVec.add(new Boolean(true));

        }
        else
        {
          rectA = new Rectangle(oR.x, oR.y, oR.width, (int)(oR.height*dRat));
          rectB = new Rectangle(oR.x, oR.y+(int)(dRat*oR.height), oR.width, (int)((1.0-dRat)*oR.height));
          horizVec.add(new Boolean(false));
          horizVec.add(new Boolean(false));
        }

        rectList.add(rectA);
        rectList.add(rectB);
        g.setColor(elt.c1);
        g.fillRect(oR.x, oR.y, oR.width, oR.height);

        g.setColor(new Color(0,0,0,60));
        g.drawRect(oR.x, oR.y, oR.width, oR.height);

        g.setColor(elt.c2);

        g.fillRect(rectB.x, rectB.y, rectB.width, rectB.height);
        g.setColor(new Color(0,0,0,60));
       g.drawRect(rectB.x, rectB.y, rectB.width, rectB.height);

        curPos++;
      }

   }

  }
