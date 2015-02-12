package ru.po_znaika.alphabet;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Represents an exercise for creation sub words from the main
 */
public class CreateWordsFromSpecifiedExercise implements IExercise
{
    public CreateWordsFromSpecifiedExercise(Context _context, AlphabetDatabase _alphabetDatabase, int _exerciseId)
    {
        if ((_context == null) || (_alphabetDatabase == null) || (_exerciseId == DatabaseConstant.InvalidDatabaseIndex))
            throw new IllegalArgumentException();

        {
            m_context = _context;
            m_exerciseId = _exerciseId;
        }

        // get additional information from database
        {
            AlphabetDatabase.ExerciseInfo exerciseInfo = _alphabetDatabase.getExerciseInfoById(m_exerciseId);
            if (exerciseInfo == null)
                throw new IllegalArgumentException();

            m_exerciseName = exerciseInfo.name;
            m_exerciseDisplayName = exerciseInfo.displayName;

            if (exerciseInfo.imageId != DatabaseConstant.InvalidDatabaseIndex)
            {
                final String displayImageFileName = _alphabetDatabase.getImageFileNameById(exerciseInfo.imageId);
                if (TextUtils.isEmpty(displayImageFileName))
                {
                    Log.e(CreateWordsFromSpecifiedExercise.class.getName(),
                            String.format("Could not get image file name with id: %d", exerciseInfo.imageId));
                    throw new IllegalArgumentException();
                }

                m_exerciseDisplayImageResourceId = DatabaseHelpers.getDrawableIdByName(m_context.getResources(), displayImageFileName);
                if (m_exerciseDisplayImageResourceId == 0)
                    throw new IllegalArgumentException();
            }
        }
    }

    /*
    Launches the exercise
    May throw exceptions
    */
    public void process()
    {
        CreateWordsFromSpecifiedActivity.startActivity(m_context, m_exerciseName, AlphabetDatabase.AlphabetType.Russian);
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
