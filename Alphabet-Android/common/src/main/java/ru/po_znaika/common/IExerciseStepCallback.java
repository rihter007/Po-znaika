package ru.po_znaika.common;

/**
 * Created by Rihter on 13.08.2014.
 */
public interface IExerciseStepCallback
{
    void processNextStep();
    void processPreviousStep();
    void repeatExercise();
}
