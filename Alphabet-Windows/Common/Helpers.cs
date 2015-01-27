using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ru.po_znaika.common
{
    public static class Helpers
    {
        public static IList<KeyValuePair<string, string>> ParseStringValuePairs(string dataLiteral, string valuesDelimiter, string valuePairDelimiter)
        {
            List<KeyValuePair<string, string>> values = new List<KeyValuePair<string, string>>();

            int currentIndex = 0;
            while (currentIndex < dataLiteral.Length)
            {
                int newDelimiterIndex = dataLiteral.IndexOf(valuePairDelimiter, currentIndex, currentIndex);

                string valuePairLiteral = string.Empty;
                if (newDelimiterIndex == -1)
                {
                    valuePairLiteral = dataLiteral.Substring(currentIndex, dataLiteral.Length);
                    currentIndex = dataLiteral.Length;
                }
                else
                {
                    valuePairLiteral = dataLiteral.Substring(currentIndex, newDelimiterIndex); 
                    currentIndex = newDelimiterIndex + valuePairDelimiter.Length;
                }

                int valuesDelimiterIndex = valuePairLiteral.IndexOf(valuesDelimiter);
                if (valuesDelimiterIndex == -1)
                {
                    KeyValuePair<string, string> valuePair = new KeyValuePair<string, string>(valuePairLiteral, string.Empty);
                    values.Add(valuePair);
                }
                else
                {
                    KeyValuePair<string, string> valuePair = new KeyValuePair<string, string>(valuePairLiteral.Substring(0, valuesDelimiterIndex), valuePairLiteral.Substring(valuesDelimiterIndex + valuesDelimiter.Length));
                    values.Add(valuePair);
                }
            }

            return values;
        }

        public static string GetCurrentPlatform()
        {
            return string.Format("{0}:{1}.{2}.{3}.{4}",
                ConvertPlatform(System.Environment.OSVersion.Platform),
                System.Environment.OSVersion.Version.Major,
                System.Environment.OSVersion.Version.Minor,
                System.Environment.OSVersion.Version.Revision,
                System.Environment.OSVersion.Version.Build);
        }

        private static string ConvertPlatform(PlatformID platform)
        {
            string result = string.Empty;
            switch (platform)
            {
                case PlatformID.MacOSX:
                    result = "MacOS";
                    break;

                case PlatformID.WinCE:
                    result = "WindowsCE";
                    break;

                case PlatformID.Win32NT:
                    result = "WindowsNt";
                    break;

                case PlatformID.Win32S:
                    result = "WindowsS";
                    break;

                case PlatformID.Win32Windows:
                    result = "Windows9x";
                    break;
            }

            return result;
        }
    }
}
