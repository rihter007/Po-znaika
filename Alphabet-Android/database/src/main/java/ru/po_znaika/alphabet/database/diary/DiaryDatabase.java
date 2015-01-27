package ru.po_znaika.alphabet.database.diary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.po_znaika.alphabet.database.DatabaseConstant;

/**
 * Handles requests on diary
 */
public class DiaryDatabase extends SQLiteOpenHelper
{
    public class ExerciseDiaryInfo
    {
        public int id;
        public Date date;
        public int exerciseId;
        public int score;
        public boolean isServerSaved;
    }

    public class ExerciseDiaryShortInfo
    {
        public Date date;
        public int exerciseId;
        public int score;
    }

    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TableName = "exercise_diary";

    private static final String CreateExerciseDiaryTableSqlStatement = "CREATE TABLE " + TableName + "(" +
        "id INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
        "date INTEGER NOT NULL, " +
        "exercise_id INTEGER NOT NULL, " +
        "score INTEGER NOT NULL, " +
        "server_saved INTEGER NOT NULL)";
    private static final String DropExerciseDiaryTableSqlStatement = "DROP TABLE exercise_diary";

    private static final String ExtractAllExercisesScoresOrderedByDateSqlStatement =
            "SELECT date, exercise_id, score FROM exercise_diary ORDER BY date";
    private static final String UpdateExerciseServerSavedByIdSqlStatement =
            "UPDATE exercise_diary SET serverSaved = ? WHERE id = ?";
    private static final String ExtractAllNonServerSavedSqlStatement =
            "SELECT id, date, exercise_id, score, serverSaved FROM exercise_diary WHERE serverSaved = 0";

    public DiaryDatabase(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase database)
    {
        database.execSQL(CreateExerciseDiaryTableSqlStatement);
    }

    // Method is called during an upgrade of the database,
    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {
        database.execSQL(DropExerciseDiaryTableSqlStatement);
        onCreate(database);
    }

    public int insertExerciseScore(Date dateTime, int exerciseId, int score, boolean isServerSaved)
    {
        int resultId = DatabaseConstant.InvalidDatabaseIndex;

        SQLiteDatabase database = getWritableDatabase();
        try
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("date", dateTime.getTime());
            contentValues.put("exercise_id", exerciseId);
            contentValues.put("score", score);
            contentValues.put("server_saved", isServerSaved);

            resultId = (int)database.insert(TableName, null, contentValues);
        }
        catch (Exception e)
        {
            resultId = DatabaseConstant.InvalidDatabaseIndex;
        }
        finally
        {
            database.close();
        }

        return resultId;
    }

    public boolean UpdateExerciseServerSavedById(int id, boolean isServerSaved)
    {
        boolean result = false;

        SQLiteDatabase database = getWritableDatabase();
        try
        {
            database.rawQuery(UpdateExerciseServerSavedByIdSqlStatement,
                    new String[]
                            {
                                    ((Integer) id).toString(),
                                    ((Boolean) isServerSaved).toString()
                            });
            result = true;
        }
        catch (Exception exp)
        {
            result = false;
        }
        finally
        {
            database.close();
        }

        return result;
    }

    public ExerciseDiaryInfo[] getAllNonServerSavedDiaryRecords()
    {
        ExerciseDiaryInfo resultRecords[] = null;

        SQLiteDatabase database = getReadableDatabase();
        Cursor dataReader = null;
        try
        {
            dataReader = database.rawQuery(ExtractAllNonServerSavedSqlStatement, null);
            if (dataReader.moveToFirst())
            {
                List<ExerciseDiaryInfo> items = new ArrayList<ExerciseDiaryInfo>();
                do
                {
                    ExerciseDiaryInfo item = new ExerciseDiaryInfo();
                    item.id = dataReader.getInt(0);
                    item.date = new Date(dataReader.getLong(1));
                    item.exerciseId = dataReader.getInt(2);
                    item.score = dataReader.getInt(3);
                    item.isServerSaved = dataReader.getInt(4) > 0;

                    items.add(item);
                }while (dataReader.moveToNext());

                resultRecords = new ExerciseDiaryInfo[items.size()];
                items.toArray(resultRecords);
            }
        }
        catch (Exception exp)
        {
            resultRecords = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();

            database.close();
        }

        return resultRecords;
    }

    public ExerciseDiaryShortInfo[] getAllDiaryRecordsOrderedByDate()
    {
        ExerciseDiaryShortInfo[] diaryRecords = null;

        SQLiteDatabase database = getReadableDatabase();
        Cursor dataReader = null;
        try
        {
            dataReader = database.rawQuery(ExtractAllExercisesScoresOrderedByDateSqlStatement, null);
            if (dataReader.moveToFirst())
            {
                List<ExerciseDiaryShortInfo> items = new ArrayList<ExerciseDiaryShortInfo>();

                do
                {
                    ExerciseDiaryShortInfo item = new ExerciseDiaryShortInfo();
                    item.date = new Date(dataReader.getLong(0));
                    item.exerciseId = dataReader.getInt(1);
                    item.score = dataReader.getInt(2);

                    items.add(item);
                } while (dataReader.moveToNext());

                diaryRecords = new ExerciseDiaryShortInfo[items.size()];
                items.toArray(diaryRecords);
            }
        }
        catch (Exception e)
        {
            diaryRecords = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();

            database.close();
        }

        return diaryRecords;
    }
}
