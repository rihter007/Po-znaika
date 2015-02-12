package ru.po_znaika.alphabet.database.diary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

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
        public Date date;
        public String exerciseName;
        public int score;
    }

    private static final String DATABASE_NAME = "diary.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TableName = "exercise_diary";
    private static final String IdColumnName = "id";
    private static final String DateColumnName = "date";
    private static final String ExerciseNameColumnName = "exercise_name";
    private static final String ScoreColumnName = "score";

    private static final String CreateExerciseDiaryTableSqlStatement = "CREATE TABLE " + TableName + "(" +
        IdColumnName + " INTEGER PRIMARY KEY ASC AUTOINCREMENT," +
        DateColumnName + " INTEGER NOT NULL, " +
        ExerciseNameColumnName + " TEXT NOT NULL, " +
        ScoreColumnName + " INTEGER NOT NULL)";
    private static final String DropExerciseDiaryTableSqlStatement = "DROP TABLE exercise_diary";

    private static final String ExtractAllExercisesScoresOrderedByDateSqlStatement =
            "SELECT date, exercise_id, score FROM exercise_diary ORDER BY date";

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

    public int insertExerciseScore(@NonNull Date dateTime, @NonNull String exerciseId, int score)
    {
        int resultId = DatabaseConstant.InvalidDatabaseIndex;

        SQLiteDatabase database = getWritableDatabase();
        try
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put("date", dateTime.getTime());
            contentValues.put("exercise_name", exerciseId);
            contentValues.put("score", score);

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

    public ExerciseDiaryInfo[] getAllDiaryRecordsOrderedByDate()
    {
        ExerciseDiaryInfo[] diaryRecords = null;

        SQLiteDatabase database = getReadableDatabase();
        Cursor dataReader = null;
        try
        {
            dataReader = database.rawQuery(ExtractAllExercisesScoresOrderedByDateSqlStatement, null);
            if (dataReader.moveToFirst())
            {
                List<ExerciseDiaryInfo> items = new ArrayList<>();

                do
                {
                    ExerciseDiaryInfo item = new ExerciseDiaryInfo();
                    item.date = new Date(dataReader.getLong(0));
                    item.exerciseName = dataReader.getString(1);
                    item.score = dataReader.getInt(2);

                    items.add(item);
                } while (dataReader.moveToNext());

                diaryRecords = new ExerciseDiaryInfo[items.size()];
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
