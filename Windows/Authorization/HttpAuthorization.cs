using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Net;

namespace ru.pozniaka.authorization
{
    public class HttpAuthorization : IAuthorization, IDisposable
    {
        private const int BufferLength = 5*1024;

        private const int MaLoginDelayInDays = 7;
        private const string LicesenseExpiration = "License-Expiration";

        public HttpAuthorization()
        {
            m_authCache = new FileCache(Constant.PathToCacheFile);
        }

        public bool Authorize()
        {
            return Authorize(m_authCache.Login, m_authCache.Password);
        }

        public bool Authorize(string login, string password)
        {
            bool authResult = false;
            try
            {
                HttpWebRequest webRequest = WebRequest.Create(Constant.AuthentificationUrl) as HttpWebRequest;
                webRequest.Method = "POST";

                {
                    Stream requestStream = webRequest.GetRequestStream();

                    {
                        string loginPart = string.Format("Login={0}\r\n", login);
                        byte[] loginPartBinary = Encoding.ASCII.GetBytes(loginPart);
                        requestStream.Write(loginPartBinary, 0, loginPartBinary.Length);
                    }

                    {
                        string passwordPart = string.Format("Password={0}\r\n", password);
                        byte[] passwordPartBinary = Encoding.ASCII.GetBytes(passwordPart);
                        requestStream.Write(passwordPartBinary, 0, passwordPartBinary.Length);
                    }

                    {
                        string platformPart = string.Format("Platform={0}\r\n", ru.po_znaika.common.Helpers.GetCurrentPlatform());
                        byte[] platformPartBinary = Encoding.ASCII.GetBytes(platformPart);
                        requestStream.Write(platformPartBinary, 0, platformPartBinary.Length);
                    }

                    requestStream.Flush();
                }

                HttpWebResponse webResponse = null;
                try
                {
                    webResponse = webRequest.GetResponse() as HttpWebResponse;
                }
                catch
                {
                    if (DateTime.Now - m_authCache.LastLoginDate <= TimeSpan.FromDays(MaLoginDelayInDays))
                        return true;
                }

                if (webResponse.StatusCode == HttpStatusCode.OK)
                {                    
                    m_authCache.LastLoginDate = DateTime.Now;
                    authResult = true;
                }
                else if (webResponse.StatusCode == HttpStatusCode.Forbidden)
                {
                    authResult = false;
                }
                else
                {
                    webResponse.Close();
                    throw new Exception("Login failed");
                }

                m_authCache.Login = login;
                m_authCache.Password = password;

                Stream responseStream = webResponse.GetResponseStream();
                if (responseStream.Length == 0)
                {
                    webResponse.Close();
                    throw new Exception("Web response data is empty");
                }

                byte[] responseData = new byte[responseStream.Length];
                if (responseStream.Read(responseData, 0, responseData.Length) != responseData.Length)
                {
                    webResponse.Close();
                    throw new Exception("Failed to get web response data");
                }

                DateTime licenseExpiration = DateTime.MinValue;

                IList<KeyValuePair<string, string>> valuePairs = ru.po_znaika.common.Helpers.ParseStringValuePairs(Encoding.ASCII.GetString(responseData), "=", "\r\n");
                foreach (var valuePair in valuePairs)
                {
                    if (string.Compare(valuePair.Key, LicesenseExpiration, true) == 0)
                    {
                        long dateBinary = 0;

                        if (long.TryParse(valuePair.Value, out dateBinary))
                        {
                            licenseExpiration = DateTime.FromBinary(dateBinary);
                        }
                    }
                }

                m_authCache.LicenseExpirationDate = licenseExpiration;

                if (DateTime.Now >= licenseExpiration)
                {
                    webResponse.Close();
                    throw new Exception("License is expired");
                }

                webResponse.Close();
                authResult = true;
            }
            catch
            {
                authResult = false;
            }
            return authResult;
        }

        public DateTime GetLicenseExpirationDate()
        {
            return m_authCache.LicenseExpirationDate;
        }

        public DateTime GetLastLoginDate()
        {
            return m_authCache.LastLoginDate;
        }

        public void Dispose()
        {
            m_authCache.Dispose();
        }

        private FileCache m_authCache;        
    }
}
