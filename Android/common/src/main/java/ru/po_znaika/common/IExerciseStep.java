package ru.po_znaika.common;

/**
 * Created by Rihter on 06.08.2014.
 *
 * Represents a single activity in general exercise.
 */
public interface IExerciseStep
{
    /**
     *  Starts an exercise step
     */
    void Process();

    void SetPreviousExerciseStep(IExerciseStep prevExerciseStep);
    void SetNextExerciseStep(IExerciseStep nextExerciseStep);
}
