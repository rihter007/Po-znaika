using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace network_test_client
{
    class ConsoleColorGuard : IDisposable
    {
        public ConsoleColorGuard()
        {
            m_previousColor = Console.ForegroundColor;
        }

        public ConsoleColorGuard(ConsoleColor newColor)
        {            
            m_previousColor = Console.ForegroundColor;
            Console.ForegroundColor = newColor;
        }

        public void Dispose()
        {
            Console.ForegroundColor = m_previousColor;
        }

        private ConsoleColor m_previousColor;
    }
}
