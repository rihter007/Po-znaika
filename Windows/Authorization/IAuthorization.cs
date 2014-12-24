using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace ru.pozniaka.authorization
{
    public interface IAuthorization
    {
        bool Authorize();
        bool Authorize(string login, string password);
        
        DateTime GetLicenseExpirationDate();
        DateTime GetLastLoginDate();
    }

    class Constant
    {
        public const string AuthentificationUrl = "http://software.po-znaika.com/authentication";
        public const string PathToCacheFile = "do9f1nv0a";
    }
}
