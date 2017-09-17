package activitytest.example.com.ballgame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by haha on 2017-08-08.
 */

public class MyRect {
    private float x, y, w, h;
    private Bitmap bmp;

    public MyRect(Bitmap bmp,  float x, float y) {
        this.x = x;
        this.y = y;
        this.bmp = bmp;
    }

    public void drawRect(Canvas canvas, Paint paint){

        canvas.drawBitmap(bmp, x, y, paint);
    }

    public void setX(float x) {

        this.x = x;
    }

    public void setY(float y) {

        this.y = y;
    }

    public float getW() {
        return bmp.getWidth();
    }

    public float getH() {
        return bmp.getHeight();
    }
}
