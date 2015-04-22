package ru.po_znaika.alphabet;

import android.support.annotation.NonNull;

import com.arz_x.CommonException;

import com.arz_x.NetworkException;

/**
 * Created by Rihter on 10.02.2015.
 * Declares an interface for processing exercise scores
 */
public interface IExerciseScoreProcessor
{
    /**
     * Sends cached exercise marks to server
     */
    void syncCache() throws CommonException, NetworkException;

    void clearCache();

    /**
     * Saves exercise score and tries to sync it with server in background
     * @param exerciseNameId Unique exercise name
     * @param score Exercise score
     * @throws CommonException
     */
    void reportExerciseScore(@NonNull String exerciseNameId, int score) throws CommonException;
}
