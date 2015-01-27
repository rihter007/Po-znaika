package ru.po_znaika.network;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import ru.po_znaika.common.ExerciseScore;
import ru.po_znaika.common.CommonException;

/**
 * Created by Rihter on 08.08.2014.
 * Represents communcation with server
 */
public interface IServerOperations
{
    /**
     * Sends student score on server
     * @param exerciseId unique exercise id
     * @param score exercise score
     * @throws CommonException
     * @throws NetworkException
     */
    void reportExerciseScore(int exerciseId, int score)
            throws CommonException, NetworkException;

    /**
     * Returns obtained scores for specified time period
     * @param exerciseGroupId id of the single exercise or logical group of exercises, if 0 - all alphabet exercises
     * @param startDate start date of exercise completion in GMT. If null - no start date
     * @param endDate end date of exercise completion n GMT. If null - no end date
     * @return
     * @throws CommonException
     * @throws NetworkException
     */
    List<ExerciseScore> getExercisesScores(int exerciseGroupId, Date startDate, Date endDate)
            throws CommonException, NetworkException;
}