package ru.po_znaika.alphabet;

import android.content.Context;
import android.support.annotation.NonNull;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;

import ru.po_znaika.common.CommonException;

import ru.po_znaika.licensing.ILicensing;
import ru.po_znaika.licensing.Licensing;

import ru.po_znaika.network.CacheAuthenticationProvider;
import ru.po_znaika.network.IServerOperations;
import ru.po_znaika.network.ServerOperationsManager;

/**
 * Created by Rihter on 08.02.2015.
 * A simple way to initialize and access common services and helpers
 */
public class CoreServiceLocator
{
    public CoreServiceLocator(@NonNull Context _context) throws CommonException
    {
        m_alphabetDatabase = new AlphabetDatabase(_context, false);
        m_diaryDatabase = new DiaryDatabase(_context);
        m_serverOperations = new ServerOperationsManager();
        m_exerciseScoreProcessor = new ExerciseScoreProcessor(_context, m_serverOperations);

        CacheAuthenticationProvider authProvider = new CacheAuthenticationProvider(_context);
        m_licensing = new Licensing(_context, authProvider);
    }

    public AlphabetDatabase getAlphabetDatabase()
    {
        return m_alphabetDatabase;
    }

    public DiaryDatabase getDiaryDatabase()
    {
        return m_diaryDatabase;
    }

    public IExerciseScoreProcessor getexerciseScoreProcessor()
    {
        return m_exerciseScoreProcessor;
    }

    public IServerOperations getServerOperations()
    {
        return m_serverOperations;
    }

    public ILicensing getLicensing()
    {
        return m_licensing;
    }

    private AlphabetDatabase m_alphabetDatabase;
    private DiaryDatabase m_diaryDatabase;
    IServerOperations m_serverOperations;
    private IExerciseScoreProcessor m_exerciseScoreProcessor;
    private ILicensing m_licensing;
}
