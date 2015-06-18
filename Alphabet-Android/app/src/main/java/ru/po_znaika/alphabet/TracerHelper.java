package ru.po_znaika.alphabet;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.arz_x.CommonException;
import com.arz_x.android.product_tracer.FileTracerInstance;

import java.io.File;

/**
 * Created by Rihter on 17.06.2015.
 * Helper for one line restore trace
 */
public class TracerHelper
{
    public static String getTracesDirectory(@NonNull Context context)
    {
        return new File(context.getCacheDir(), Constant.Tracer.TraceDirectory).getAbsolutePath();
    }

    public static FileTracerInstance createFileTraceIfRestorationFailed(@NonNull Context context, Bundle savedInstance)
            throws CommonException
    {
        if (savedInstance != null)
        {
            try
            {
                return FileTracerInstance.restoreExistingTracerInstance(savedInstance);
            }
            catch (CommonException exp)
            {
                // just try bellow to create a new trace file
            }
        }

        return FileTracerInstance.createNewTracerInstance(Constant.Tracer.MaxTraceFilesCount
                , Constant.Tracer.MaxTraceFileSize
                , Constant.Tracer.MinTraceLevel
                , getTracesDirectory(context));
    }

    public static FileTracerInstance continueOrCreateFileTracer(@NonNull Context context, Bundle savedInstance)
            throws CommonException
    {
        if (savedInstance != null)
        {
            try
            {
                return FileTracerInstance.restoreExistingTracerInstance(savedInstance);
            }
            catch (CommonException exp)
            {
                // just try bellow to create a new trace file
            }
        }

        return FileTracerInstance.getExistingTracerInstanceOrCreate(Constant.Tracer.MaxTraceFilesCount
                , Constant.Tracer.MaxTraceFileSize
                , Constant.Tracer.MinTraceLevel
                , new File(getTracesDirectory(context), Constant.Tracer.TraceDirectory).getAbsolutePath());
    }
}
