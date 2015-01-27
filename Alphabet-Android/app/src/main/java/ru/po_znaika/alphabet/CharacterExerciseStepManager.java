package ru.po_znaika.alphabet;

import android.app.Fragment;

/**
 * Created by Rihter on 13.08.2014.
 * Performs management of exercise steps represented in fragments
 */
public class CharacterExerciseStepManager
{
    public static interface ICustomExerciseStepFactory
    {
        Fragment createExerciseStep(CharacterExerciseItemStepState data);
    }

    public CharacterExerciseStepManager(int _currentStep,
                                        CharacterExerciseItemStepState[] _exerciseSteps,
                                        ICustomExerciseStepFactory _customExerciseStepFactory)
    {
        assert _exerciseSteps != null;
        assert (_currentStep >= 0) && (_currentStep < _exerciseSteps.length);

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

        Fragment resultFragment = null;
        switch (m_exerciseSteps[m_currentStep].actionType)
        {
            case CustomAction:
                assert m_exerciseStepFactory != null;
                resultFragment = m_exerciseStepFactory.createExerciseStep(m_exerciseSteps[m_currentStep]);
                break;

            case TheoryPage:
                resultFragment = TheoryPageFragment.createFragment(m_exerciseSteps[m_currentStep]);
                break;
        }

        return resultFragment;
    }

    private int m_currentStep;
    private CharacterExerciseItemStepState[] m_exerciseSteps;
    private ICustomExerciseStepFactory m_exerciseStepFactory;
}
