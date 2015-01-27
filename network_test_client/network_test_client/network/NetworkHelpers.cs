using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;
using System.Diagnostics;

namespace network_test_client.network
{
    static class CommonHttpHeaders
    {
        public const string LoginHeader = "Login";
        public const string PasswordHeader = "Password";
        public const string ExerciseHeader = "Exercise";
    }

    static class NetworkHelpers
    {
        public static void PrintHttpWebRequest(HttpWebRequest request)
        {
            Debug.Assert(request != null);

            CommonHelpers.PrintColoredLine("Http request", ConsoleColor.Yellow);
            using (ConsoleColorGuard colorGuard = new ConsoleColorGuard(ConsoleColor.Green))
            {               
                Console.WriteLine("Url: \"{0}\"", request.RequestUri);
                Console.WriteLine(request.Method);
                Console.WriteLine("Headers:");
                Console.WriteLine(request.Headers);

                if (string.Equals(request.Method, "post", StringComparison.InvariantCultureIgnoreCase))
                    CommonHelpers.PrintAsciiStream(request.GetRequestStream(), request.ContentLength);                
            }
        }

        public static void PrintHttpWebResponse(HttpWebResponse response)
        {
            Debug.Assert(response != null);

            CommonHelpers.PrintColoredLine("Http response", ConsoleColor.Yellow);
            using (ConsoleColorGuard colorGuard = new ConsoleColorGuard(ConsoleColor.Green))
            {
                Console.WriteLine("StatusCode: {0}", response.StatusCode);
                Console.WriteLine("Headers:");
                Console.WriteLine(response.Headers);

                CommonHelpers.PrintAsciiStream(response.GetResponseStream(), response.ContentLength);               
            }
        }

        public static HttpWebRequest CreateHttpGetWebRequest(string url, string login, string password)
        {
            Debug.Assert(!string.IsNullOrEmpty(url));

            HttpWebRequest request = (HttpWebRequest)WebRequest.Create(url);
            request.Method = "GET";
            request.ContentLength = 0;

            if (!string.IsNullOrEmpty(login))
                request.Headers.Add(CommonHttpHeaders.LoginHeader, login);
            if (!string.IsNullOrEmpty(password))
                request.Headers.Add(CommonHttpHeaders.PasswordHeader, password);

            return request;
        }
    }
}
