using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

using System.Data.SQLite;

namespace ru.po_znaika.database.Diary
{
    public struct ExerciseDiaryInfo
    {
        public int id;
        public DateTime date;
        public int exerciseId;
        public int score;
        public bool isServerSaved;
    }

    public struct ExerciseDiaryShortInfo
    {
        public DateTime date;
        public int exerciseId;
        public int score;
    }

    public class DiaryDatabase
    {
        private const string IdParameter = "@Id";
        private const string DateParameter = "@Date";
        private const string ExerciseIdParameter = "@ExerciseId";
        private const string ScoreParameter = "@Score";
        private const string ServerSavedParameter = "@ServerSaved";

        private const string CreateExerciseDiaryTableSqlStatement = "CREATE TABLE exercise_diary(" +
            "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
            "date INTEGER NOT NULL, " +
            "exercise_id INTEGER NOT NULL, " +
            "score INTEGER NOT NULL, " +
            "server_saved INTEGER NOT NULL)";

        private const string ExtractAllExercisesScoresOrderedByDateSqlStatement = "SELECT date, exercise_id, score FROM exercise_diary ORDER BY date";

        private const string InsertExerciseScoreSqlStatement = "INSERT INTO exercise_diary(date, exercise_id, score, server_saved) " +
            "VALUES (" + DateParameter + ", " + ExerciseIdParameter + ", " + ScoreParameter + ", " + ServerSavedParameter + ")";
        private const string UpdateExerciseServerSavedByIdSqlStatement = "UPDATE exercise_diary SET serverSaved = " + ServerSavedParameter +
            "WHERE id = " + IdParameter;
        private const string ExtractAllNonServerSavedSqlStatement = "SELECT id, date, exercise_id, score, serverSaved FROM exercise_diary WHERE serverSaved = 0";

        public DiaryDatabase(string pathToDatabase, bool failIfNotFound)
        {
            ///
            /// Initialize connection to database
            /// 

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
                    CreateExerciseDiaryTableSqlStatement
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

        public int InsertExerciseScore(DateTime dateTime, int exerciseId, int score, bool isServerSaved)
        {
            int resultId = Constant.InvalidDatabaseIndex;

            try
            {
                using (SQLiteCommand insertionCommand = new SQLiteCommand(InsertExerciseScoreSqlStatement, m_databaseConnection))
                {
                    insertionCommand.Parameters.Add(new SQLiteParameter(DateParameter, dateTime));
                    insertionCommand.Parameters.Add(new SQLiteParameter(ExerciseIdParameter, exerciseId));
                    insertionCommand.Parameters.Add(new SQLiteParameter(ScoreParameter, score));
                    insertionCommand.Parameters.Add(new SQLiteParameter(ServerSavedParameter, isServerSaved));
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

        public bool UpdateExerciseServerSavedById(int id, bool isServerSaved)
        {
            bool result = false;

            try
            {
                using (SQLiteCommand updateCommand = new SQLiteCommand(UpdateExerciseServerSavedByIdSqlStatement, m_databaseConnection))
                {
                    updateCommand.Parameters.Add(new SQLiteParameter(IdParameter, id));
                    updateCommand.Parameters.Add(new SQLiteParameter(ServerSavedParameter, isServerSaved));
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

        public List<ExerciseDiaryInfo> GetAllNonServerSavedDiaryRecords()
        {
            List<ExerciseDiaryInfo> resultRecords = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllNonServerSavedSqlStatement, m_databaseConnection))
                {
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        resultRecords = new List<ExerciseDiaryInfo>();

                        while (dataReader.Read())
                        {
                            ExerciseDiaryInfo diaryRecord = new ExerciseDiaryInfo()
                            {
                                id = dataReader.GetInt32(0),
                                date = dataReader.GetDateTime(1),
                                exerciseId = dataReader.GetInt32(2),
                                score = dataReader.GetInt32(3),
                                isServerSaved = dataReader.GetBoolean(4)
                            };

                            resultRecords.Add(diaryRecord);
                        }
                    }
                }
            }
            catch
            {
                resultRecords = null;
            }

            return resultRecords;
        }

        public List<ExerciseDiaryShortInfo> GetAllDiaryRecordsOrderedByDate()
        {
            List<ExerciseDiaryShortInfo> diaryRecords = null;

            try
            {
                using (SQLiteCommand selectionCommand = new SQLiteCommand(ExtractAllExercisesScoresOrderedByDateSqlStatement, m_databaseConnection))
                {
                    using (SQLiteDataReader dataReader = selectionCommand.ExecuteReader())
                    {
                        diaryRecords = new List<ExerciseDiaryShortInfo>();

                        while (dataReader.Read())
                        {
                            ExerciseDiaryShortInfo record = new ExerciseDiaryShortInfo()
                            {
                                date = dataReader.GetDateTime(0),
                                exerciseId = dataReader.GetInt32(1),
                                score = dataReader.GetInt32(2)
                            };

                            diaryRecords.Add(record);
                        }
                    }
                }
            }
            catch// (Exception e)
            {
                diaryRecords = null;
            }

            return diaryRecords;
        }

        private SQLiteConnection m_databaseConnection;
    }
}
