using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

using System.Data.SQLite;

namespace ru.po_znaika.database.Alphabet
{
    public enum AlphabetType
    {
        Russian = 2092928056                  // crc32 of 'ru'
    }

    public enum SoundType
    {
        Good = -860429908,                    // crc32 of 'Good'
        Bad = -1167687909,                    // crc32 of 'Bad'
        ExerciseCompleted = -1106900545       // crc32 of 'ExerciseCompleted'
    }

    public enum ExerciseType
    {
        Character = 294335127,                // crc32 of 'Character'
        WordGather = 402850721,               // crc32 of 'WordGather'
        CreateWordsFromSpecified = -858355490 // crc32 of 'CreateWordsFromSpecified'
    }

    public struct ImageShortInfo
    {
        public int id;
        public string hash;
        public string comment;
    }

    public struct SoundShortInfo
    {
        public int id;
        public string hash;
        public string comment;
    }

    public struct ExerciseInfo
    {
        public int id;
        public ExerciseType exerciseType;
        public string name;
        public string displayName;
        public int imageId;
    }

    public struct CharacterExerciseInfo
    {
        public char value;
        public AlphabetType alphabetType;
    }

    public struct CharacterTheoryExerciseInfo
    {
        public int id;
        public int stepNumber;
        public int imageId;
        public int soundId;
    }

    public struct WordInfo
    {
        public string word;
        public int wordComplexity;
    }

    public enum CharacterDispositionFlag
    {
        Contains = 0,
        Begining = 1,
        End = 2
    }

    public struct CharacterObjectImage
    {
        public int imageId;
        public CharacterDispositionFlag dispositionFlag;
        public WordInfo word;
    }

    /// <summary>
    /// Represents structure about main word and all words that can be created from its characters
    /// </summary>
    public struct SubWords
    {
        /// <summary>
        /// Main word
        /// </summary>
        public WordInfo mainWord;

        /// <summary>
        /// Subwords of the main word without any additional information
        /// </summary>
        public WordInfo[] subwords;

        /// <summary>
        /// Subwords of the main word with images as additional information
        /// </summary>
        public CharacterObjectImage[] imageSubwords;
    }

    public class AlphabetDatabase : IDisposable
    {
        private const int ChunkSize = 10 * 1024;

        private const string IdParameter = "@Id";
        private const string ExerciseIdParameter = "@ExerciseId";
        private const string CharacterExerciseIdParameter = "@CharacterExerciseId";
        private const string SoundTypeParameter = "@SoundType";
        private const string HashParamater = "@hash";
        private const string DataParameter = "@data";
        private const string CountParamter = "@count";
        private const string CommentParameter = "@comment";
        private const string AlphabetIdParameter = "@alphabetId";
        private const string MaxLengthParameter = "@maxLength";
        private const string MinLengthParameter = "@minLength";
        private const string LikeParameter1 = "@like1";
        private const string LikeParameter2 = "@like2";
        private const string AccessoryFlagParameter = "@accessory_flag";
        private const string IntArrayParmater = "@idArray";

        private const string CreateExerciseTableSqlStatement = "CREATE TABLE exercise (" +
           "id INTEGER PRIMARY KEY," +
           "type INTEGER, " +
           "name TEXT NOT NULL," +
           "display_name TEXT NOT NULL," +
           "image_id INTEGER," +
           "FOREIGN KEY(image_id) REFERENCES image(id)," +
           "UNIQUE (name) ON CONFLICT FAIL)";

        private const string CreateCharacterExerciseTableSqlStatement = "CREATE TABLE character_exercise (" +
            "id INTEGER PRIMARY KEY," +
            "exercise_id INTEGER NOT NULL," +
            "character TEXT NOT NULL," +
            "alphabet_id INTEGER NOT NULL," +
            "FOREIGN KEY (exercise_id) REFERENCES exercise(id)," +
            "UNIQUE (exercise_id) ON CONFLICT FAIL)";

        private const string CreateCharacterVerseTableSqlStatement = "CREATE TABLE character_verse (" +
            "id INTEGER PRIMARY KEY," +
            "character_exercise_id INTEGER NOT NULL," +
            "verse TEXT NOT NULL," +
            "FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(id)," + 
            "UNIQUE (character_exercise_id, verse) ON CONFLICT FAIL)";

        private const string CreateCharacterTheoryTableSqlStatement = "CREATE TABLE character_theory (" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "character_exercise_id INTEGER NOT NULL," +
            "step_number INTEGER NOT NULL," +
            "image_id INTEGER," +
            "sound_id INTEGER," +
            "FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(id)," +
            "FOREIGN KEY(image_id) REFERENCES image(id)," +
            "FOREIGN KEY(sound_id) REFERENCES sound(id))";

        private const string CreateImageTableSqlStatement = "CREATE TABLE image (" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "hash TEXT NOT NULL," +
            "data BLOB NOT NULL," +
            "comment TEXT," +
            "UNIQUE (hash) ON CONFLICT FAIL)";

