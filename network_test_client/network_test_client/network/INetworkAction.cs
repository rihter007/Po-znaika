using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace network_test_client.network
{
    interface INetworkAction
    {
        void SetUrl(string url);
        string GetUrl();

        void SetLogin(string login);
        void SetPassword(string password);

        void Run();
    }
}
