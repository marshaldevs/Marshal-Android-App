package com.basmapp.marshal.util;

import android.content.Context;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import com.basmapp.marshal.interfaces.OnHashTagClickListener;

public class HashTag extends ClickableSpan {

    OnHashTagClickListener onClickListener;
    Context context;
    TextPaint textPaint;

    public HashTag(Context ctx) {
        super();
        context = ctx;
    }

    public HashTag(Context ctx, OnHashTagClickListener clickListener) {
        super();
        context = ctx;
        this.onClickListener = clickListener;
    }

    @Override
    public void updateDrawState(TextPaint ds) {
        textPaint = ds;
        ds.setColor(ds.linkColor);
        ds.setARGB(255, 30, 144, 255);
    }

    @Override
    public void onClick(View widget) {
        TextView tv = (TextView) widget;
        Spanned s = (Spanned) tv.getText();
        int start = s.getSpanStart(this);
        int end = s.getSpanEnd(this);
        String theWord = s.subSequence(start + 1, end).toString();
        onClickListener.onClick(theWord);
    }
}
