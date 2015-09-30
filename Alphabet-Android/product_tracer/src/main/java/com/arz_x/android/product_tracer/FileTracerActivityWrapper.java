package com.arz_x.android.product_tracer;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.arz_x.CommonException;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.TraceLevel;

/**
 * Created by Rihter on 15.09.2015.
 * Wrapes tracer in activity life cycle
 */
public class FileTracerActivityWrapper implements ITracer
{
    private static final String FileNameTag = "com.arz_x.android.product_tracer.FileTracerActivityWrapper.FileName";

    public FileTracerActivityWrapper(@NonNull Context _context, Bundle savedInstanceState) throws CommonException
    {

    }

    public void traceMessage(TraceLevel traceLevel, String message)
    {
    }

    private ITracer m_tracer;
}
