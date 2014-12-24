using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Xml;
using System.Security.Cryptography;

namespace ru.pozniaka.authorization
{
    class FileCache : IDisposable
    {
        private const string LoginNodeName = "login";
        private const string PasswordNodeName = "password";
        private const string LicenseExpirationDateNodeName = "license_expiration";
        private const string LastLoginDateNodeName = "last_login";

        public FileCache(string pathToFile)
        {
            if (string.IsNullOrWhiteSpace(pathToFile))
                throw new ArgumentException("patoToFile must be a valid file name or path");

            m_cacheFileName = pathToFile;
            m_fileDocument = new XmlDocument();

            m_login = m_password = string.Empty;
            m_licenseExpirationDate = m_lastLoginDate = DateTime.MinValue;            

            if (!ParseFile(pathToFile))
                throw new Exception("File parse exception");

            m_isLoginChanged = m_isPasswordChanged = m_isLicenseExpirationDateChanged = m_isLastLoginDateChanged = false;
        }

        public string Login
        {
            get
            {
                return m_login;
            }

            set
            {
                if (m_login != value)
                {
                    m_isLoginChanged = true;
                    m_login = value;
                }
            }
        }

        public string Password
        {
            get
            {
                return m_password;
            }

            set
            {
                if (m_password != value)
                {
                    m_isPasswordChanged = true;
                    m_password = value;
                }
            }
        }

        public DateTime LicenseExpirationDate
        {
            get
            {
                return m_licenseExpirationDate;
            }

            set
            {
                if (m_licenseExpirationDate != value)
                {
                    m_isLicenseExpirationDateChanged = true;
                    m_licenseExpirationDate = value;
                }
            }
        }

        public DateTime LastLoginDate
        {
            get
            {
                return m_lastLoginDate;
            }

            set
            {
                if (m_lastLoginDate != value)
                {
                    m_isLastLoginDateChanged = true;
                    m_lastLoginDate = value;
                }
            }
        }

        public void Dispose()
        {
            ///
            /// Save file result
            ///
            if (m_isLoginChanged)
            {
                XmlNode loginNode = m_fileDocument.FirstChild.SelectSingleNode(LoginNodeName);
                loginNode.InnerText = m_login;
            }

            if (m_isPasswordChanged)
            {
                XmlNode passwordNode = m_fileDocument.FirstChild.SelectSingleNode(PasswordNodeName);
                passwordNode.InnerText = m_password;
            }

            if (m_isLicenseExpirationDateChanged)
            {
                XmlNode licenseExpirationNode = m_fileDocument.FirstChild.SelectSingleNode(LicenseExpirationDateNodeName);
                licenseExpirationNode.InnerText = m_licenseExpirationDate.ToBinary().ToString();
            }

            if (m_isLastLoginDateChanged)
            {
                XmlNode lastLoginNode = m_fileDocument.FirstChild.SelectSingleNode(LastLoginDateNodeName);
                lastLoginNode.InnerText = m_lastLoginDate.ToBinary().ToString();
            }

            using (MemoryStream documentData = new MemoryStream())
            {
                m_fileDocument.Save(documentData);

                using (AesCryptoServiceProvider cryptoProvider = new AesCryptoServiceProvider())
                {
                    using (ICryptoTransform encryptor = cryptoProvider.CreateEncryptor(m_aesKey, m_aesInitializationVerctor))
                    {
                        using (MemoryStream encryptedData = new MemoryStream())
                        {
                            using (CryptoStream csEncrypt = new CryptoStream(encryptedData, encryptor, CryptoStreamMode.Write))
                            {
                                documentData.CopyTo(csEncrypt);
                            }

                            File.WriteAllBytes(m_cacheFileName, encryptedData.GetBuffer());                            
                        }
                    }
                }

                
            }
        }

