package ru.po_znaika.alphabet;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Represents start wrapper for WordGather exercise
 */
public class WordGatherExercise implements IExercise
{
    public WordGatherExercise(Context _context, AlphabetDatabase _databaseConnection, int _exerciseId)
    {
        if ((_context == null) || (_databaseConnection == null) || (_exerciseId == DatabaseConstant.InvalidDatabaseIndex))
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
        m_exerciseDisplayImageResourceId = m_context.getResources().getIdentifier(
                _databaseConnection.getImageFileNameById(exerciseInfo.imageId),
                Constant.DrawableResourcesTag,
                m_context.getPackageName());

        if (m_exerciseDisplayImageResourceId == 0)
            throw new IllegalArgumentException();
    }

    public void process()
    {
        Intent intent = new Intent(m_context, WordGatherActivity.class);
        intent.putExtra(Constant.ExerciseIdTag, m_exerciseId);
        intent.putExtra(Constant.AlphabetTypeTag, AlphabetDatabase.AlphabetType.Russian.getValue());

        m_context.startActivity(intent);
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
