package ru.po_znaika.alphabet;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Represents start wrapper for WordGather exercise
 */
public class WordGatherExercise implements IExercise
{
    public WordGatherExercise(@NonNull Context _context, @NonNull AlphabetDatabase _databaseConnection, int _exerciseId)
    {
        if (_exerciseId == DatabaseConstant.InvalidDatabaseIndex)
            throw new IllegalArgumentException();

        {
            m_context = _context;
            m_exerciseId = _exerciseId;
        }

        AlphabetDatabase.ExerciseInfo exerciseInfo = _databaseConnection.getExerciseInfoById(m_exerciseId);
        if (exerciseInfo == null)
            throw new IllegalArgumentException();

        m_exerciseName = exerciseInfo.name;
        m_exerciseDisplayName = exerciseInfo.displayName;
        m_exerciseDisplayImageResourceId = DatabaseHelpers.getDrawableIdByName(m_context.getResources(),
                _databaseConnection.getImageFileNameById(exerciseInfo.imageId));

        if (m_exerciseDisplayImageResourceId == 0)
            throw new IllegalArgumentException();
    }

    public void process()
    {
        WordGatherActivity.startActivity(m_context, m_exerciseName, AlphabetDatabase.AlphabetType.Russian);
    }

    /* Returns a unique id of the exercise */
    public int getId()
    {
        return m_exerciseId;
    }

    /* Returns exercise internal format name */
    public String getName()
    {
        return m_exerciseName;
    }

    /* Returns user-friendly exercise name to display */
    public String getDisplayName()
    {
        return m_exerciseDisplayName;
    }

    /* Returns an exercise icon */
    public Drawable getDisplayImage()
    {
        return m_context.getResources().getDrawable(m_exerciseDisplayImageResourceId);
    }

    private Context m_context;

    private int m_exerciseId;
    private String m_exerciseName;
    private String m_exerciseDisplayName;
    private int m_exerciseDisplayImageResourceId;
}
