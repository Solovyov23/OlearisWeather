package com.example.gentl.olearisweather;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageButton;

// In this case, stores only one method for displaying the animation of the view elements
public class Helper
{
    final static OvershootInterpolator overshootInterpolator = new OvershootInterpolator();
    public static void animateViewVisibility(final View view)
    {
        float scaleFrom = 0.95f;
        if(view instanceof Button || view instanceof ImageButton) scaleFrom = 0f;
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", scaleFrom, 1);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", scaleFrom, 1);
        AnimatorSet animSetXY = new AnimatorSet();
        animSetXY.playTogether(scaleX, scaleY);
        animSetXY.setInterpolator(overshootInterpolator);
        animSetXY.setDuration(500);
        animSetXY.start();
        view.setVisibility(View.VISIBLE);
    }
}
