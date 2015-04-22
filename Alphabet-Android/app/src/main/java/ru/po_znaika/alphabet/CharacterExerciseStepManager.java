package ru.po_znaika.alphabet;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arz_x.CommonException;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Created by Rihter on 13.08.2014.
 * Performs management of exercise steps represented in fragments
 */
final class CharacterExerciseStepManager
{
    public interface IExerciseStepFactory
    {
        Fragment createExerciseStep(@NonNull AlphabetDatabase.CharacterExerciseActionType actionType,
                                    int value) throws CommonException;
    }

    private static final String LogTag = CharacterExerciseStepManager.class.getName();

    public CharacterExerciseStepManager(int _currentStep,
                                        @NonNull CharacterExerciseItemStep[] _exerciseSteps,
                                        @NonNull IExerciseStepFactory _customExerciseStepFactory)
    {
        if (_currentStep >= _exerciseSteps.length)
            throw new IllegalArgumentException();

        m_currentStep = _currentStep;
        m_exerciseSteps = _exerciseSteps;
        m_exerciseStepFactory = _customExerciseStepFactory;
    }

    /**
     * Indicates whether all steps are processed
     * @return true if all exercise steps are processed, false otherwise
     */
    public boolean isFinished()
    {
        return m_currentStep >= m_exerciseSteps.length - 1;
    }

    public Fragment getPreviousExerciseStep()
    {
        if (m_currentStep <= 0)
            return  null;
        --m_currentStep;

        return getCurrentExerciseStep();
    }

    public Fragment getNextExerciseStep()
    {
        if (m_currentStep >= m_exerciseSteps.length - 1)
            return null;
        ++m_currentStep;

        return getCurrentExerciseStep();
    }

    public Fragment getCurrentExerciseStep()
    {
        if (m_currentStep >= m_exerciseSteps.length)
            return null;

        try
        {
            return m_exerciseStepFactory.createExerciseStep(m_exerciseSteps[m_currentStep].actionType,
                    m_exerciseSteps[m_currentStep].value);
        }
        catch (CommonException exp)
        {
            Log.e(LogTag, "Failed to create fragment: " + exp.getMessage());
        }
        return null;
    }

    private int m_currentStep;
    private CharacterExerciseItemStep[] m_exerciseSteps;
    private IExerciseStepFactory m_exerciseStepFactory;
}
