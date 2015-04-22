package ru.po_znaika.alphabet;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;
import ru.po_znaika.common.ExerciseScore;
import ru.po_znaika.common.ru.po_znaika.common.helpers.CommonHelpers;
import ru.po_znaika.network.IServerOperations;
import com.arz_x.NetworkException;
import com.arz_x.NetworkResultCode;

/**
 * Created by Rihter on 10.02.2015.
 * Fully manages the scores of the exercises
 */
public class ExerciseScoreProcessor implements IExerciseScoreProcessor, Closeable
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
            public String exerciseNameId;
            public int score;
        }

        private static final String LogTag = ExerciseCacheDatabase.class.getName();

        private static final String DatabaseFilename = "exercise_server_cache.db";
        private static final int DatabaseVersion = 1;

        private static final String TableName = "ExerciseCache";
        private static final String IdColumnName = "id";
        private static final String DateColumnName = "date";
        private static final String ExerciseNameColumnName = "exercise_id";
        private static final String ScoreColumnName = "score";

        private static final String CreateExerciseCacheTableSqlStatement = "CREATE TABLE " + TableName + "(" +
                IdColumnName + " INTEGER PRIMARY KEY ASC AUTOINCREMENT, " +
                DateColumnName + " INTEGER NOT NULL, " +
                ExerciseNameColumnName + " INTEGER NOT NULL, " +
                ScoreColumnName + " INTEGER NOT NULL)";
        private static final String DropExerciseCacheTableSqlStatement = "DROP TABLE " + TableName;

        private static final String SelectCacheTableEmptySqlStatement = "SELECT 1 FROM " + TableName ;

        private static final String ClearCacheTableSqlStatement = "DELETE FROM " + TableName;

        private static final String ExtractAllCacheTableItemsSqlStatement = "SELECT " +
                IdColumnName + ", " +
                DateColumnName + ", " +
                ExerciseNameColumnName + ", " +
                ScoreColumnName + " " +
                "FROM " + TableName;

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

        public CacheItem[] getCacheItems()
        {
            SQLiteDatabase database = getReadableDatabase();
            Cursor dataReader = null;
            try
            {
                dataReader = database.rawQuery(ExtractAllCacheTableItemsSqlStatement, null);
                if (dataReader.moveToFirst())
                {
                    List<CacheItem> items = new ArrayList<>();

                    do
                    {
                        CacheItem item = new CacheItem();
                        item.id = dataReader.getInt(0);
                        item.date = new Date(dataReader.getLong(1));
                        item.exerciseNameId = dataReader.getString(2);
                        item.score = dataReader.getInt(3);

                        items.add(item);
                    } while (dataReader.moveToNext());

                    CacheItem[] result = new CacheItem[items.size()];
                    items.toArray(result);
                    return result;
                }
            }
            catch (Exception exp)
            {
                Log.e(LogTag, String.format("getCacheItems exception: \"%s\"", exp.getMessage()));
                return null;
            }
            finally
            {
                if (dataReader != null)
                    dataReader.close();

                database.close();
            }

            return new CacheItem[0];
        }

        public boolean isCacheEmpty()
        {
            SQLiteDatabase database = getReadableDatabase();
            Cursor dataReader = null;

            try
            {
                dataReader = database.rawQuery(SelectCacheTableEmptySqlStatement, null);
                return !dataReader.moveToFirst();
            }
            catch (SQLiteException exp)
            {
                Log.e(LogTag, "isCacheEmpty: " + exp.getMessage());
            }
            finally
            {
                if (dataReader != null)
                    dataReader.close();

                database.close();
            }
            return false;
        }

        public int addCacheItem(@NonNull Date date, @NonNull String exerciseName, int score)
        {
            SQLiteDatabase database = getWritableDatabase();
            try
            {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DateColumnName, date.getTime());
                contentValues.put(ExerciseNameColumnName, exerciseName);
                contentValues.put(ScoreColumnName, score);

                return (int)database.insert(TableName, null, contentValues);
            }
            catch (Exception exp)
            {
                Log.e(LogTag, String.format("addCacheItem exception: \"%s\"", exp.getMessage()));
            }
            finally
            {
                database.close();
            }

            return DatabaseConstant.InvalidDatabaseIndex;
        }

        public boolean removeCacheItems(@NonNull Collection<Integer> itemIds)
        {
            if (itemIds.isEmpty())
                return true;

            SQLiteDatabase database = getWritableDatabase();
            try
            {
                int index = 0;
                String[] deletionIds = new String[itemIds.size()];
                for (Integer itemIndex : itemIds)
                    deletionIds[index++] = itemIds.toString();

                database.delete(TableName, IdColumnName + '=', deletionIds);
                return true;
            }
            catch (Exception exp)
            {
                Log.e(LogTag, String.format("removeCacheItem exception: \"%s\"", exp.getMessage()));
            }
            finally
            {
                database.close();
            }
            return false;
        }

        public boolean removeCacheItems()
        {
            SQLiteDatabase database = getWritableDatabase();

            try
            {
                database.execSQL(ClearCacheTableSqlStatement);
            }
            catch (SQLiteException exp)
            {
                Log.e(LogTag, "Assert: removeCacheItems, message: " + exp.getMessage());
                return false;
            }
            finally
            {
                database.close();
            }
            return true;
        }
    }

    private class ServerScoreReporter implements Runnable
    {
        public ServerScoreReporter(@NonNull Map<Integer, ExerciseScore> reportedScores)
        {
            m_commonErrorCode = null;
            m_networkErrorCode = null;

            m_reportedScores = reportedScores;
        }

        @Override
        public void run()
        {
            // TODO: temporary realization due to inconsistent protocol
            List<Integer> sendedItemsIds = new ArrayList<>();

            final Collection<Map.Entry<Integer, ExerciseScore> > allExerciseItems = m_reportedScores.entrySet();
            try
            {
                for (Map.Entry<Integer, ExerciseScore> item : allExerciseItems)
                {
                    final ExerciseScore exerciseScore = item.getValue();
                    m_serverOperations.reportExerciseScore(exerciseScore.date, exerciseScore.exerciseName, exerciseScore.score);
                    sendedItemsIds.add(item.getKey());
                }
            }
            catch (CommonException | NetworkException exp)
            {
                Log.w(LogTag, String.format("Failed to send cache item, exp:\"%s\"", exp.getMessage()));
                if (exp instanceof CommonException)
                    m_commonErrorCode = ((CommonException)exp).getResultCode();
                else
                    m_networkErrorCode = ((NetworkException) exp).getResultCode();
            }

            m_exerciseScoreCache.removeCacheItems(sendedItemsIds);
        }

        public CommonResultCode getCommonErrorCode()
        {
            return m_commonErrorCode;
        }

        public NetworkResultCode getNetworkResultCode()
        {
            return m_networkErrorCode;
        }

        private CommonResultCode m_commonErrorCode;
        private NetworkResultCode m_networkErrorCode;

        private Map<Integer, ExerciseScore> m_reportedScores;
    }

    private class ServerScoreReporterThread extends Thread
    {
        public ServerScoreReporterThread()
        {
            super("ServerScoreReporter");
        }

        public synchronized void reportExercisesScores(@NonNull Map<Integer, ExerciseScore> scores)
        {
            if (isAlive())
                return;
            m_scoreReporter = new ServerScoreReporter(scores);
            start();
        }

        @Override
        public void run()
        {
            m_scoreReporter.run();
        }

        private ServerScoreReporter m_scoreReporter;
    }

    private final String LogTag = ExerciseScoreProcessor.class.getName();

    public ExerciseScoreProcessor(@NonNull Context _context, @NonNull IServerOperations _serverOperations) throws CommonException
    {
        m_backGroundThread = new ServerScoreReporterThread();

        m_diaryDatabase = new DiaryDatabase(_context);
        m_exerciseScoreCache = new ExerciseCacheDatabase(_context);
        m_serverOperations = _serverOperations;
    }

    @Override
    public void syncCache() throws CommonException, NetworkException
    {
        if (m_exerciseScoreCache.isCacheEmpty())
            return;

        {
            ServerScoreReporter scoreReporter = new ServerScoreReporter(getCacheItems());
            scoreReporter.run();
            if (scoreReporter.getCommonErrorCode() != null)
                throw new CommonException(scoreReporter.getCommonErrorCode());
            if (scoreReporter.getNetworkResultCode() != null)
                throw new NetworkException(scoreReporter.getNetworkResultCode());
        }
    }

    @Override
    public void clearCache()
    {
        m_exerciseScoreCache.removeCacheItems();
    }

    @Override
    public void reportExerciseScore(@NonNull String exerciseName, int score) throws CommonException
    {
        Log.i(LogTag, String.format("Report exercise score: exerciseId:\"%s\", score:\"%d\"", exerciseName, score));

        final Date scoreDate = CommonHelpers.beginOfTheDay(new Date());

        {
            final int rowId = m_diaryDatabase.insertExerciseScore(scoreDate, exerciseName, score);
            if (rowId == DatabaseConstant.InvalidDatabaseIndex)
            {
                Log.e(LogTag, "Failed to insert database score");
                throw new CommonException(CommonResultCode.UnknownReason);
            }
        }
        m_exerciseScoreCache.addCacheItem(scoreDate, exerciseName, score);

        m_backGroundThread.reportExercisesScores(getCacheItems());
    }

    @Override
    public void close()
    {
        try
        {
            m_backGroundThread.join();
        }
        catch (InterruptedException exp)
        {
            Log.e(LogTag, "Assert: backGround thread has been aborted");
        }
    }

    private Map<Integer, ExerciseScore> getCacheItems()
    {
        ExerciseCacheDatabase.CacheItem[] cacheItems = m_exerciseScoreCache.getCacheItems();
        Map<Integer, ExerciseScore> exerciseScores = new HashMap<>();

        if (cacheItems != null)
        {
            for (ExerciseCacheDatabase.CacheItem cachedItem : cacheItems)
            {
                ExerciseScore exerciseScore = new ExerciseScore();
                exerciseScore.date = cachedItem.date;
                exerciseScore.exerciseName = cachedItem.exerciseNameId;
                exerciseScore.score = cachedItem.score;

                exerciseScores.put(cachedItem.id, exerciseScore);
            }
        }

        return exerciseScores;
    }

    private ServerScoreReporterThread m_backGroundThread;

    private DiaryDatabase m_diaryDatabase;
    private ExerciseCacheDatabase m_exerciseScoreCache;
    private IServerOperations m_serverOperations;
}
