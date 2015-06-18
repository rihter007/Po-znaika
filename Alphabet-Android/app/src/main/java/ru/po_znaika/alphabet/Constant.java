package ru.po_znaika.alphabet;

import com.arz_x.tracer.TraceLevel;

/**
 * Created by Rihter on 21.08.2014.
 * Contains general constants for application module. Must NOT be shared with anything else
 */
public final class Constant
{
    public static final class Color
    {
        public static final int NoColor = 0;             // android.R.color.transparent;
        public static final int LightBlue = -13388315;   // #ff33b5e5
        public static final int LightGreen = -6697984;   // #ff99cc00 android.R.color.holo_green_light;
        public static final int LightRed = -48060;       // ffff4444 android.R.color.holo_red_light;
    }

    public static final class Tracer
    {
        public static final int MaxTraceFilesCount = 5;
        public static final int MaxTraceFileSize = 2 * 1024 * 1024;
        public static final TraceLevel MinTraceLevel = TraceLevel.Debug;
        public static final String TraceDirectory = "trace_files";
    }

    public static final String NewLineDelimiter = "</br>";
}
