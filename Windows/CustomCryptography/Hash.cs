using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Security.Cryptography;

namespace CustomCryptography
{
    public static class Hash
    {
        public static string CalculateSHA1Literal(Stream stream)
        {
            SHA1 shaHash = new SHA1CryptoServiceProvider();

            byte[] hashBytes = shaHash.ComputeHash(stream);
            if (hashBytes == null)
                return null;

            return BitConverter.ToString(hashBytes).Replace("-",  "");
        }
    }
}
