package com.example.assignment03_v2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import java.lang.Math;

public class EditImage extends android.support.v7.widget.AppCompatImageView {
    private int lineSize; //Stores the line thickness
    private int my_color;
    private Bitmap bm;
    private Bitmap altered;
    private Canvas canvas;
    private Paint paint;
    private Matrix matrix;
    private float downx;
    private float downy;
    private float upx;
    private float upy;
    private String currentMode;
    private float radius;
    private float height;
    private float width;
    private float xAxis;
    private float yAxis;
    private float tempUx;
    private float tempUy;
    private float tempDx;
    private float tempDy;


    public EditImage(Context context){
        super(context);
        setup(null);
    }
    public EditImage(Context context, AttributeSet attrs){
        super(context, attrs);
        setup(attrs);
    }
    public EditImage(Context context, AttributeSet attrs, int defaultStyleAttr){
        super(context, attrs,defaultStyleAttr);
        setup(attrs);
    }

    private void setup(AttributeSet attrs) {
        lineSize = 2; //Stores the line thickness
        my_color = Color.BLACK;
        downx = 0;
        downy = 0;
        upx = 0;
        upy = 0;
        currentMode = "free form";
    }

    public void setColor(int newColor){
        my_color = newColor;
        paint.setColor(my_color);
    }
    public void setThickness(int nThickness){
        lineSize = nThickness;
        paint.setStrokeWidth(lineSize);
    }
    public void setUx(float nUx){
        upx = nUx;
    }
    public void setUy(float nUy){
        upy = nUy;
    }
    public void setDx(float nDx){
        downx = nDx;
    }
    public void setDy(float nDy){
        downy = nDy;
    }
    public void onImageSelect(Bitmap og, Bitmap changed){
        bm = og;
        altered = changed;
        canvas = new Canvas(altered);
        paint = new Paint();
        paint.setColor(my_color);
        paint.setStrokeWidth(lineSize);
        matrix = new Matrix();
        canvas.drawBitmap(og, matrix, paint);
        setImageBitmap(altered);

        height = canvas.getHeight();
        width = canvas.getWidth();
        xAxis = (canvas.getWidth() / 2);
        yAxis = (canvas.getHeight() / 2);
    }

    public void drawSomething(){
        switch(currentMode){
            case "free form":
                canvas.drawLine(downx, downy, upx, upy, paint);
                invalidate();
                break;
            case "rectangle":
                Log.i("debug", upx + ", " + upy + ", " + downx + ", " + downy );
                canvas.drawRect(downx, downy, upx, upy, paint);
                invalidate();
                break;
            case "circle":
                radius = (float) Math.sqrt(((upx - downx)* (upx - downx)) + ((upy-downy)*(upy - downy)));
                canvas.drawCircle(downx, downy, radius, paint);
                invalidate();
                break;
            case "symmetry":
                Log.i("sym", "symmetry working");
                canvas.drawLine(downx, downy, upx, upy, paint);
                if(downx > xAxis){tempDx = (xAxis - (downx - xAxis));}else{tempDx = (xAxis + (xAxis - downx));}
                if(downy > yAxis){tempDy = (yAxis - (downy - xAxis));}else{tempDy = (yAxis + (yAxis - downy));}
                if(upx > xAxis){tempUx = (xAxis - (upx - xAxis));}else{tempUx = (xAxis + (xAxis - upx));}
                if(upy > yAxis){tempUy = (yAxis - (upy - xAxis));}else{tempUy = (yAxis + (yAxis - upy));}
                canvas.drawLine(tempDx, tempDy, tempUx, tempUy, paint);
                invalidate();
                break;
            case "oval":
                Log.i("sym", "oval working");
                canvas.drawOval(downx, downy, upx, upy, paint);
                invalidate();
                break;
            case "borders":
                Log.i("sym", "borders worked");
                canvas.drawLine(0, 0, (width-1), 0, paint);
                canvas.drawLine(0, 0, 0, (height-1), paint);
                canvas.drawLine((width-1), (height-1),0, (height-1), paint);
                canvas.drawLine((width-1), (height-1), (width-1), 0, paint);
                invalidate();
                modeSwap(0);
                break;
            default: canvas.drawLine(downx, downy, upx, upy, paint); invalidate();
        }
    }

    public void modeSwap(int mode){
        switch(mode){
            case 0:
                currentMode = "free form";
                break;
            case 1:
                currentMode = "rectangle";
                break;
            case 2:
                currentMode = "circle";
                break;
            case 3:
                currentMode = "symmetry";
                break;
            case 4:
                currentMode = "oval";
                break;
            case 5:
                currentMode = "borders";
                break;
        }
        Log.i("mode", "mode is set to :" + currentMode);
    }
    public String getCurrentMode(){
        Log.i("mode", "mode is :" + currentMode);
        return currentMode;
    }

}