        private const string CreateSoundTableSqlStatement = "CREATE TABLE sound (" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "hash TEXT NOT NULL," +
            "data BLOB NOT NULL," +
            "comment TEXT," +
            "UNIQUE (hash) ON CONFLICT FAIL)";

        private const string CreateBackgroundImageTableSqlStatement = "CREATE TABLE background_image (" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," + 
            "image_id INTEGER NOT NULL," +
            "FOREIGN KEY(image_id) REFERENCES image(id)," +
            "UNIQUE (image_id) ON CONFLICT FAIL)";

        private const string CreateSpecialSoundTableSqlStatement = "CREATE TABLE special_sound (" +
           "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
           "sound_id INTEGER NOT NULL," +
           "sound_type INTEGER NOT NULL," +
           "FOREIGN KEY(sound_id) REFERENCES sound(id)," +
           "UNIQUE (sound_id) ON CONFLICT FAIL)";

        //private const string CreateCharacterObjectImageTableSqlStatement = "CREATE TABLE character_object_image (" +
        //    "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," + 
        //    "character_exercise_id INTEGER NOT NULL," +
        //    "accessory_flag INTEGER NOT NULL," +            // flag of character accessory in the word: 0-just belongs, 1-begining, 2-end
        //    "word_image_description_id INTEGER NOT NULL," +
        //    "FOREIGN KEY(word_image_description_id) REFERENCES word_image_description(id)," +
        //    "FOREIGN KEY(character_exercise_id) REFERENCES character_exercise(id)," +
        //    "UNIQUE (character_exercise_id, word_image_description_id) ON CONFLICT FAIL)";

        private const string CreateWordDescriptionTableSqlStatement = "CREATE TABLE word_image_description (" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "word_id INTEGER NOT NULL," +
            "image_id INTEGER NOT NULL," +
            "FOREIGN KEY(word_id) REFERENCES word(id)," +
            "FOREIGN KEY(image_id) REFERENCES image(id)," +
            "UNIQUE (image_id, word_id) ON CONFLICT FAIL)";

        /// <summary>
        /// Keeps all words
        /// </summary>

        private const string CreateWordTableSqlStatement = "CREATE TABLE word (" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "alphabet_id INTEGER NOT NULL," +
            "word TEXT NOT NULL," +
            "complexity INTEGER NOT NULL," +
            "UNIQUE (alphabet_id, word) ON CONFLICT FAIL)";

        /// <summary>
        /// ***********************
        /// Keeps words that are aproperiate for words creation exercise
        /// ***********************
        /// </summary>

        private const string CreateWordCreationExerciseTableSqlStatement = "CREATE TABLE word_creation_exercise (" + 
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "word_id INTEGER NOT NULL," +
            "UNIQUE (word_id) ON CONFLICT FAIL)";

        private const string ExtractAllExercisesSqlStatement = "SELECT id, type, name, display_name, image_id FROM exercise";
        private const string ExtractCharacterStepsByExerciseIdSqlStatement = "SELECT id, step_number, image_id, sound_id FROM character_theory WHERE character_exercise_id = " + CharacterExerciseIdParameter;

        private const string ExtractAllImagesShortInfoSqlStatement = "SELECT id, hash, comment FROM image";
        private const string ExtractImageByIdSqlStatement = "SELECT data FROM image WHERE Id = " + IdParameter;
        private const string InsertImageSqlStatement = "INSERT INTO image(hash, data) VALUES(" +
            HashParamater + ", " +
            DataParameter +
            ")";
        private const string UpdateImageDataByIdSqlStatement = "UPDATE image " +
            "SET " +
            "hash=" + HashParamater + ", " +
            "data=" + DataParameter + " " +
            "WHERE id = " + IdParameter;
        private const string UpdateImageCommentByIdSqlStatement = "UPDATE image " +
            "SET " +
            "comment = " + CommentParameter + " " +
            "WHERE id = " + IdParameter;
        private const string DeleteImageByIdSqlStatement = "DELETE FROM image WHERE id = " + IdParameter;

        private const string ExtractAllSoundsShortInfoSqlStatement = "SELECT id, hash, comment FROM sound";
        private const string ExtractSoundByIdSqlStatement = "SELECT data FROM sound WHERE Id = " + IdParameter;
        private const string InsertSoundSqlStatement = "INSERT INTO sound(hash, data) VALUES(" +
            HashParamater + ", " +
            DataParameter +
            ")";
        private const string UpdateSoundDataByIdSqlStatement = "UPDATE sound " +
            "SET " +
            "hash=" + HashParamater + ", " +
            "data=" + DataParameter + " " +
            "WHERE id = " + IdParameter;
        private const string UpdateSoundCommentByIdSqlStatement = "UPDATE sound " +
            "SET " +
            "comments = " + CommentParameter + " " +
            "WHERE id = " + IdParameter;
        private const string DeleteSoundByIdSqlStatement = "DELETE FROM sound WHERE id = " + IdParameter;

