package ru.po_znaika.alphabet;

import android.content.Context;

import ru.po_znaika.common.IExercise;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Simple factory class pattern that creates and initializes all Alphabet exercises
 */
public class ExerciseFactory
{
    public ExerciseFactory(Context _context, AlphabetDatabase _alphabetDatabase)
    {
        m_context = _context;
        m_alphabetDatabase = _alphabetDatabase;
    }

    public IExercise CreateExerciseFromId(int exerciseId, AlphabetDatabase.ExerciseType exerciseType)
    {
        if (exerciseId == DatabaseConstant.InvalidDatabaseIndex)
            return  null;

        IExercise exercise = null;
        try
        {
            switch (exerciseType)
            {
                case Character:
                    exercise = new CharacterExercise(m_context, m_alphabetDatabase, exerciseId);
                    break;

                case WordGather:
                    exercise = new WordGatherExercise(m_context, m_alphabetDatabase, exerciseId);
                    break;

                case CreateWordsFromSpecified:
                    exercise = new CreateWordsFromSpecifiedExercise(m_context, m_alphabetDatabase, exerciseId);
                    break;
            }
        }
        catch (Exception exp)
        {
            exercise = null;
        }

        return exercise;
    }

    private Context m_context;
    private AlphabetDatabase m_alphabetDatabase;
}
