package com.example.gentl.olearisweather.helpful;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.RelativeLayout;

// The class is needed for the ListView list item, since there are problems with the response by the Click
public class CheckableListItem extends RelativeLayout implements Checkable
{
    public CheckableListItem(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    private boolean checked;

    @Override
    public boolean isChecked()
    {
        return checked;
    }

    @Override
    public void setChecked(boolean checked)
    {
        this.checked = checked;
    }

    @Override
    public void toggle()
    {
        setChecked(!this.checked);
    }
}