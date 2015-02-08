package ru.po_znaika.alphabet;

import java.lang.String;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExercise;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Representation for main menu of exercise
 */
public class CharacterExercise implements IExercise
{
    public CharacterExercise(@NonNull Context _context, @NonNull AlphabetDatabase _databaseConnection, int _exerciseId)
    {
        if (_exerciseId == DatabaseConstant.InvalidDatabaseIndex)
        {
            Log.e(CharacterExercise.class.getName(), "Exercise id is invalid");
            throw new IllegalArgumentException();
        }

        // Initialize internal variables from constructor parameters
        {
            m_context = _context;
            m_alphabetDatabase = _databaseConnection;
            m_exerciseId = _exerciseId;
        }

        // Get additional information from database
        {
            AlphabetDatabase.ExerciseInfo exerciseInfo = m_alphabetDatabase.getExerciseInfoById(m_exerciseId);
            if (exerciseInfo == null)
                throw new RuntimeException("Can`t load exercise information from database");

            m_exerciseName = exerciseInfo.name;
            m_exerciseDisplayName = exerciseInfo.displayName;

            if (exerciseInfo.imageId != DatabaseConstant.InvalidDatabaseIndex)
            {
                final String resourceFileName = m_alphabetDatabase.getImageFileNameById(exerciseInfo.imageId);
                if (TextUtils.isEmpty(resourceFileName))
                    throw new IllegalArgumentException();

                m_exerciseDisplayImageResourceId = DatabaseHelpers.getDrawableIdByName(m_context.getResources(), resourceFileName);
                if (m_exerciseDisplayImageResourceId == 0)
                    throw new IllegalArgumentException();
            }
        }
    }

    public void process()
    {
        /**
         * Extract general information about character exercise
         */
        AlphabetDatabase.CharacterExerciseInfo exerciseInfo = m_alphabetDatabase.getCharacterExerciseByExerciseId(m_exerciseId);
        if (exerciseInfo == null)
            throw new IllegalArgumentException("Failed to extract character exercise information");

        /**
         * Start character exercise menu activity
         */
        Intent intent = new Intent(m_context, SingleCharacterExerciseMenuActivity.class);
        intent.putExtra(Constant.CharacterExerciseIdTag, exerciseInfo.id);
        intent.putExtra(Constant.CharacterTag, exerciseInfo.character);
        m_context.startActivity(intent);
    }

    public int getId()
    {
        return m_exerciseId;
    }

    public String getName()
    {
        return m_exerciseName;
    }

    public String getDisplayName()
    {
        return m_exerciseDisplayName;
    }

    public Drawable getDisplayImage()
    {
        return m_context.getResources().getDrawable(m_exerciseDisplayImageResourceId);
    }

    private Context m_context;

    private AlphabetDatabase m_alphabetDatabase;

    private int m_exerciseId;
    private String m_exerciseName;
    private String m_exerciseDisplayName;
    private int m_exerciseDisplayImageResourceId;
}
