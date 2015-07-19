using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;

namespace AlphabetDatabaseUtils
{
    class Program
    {
        static void Main(string[] args)
        {
            SoundWordRelationsByImage relations =
                new SoundWordRelationsByImage("D:\\po-znaika\\Repository\\Po-znaika\\Alphabet-Data\\tables_creation.sql",
                    "D:\\result.sql");
            relations.Process();
        }
    }
}
