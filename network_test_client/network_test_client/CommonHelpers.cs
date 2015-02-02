using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Diagnostics;

namespace network_test_client
{
    static class CommonHelpers
    {
        public static string ReturnEmptyIfNull(string s)
        {
            return (s == null) ? string.Empty : s;
        }

        public static bool ProcessYesNoChoise()
        {
            bool result = false;
            while (true)
            {
                ConsoleKeyInfo key = Console.ReadKey();
                if (key.Key == ConsoleKey.Y)
                {
                    result = true;
                    break;
                }
                else if (key.Key == ConsoleKey.N)
                {
                    result = false;
                    break;
                }
            }

            return result;
        }

        public static void PrintAsciiStream(Stream stream)
        {
            Debug.Assert(stream != null);
            
            try
            {
                // fuck the System.Net.ConnectStream
                try
                {
                    stream.Seek(0, SeekOrigin.Begin);
                }
                catch { }

                using (MemoryStream ms = new MemoryStream())
                {
                    const int readBufferLength = 1024;
                    byte[] buffer = new byte[readBufferLength];
                    for (; ; )
                    {
                        int readBytes = stream.Read(buffer, 0, buffer.Length);
                        if (readBytes <= 0)
                            break;
                        ms.Write(buffer, 0, readBytes);
                    }
                    byte[] resultBuffer = ms.ToArray();
                    string textBuffer = ASCIIEncoding.ASCII.GetString(resultBuffer, 0, resultBuffer.Length);

                    using (ConsoleColorGuard colorGuard = new ConsoleColorGuard(ConsoleColor.Yellow))
                    {
                        Console.WriteLine(textBuffer);
                    }
                }
            }
            catch (Exception exp)
            {
                CommonHelpers.PrintColoredLine(string.Format("Failed to print stream, exception: \"{0}\"", exp.Message), ConsoleColor.Red);
            }
        }

        public static void PrintColored(string message, ConsoleColor messageColor)
        {
            using (ConsoleColorGuard colorGuard = new ConsoleColorGuard(messageColor))
            {
                Console.Write(message);
            }
        }

        public static void PrintColoredLine(string message, ConsoleColor messageColor)
        {
            using (ConsoleColorGuard colorGuard = new ConsoleColorGuard(messageColor))
            {
                Console.WriteLine(message);
            }
        }
    }
}
