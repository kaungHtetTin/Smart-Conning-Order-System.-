package com.calamus.smartconningsystempanel.panels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class SteeringJoystickView extends JoystickView{

    Paint border;

    public SteeringJoystickView(Context context) {
        super(context);
    }

    public SteeringJoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SteeringJoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
    }

    @Override
    protected void initJoystickView() {
        mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainCircle.setColor(Color.WHITE);
        mainCircle.setStyle(Paint.Style.FILL_AND_STROKE);

        secondaryCircle = new Paint();
        secondaryCircle.setColor(Color.GREEN);
        secondaryCircle.setStyle(Paint.Style.STROKE);

        verticalLine = new Paint();
        verticalLine.setStrokeWidth(5);
        verticalLine.setColor(Color.RED);

        horizontalLine = new Paint();
        horizontalLine.setStrokeWidth(2);
        horizontalLine.setColor(Color.BLACK);

        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.RED);
        button.setStyle(Paint.Style.FILL);

        border=new Paint(Paint.ANTI_ALIAS_FLAG);;
        border.setColor(Color.BLACK);

    }


    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;


        // painting the main circle
        canvas.drawRect((int)centerX-joystickRadius,(int)centerY-70,(int)centerX+joystickRadius,(int)centerY+70,mainCircle);

//        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius,
//                mainCircle);
        // painting the secondary circle
//        canvas.drawCircle((int) centerX, (int) centerY, joystickRadius / 2,
//                secondaryCircle);
        // paint lines
        canvas.drawLine((float) (centerX - joystickRadius), (float) centerY,
                (float) (centerX + joystickRadius), (float) centerY,
                horizontalLine);
//        canvas.drawLine((float) centerX, (float) (centerY + joystickRadius),
//                (float) centerX, (float) centerY, horizontalLine);

        // painting the move button
        canvas.drawCircle(xPosition, yPosition, buttonRadius, button);

        canvas.drawRect((int)centerX-joystickRadius,(int)centerY-70,(int)centerX+joystickRadius,(int)centerY-65,border);

        canvas.drawRect((int)centerX-joystickRadius,(int)centerY+65,(int)centerX+joystickRadius,(int)centerY+70,border);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        xPosition = (int) event.getX();
        yPosition = (int)centerY;
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }
        invalidate();
        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = (int) centerX;
            yPosition = (int) centerY;
            thread.interrupt();
//            if (onJoystickMoveListener != null)
//                onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
//                        getDirection());
        }
        if (onJoystickMoveListener != null
                && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (thread != null && thread.isAlive()) {
                thread.interrupt();
            }
            thread = new Thread(this);
            thread.start();
            if (onJoystickMoveListener != null)
                onJoystickMoveListener.onValueChanged(getAngle(), getPower(),
                        getDirection());
        }
        return true;
    }
}