        private const string ExtractRandomBackgroundImageSqlStatement = "SELECT image_id FROM background_image ORDER BY RANDOM() LIMIT 1";
        private const string ExtractRandomBackgroundImagesSqlStatement = "SELECT image_id FROM background_image ORDER BY RANDOM() LIMIT " + CountParamter;
        private const string ExtractAllBackgroundImagesSqlStatement = "SELECT image_id FROM background_image";

        //private const string ExtractRandomCharacterObjectImagesSqlStatement = "SELECT coi.accessory_flag, wid.image_id, w.word, w.complexity FROM character_object_image coi, word_image_description wid, word w WHERE (coi.word_image_description_id = wid.id) AND (w.id = wid.word_id) AND (w.alphabet_id = " + AlphabetIdParameter + ") AND (coi.accessory_flag = " + AccessoryFlagParameter + ") ORDER BY RANDOM() LIMIT" + CountParamter;
        //private const string ExtractRandomCharacterObjectImagesSqlStatement = "SELECT coi.accessory_flag, wid.image_id, w.word, w.complexity FROM character_object_image coi, word_image_description wid, word w WHERE (coi.word_image_description_id = wid.id) AND (w.id = wid.word_id) AND (w.alphabet_id = " + AlphabetIdParameter + ") AND (coi.accessory_flag = " + AccessoryFlagParameter + ") ORDER BY RANDOM() LIMIT" + CountParamter;
        private const string ExtractRandomCharacterObjectImagesSqlStatement = "SELECT wid.image_id, w.word, w.complexity FROM word w, word_image_description wid WHERE (w.id = wid.word_id ) AND (w.alphabet_id = " + AlphabetIdParameter + ") AND ((w.word LIKE " + LikeParameter1 + ") OR (w.word LIKE " + LikeParameter2 + ")) ORDER BY RANDOM() LIMIT " + CountParamter;
        private const string ExtractRandomOtherCharacterObjectImagesSqlStatement = "SELECT wid.image_id, w.word, w.complexity FROM word w, word_image_description wid WHERE (w.id = wid.word_id) AND (w.alphabet_id = " + AlphabetIdParameter + ") AND NOT ((w.word LIKE " + LikeParameter1 + ") OR (w.word LIKE " + LikeParameter2 + ")) ORDER BY RANDOM() LIMIT " + CountParamter;

        private const string ExtractRandomSoundByTypeSqlStatement = "SELECT sound_id FROM special_sound WHERE sound_type = " + SoundTypeParameter + " ORDER BY RANDOM() LIMIT 1";

        private const string ExtractCharacterIdByExerciseIdSqlStatement = "SELECT id FROM character_exercise WHERE exercise_id = " + ExerciseIdParameter;
        private const string ExtractCharacterValueAndAlphabetIdByExerciseIdSqlStatement = "SELECT character, alphabet_id FROM character_exercise WHERE exercise_id = " + ExerciseIdParameter;
        private const string ExtractCharacterValueAndAlphabetIdByIdSqlStatement = "SELECT character, alphabet_id FROM character_exercise WHERE id = " + IdParameter;

        private const string ExtractRandomVerseByCharacterIdAndMaxLengthSqlStatement = "SELECT verse FROM character_verse WHERE (character_exercise_id = " + CharacterExerciseIdParameter + ") AND (length(verse) <= " + MaxLengthParameter + ") ORDER BY RANDOM() LIMIT 1";

        private const string ExtractRandomWordAndObjectByLengthByAlphabetSqlStatement = "SELECT wid.image_id, wd.word, wd.complexity FROM word_image_description wid, word wd WHERE (wid.word_id = wd.id) AND (wd.alphabet_id = " + AlphabetIdParameter + ") AND (length(wd.word) <= " + MaxLengthParameter + ") ORDER BY RANDOM() LIMIT 1";
        private const string ExtractImagesForSpecifiedWordsSqlStatement = "SELECT word_id, image_id FROM word_image_description WHERE word_id IN ({0})";

        private const string ExtractRandomWordByAlphabetAndLengthSqlStatement = "SELECT word, complexity FROM word WHERE (alphabet_id = " + AlphabetIdParameter + ") AND (length(word) >=" + MinLengthParameter + ") AND (length(word) <=" + MaxLengthParameter + ") ORDER BY RANDOM() LIMIT 1";
        private const string ExtractAllWordsByAlphabetIdAndMaxLength = "SELECT id, word, complexity FROM word WHERE (alphabet_id =" + AlphabetIdParameter + ") AND (length(word) <= " + MaxLengthParameter + ")";

