package com.arz_x.tracer;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;

public class FileTracer implements ITracer, Closeable
{
    public static long UnLimitedFileSize = 0;

    public FileTracer(String pathToFile, boolean appendIfExist, TraceLevel minTraceLevel, long maxFileSize) throws CommonException
    {
        if ((pathToFile == null) ||(minTraceLevel == null))
            throw new CommonException(CommonResultCode.InvalidArgument);

        try
        {
            FileOutputStream fileStream = new FileOutputStream(pathToFile, appendIfExist);
            m_traceFile = new OutputStreamWriter(fileStream, "UTF-8");
            m_currentTraceSize = new File(pathToFile).length();
            m_maxFileSize = maxFileSize;
            m_pathToFile = pathToFile;
            m_minTraceLevel = minTraceLevel.getValue();
        }
        catch (FileNotFoundException exp)
        {
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        }
        catch (UnsupportedEncodingException exp)
        {
            throw new CommonException(CommonResultCode.AssertError);
        }
    }

    public FileTracer(String pathToFile, boolean appendIfExist, TraceLevel minTraceLevel) throws CommonException
    {
       this(pathToFile, appendIfExist, minTraceLevel, UnLimitedFileSize);
    }

    public String getFullPathToFile()
    {
        return m_pathToFile;
    }

    public TraceLevel getTraceLevel()
    {
        return TraceLevel.getTypeByValue(m_minTraceLevel);
    }

    public boolean isOverflow()
    {
        return m_maxFileSize != UnLimitedFileSize && m_currentTraceSize >= m_maxFileSize;
    }

    @Override
    public void traceMessage(TraceLevel traceLevel, String message)
    {
        if (traceLevel.getValue() >= m_minTraceLevel)
            internalTraceMessage(message);
    }

    @Override
    public synchronized void close()
    {
        try
        {
            m_traceFile.flush();
            m_traceFile.close();
            m_traceFile = null;
        }
        catch (IOException exp)
        {
            /* it is impossible */
        }
    }

    private synchronized void internalTraceMessage(String message)
    {
        // check if the file is closed already
        if ((m_traceFile == null) || (isOverflow()))
            return;

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
            m_currentTraceSize += traceMessage.length();
        }
        catch (IOException exp) { /*Fatal assert*/ }
    }

    private String m_pathToFile;
    private OutputStreamWriter m_traceFile;
    private volatile long m_currentTraceSize;
    private volatile long m_maxFileSize;
    private volatile int m_minTraceLevel;
}
