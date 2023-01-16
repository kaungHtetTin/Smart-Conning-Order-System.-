package com.calamus.smartconningsystempanel.panels;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

public class SteeringGaugeView extends GaugeView{

    private int [] scales={30,25,20,15,10,5,0,5,10,15,20,25,30};
    public static final float[] RANGE_VALUES = {16.0f, 25.0f, 40.0f, 100.0f};

    public SteeringGaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SteeringGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SteeringGaugeView(Context context) {
        super(context);
    }

    @Override
    public void drawScale(final Canvas canvas) {

        canvas.save();
        // On canvas, North is 0 degrees, East is 90 degrees, South is 180 etc.
        // We start the scale somewhere South-West so we need to first rotate the canvas.
        canvas.rotate(mScaleRotation, 0.5f, 0.5f);
        final int totalTicks = mDivisions * mSubdivisions + 1;

        Log.e("totalTicks ",totalTicks+"");
        int index=0;
        for (int i = 0; i < totalTicks; i++) {

            final float y1 = mScaleRect.top;
            final float y2 = y1 + 0.015f; // height of division
            final float y3 = y1 + 0.045f; // height of subdivision

            final float value = getValueForTick(i);
            //final Paint paint = getRangePaint(mScaleStartValue + value);
            final Paint paint = getRangePaint(value);

            float mod = value % mDivisionValue;
            /*if ((Math.abs(mod - 0) < 0.001) || (Math.abs(mod - mDivisionValue) < 0.001)) {
				// Draw a division tick
				canvas.drawLine(0.5f, y1, 0.5f, y3, paint);
				// Draw the text 0.15 away from the division tick
				drawTextOnCanvasWithMagnifier(canvas, valueString(value), 0.5f, y3 + 0.045f, paint);
			} else {
				// Draw a subdivision tick
				canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
			}*/
            if ((Math.abs(mod - 0) < 0.001) || (Math.abs(mod - mDivisionValue) < 0.001)) {
                // Draw a division tick
                canvas.drawLine(0.5f, y1, 0.5f, y3, paint);
                // Draw the text 0.15 away from the division tick
                drawTextOnCanvasWithMagnifier(canvas, scales[index]+"", 0.5f, y3 + 0.045f, paint);

                index++;

            } else {
                // Draw a subdivision tick
                canvas.drawLine(0.5f, y1, 0.5f, y2, paint);
            }


            canvas.rotate(mSubdivisionAngle, 0.5f, 0.5f);
        }
        canvas.restore();
    }

    @Override
    public void drawTextOnCanvasWithMagnifier(Canvas canvas, String text, float x, float y, Paint paint) {
        if (android.os.Build.VERSION.SDK_INT <= 15) {
            //draw normally
            canvas.drawText(text, x, y, paint);
        }
        else {
            //workaround
            float originalStrokeWidth = paint.getStrokeWidth();
            float originalTextSize = paint.getTextSize();
            final float magnifier = 1000f;

            canvas.save();
            canvas.scale(1f / magnifier, 1f / magnifier);

            paint.setTextSize(originalTextSize * magnifier);
            paint.setStrokeWidth(originalStrokeWidth * magnifier);

            canvas.drawText(text, x * magnifier, y * magnifier, paint);
            canvas.restore();

            paint.setTextSize(originalTextSize);
            paint.setStrokeWidth(originalStrokeWidth);
        }
    }

}
