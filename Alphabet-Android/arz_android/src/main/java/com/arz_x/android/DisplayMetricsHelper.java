package com.arz_x.android;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

/**
 * Created by Rihter on 09.07.2015.
 * Helper for scaling sizes
 */
public class DisplayMetricsHelper
{
    public int m_screenWidthInPx;
    public int m_screenHeightInPx;

    public DisplayMetricsHelper(@NonNull Activity _activity)
    {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        _activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        m_screenHeightInPx = displayMetrics.heightPixels;
        m_screenWidthInPx = displayMetrics.widthPixels;
    }

    public int getDisplayWidth()
    {
        return m_screenWidthInPx;
    }

    public int getDisplayHeight()
    {
        return m_screenHeightInPx;
    }

    public int getWidthInProportionPx(double proportion)
    {
        return (int)(m_screenWidthInPx * proportion);
    }

    public int getHeightInProportionPx(double proportion)
    {
        return (int)(m_screenHeightInPx * proportion);
    }

    public int getWidthInProportionPx(double proportion, int offset)
    {
        return (int)((m_screenWidthInPx - offset) * proportion);
    }

    public int getHeightInProportionPx(double proportion, int offset)
    {
        return (int)((m_screenHeightInPx - offset) * proportion);
    }
}
