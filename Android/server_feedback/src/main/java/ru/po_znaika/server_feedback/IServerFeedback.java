package ru.po_znaika.server_feedback;

/**
 * Created by Rihter on 08.08.2014.
 */
public interface IServerFeedback
{
    boolean reportExerciseResult(int exerciseId, int score);
}
