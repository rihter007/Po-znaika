package ru.po_znaika.alphabet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;
import ru.po_znaika.network.IServerOperations;
import ru.po_znaika.network.NetworkException;

/**
 * Created by Rihter on 10.02.2015.
 * Fully manages the scores of the exercises
 */
public class ExerciseScoreProcessor implements IExerciseScoreProcessor
{
    /**
     * Caches operations that were not processed by server side
     */
    private static class ExerciseCacheDatabase extends SQLiteOpenHelper
    {
        private class CacheItem
        {
            public int id;
            public Date date;
            public String exerciseLiteralId;
            public int score;
        }

        private static final String DatabaseFilename = "exercise_server_cache.db";
        private static final int DatabaseVersion = 0;

        private static final String TableName = "ExerciseCache";
        private static final String IdColumnName = "id";
        private static final String DateColumnName = "date";
        private static final String ExerciseIdColumnName = "exercise_id";
        private static final String ScoreColumnName = "score";

        private static final String CreateExerciseCacheTableSqlStatement = "CREATE TABLE " + TableName + "(" +
                IdColumnName + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                DateColumnName + " INTEGER NOT NULL, " +
                ExerciseIdColumnName + " INTEGER NOT NULL, " +
                ScoreColumnName + " INTEGER NOT NULL)";
        private static final String DropExerciseCacheTableSqlStatement = "DROP TABLE " + TableName;

        public ExerciseCacheDatabase(@NonNull Context _context)
        {
            super(_context, DatabaseFilename, null, DatabaseVersion);
        }

        // Method is called during creation of the database
        @Override
        public void onCreate(SQLiteDatabase database)
        {
            database.execSQL(CreateExerciseCacheTableSqlStatement);
        }

        // Method is called during an upgrade of the database,
        @Override
        public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
        {
            database.execSQL(DropExerciseCacheTableSqlStatement);
            onCreate(database);
        }

        public List<CacheItem> getCacheItems()
        {
            return new ArrayList<>();
        }

        public int addCacheItem(@NonNull Date date, @NonNull String exerciseName, int score)
        {
            return DatabaseConstant.InvalidDatabaseIndex;
        }

        public boolean removeCacheItem(int itemId)
        {
            return false;
        }

        public boolean removeCacheItem(@NonNull List<Integer> itemIds)
        {
            return false;
        }
    }

    private final String LogTag = ExerciseScoreProcessor.class.getName();

    public ExerciseScoreProcessor(@NonNull Context _context, @NonNull IServerOperations _serverOperations) throws CommonException
    {
        m_diaryDatabase = new DiaryDatabase(_context);
        m_exerciseScoreCache = new ExerciseCacheDatabase(_context);
        m_serverOperations = _serverOperations;
    }

    public void reportExerciseScore(@NonNull String exerciseName, int score) throws CommonException
    {
        Log.i(LogTag, String.format("Report exercise score: exerciseId:\"%s\", score:\"%d\"", exerciseName, score));

        final Date scoreDate = new Date();

        {
            final int rowId = m_diaryDatabase.insertExerciseScore(scoreDate, exerciseName, score);
            if (rowId == DatabaseConstant.InvalidDatabaseIndex)
                throw new CommonException(CommonResultCode.UnknownReason);
        }

        try
        {
            m_serverOperations.reportExerciseScore(scoreDate, exerciseName, score);
            List<ExerciseCacheDatabase.CacheItem> cacheItems = m_exerciseScoreCache.getCacheItems();
            if (cacheItems != null)
            {
                List<Integer> sendedItems = new ArrayList<>();
                try
                {
                    for (ExerciseCacheDatabase.CacheItem cacheItem : cacheItems)
                    {
                        Log.i(LogTag, String.format("Try to send cache item - id:\"%d\" date:\"%s\" literalId:\"%s\" score:\"%d\"",
                                        cacheItem.id,
                                        cacheItem.date,
                                        cacheItem.exerciseLiteralId,
                                        cacheItem.score));
                        m_serverOperations.reportExerciseScore(cacheItem.date, cacheItem.exerciseLiteralId, score);
                    }
                }
                catch (CommonException | NetworkException exp)
                {
                    Log.w(LogTag, "Failed to send cache item");
                }

                if (!sendedItems.isEmpty())
                    m_exerciseScoreCache.removeCacheItem(sendedItems);
            }
        }
        catch (CommonException | NetworkException exp)
        {
            Log.w(LogTag, String.format("Failed to send cache item, exp:\"%s\"", exp.getMessage()));
            if (m_exerciseScoreCache.addCacheItem(scoreDate, exerciseName, score) == DatabaseConstant.InvalidDatabaseIndex)
            {
                Log.e(LogTag, "Failed to save items both on server and cache");
                throw new CommonException(CommonResultCode.UnknownReason);
            }
        }
    }

    private DiaryDatabase m_diaryDatabase;
    private ExerciseCacheDatabase m_exerciseScoreCache;
    private IServerOperations m_serverOperations;
}
