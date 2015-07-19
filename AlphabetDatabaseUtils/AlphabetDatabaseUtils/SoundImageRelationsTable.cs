using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Text.RegularExpressions;
using System.IO;

namespace AlphabetDatabaseUtils
{
    class SoundWordRelationsByImage
    {
        // INSERT INTO word_image_description(word_id, image_id) VALUES (652,5);
        private static Regex WordImageDescriptionTableRegex =
           new Regex("INSERT INTO word_image_description\\(word_id, image_id\\) VALUES\\s*\\((\\d+)\\s*,\\s*(\\d+)\\)");
        // INSERT INTO image(_id, file_name) VALUES (154,'database_menu');
        private static Regex ImageTableRegex =
            new Regex("INSERT INTO image\\(_id, file_name\\) VALUES\\s*\\((\\d+)\\s*,\\s*'([^']*)'\\)");
        // INSERT INTO sound(_id, file_name) VALUES(301, 'database_sound_yogurt');
        private static Regex SoundTableRegex =
            new Regex("INSERT INTO sound\\(_id, file_name\\) VALUES\\s*\\((\\d+)\\s*,\\s*'([^']*)'\\)");

        private static string ImageFileNamePrefix = "database_";
        private static string SoundFileNamePrefix = "database_sound_";

        private static string WordSoundDescriptionInsertionTemplate =
            "INSERT INTO word_sound_description(word_id, sound_id) VALUES ({0}, {1});";

        public SoundWordRelationsByImage(string pathToSql, string pathToResultSql)
        {
            if ((string.IsNullOrEmpty(pathToSql)) || (string.IsNullOrEmpty(pathToResultSql)))
                throw new ArgumentException();

            m_pathToSql = pathToSql;
            m_pathToResultSql = pathToResultSql;
        }

        public void Process()
        {
            IDictionary<int, int> wordImageDescriptionTable = new SortedDictionary<int, int>();
            IDictionary<int, string> imageTable = new SortedDictionary<int, string>();
            IDictionary<string, int> soundTable = new SortedDictionary<string, int>();

            IDictionary<int, int> soundWordTable = new Dictionary<int, int>();

            using (StreamReader sr = new StreamReader(m_pathToSql))
            {
                while (!sr.EndOfStream)
                {
                    string inputLine = sr.ReadLine();

                    {
                        Match match = WordImageDescriptionTableRegex.Match(inputLine);
                        if (match.Success)
                        {
                            wordImageDescriptionTable.Add(int.Parse(match.Groups[2].Value)
                                , int.Parse(match.Groups[1].Value));
                            continue;
                        }
                    }

                    {
                        Match match = ImageTableRegex.Match(inputLine);
                        if (match.Success)
                        {
                            int index = int.Parse(match.Groups[1].Value);
                            string fileName = match.Groups[2].Value.ToLower();
                            if (!fileName.StartsWith(ImageFileNamePrefix))
                            {
                                Console.WriteLine("assert");
                            }
                            imageTable.Add(index, fileName.Substring(ImageFileNamePrefix.Length));

                            continue;
                        }
                    }

                    {
                        Match match = SoundTableRegex.Match(inputLine);
                        if (match.Success)
                        {
                            int index = int.Parse(match.Groups[1].Value);
                            string fileName = match.Groups[2].Value.ToLower();
                            if (!fileName.StartsWith(SoundFileNamePrefix))
                            {
                                Console.WriteLine("assert");
                            }
                            soundTable.Add(fileName.Substring(SoundFileNamePrefix.Length), index);

                            continue;
                        }
                    }
                }

                Console.WriteLine("Parsing is finished");

                foreach (var wordImageDescription in wordImageDescriptionTable)
                {
                    int imageId = wordImageDescription.Key;
                    int wordId = wordImageDescription.Value;

                    string objectName = imageTable[imageId];

                    if (soundTable.ContainsKey(objectName))
                    {
                        soundWordTable.Add(wordId, soundTable[objectName]);
                    }
                    else
                    {
                        Console.WriteLine("Object name '{0}' is not found", objectName);
                    }
                }

                Console.WriteLine("finished");

                using (StreamWriter sw = new StreamWriter(m_pathToResultSql))
                {
                    foreach (var wordSoundDescription in soundWordTable)
                    {
                        sw.WriteLine(string.Format(WordSoundDescriptionInsertionTemplate, wordSoundDescription.Key,
                            wordSoundDescription.Value));
                    }
                }
            }
        }

        private string m_pathToSql;
        private string m_pathToResultSql;
    }
}
