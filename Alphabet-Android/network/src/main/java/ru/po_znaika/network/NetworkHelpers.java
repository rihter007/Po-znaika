package ru.po_znaika.network;

import android.support.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by Rihter on 04.03.2015.
 * Common helpers when working with network
 */
public class NetworkHelpers
{
    /**
     * Date format pattern used to parse HTTP date headers in ANSI C
     * <code>asctime()</code> format.
     */
    private static final String AsciiTimeDatePattern = "EEE MMM d HH:mm:ss yyyy";

    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");

    public static String getHttpDateRepresentation(@NonNull Date date)
    {
        SimpleDateFormat formatter = new SimpleDateFormat(AsciiTimeDatePattern, Locale.US);
        formatter.setTimeZone(GMT);
        return formatter.format(date);
    }
}
