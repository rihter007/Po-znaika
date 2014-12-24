using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading;
using System.IO;

namespace Tracer
{
    public interface ITracer
    {
        void TraceString(uint traceLevel, string traceString);
    }

    public class helpers
    {
        public const uint LowTraceLevel = 800;
        public const uint NormalTraceLevel = 400;
        public const uint HighTraceLevel = 200;
        public const uint CriticalTraceLevel = 100;

        public static void TraceString(ITracer obj, uint traceLevel, string traceString)
        {
            if (obj == null)
                return;
            obj.TraceString(traceLevel, traceString);
        }

        public static void SetTracer(ITracer tracer, object tracableObject)
        {
            ITracerVisitor tracerVisitor = tracableObject as ITracerVisitor;
            if (tracerVisitor != null)
                tracerVisitor.SetTracer(tracer);
        }
    }

    public interface ITracerVisitor
    {
        void SetTracer(ITracer tracer);
    }

    public class FileTracer : ITracer
    {
        public FileTracer(string filePath, bool isInvisible, bool reWrite, int linesCountPeriod, uint traceLevel)
        {
            m_traceFile = new FileStream(filePath, reWrite ? FileMode.Create : FileMode.Append);            
            //File.SetAttributes(filePath, (isInvisible) ? FileAttributes.Hidden : FileAttributes.Normal);

            m_traceFileStream = new StreamWriter(m_traceFile);
            m_traceFileStream.AutoFlush = true;            

            m_linesCountPeriod = linesCountPeriod;
            m_currentWrittenLines = 0;

            m_traceLevel = traceLevel;
        }

        public void TraceString(uint traceLevel, string traceString)
        {
            if ((string.IsNullOrEmpty(traceString)) || (m_traceLevel < traceLevel))
                return;

            lock (m_traceFile)
            {
                string traceLine = string.Format("{0}\t{1}\t{2}", DateTime.Now.ToString(), Thread.CurrentThread.ManagedThreadId.ToString(), traceString);
                m_traceFileStream.WriteLine(traceLine);

                if (m_linesCountPeriod > 0)
                {
                    ++m_currentWrittenLines;
                    if (m_currentWrittenLines == m_linesCountPeriod)
                    {
                        m_traceFile.SetLength(0);                        
                        m_currentWrittenLines = 0;
                    }
                }
            }
        }

        private uint m_traceLevel;
        private FileStream m_traceFile;
        private StreamWriter m_traceFileStream;

        private int m_linesCountPeriod;
        private int m_currentWrittenLines;
    }
}
