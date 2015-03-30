package com.richdomapps.ticker;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by richard on 3/29/15.
 */
public class GroovyTextView extends TextView {

    public GroovyTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public GroovyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GroovyTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/coaster.ttf");
            setTypeface(tf);
        }
    }

}
