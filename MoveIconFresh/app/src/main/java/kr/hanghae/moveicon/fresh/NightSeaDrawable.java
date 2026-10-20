package kr.hanghae.moveicon.fresh;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.PixelFormat;
import java.util.Random;

/** Lightweight procedural moonlit-ocean background; all artwork is local. */
public final class NightSeaDrawable extends Drawable {
 private final Paint paint=new Paint(3);
 @Override public void draw(Canvas c){
  android.graphics.Rect b=getBounds();float w=b.width(),h=b.height();
  paint.setShader(new LinearGradient(0,0,0,h,new int[]{0xff020b1c,0xff072650,0xff0c3552,0xff020e22},null,Shader.TileMode.CLAMP));
  c.drawRect(b.left,b.top,b.right,b.bottom,paint);paint.setShader(null);
  Random r=new Random(8472);
  for(int i=0;i<135;i++){
   float x=r.nextFloat()*w,y=r.nextFloat()*(h*.51f);
   paint.setColor((i%5==0)?0xffe6d3a0:0xffcadcff);
   paint.setAlpha(65+r.nextInt(125));
   c.drawCircle(x,y,i%7==0?2.2f:.8f,paint);
  }
  paint.setAlpha(255);
  float moonX=w*.81f, moonY=h*.16f, moon=Math.min(w,h)*.045f;
  paint.setColor(0x33fff3d1);c.drawCircle(moonX,moonY,moon*2.6f,paint);
  paint.setColor(0xffeedfbb);c.drawCircle(moonX,moonY,moon,paint);
  paint.setColor(0x99ffffff);c.drawCircle(moonX-moon*.2f,moonY-moon*.24f,moon*.7f,paint);
  float horizon=h*.43f;
  paint.setColor(0x99041831);
  android.graphics.Path coast=new android.graphics.Path();
  coast.moveTo(0,horizon);coast.lineTo(w*.22f,horizon-h*.025f);
  coast.lineTo(w*.41f,horizon+h*.012f);coast.lineTo(w*.62f,horizon-h*.013f);
  coast.lineTo(w,horizon+h*.015f);coast.lineTo(w,horizon+h*.065f);
  coast.lineTo(0,horizon+h*.06f);coast.close();c.drawPath(coast,paint);
  r=new Random(8504);
  for(int n=0;n<105;n++){
   float y=horizon+r.nextFloat()*(h-horizon),x=r.nextFloat()*w;
   float broad=(y-horizon)/(h-horizon);
   int gold=(n%5==0)?0xffb99558:0xff6a93b2;
   paint.setColor(gold);paint.setAlpha(35+r.nextInt(50));
   float length=4+broad*(10+r.nextInt(85));
   c.drawRoundRect(x,y,x+length,y+1+broad*2,2,2,paint);
  }
  paint.setColor(0xffcfb06a);paint.setAlpha(70);
  for(int n=0;n<25;n++){
   float y=horizon+n*(h-horizon)/25f;float drift=(n%3)*w*.007f;
   float reach=(y-horizon)/(h-horizon);
   c.drawRoundRect(moonX-drift-reach*w*.09f,y,moonX+drift+reach*w*.09f,y+1.8f,2,2,paint);
  }
  paint.setAlpha(255);
 }
 @Override public void setAlpha(int a){paint.setAlpha(a);}
 @Override public void setColorFilter(android.graphics.ColorFilter f){paint.setColorFilter(f);}
 @Override public int getOpacity(){return PixelFormat.OPAQUE;}
}