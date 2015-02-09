package ru.po_znaika.alphabet;

import android.support.annotation.NonNull;

import ru.po_znaika.common.CommonException;

/**
 * Created by Rihter on 10.02.2015.
 * Declares an interface for processing exercise scores
 */
public interface IExerciseScoreProcessor
{
    void reportExerciseScore(@NonNull String exerciseId, int score) throws CommonException;
}
