package ru.po_znaika.network;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.ExerciseScore;

/**
 * Created by Rihter 19.01.2015
 * Implements feedback with server with caching results in Diary database
 */

class ServerOperationsCache extends SQLiteOpenHelper
{
    private static final String DatabaseName = "server_operations_cache.db";
    private static final int DatabaseVersion = 0;



    public ServerOperationsCache(Context context)
    {
        super(context, DatabaseName, null, DatabaseVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase database)
    {

    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion)
    {

    }
}

public class ServerOperationsManager implements IServerOperations, IAuthenticationProvider
{
    //private static final String ServerAddressUri = "";

    public ServerOperationsManager(Context context)
    {
        m_operationsCache = new ServerOperationsCache(context);
    }

    ///
    /// Implementation of IAuthenticationTokenSetter
    ///

    @Override
    public void SetAuthenticationToken(@NonNull AuthenticationToken token)
    {
        m_token = token;
    }

    ///
    /// Implementation of IServerOperations
    ///

    @Override
    public void reportExerciseScore(int exerciseId, int score)
            throws CommonException, NetworkException
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<ExerciseScore> getExercisesScores(int exerciseGroupId, Date startDate, Date endDate)
            throws CommonException, NetworkException
    {
        throw new UnsupportedOperationException();
    }

    private ServerOperationsCache m_operationsCache;
    private AuthenticationToken m_token;
}
