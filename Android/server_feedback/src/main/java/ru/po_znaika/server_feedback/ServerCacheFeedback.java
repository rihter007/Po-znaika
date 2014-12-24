package ru.po_znaika.server_feedback;

import android.content.Context;

import java.util.Date;

import ru.po_znaika.database.diary.DiaryDatabase;

/**
 * Implements feedback with server with caching results in Diary database
 */
public class ServerCacheFeedback implements IServerFeedback
{
    //private static final String ServerAddressUri = "";

    public ServerCacheFeedback(Context context)
    {
        m_diaryDatabase = new DiaryDatabase(context);
    }

    public boolean reportExerciseResult(int exerciseId, int score)
    {
        //final boolean IsDataSended = sendDataToServer(exerciseId, score);

        return saveInLocalCache(exerciseId, score, false);
    }

    private boolean saveInLocalCache(int exerciseId, int score, boolean isSentToServer)
    {
        return m_diaryDatabase.insertExerciseScore(new Date(System.currentTimeMillis()), exerciseId, score,
                isSentToServer) != ru.po_znaika.database.DatabaseConstant.InvalidDatabaseIndex;
    }

    private DiaryDatabase m_diaryDatabase;
}
