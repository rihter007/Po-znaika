package ru.po_znaika.alphabet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Closeable;

import com.arz_x.CommonException;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;

import ru.po_znaika.licensing.ILicensing;
import ru.po_znaika.licensing.Licensing;

import ru.po_znaika.network.CacheAuthenticationProvider;
import ru.po_znaika.network.IAuthenticationProvider;
import ru.po_znaika.network.IServerOperations;
import ru.po_znaika.network.ServerOperationsManager;

/**
 * Created by Rihter on 08.02.2015.
 * A simple way to initialize and access common services and helpers
 */
public class CoreServiceLocator implements Closeable
{
    private static final String LogTag = CoreServiceLocator.class.getName();

    public CoreServiceLocator(@NonNull Context _context)
    {
        m_context = _context;
    }

    public synchronized AlphabetDatabase getAlphabetDatabase()
    {
        if (m_alphabetDatabase == null)
        {
            try
            {
                m_alphabetDatabase = new AlphabetDatabase(m_context, false);
            }
            catch (CommonException exp)
            {
                Log.e(LogTag, "Failed to create alphabet database: " + exp.getMessage());
            }
        }

        return m_alphabetDatabase;
    }

    public synchronized DiaryDatabase getDiaryDatabase()
    {
        if (m_diaryDatabase == null)
            m_diaryDatabase = new DiaryDatabase(m_context);

        return m_diaryDatabase;
    }

    public synchronized IAuthenticationProvider getAuthenticationProvider()
    {
        if (m_authenticationProvider == null)
            m_authenticationProvider = new CacheAuthenticationProvider(m_context);

        return m_authenticationProvider;
    }

    public synchronized IExerciseScoreProcessor getExerciseScoreProcessor()
    {
        if (m_exerciseScoreProcessor == null)
        {
            try
            {
                m_exerciseScoreProcessor = new ExerciseScoreProcessor(m_context, getServerOperations());
            }
            catch (CommonException exp)
            {
                Log.e(LogTag, "Failed to create exercise score processor: " + exp.getMessage());
            }
        }
        return m_exerciseScoreProcessor;
    }

    public synchronized IServerOperations getServerOperations()
    {
        if (m_serverOperations == null)
            m_serverOperations = new ServerOperationsManager(getAuthenticationProvider());

        return m_serverOperations;
    }

    public synchronized ILicensing getLicensing()
    {
        if (m_licensing == null)
            m_licensing = new Licensing(m_context, getAuthenticationProvider());

        return m_licensing;
    }

    public synchronized IMediaPlayerManager getMediaPlayerManager()
    {
        if (m_mediaPlayerManager == null)
            m_mediaPlayerManager = new MediaPlayerManager(m_context, getAlphabetDatabase());

        return m_mediaPlayerManager;
    }

    @Override
    public synchronized void close()
    {
        if (m_authenticationProvider != null)
        {
            try
            {
                ((Closeable) m_exerciseScoreProcessor).close();
            }
            catch (Exception exp) { }
        }

        m_context = null;
        m_alphabetDatabase = null;
        m_diaryDatabase = null;
        m_authenticationProvider = null;
        m_serverOperations = null;
        m_exerciseScoreProcessor = null;
        m_licensing = null;
        m_mediaPlayerManager = null;
    }

    private Context m_context;
    private AlphabetDatabase m_alphabetDatabase;
    private DiaryDatabase m_diaryDatabase;
    private IAuthenticationProvider m_authenticationProvider;
    private IServerOperations m_serverOperations;
    private IExerciseScoreProcessor m_exerciseScoreProcessor;
    private ILicensing m_licensing;
    private IMediaPlayerManager m_mediaPlayerManager;
}
