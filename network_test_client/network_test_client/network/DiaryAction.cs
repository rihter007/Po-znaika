using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;

namespace network_test_client.network
{
    class DiaryAction : INetworkAction
    {
        public void SetUrl(string url)
        {
            m_url = url;
        }

        public void SetLogin(string login)
        {
            m_login = login;
        }

        public void SetPassword(string password)
        {
            m_password = password;
        }

        public void Run()
        {
            if (string.IsNullOrEmpty(m_url))
                throw new ArgumentException();

            HttpWebRequest request = NetworkHelpers.CreateHttpGetWebRequest(m_url, m_login, m_password);
            CommonHelpers.PrintColored("Enter exercise literal id: ", ConsoleColor.Yellow);
            request.Headers.Add(CommonHttpHeaders.ExerciseHeader, Console.ReadLine());
            // See: https://msdn.microsoft.com/en-us/library/az4se3k1(v=vs.110).aspx
            CommonHelpers.PrintColored("Add start date header? (Y/N): ", ConsoleColor.Yellow);
            if (CommonHelpers.ProcessYesNoChoise())
                request.Headers.Add("Start_Date", (DateTime.UtcNow - TimeSpan.FromDays(2)).ToString("r"));
            Console.WriteLine();
            CommonHelpers.PrintColored("Add end date header? (Y/N): ", ConsoleColor.Yellow);
            if (CommonHelpers.ProcessYesNoChoise())
                request.Headers.Add("End_Date", DateTime.UtcNow.ToString("r"));
            Console.WriteLine();
            NetworkHelpers.PrintHttpWebRequest(request);

            try
            {
                NetworkHelpers.PrintHttpWebResponse((HttpWebResponse)request.GetResponse());
            }
            catch (Exception exp)
            {
                Console.WriteLine("An uknown exception occured: {0}", exp.Message);
            }
        }

        private string m_url;
        private string m_login;
        private string m_password;
    }
}
