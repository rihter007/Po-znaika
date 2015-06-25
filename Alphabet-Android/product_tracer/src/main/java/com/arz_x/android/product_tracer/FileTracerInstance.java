package com.arz_x.android.product_tracer;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.tracer.FileTracer;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Rihter on 18.05.2015.
 * Helps keeping same trace file during complicated android application lifecycle
 */
public class FileTracerInstance implements ITracer
{
    public static final int Unlimited = 0;

    private static final String LogTag = FileTracerInstance.class.getName();

    private static final String TracerMaxFilesCount = "com.arz_x.android.product_tracer.FileTracerInstance.MaxFilesCount";
    private static final String TracerMaxFileSize = "com.arz_x.android.product_tracer.FileTracerInstance.MaxFileSize";
    private static final String TracerFileNameTag = "com.arz_x.android.product_tracer.FileTracerInstance.FileName";
    private static final String TraceLevelTag = "com.arz_x.android.product_tracer.FileTracerInstance.TraceLevel";

    private FileTracerInstance(int maxTraceFilesCount,
                              int maxTraceFileSize,
                              @NonNull TraceLevel minTraceLevel,
                              String tracesDirectory,
                              String existingTraceFile) throws CommonException
    {
        m_maxTraceFilesCount = maxTraceFilesCount;
        m_maxTraceFileSize = maxTraceFileSize;
        if (existingTraceFile != null)
        {
            m_tracesDirectory = new File(existingTraceFile).getParent();
            m_tracer = ProductTracer.openExistingFileTracer(existingTraceFile, minTraceLevel, maxTraceFileSize);
        }
        else
        {
            if (tracesDirectory == null)
                throw new CommonException(CommonResultCode.InvalidArgument);
            m_tracesDirectory = tracesDirectory;
            m_tracer = ProductTracer.createNewFileTracer(tracesDirectory, minTraceLevel, maxTraceFileSize);
        }

        initializeExistingTraceFiles();
    }

    public static FileTracerInstance createNewTracerInstance(int maxTraceFilesCount,
                                                             int maxTraceFileSize,
                                                             @NonNull TraceLevel minTraceLevel,
                                                             String tracesDirectory) throws CommonException
    {
        final String[] processingTraceFiles = ProductTracer.getAllProcessingTraceFiles(tracesDirectory);
        if (processingTraceFiles != null)
        {
            for (String fileName : processingTraceFiles)
            {
                try
                {
                    ProductTracer.finishTraceFile(fileName);
                }
                catch (CommonException exp)
                {
                    // should never happen, skip
                }
            }
        }

        return new FileTracerInstance(maxTraceFilesCount, maxTraceFileSize, minTraceLevel, tracesDirectory, null);
    }

    public static FileTracerInstance restoreExistingTracerInstance(@NonNull Bundle savedInstance) throws CommonException
    {
        if ((!savedInstance.containsKey(TracerFileNameTag))
                || (!savedInstance.containsKey(TraceLevelTag))
                || (!savedInstance.containsKey(TracerMaxFilesCount))
                || (!savedInstance.containsKey(TracerMaxFileSize)))
        {
            throw new CommonException(CommonResultCode.InvalidArgument);
        }

        final String traceFilePath = savedInstance.getString(TracerFileNameTag);
        final TraceLevel traceLevel = TraceLevel.getTypeByValue(savedInstance.getInt(TraceLevelTag));
        if ((traceFilePath == null) || (traceLevel == null))
            throw new CommonException(CommonResultCode.AssertError);
        return new FileTracerInstance(savedInstance.getInt(TracerMaxFileSize)
                , savedInstance.getInt(TracerMaxFilesCount)
                , traceLevel
                , null
                , traceFilePath);
    }

    public static FileTracerInstance getExistingTracerInstanceOrCreate(int maxTraceFilesCount,
                                                                       int maxTraceFileSize,
                                                                       @NonNull TraceLevel minTraceLevel,
                                                                       String tracesDirectory) throws CommonException
    {
        final String[] processingTraceFiles = ProductTracer.getAllProcessingTraceFiles(tracesDirectory);
        if ((processingTraceFiles == null) || (processingTraceFiles.length == 0))
            return new FileTracerInstance(maxTraceFilesCount, maxTraceFileSize, minTraceLevel, tracesDirectory, null);

        // get the 'newest' trace file if many
        int remainTraceFileIndex = 0;
        String remainTraceFile = processingTraceFiles[remainTraceFileIndex];
        for (int fileIndex = 1; fileIndex < processingTraceFiles.length; ++fileIndex)
        {
            if (remainTraceFile.compareToIgnoreCase(processingTraceFiles[fileIndex]) == -1)
            {
                remainTraceFileIndex = fileIndex;
                remainTraceFile =  processingTraceFiles[remainTraceFileIndex];
            }
        }

        // finish other
        for (int fileIndex = 0; fileIndex < processingTraceFiles.length; ++fileIndex)
        {
            try
            {
                if (fileIndex != remainTraceFileIndex)
                    ProductTracer.finishTraceFile(processingTraceFiles[fileIndex]);
            }
            catch (CommonException exp)
            {
                // silently skip all exceptions
            }
        }

        return new FileTracerInstance(maxTraceFilesCount, maxTraceFileSize, minTraceLevel, null, remainTraceFile);
    }

    public static void saveInstance(FileTracerInstance tracer, Bundle savedInstance)
    {
        if (tracer == null)
            return;

        tracer.saveInstance(savedInstance);
    }

    private void initializeExistingTraceFiles()
    {
        m_currentTraceFiles = new LinkedList<>();

        final String[] traceFiles = ProductTracer.getAllFinishedTraceFiles(m_tracesDirectory);
        if (traceFiles == null)
            return;

        Collections.addAll(m_currentTraceFiles, traceFiles);
        // assume that filenames contain datetime and are properly sorted
        Collections.sort(m_currentTraceFiles);

        processFilesCountOverflow();
    }

    public synchronized void saveInstance(@NonNull Bundle savedInstance)
    {
        savedInstance.putString(TracerFileNameTag, m_tracer.getFullPathToFile());
        savedInstance.putInt(TraceLevelTag, m_tracer.getTraceLevel().getValue());
        savedInstance.putInt(TracerMaxFilesCount, m_maxTraceFilesCount);
        savedInstance.putInt(TracerMaxFileSize, m_maxTraceFileSize);
    }

    public synchronized void traceMessage(TraceLevel traceLevel, String message)
    {
        try
        {
            if (m_tracer.isOverflow())
            {
                m_currentTraceFiles.add(ProductTracer.finishFileTracer(m_tracer));
                processFilesCountOverflow();
            }
        }
        catch (CommonException exp)
        {
            Log.e(LogTag, "Error on tracing message " + exp.getMessage());
        }

        m_tracer.traceMessage(traceLevel, message);
    }

    private synchronized void processFilesCountOverflow()
    {
        if ((m_maxTraceFileSize == Unlimited) || (m_maxTraceFileSize <= m_currentTraceFiles.size()))
            return;

        final int filesDeleteCount = m_currentTraceFiles.size() - m_maxTraceFilesCount;
        for (int fileIndex = 0; fileIndex < filesDeleteCount; ++fileIndex)
        {
            if (new File(m_currentTraceFiles.get(0)).delete())
                m_currentTraceFiles.remove(0);
        }
    }

    private int m_maxTraceFilesCount;
    private int m_maxTraceFileSize;
    private String m_tracesDirectory;
    private List<String> m_currentTraceFiles;
    private FileTracer m_tracer;
}
