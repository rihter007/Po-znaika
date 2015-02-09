package ru.po_znaika.network;

import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.ExerciseScore;

/**
 * Created by Rihter on 10.02.2015.
 * Provides implementation for all server operations
 */
public class ServerOperationsManager implements IServerOperations
{
    /**
     * Sends student score on server
     * @param date date of the operation in GMT
     * @param exerciseName unique exercise id
     * @param score exercise score
     * @throws ru.po_znaika.common.CommonException
     * @throws NetworkException
     */
    public void reportExerciseScore(@NonNull Date date, @NonNull String exerciseName, int score)
            throws CommonException, NetworkException
    {
        //throw new UnsupportedOperationException();
    }

    /**
     * Returns obtained scores for specified time period
     * @param exerciseGroupId id of the single exercise or logical group of exercises, if 0 - all alphabet exercises
     * @param startDate start date of exercise completion in GMT. If null - no start date
     * @param endDate end date of exercise completion n GMT. If null - no end date
     * @return
     * @throws CommonException
     * @throws NetworkException
     */
   public List<ExerciseScore> getExercisesScores(int exerciseGroupId, Date startDate, Date endDate)
            throws CommonException, NetworkException
    {
        return null;
        //throw new UnsupportedOperationException();
    }
}
