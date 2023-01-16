package com.calamus.smartconningsystempanel.panels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TelegraphJoystickView extends JoystickView{
    public TelegraphJoystickView(Context context) {
        super(context);
    }

    public TelegraphJoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TelegraphJoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
    }

    public void setButtonColor(int color){
        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(color);
        button.setStyle(Paint.Style.FILL);
    }



    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;



        // painting the main circle
//        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius,
//                mainCircle);
        canvas.drawRect((int)centerX-(int)(joystickRadius/2),(int)centerY-joystickRadius-30,(int)centerX+(int)(joystickRadius/2),(int)centerY+joystickRadius+30,mainCircle);

        canvas.drawRect((int)centerX-(int)(joystickRadius/2),
                (int)centerY-joystickRadius-30,
                (int)centerX-(int)(joystickRadius/2)+5,
                (int)centerY+joystickRadius+30,
                horizontalLine);

        canvas.drawRect((int)centerX+(int)(joystickRadius/2)-5,
                (int)centerY-joystickRadius-30,
                (int)centerX+(int)(joystickRadius/2),
                (int)centerY+joystickRadius+30,
                horizontalLine);

        // painting the secondary circle
//        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius / 2,
//                secondaryCircle);
        // paint lines
        canvas.drawLine((float) centerX, (float) centerY, (float) centerX,
                (float) (centerY - joystickRadius), verticalLine);


        canvas.drawLine((float) centerX, (float) (centerY + joystickRadius),
                (float) centerX, (float) centerY, horizontalLine);

        // painting the move button
        canvas.drawRect(xPosition-60,(float) yPosition-30,xPosition+60,(float) yPosition+30,button);

        canvas.drawLine(xPosition-80, (float)yPosition,
                xPosition+80, yPosition, horizontalLine);

        int numberX=(int)centerX-(int)(joystickRadius/2);
        numberX=numberX+15;
        setUpGearBox(canvas,numberX);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int d = 400;

        setMeasuredDimension(d/2, d+70);

    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = (int) getWidth() / 2;
        yPosition = (int) getHeight() / 2;
        int d = Math.min(xNew, yNew);
        buttonRadius = (int) (d / 2 * 0.25);
        joystickRadius = (int) 200;

    }

    public void setJoystick(int gear){
        gear=gear*40;
        yPosition =(int)centerY-gear;
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }
        invalidate();

        if (onJoystickMoveListener != null)
            onJoystickMoveListener.onValueChanged(getAngle(), getPower(),getDirection());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPosition = (int) centerX;
        yPosition = (int)event.getY();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }
        invalidate();
        if (event.getAction() == MotionEvent.ACTION_UP) {
//            xPosition = (int) centerX;
//            yPosition = (int) centerY;
            thread.interrupt();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
                        getDirection());
        }
        if (onJoystickMoveListener != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();
           // if (onJoystickMoveListener != null) onJoystickMoveListener.onValueChanged(getAngle(), getPower(),getDirection());
        }
        return true;
    }

    private void setUpGearBox(Canvas canvas,int x){
        Paint paint = new Paint();
        paint.setColor(Color.BLACK);
        paint.setTextSize(30);
        canvas.drawText("1-", x, (float) centerY+47, paint);
        canvas.drawText("2-", x, (float) centerY+88, paint);
        canvas.drawText("3-", x, (float) centerY+129, paint);
        canvas.drawText("4-", x, (float) centerY+170, paint);
        canvas.drawText("5-", x, (float) centerY+211, paint);

        canvas.drawText("1-", x, (float) centerY-32, paint);
        canvas.drawText("2-", x, (float) centerY-73, paint);
        canvas.drawText("3-", x, (float) centerY-114, paint);
        canvas.drawText("4-", x, (float) centerY-155, paint);
        canvas.drawText("5-", x, (float) centerY-196, paint);



    }
}
