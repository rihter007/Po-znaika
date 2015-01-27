using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

using System.Data.SQLite;

namespace Alphabet
{
    public enum ExerciseType
    {
        Character
    }

    public struct ExerciseInfo
    {
        public int id;
        public ExerciseType exerciseType;
        public string name;
        public string displayName;
        public int imageId;
    }

    public struct CharacterTheoryExerciseInfo
    {
        public int id;
        public int stepNumber;
        public int imageId;
        public int soundId;
    }
       
    public class LocalDatabase : IDisposable
    {
        private const int ChunkSize = 10 * 1024;

        private const string IdParameter = "@Id";
        private const string ExerciseIdParamater = "@ExerciseId";

        private const string ExtractAllExercisesSqlStatement = "SELECT id, type, name, display_name, image_id FROM Exercise";
        private const string ExtractCharacterStepsByExerciseIdSqlStatement = "SELECT uid, step_number, image_id, sound_id FROM CharacterTheory WHERE ExerciseId = " + ExerciseIdParamater;

        private const string ExtractImageByIdSqlStatement = "SELECT data FROM Image WHERE Id = " + IdParameter;
        private const string ExtractSoundByIdSqlStatement = "SELECT data FROM Sound WHERE Id = " + IdParameter;


        public LocalDatabase(string pathToDatabase)
        {
            ///
            /// Initialize connection to database
            /// 
            {
                if (!File.Exists(pathToDatabase))
                    throw new FileNotFoundException("Database path is not found");

                m_databaseConnection = new SQLiteConnection(pathToDatabase);
            }

            ///
            /// Fill exercise types map
            /// 
            {                
                m_exerciseTypes = new SortedList<int, ExerciseType>();
                                
                foreach (var exerciseType in Enum.GetValues(typeof(ExerciseType)))
                {
                    int crc32Value = (int)Cryptography.Crc32.Compute(Encoding.ASCII.GetBytes(((ExerciseType)exerciseType).ToString()));
                    m_exerciseTypes.Add(crc32Value, ((ExerciseType)exerciseType));
                }
            }
        }      

        public List<ExerciseInfo> GetAllExercises()
        {
            List<ExerciseInfo> result = null;
            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllExercisesSqlStatement, m_databaseConnection))
                {                    
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        result = new List<ExerciseInfo>();
                        while (dataReader.Read())
                        {                      
                            int typeId = dataReader.GetInt32(1);
                            if (!m_exerciseTypes.ContainsKey(typeId))
                                continue;

                            ExerciseInfo info = new ExerciseInfo()
                            {
                                id = dataReader.GetInt32(0),
                                exerciseType = m_exerciseTypes[typeId],
                                name = dataReader.GetString(2),
                                displayName = dataReader.GetString(3),
                                imageId = dataReader.IsDBNull(4) ? 0 : dataReader.GetInt32(4)
                            };

                            result.Add(info);
                        }
                    }
                }
            }
            catch // (Exception e)
            {
                result = null;
            }

            return result;
        }

        public List<CharacterTheoryExerciseInfo> GetCharacterTheoryInfoByExerciseId(int exerciseId)
        {
            List<CharacterTheoryExerciseInfo> result = null;

            return result;
        }

        public MemoryStream GetImageById(int imageId)
        {
            MemoryStream resultImageContent = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractImageByIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(IdParameter, imageId));
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                        {
                            resultImageContent = new MemoryStream();

                            byte[] buffer = new byte[ChunkSize];
                            long bytesReadCount = 0;
                            long iterationBytesRead = 0;
                            do
                            {
                                iterationBytesRead = dataReader.GetBytes(1, bytesReadCount, buffer, 0, buffer.Length);
                                bytesReadCount += iterationBytesRead;

                                resultImageContent.Write(buffer, 0, (int)iterationBytesRead);
                            }
                            while (iterationBytesRead != 0);

                            resultImageContent.Flush();
                        }
                    }
                }
            }
            catch // (Exception e)
            {
                if (resultImageContent != null)
                    resultImageContent.Dispose();
                resultImageContent = null;
            }

            return resultImageContent;
        }

        public MemoryStream GetSoundById(int soundId)
        {
            MemoryStream resultSound = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractSoundByIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(IdParameter, soundId));
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                        {
                            resultSound = new MemoryStream();

                            byte[] buffer = new byte[ChunkSize];
                            long bytesReadCount = 0;
                            long iterationBytesRead = 0;
                            do
                            {
                                iterationBytesRead = dataReader.GetBytes(1, bytesReadCount, buffer, 0, buffer.Length);
                                bytesReadCount += iterationBytesRead;

                                resultSound.Write(buffer, 0, (int)iterationBytesRead);
                            }
                            while (iterationBytesRead != 0);

                            resultSound.Flush();
                        }
                    }
                }
            }
            catch
            {
                if (resultSound != null)
                    resultSound.Dispose();

                resultSound = null;
            }

            return resultSound;
        }

        public void Dispose()
        {
            m_databaseConnection.Dispose();
        }        
        
        private SQLiteConnection m_databaseConnection;
        private SortedList<int, ExerciseType> m_exerciseTypes;
    }
}
