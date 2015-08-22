package ru.po_znaika.alphabet;

import android.support.annotation.NonNull;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Rihter on 11.07.2015.
 * Represents helper for managing framed_text_item layout
 */
public class FramedTextItem
{
    public static void setBorderColor(@NonNull RelativeLayout framedItemLayout, int color)
    {
       framedItemLayout.setBackgroundColor(color);
    }

    public static void setTextColor(@NonNull RelativeLayout framedItemLayout, int color)
    {
        TextView textView = (TextView) framedItemLayout.findViewById(R.id.textView);
        textView.setTextColor(color);
    }

    public static void setInternalColor(@NonNull RelativeLayout framedItemLayout, int color)
    {
        RelativeLayout internalLayout = (RelativeLayout)framedItemLayout.findViewById(R.id.internalLayout);
        internalLayout.setBackgroundColor(color);
    }

    public static void setInternalColorWithNoBorder(@NonNull RelativeLayout framedItemLayout, int color)
    {
        setInternalColor(framedItemLayout, color);
        setBorderColor(framedItemLayout, color);
    }

    public static void setTextSize(@NonNull RelativeLayout framedItemLayout, float textSize)
    {
        TextView textView = (TextView)framedItemLayout.findViewById(R.id.textView);
        textView.setTextSize(textSize);
    }

    public static void setText(@NonNull RelativeLayout framedItemLayout, char ch)
    {
        TextView textView = (TextView)framedItemLayout.findViewById(R.id.textView);
        textView.setText(((Character) ch).toString());
    }

    public static void setText(@NonNull RelativeLayout framedItemLayout, @NonNull String text)
    {
        TextView textView = (TextView)framedItemLayout.findViewById(R.id.textView);
        textView.setText(text);
    }

    public static String getText(@NonNull RelativeLayout framedItemLayout)
    {
        TextView textView = (TextView)framedItemLayout.findViewById(R.id.textView);
        return textView.getText().toString();
    }
}
