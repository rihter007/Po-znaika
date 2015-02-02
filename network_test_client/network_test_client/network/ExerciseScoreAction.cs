using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;

namespace network_test_client.network
{
    class ExerciseScoreAction : INetworkAction
    {
        private const string DefaultUrl = NetworkConstants.PoZnaikaDomainName + "/score";

        public void SetUrl(string url)
        {
            m_url = url;
        }

        public string GetUrl()
        {
            return m_url;
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
                throw new ArgumentException("Incorrect url");

            HttpWebRequest request = NetworkHelpers.CreateHttpGetWebRequest(m_url, m_login, m_password);
            CommonHelpers.PrintColored("Enter exercise literal id: ", ConsoleColor.Yellow);
            request.Headers.Add(NetworkConstants.ExerciseHeader, Console.ReadLine());            
            CommonHelpers.PrintColored("Enter score: ", ConsoleColor.Yellow);
            request.Headers.Add("Score", Console.ReadLine());
            request.Date = DateTime.UtcNow;
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

        private string m_url = DefaultUrl;
        private string m_login;
        private string m_password;
    }
}
