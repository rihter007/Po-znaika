package ru.po_znaika.alphabet.database.diary;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.common.ExerciseScore;

/**
 * Handles requests on diary
 */
public final class DiaryDatabase extends SQLiteOpenHelper
{
    private static final String LogTag = DiaryDatabase.class.getName();

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
            "SELECT " +
            DateColumnName + ", " +
            ExerciseNameColumnName + ", " +
            ScoreColumnName + " " +
            "FROM " + TableName + " " +
            "ORDER BY " + DateColumnName;

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

    public int getExerciseScore(@NonNull String exerciseName)
    {
        // todo: provide realization
        return 0;
    }

    public int insertExerciseScore(@NonNull Date dateTime, @NonNull String exerciseId, int score)
    {
        int resultId = DatabaseConstant.InvalidDatabaseIndex;

        SQLiteDatabase database = getWritableDatabase();
        try
        {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DateColumnName, dateTime.getTime());
            contentValues.put(ExerciseNameColumnName, exerciseId);
            contentValues.put(ScoreColumnName, score);

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

    public ExerciseScore[] getAllDiaryRecordsOrderedByDate()
    {
        SQLiteDatabase database = getReadableDatabase();
        Cursor dataReader = null;
        try
        {
            dataReader = database.rawQuery(ExtractAllExercisesScoresOrderedByDateSqlStatement, null);
            if (dataReader.moveToFirst())
            {
                List<ExerciseScore> items = new ArrayList<>();

                do
                {
                    ExerciseScore item = new ExerciseScore();
                    item.date = new Date(dataReader.getLong(0));
                    item.exerciseName = dataReader.getString(1);
                    item.score = dataReader.getInt(2);

                    items.add(item);
                } while (dataReader.moveToNext());

                ExerciseScore[] diaryRecords = new ExerciseScore[items.size()];
                items.toArray(diaryRecords);
                return diaryRecords;
            }
        }
        catch (Exception exp)
        {
            Log.e(LogTag, String.format("getAllDiaryRecordsOrderedByDate exception:\"%s\"", exp.getMessage()));
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();

            database.close();
        }

        return null;
    }

    public void deleteRecordsOlderThan(@NonNull Date minRecordDate)
    {
        SQLiteDatabase database = getWritableDatabase();
        try
        {
            database.delete(TableName, DateColumnName + " < ?", new String[]{((Long) minRecordDate.getTime()).toString()});
        }
        catch (SQLiteException exp)
        {
            Log.e(LogTag, String.format("Failed to delete records exception message: \"%s\"", exp.getMessage()));
        }
        finally
        {
            database.close();
        }
    }
}
