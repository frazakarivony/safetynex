package com.example.myapplication;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import com.andremion.counterfab.CounterFab;

public class TextFab extends CounterFab {

    String mText;
    GestureDetector gestureDetector;

    public TextFab(Context context) {
        super(context);
    }

    public TextFab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TextFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context,attrs,defStyleAttr);
    }

    public void setText(String text){
        this.mText = text;
    }

}