        private const string ExtractRandomCreationWordByAlphabetAndLengthSqlStatement = "SELECT w.word, w.complexity FROM word w, word_creation_exercise wce WHERE (w.id = wce.word_id) AND (length(w.word) >= " + MinLengthParameter + ") AND (length(w.word) <= " + MaxLengthParameter + ") ORDER BY RANDOM() LIMIT 1";

        public AlphabetDatabase(string pathToDatabase, bool failIfNotFound)
        {
            ///
            /// Initialize connection to database
            /// 
            {
                if (File.Exists(pathToDatabase))
                {
                    m_databaseConnection = new SQLiteConnection(string.Format("Data Source={0};Version=3;UseUTF8Encoding=True;", pathToDatabase));                    
                    m_databaseConnection.Open();

                    if (!VerifyDatabaseStructure())
                        throw new Exception("Failed to verify database structure");
                }
                else
                {
                    if (failIfNotFound)
                        throw new FileNotFoundException("Database path is not found");

                    // File.Create(pathToDatabase).Dispose();

                    m_databaseConnection = new SQLiteConnection(string.Format("Data Source={0};Version=3;UseUTF8Encoding=True;", pathToDatabase));
                    m_databaseConnection.Open();

                    if (!CreateDatabaseStructure())
                        throw new Exception("Failed to create database structure");
                }
            }

            /////
            ///// Fill exercise types map
            ///// 
            //{
            //    m_exerciseTypes = new SortedList<int, ExerciseType>();

            //    foreach (var exerciseType in Enum.GetValues(typeof(ExerciseType)))
            //    {
            //        int crc32Value = (int)CustomCryptography.Crc32.Compute(Encoding.ASCII.GetBytes(((ExerciseType)exerciseType).ToString()));
            //        m_exerciseTypes.Add(crc32Value, ((ExerciseType)exerciseType));
            //    }
            //}

            /////
            ///// Fill sound types
            ///// 
            //{
            //    m_soundTypes = new SortedList<SoundType, int>();
                
            //    foreach (var soundType in Enum.GetValues(typeof(SoundType)))
            //    {
            //        int crc32Value = (int)CustomCryptography.Crc32.Compute(Encoding.ASCII.GetBytes(((SoundType)soundType).ToString()));
            //        m_soundTypes.Add(((SoundType)soundType), crc32Value);
            //    }
            //}
        }

        private bool VerifyDatabaseStructure()
        {
            return true;
        }

        private bool CreateDatabaseStructure()
        {
            bool result = false;
            try
            {
                string[] createTableSqlExpressions = new string[]
                {
                    // General object tables
                    CreateImageTableSqlStatement,
                    CreateSoundTableSqlStatement,
                    CreateBackgroundImageTableSqlStatement,
                    CreateSpecialSoundTableSqlStatement,
                    CreateWordDescriptionTableSqlStatement,
                    
                    // Exercise tables
                    CreateExerciseTableSqlStatement,
                    CreateCharacterExerciseTableSqlStatement,
                    CreateCharacterTheoryTableSqlStatement,
                    CreateCharacterVerseTableSqlStatement,
                    //CreateCharacterObjectImageTableSqlStatement,
                    CreateWordCreationExerciseTableSqlStatement
                };

                foreach (var tableCreateStatement in createTableSqlExpressions)
                {
                    using (SQLiteCommand createTableCommand = new SQLiteCommand(tableCreateStatement, m_databaseConnection))
                    {
                        createTableCommand.ExecuteNonQuery();
                    }
                }

                result = true;
            }
            catch// (Exception e)
            {
                result = false;
            }

            return result;
        }

