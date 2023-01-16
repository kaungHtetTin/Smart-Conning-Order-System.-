package com.calamus.smartconningsystempanel.panels;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PortEngineJoystickView extends JoystickView{
    public PortEngineJoystickView(Context context) {
        super(context);
    }

    public PortEngineJoystickView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortEngineJoystickView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}
