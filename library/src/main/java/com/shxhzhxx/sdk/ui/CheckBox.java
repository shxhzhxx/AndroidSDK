package com.shxhzhxx.sdk.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import androidx.core.widget.CompoundButtonCompat;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.shxhzhxx.sdk.R;

public class CheckBox extends androidx.appcompat.widget.AppCompatCheckBox {
    public CheckBox(Context context) {
        this(context, null);
    }

    public CheckBox(Context context, AttributeSet attrs) {
        this(context, attrs, androidx.appcompat.R.attr.checkboxStyle);
    }

    public CheckBox(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        int primaryColor = typedValue.data;

        int color_for_state_checked, color_for_state_normal;
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.CheckBox, 0, 0);
        try {
            color_for_state_checked = a.getColor(R.styleable.CheckBox_checkBoxCheckedColor, primaryColor);
            color_for_state_normal = a.getColor(R.styleable.CheckBox_checkBoxColor, Color.GRAY);
        } finally {
            a.recycle();
        }
        int states[][] = {{android.R.attr.state_checked}, {}};
        int colors[] = {color_for_state_checked, color_for_state_normal};
        CompoundButtonCompat.setButtonTintList(this, new ColorStateList(states, colors));
    }
}