        private bool ParseFile(string pathToFile)
        {
            bool result = false;

            byte[] fileData = null;
            try
            {
                fileData = File.ReadAllBytes(pathToFile);
            }
            catch
            {
                fileData = null;
            }

            try
            {
                bool isInvalidDocumentFormat = true;

                if (fileData != null)
                {
                    try
                    {
                        ///
                        /// Decrypt data
                        ///

                        using (AesCryptoServiceProvider cryptoProvider = new AesCryptoServiceProvider())
                        {
                            using (ICryptoTransform aesDecryptor = cryptoProvider.CreateDecryptor(m_aesKey, m_aesInitializationVerctor))
                            {
                                using (MemoryStream msDecrypt = new MemoryStream(fileData))
                                {
                                    using (CryptoStream csDecrypt = new CryptoStream(msDecrypt, aesDecryptor, CryptoStreamMode.Read))
                                    {
                                        using (StreamReader srDecrypt = new StreamReader(csDecrypt))
                                        {
                                            m_fileDocument.Load(srDecrypt);
                                        }
                                    }
                                }
                            }
                        }

                        ///
                        /// Parse document
                        ///

                        if (m_fileDocument.ChildNodes.Count != 1)
                            throw new Exception("Invalid document format");

                        XmlNode rootNode = m_fileDocument.SelectSingleNode("root");

                        {
                            XmlNode loginNode = rootNode.SelectSingleNode(LoginNodeName);
                            m_login = loginNode.InnerText;
                        }

                        {
                            XmlNode passwordNode = rootNode.SelectSingleNode(PasswordNodeName);
                            m_password = passwordNode.InnerText;
                        }

                        {
                            XmlNode licenseExpirationNode = rootNode.SelectSingleNode(LicenseExpirationDateNodeName);
                            m_licenseExpirationDate = DateTime.FromBinary(long.Parse(licenseExpirationNode.InnerText));
                        }

                        {
                            XmlNode lastLoginNode = rootNode.SelectSingleNode(LastLoginDateNodeName);
                            m_lastLoginDate = DateTime.FromBinary(long.Parse(lastLoginNode.InnerText));
                        }

                        isInvalidDocumentFormat = false;
                    }
                    catch { }
                }

                if (isInvalidDocumentFormat)
                {
                    try
                    {
                        File.Delete(pathToFile);
                    }
                    catch { }

                    XmlNode rootNode = m_fileDocument.CreateNode(XmlNodeType.Element, "root", null);
                    m_fileDocument.AppendChild(rootNode);

                    string[] SubNodes = new string[] { LoginNodeName, PasswordNodeName, LicenseExpirationDateNodeName, LastLoginDateNodeName };
                    foreach (string subNodeName in SubNodes)
                    {
                        XmlNode subNode = m_fileDocument.CreateNode(XmlNodeType.Element, subNodeName, null);
                        rootNode.AppendChild(subNode);
                    }
                }

                result = true;
            }
            catch
            {
                result = false;
            }

            return result;
        }

        private readonly byte[] m_aesKey = new byte[]
        {
            0xf4, 0x62, 0x4f, 0xef, 0x55, 0x22, 0x01, 0x43,
            0xd2, 0x37, 0x00, 0x00, 0x3c, 0x2a, 0xfd, 0xbe,
            0x89, 0xad, 0x34, 0x4c, 0x85, 0xe2, 0x1a, 0x67,
            0x2d, 0x82,	0x02, 0xd3, 0x66, 0x39, 0x03, 0x4c
        };

        private readonly byte[] m_aesInitializationVerctor = new byte[]
        {
            0x71, 0xd3, 0xc1, 0x8a, 0xa9, 0x79, 0xd5, 0xd1,
            0xfd, 0xa0, 0x37, 0xe6, 0xb5, 0x61, 0x12, 0xee
        };

        private string m_cacheFileName;
        private XmlDocument m_fileDocument;

        private string m_login;
        private bool m_isLoginChanged;
        private string m_password;
        private bool m_isPasswordChanged;

        private DateTime m_licenseExpirationDate;
        private bool m_isLicenseExpirationDateChanged;
        private DateTime m_lastLoginDate;
        private bool m_isLastLoginDateChanged;
    }
}