        public string[] GetTableNames()
        {
            return new string[] { "exercise", "character_theory", "image", "sound", "background_image", "character_object_image" };
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
                            
                            ExerciseInfo info = new ExerciseInfo()
                            {
                                id = dataReader.GetInt32(0),
                                exerciseType = (ExerciseType)typeId,
                                name = dataReader.GetString(2),
                                displayName = dataReader.GetString(3),
                                imageId = dataReader.IsDBNull(4) ? 0 : dataReader.GetInt32(4)
                            };

                            result.Add(info);
                        }
                    }
                }
            }
            catch// (Exception e)
            {
                result = null;
            }

            return result;
        }

        public List<CharacterTheoryExerciseInfo> GetCharacterTheoryInfoByExerciseId(int characterExerciseId)
        {
            List<CharacterTheoryExerciseInfo> result = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractCharacterStepsByExerciseIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(CharacterExerciseIdParameter, characterExerciseId));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        result = new List<CharacterTheoryExerciseInfo>();

                        while (dataReader.Read())
                        {
                            CharacterTheoryExerciseInfo characterTheory = new CharacterTheoryExerciseInfo()
                            {
                                id = dataReader.GetInt32(0),
                                stepNumber = dataReader.GetInt32(1),
                                imageId = dataReader.IsDBNull(2) ?  Constant.InvalidDatabaseIndex : dataReader.GetInt32(2),
                                soundId = dataReader.IsDBNull(3) ?  Constant.InvalidDatabaseIndex : dataReader.GetInt32(3)
                            };

                            result.Add(characterTheory);
                        }
                    }
                }
            }
            catch
            {
                result = null;
            }

            return result;
        }

        public List<ImageShortInfo> GetAllImagesShortInfo()
        {
            List<ImageShortInfo> resultImages = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllImagesShortInfoSqlStatement, m_databaseConnection))
                {
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        resultImages = new List<ImageShortInfo>();

                        while (dataReader.Read())
                        {
                            ImageShortInfo imageInfo = new ImageShortInfo()
                            {
                                id = dataReader.GetInt32(0),
                                hash = dataReader.GetString(1),
                                comment = dataReader.IsDBNull(2) ? null : dataReader.GetString(2)
                            };

                            resultImages.Add(imageInfo);
                        }
                    }
                }
            }
            catch
            {
                resultImages = null;
            }

            return resultImages;
        }
        
        public int InsertImage(string imageHash, byte[] imageData)
        {
            int resultId = Constant.InvalidDatabaseIndex;
            try
            {
                using (SQLiteCommand insertionCommand = new SQLiteCommand(InsertImageSqlStatement, m_databaseConnection))
                {
                    insertionCommand.Parameters.Add(new SQLiteParameter(HashParamater, imageHash));
                    insertionCommand.Parameters.Add(new SQLiteParameter(DataParameter, imageData));

                    insertionCommand.ExecuteNonQuery();
                    resultId = (int)m_databaseConnection.LastInsertRowId;                  
                }
            }
            catch// (Exception e)
            {
                resultId = Constant.InvalidDatabaseIndex;
            }

            return resultId;
        }

        public bool UpdateImageData(int id, string imageHash, byte[] imageData)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand updateCommand = new SQLiteCommand(UpdateImageDataByIdSqlStatement, m_databaseConnection))
                {
                    updateCommand.Parameters.Add(new SQLiteParameter(IdParameter, id));
                    updateCommand.Parameters.Add(new SQLiteParameter(HashParamater, imageHash));
                    updateCommand.Parameters.Add(new SQLiteParameter(DataParameter, imageData));

                    updateCommand.ExecuteNonQuery();
                }

                result = true;
            }
            catch
            {
                result = false;
            }

            return result;
        }

        public bool UpdateImageComment(int id, string comment)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand updateCommand = new SQLiteCommand(UpdateImageCommentByIdSqlStatement, m_databaseConnection))
                {
                    updateCommand.Parameters.Add(new SQLiteParameter(IdParameter, id));
                    if (comment == null)
                        updateCommand.Parameters.Add(new SQLiteParameter(CommentParameter, DBNull.Value));
                    else
                        updateCommand.Parameters.Add(new SQLiteParameter(CommentParameter, comment));

                    updateCommand.ExecuteNonQuery();
                }

                result = true;
            }
            catch// (Exception e)
            {
                result = false;
            }

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
                                iterationBytesRead = dataReader.GetBytes(0, bytesReadCount, buffer, 0, buffer.Length);
                                bytesReadCount += iterationBytesRead;

                                resultImageContent.Write(buffer, 0, (int)iterationBytesRead);
                            }
                            while (iterationBytesRead > 0);

                            resultImageContent.Flush();
                            resultImageContent.Seek(0, SeekOrigin.Begin);
                        }
                    }
                }
            }
            catch// (Exception e)
            {
                if (resultImageContent != null)
                    resultImageContent.Dispose();
                resultImageContent = null;
            }

            return resultImageContent;
        }

        public bool DeleteImageById(int imageId)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand deletionCommand = new SQLiteCommand(DeleteImageByIdSqlStatement, m_databaseConnection))
                {
                    deletionCommand.Parameters.Add(new SQLiteParameter(IdParameter, imageId));
                    deletionCommand.ExecuteNonQuery();
                }

                result = true;
            }
            catch
            {
                result = false;
            }

            return result;
        }

        public List<SoundShortInfo> GetAllSoundsShortInfo()
        {
            List<SoundShortInfo> resultSounds = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllSoundsShortInfoSqlStatement, m_databaseConnection))
                {
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        resultSounds = new List<SoundShortInfo>();

                        while (dataReader.Read())
                        {
                            SoundShortInfo imageInfo = new SoundShortInfo()
                            {
                                id = dataReader.GetInt32(0),
                                hash = dataReader.GetString(1),
                                comment = dataReader.IsDBNull(2) ? null : dataReader.GetString(2)
                            };

                            resultSounds.Add(imageInfo);
                        }
                    }
                }
            }
            catch// (Exception e)
            {
                resultSounds = null;
            }

            return resultSounds;
        }

        public int InsertSound(string soundHash, byte[] soundData)
        {
            int resultId = Constant.InvalidDatabaseIndex;
            try
            {
                using (SQLiteCommand insertionCommand = new SQLiteCommand(InsertSoundSqlStatement, m_databaseConnection))
                {
                    insertionCommand.Parameters.Add(new SQLiteParameter(HashParamater, soundHash));
                    insertionCommand.Parameters.Add(new SQLiteParameter(DataParameter, soundData));

                    insertionCommand.ExecuteNonQuery();
                    resultId = (int)m_databaseConnection.LastInsertRowId;
                }
            }
            catch // (Exception e)
            {
                resultId = Constant.InvalidDatabaseIndex;
            }

            return resultId;
        }

        public bool UpdateSoundData(int id, string soundHash, byte[] soundData)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand updateCommand = new SQLiteCommand(UpdateSoundDataByIdSqlStatement, m_databaseConnection))
                {
                    updateCommand.Parameters.Add(new SQLiteParameter(IdParameter, id));
                    updateCommand.Parameters.Add(new SQLiteParameter(HashParamater, soundHash));
                    updateCommand.Parameters.Add(new SQLiteParameter(DataParameter, soundData));

                    updateCommand.ExecuteNonQuery();
                }

                result = true;
            }
            catch
            {
                result = false;
            }

            return result;
        }

        public bool UpdateSoundComment(int id, string comment)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand updateCommand = new SQLiteCommand(UpdateSoundCommentByIdSqlStatement, m_databaseConnection))
                {
                    updateCommand.Parameters.Add(new SQLiteParameter(IdParameter, id));
                    if (comment == null)
                        updateCommand.Parameters.Add(new SQLiteParameter(CommentParameter, DBNull.Value));
                    else
                        updateCommand.Parameters.Add(new SQLiteParameter(CommentParameter, comment));

                    updateCommand.ExecuteNonQuery();
                }

                result = true;
            }
            catch
            {
                result = false;
            }

            return result;
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
                                iterationBytesRead = dataReader.GetBytes(0, bytesReadCount, buffer, 0, buffer.Length);
                                bytesReadCount += iterationBytesRead;

                                resultSound.Write(buffer, 0, (int)iterationBytesRead);
                            }
                            while (iterationBytesRead != 0);

                            resultSound.Flush();
                            resultSound.Seek(0, SeekOrigin.Begin);
                        }
                    }
                }
            }
            catch // (Exception e)
            {
                if (resultSound != null)
                    resultSound.Dispose();

                resultSound = null;
            }

            return resultSound;
        }

        public bool DeleteSoundById(int soundId)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand deletionCommand = new SQLiteCommand(DeleteSoundByIdSqlStatement, m_databaseConnection))
                {
                    deletionCommand.Parameters.Add(new SQLiteParameter(IdParameter, soundId));
                    deletionCommand.ExecuteNonQuery();
                }

                result = true;
            }
            catch
            {
                result = false;
            }

            return result;
        }

        public List<int> GetAllBackgroundImages()
        {
            List<int> resultImages = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllBackgroundImagesSqlStatement, m_databaseConnection))
                {
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        resultImages = new List<int>();

                        while (dataReader.Read())
                            resultImages.Add(dataReader.GetInt32(0));
                    }
                }
            }
            catch
            {
                resultImages = null;
            }

            return resultImages;
        }

        public int GetRandomBackgroundImage()
        {
            int resultImageId = Constant.InvalidDatabaseIndex;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractRandomBackgroundImageSqlStatement, m_databaseConnection))
                {
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                            resultImageId = dataReader.GetInt32(0);
                    }
                }
            }
            catch
            {
                resultImageId = Constant.InvalidDatabaseIndex;
            }

            return resultImageId;
        }

        public List<int> GetRandomBackgroundImages(int maxCount)
        {
            List<int> resultImages = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractRandomBackgroundImagesSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(CountParamter, maxCount));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        resultImages = new List<int>();

                        while (dataReader.Read())
                            resultImages.Add(dataReader.GetInt32(0));                        
                    }
                }
            }
            catch
            {
                resultImages = null;
            }

            return resultImages;
        }

        public List<CharacterObjectImage> GetRandomCharacterObjectImages(int characterExerciseId, CharacterDispositionFlag charDisposition, int maxCount)
        {           
            return GeneralGetRandomCharacterObjectImages(ExtractRandomCharacterObjectImagesSqlStatement, characterExerciseId, charDisposition, maxCount);
        }

        public List<CharacterObjectImage> GetRandomOtherCharacterObjectImages(int characterExerciseId, CharacterDispositionFlag charDisposition, int maxCount)
        {
            return GeneralGetRandomCharacterObjectImages(ExtractRandomOtherCharacterObjectImagesSqlStatement, characterExerciseId, charDisposition, maxCount);
        }

        public int GetRandomSoundByType(SoundType soundType)
        {
            int resultSoundId = Constant.InvalidDatabaseIndex;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractRandomSoundByTypeSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(SoundTypeParameter, (int)soundType));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                            resultSoundId = dataReader.GetInt32(0);
                    }
                }
            }
            catch
            {
                resultSoundId = Constant.InvalidDatabaseIndex;
            }

            return resultSoundId;
        }

        public int GetCharacterIdByExerciseId(int exerciseId)
        {
            int resultCharacterId = Constant.InvalidDatabaseIndex;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractCharacterIdByExerciseIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(ExerciseIdParameter, exerciseId));
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                            resultCharacterId = dataReader.GetInt32(0);
                    }
                }
            }
            catch// (Exception e)
            {
                resultCharacterId = Constant.InvalidDatabaseIndex;
            }
            return resultCharacterId;
        }

        public CharacterExerciseInfo? GetCharacterValueByExerciseId(int exerciseId)
        {
            CharacterExerciseInfo? resultChar = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractCharacterValueAndAlphabetIdByExerciseIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(ExerciseIdParameter, exerciseId));
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        CharacterExerciseInfo characterInfo = new CharacterExerciseInfo();
                        if (dataReader.Read())
                        {
                            characterInfo.value = dataReader.GetString(0)[0];
                            characterInfo.alphabetType = (AlphabetType)dataReader.GetInt32(1);
                        }
                        resultChar = characterInfo;
                    }
                }
            }
            catch
            {
                resultChar = null;
            }

            return resultChar;
        }

        public CharacterExerciseInfo? GetCharacterValueById(int characterId)
        {
            CharacterExerciseInfo? resultExerciseInfo = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractCharacterValueAndAlphabetIdByIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(IdParameter, characterId));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        CharacterExerciseInfo exerciseInfo = new CharacterExerciseInfo();
                        if (dataReader.Read())
                        {
                            exerciseInfo.value = dataReader.GetString(0)[0];
                            exerciseInfo.alphabetType = (AlphabetType)dataReader.GetInt32(1);
                        }

                        resultExerciseInfo = exerciseInfo;
                    }
                }
            }
            catch
            {
                resultExerciseInfo = null;
            }

            return resultExerciseInfo;
        }

        public string GetRandomVerseByCharacterIdAndMaxLength(int characterId, int maxLength)
        {
            string resultVerse = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractRandomVerseByCharacterIdAndMaxLengthSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(CharacterExerciseIdParameter, characterId));
                    selectionCommand.Parameters.Add(new SQLiteParameter(MaxLengthParameter, maxLength));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                            resultVerse = dataReader.GetString(0);
                    }
                }
            }
            catch
            {
                resultVerse = null;
            }

            return resultVerse;
        }

        public CharacterObjectImage? GetRandomWordImageByALphabetAndMaxLength(AlphabetType alphabetType, int maxWordLength) 
        {
            CharacterObjectImage? result = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractRandomWordAndObjectByLengthByAlphabetSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(AlphabetIdParameter, (int)alphabetType));
                    selectionCommand.Parameters.Add(new SQLiteParameter(MaxLengthParameter, maxWordLength));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {                        
                        if (dataReader.Read())
                        {
                            CharacterObjectImage resultValue = new CharacterObjectImage();
                            resultValue.imageId = dataReader.GetInt32(0);
                            resultValue.word = new WordInfo()
                            {
                                word = dataReader.GetString(1),
                                wordComplexity = dataReader.GetInt32(2)
                            };
                            result = resultValue;
                        }                        
                    }
                }
            }
            catch (Exception e)
            {
                result = null;
            }

            return result;
        }

        public SubWords? GetRandomSubwordsByAlphabetAndLength(AlphabetType alphabetType, int minWordLength, int maxWordLength)
        {
            SubWords? subWordsResult = null;

            try
            {
                SubWords subWords = new SubWords();
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractRandomCreationWordByAlphabetAndLengthSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(AlphabetIdParameter, (int)alphabetType));
                    selectionCommand.Parameters.Add(new SQLiteParameter(MinLengthParameter, minWordLength));
                    selectionCommand.Parameters.Add(new SQLiteParameter(MaxLengthParameter, maxWordLength));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (dataReader.Read())
                        {
                            subWords.mainWord = new WordInfo()
                            {
                                word = dataReader.GetString(0),
                                wordComplexity = dataReader.GetInt32(1)
                            };
                        }
                    }
                }

                ///
                /// Algorithm:
                /// - extract all words that belongs to same alphabet and contains less characters
                /// - manually fiter previous list
                /// - determine from specified list of subwords that contain image hint
                /// - push 2 seperate lists in output structure
                /// 

                if (!string.IsNullOrEmpty(subWords.mainWord.word))
                {
                    IDictionary<int, WordInfo> allSubWordsList = null;

                    // Extract all subwords
                    using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllWordsByAlphabetIdAndMaxLength, m_databaseConnection))
                    {
                        selectionCommand.Parameters.Add(new SQLiteParameter(AlphabetIdParameter, (int)alphabetType));
                        selectionCommand.Parameters.Add(new SQLiteParameter(MaxLengthParameter, maxWordLength));

                        using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                        {
                            char[] mainWordCharacters = subWords.mainWord.word.ToCharArray();

                            allSubWordsList = new SortedDictionary<int, WordInfo>();
                            while (dataReader.Read())
                            {
                                WordInfo subword = new WordInfo()
                                {
                                    word = dataReader.GetString(1),
                                    wordComplexity = dataReader.GetInt32(2)
                                };
                                char[] subWordCharacters = subword.word.ToCharArray();

                                if (subWordCharacters.Intersect(mainWordCharacters).Count() == subWordCharacters.Length)
                                    allSubWordsList.Add(dataReader.GetInt32(0), subword);                                
                            }                            
                        }
                    }

                    List<CharacterObjectImage> imageSubwords = new List<CharacterObjectImage>();

                    if (allSubWordsList.Count > 0)
                    {
                        List<int> idList = new List<int>(allSubWordsList.Select(s => s.Key));
                        string indexValuesLiteral = idList[0].ToString();
                        for (int i = 1; i < idList.Count; ++i)
                            indexValuesLiteral += ", " + idList[i].ToString();

                        // Determine subwords that have an image description
                        using (SQLiteCommand selectionCommand = new SQLiteCommand(string.Format(ExtractImagesForSpecifiedWordsSqlStatement, indexValuesLiteral), m_databaseConnection))
                        {
                            using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                            {
                                while (dataReader.Read())
                                {
                                    int wordId = dataReader.GetInt32(0);
                                    int imageId = dataReader.GetInt32(1);

                                    CharacterObjectImage subwordImage = new CharacterObjectImage()
                                    {
                                        imageId = imageId,
                                        word = allSubWordsList[wordId]
                                    };

                                    allSubWordsList.Remove(wordId);
                                    imageSubwords.Add(subwordImage);
                                }
                            }
                        }
                    }

                    subWords.subwords = allSubWordsList.Values.ToArray();
                    if (imageSubwords.Count > 0)
                        subWords.imageSubwords = imageSubwords.ToArray();

                    subWordsResult = subWords;
                }
            }
            catch (Exception e)
            {
                subWordsResult = null;
            }

            return subWordsResult;
        }

        private List<CharacterObjectImage> GeneralGetRandomCharacterObjectImages(string literalSqlStatement, int characterExerciseId, CharacterDispositionFlag charDisposition, int maxCount)
        {
            List<CharacterObjectImage> otherCharactersImages = null;

            char exerciseCharacter;
            int alphabetId = 0;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractCharacterValueAndAlphabetIdByIdSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(IdParameter, characterExerciseId));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        if (!dataReader.Read())
                            throw new Exception("No data selection");

                        exerciseCharacter = dataReader.GetString(0)[0];// GetChar(0);
                        alphabetId = dataReader.GetInt32(1);
                    }
                }
           
                ///
                /// Attention!!! Non latin characters are NOT processed in ignore case mode. So we need 2 selection statements
                ///

                string likePattern = string.Empty;
                switch (charDisposition)
                {
                    case CharacterDispositionFlag.Contains:
                        likePattern = "%{0}%";
                        break;

                    case CharacterDispositionFlag.Begining:
                        likePattern = "{0}%";
                        break;

                    case CharacterDispositionFlag.End:
                        likePattern = "%{0}";
                        break;
                    default:
                        throw new ArgumentException();
                }

                string CapLetterLike = string.Format(likePattern, exerciseCharacter.ToString().ToUpper());
                string LowLetterLike = string.Format(likePattern, exerciseCharacter.ToString().ToLower());

                using (SQLiteCommand selectionCommand = new SQLiteCommand(literalSqlStatement, m_databaseConnection))
                {
                    selectionCommand.Parameters.Add(new SQLiteParameter(AlphabetIdParameter, alphabetId));
                    selectionCommand.Parameters.Add(new SQLiteParameter(LikeParameter1, CapLetterLike));
                    selectionCommand.Parameters.Add(new SQLiteParameter(LikeParameter2, LowLetterLike));                    
                    selectionCommand.Parameters.Add(new SQLiteParameter(CountParamter, maxCount));

                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        otherCharactersImages = new List<CharacterObjectImage>();
                        while (dataReader.Read())
                        {
                            CharacterObjectImage objectImage = new CharacterObjectImage()
                            {
                                imageId = dataReader.GetInt32(0),
                                word = new WordInfo()
                                {
                                    word = dataReader.GetString(1),
                                    wordComplexity = dataReader.GetInt32(2)
                                }
                            };

                            otherCharactersImages.Add(objectImage);
                        }
                    }
                }
            }
            catch
            {
                otherCharactersImages = null;
            }

            return otherCharactersImages;
        }

        public void Dispose()
        {
            m_databaseConnection.Dispose();
        }

        private SQLiteConnection m_databaseConnection;
    }
}
