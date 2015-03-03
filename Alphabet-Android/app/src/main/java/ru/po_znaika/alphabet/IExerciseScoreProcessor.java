package ru.po_znaika.alphabet;

import android.support.annotation.NonNull;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.network.NetworkException;

/**
 * Created by Rihter on 10.02.2015.
 * Declares an interface for processing exercise scores
 */
public interface IExerciseScoreProcessor
{
    /**
     * Sends cached exercise marks to server
     */
    boolean syncCacheData();

    /**
     * Saves exercise score and tries to sync it with server in background
     * @param exerciseNameId Unique exercise name
     * @param score Exercise score
     * @throws CommonException
     */
    void reportExerciseScore(@NonNull String exerciseNameId, int score) throws CommonException;
}
