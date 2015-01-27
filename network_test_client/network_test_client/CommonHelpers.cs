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

        public static void PrintAsciiStream(Stream stream, long streamLength)
        {
            Debug.Assert(stream != null);

            if (streamLength <= 0)
                return;

            try
            {
                // fuck the System.Net.ConnectStream
                try
                {
                    stream.Seek(0, SeekOrigin.Begin);
                }
                catch { }

                byte[] buffer = new byte[streamLength];
                stream.Read(buffer, 0, buffer.Length);
                string textBuffer = ASCIIEncoding.ASCII.GetString(buffer, 0, buffer.Length);

                using (ConsoleColorGuard colorGuard = new ConsoleColorGuard(ConsoleColor.Yellow))
                {
                    Console.WriteLine(textBuffer);
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
