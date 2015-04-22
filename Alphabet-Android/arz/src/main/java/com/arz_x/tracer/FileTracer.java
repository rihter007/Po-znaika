package com.arz_x.tracer;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class FileTracer implements ITracer
{
    public FileTracer(String _pathToFile, TraceLevel _minTraceLevel) throws CommonException
    {
        if ((_pathToFile == null) ||(_minTraceLevel == null))
            throw new CommonException(CommonResultCode.InvalidArgument);

        try
        {
            OutputStream fileStream = new FileOutputStream(_pathToFile, true);
            m_traceFile = new OutputStreamWriter(fileStream, "UTF-8");

            m_minTraceLevel = _minTraceLevel.getValue();
        }
        catch (FileNotFoundException exp)
        {
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        }
        catch (UnsupportedEncodingException exp)
        {
            throw new CommonException(CommonResultCode.CodeError);
        }
    }

    @Override
    public void traceMessage(TraceLevel traceLevel, String message)
    {
        if (traceLevel.getValue() >= m_minTraceLevel)
            internalTraceMessage(message);
    }

    private synchronized void internalTraceMessage(String message)
    {
        final Calendar currentDateTime = Calendar.getInstance();
        final int milliseconds = currentDateTime.get(Calendar.MILLISECOND);
        final int seconds = currentDateTime.get(Calendar.SECOND);
        final int minutes = currentDateTime.get(Calendar.MINUTE);
        final int hours = currentDateTime.get(Calendar.HOUR_OF_DAY);

        final long currentThreadId = Thread.currentThread().getId();

        String traceMessage = String.format("%d:%d:%d:%d\t%d\t%s\n"
                , hours, minutes, seconds, milliseconds, currentThreadId, message);

        try
        {
            m_traceFile.write(traceMessage);
            m_traceFile.flush();
        }
        catch (IOException exp) { /*Fatal assert*/ }
    }

    private OutputStreamWriter m_traceFile;
    private int m_minTraceLevel;
}
