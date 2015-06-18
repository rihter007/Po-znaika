package com.arz_x.tracer;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.ResultCodeException;
import com.arz_x.common.StringHelper;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by Rihter on 27.04.2015.
 * Represents a helper for managing product traces
 */
public class ProductTracer
{
    private static final String ProcessingTracePrefix = "_progress_";
    private static final String TraceFilePrefix = "trace_";
    private static final String TraceFileExtension = ".log";
    private static final String TraceFileNamePattern = ProcessingTracePrefix + "%d.%d.%d_%d:%d:%d:%d" + TraceFileExtension;

    public static void traceMessage(ITracer tracer, TraceLevel traceLevel, String message)
    {
        if (tracer == null)
            return;
        tracer.traceMessage(traceLevel, StringHelper.getEmptyIfNull(message));
    }

    public static void traceMessage(ITracer tracer, TraceLevel traceLevel, String componentTag, String message)
    {
        if (tracer == null)
            return;

        tracer.traceMessage(traceLevel, String.format("[%s]\t%s", componentTag, StringHelper.getEmptyIfNull(message)));
    }

    public static void traceException(ITracer tracer, TraceLevel traceLevel, String componentTag, Throwable exception)
    {
        if (tracer == null)
            return;

        if (exception == null)
        {
            traceMessage(tracer, traceLevel, componentTag, "Null exception object");
            return;
        }

        String traceMessage = String.format("exp: '%s'", exception.getClass().getName());
        if (exception instanceof ResultCodeException)
        {
            traceMessage += String.format(" result code: '%d'", ((ResultCodeException) exception).getRawResultCode());
        }
        else
        {
            final String exceptionMessage = exception.getMessage();
            if (exceptionMessage != null)
                traceMessage += String.format(" message: '%s", exceptionMessage);
        }

        final StackTraceElement[] callStack = exception.getStackTrace();
        if (callStack != null)
        {
            final int elementsCount = Math.min(5, callStack.length);
            for (int elementIndex = 0; elementIndex < elementsCount; ++elementIndex)
            {
                final StackTraceElement stackElement = callStack[elementIndex];
                traceMessage += String.format("\n [%s:%s] %s:%s"
                        , stackElement.getFileName()
                        , stackElement.getLineNumber()
                        , stackElement.getClassName()
                        , stackElement.getMethodName());
            }
        }

        tracer.traceMessage(traceLevel, traceMessage);
    }

    public static String[] getAllTraceFiles(String tracesDirectory)
    {
        return getTraceFiles(tracesDirectory, new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return (filename.startsWith(TraceFilePrefix) || filename.startsWith(ProcessingTracePrefix)) && filename.endsWith(TraceFileExtension);
            }
        });
    }

    public static String[] getAllFinishedTraceFiles(String tracesDirectory)
    {
        return getTraceFiles(tracesDirectory, new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return filename.startsWith(TraceFilePrefix) && filename.endsWith(TraceFileExtension);
            }
        });
    }

    public static String[] getAllProcessingTraceFiles(String tracesDirectory)
    {
        return getTraceFiles(tracesDirectory, new FilenameFilter()
        {
            @Override
            public boolean accept(File dir, String filename)
            {
                return filename.startsWith(ProcessingTracePrefix) && filename.endsWith(TraceFileExtension);
            }
        });
    }

    private static String[] getTraceFiles(String tracesDirectory, FilenameFilter filenameFilter)
    {
        File[] traceFiles = new File(tracesDirectory).listFiles(filenameFilter);
        if (traceFiles == null)
            return null;

        String[] resultFileNames = new String[traceFiles.length];
        for (int fileIndex = 0; fileIndex < resultFileNames.length; ++fileIndex)
            resultFileNames[fileIndex] = traceFiles[fileIndex].getAbsolutePath();

        return resultFileNames;
    }

    public static FileTracer createNewFileTracer(String tracesDirectory, TraceLevel minTraceLevel, long maxFileSize)
            throws CommonException
    {
        try
        {
            File tracesDirectoryObj = new File(tracesDirectory);
            if ((!tracesDirectoryObj.isDirectory()) && (!tracesDirectoryObj.mkdirs()))
                throw new CommonException(CommonResultCode.InvalidArgument);
        }
        catch (SecurityException exp)
        {
            throw new CommonException(CommonResultCode.AccessDenied);
        }

        return new FileTracer(new File(tracesDirectory, createTraceFileName()).getAbsolutePath(), false, minTraceLevel, maxFileSize);
    }

    public static FileTracer openExistingFileTracer(String pathToTraceFile, TraceLevel minTraceLevel, long maxFileSize)
            throws CommonException
    {
        return new FileTracer(pathToTraceFile, true, minTraceLevel, maxFileSize);
    }

    public static String finishTraceFile(String traceFilePath) throws CommonException
    {
        File oldTraceFile = new File(traceFilePath);
        final String oldTraceFileName = oldTraceFile.getName();
        if (!oldTraceFileName.startsWith(ProcessingTracePrefix))
            throw new CommonException(CommonResultCode.InvalidArgument);

        final String newTraceFileName = TraceFilePrefix + oldTraceFileName.substring(ProcessingTracePrefix.length());
        final File finishedTraceFile = new File(oldTraceFile.getParent(), newTraceFileName);

        if (!oldTraceFile.renameTo(finishedTraceFile))
            throw new CommonException(CommonResultCode.AccessDenied);

        return finishedTraceFile.getAbsolutePath();
    }

    public static String finishFileTracer(FileTracer fileTracer) throws CommonException
    {
        fileTracer.close();
        return finishTraceFile(fileTracer.getFullPathToFile());
    }

    private static String createTraceFileName()
    {
        final Calendar currentTime = new GregorianCalendar();
        return String.format(TraceFileNamePattern,
                currentTime.get(Calendar.YEAR),
                currentTime.get(Calendar.MONTH),
                currentTime.get(Calendar.DAY_OF_MONTH),
                currentTime.get(Calendar.HOUR_OF_DAY),
                currentTime.get(Calendar.MINUTE),
                currentTime.get(Calendar.SECOND),
                currentTime.get(Calendar.MILLISECOND));
    }

    private ProductTracer() { }
}
