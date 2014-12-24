using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

using ru.po_znaika.server_feedback;

namespace ru.po_znaika.common
{
    public static class ExerciseConstant
    {
        public const int MaxScore = 100;
    }

    public interface IExerciseStepsScoreNotification
    {
        void SetExerciseStepScore(int score);
        void Save();
    }

    public class AverageExerciseStepsScoreNotification : IExerciseStepsScoreNotification
    {
        public AverageExerciseStepsScoreNotification(IServerFeedback serverFeedback, int exerciseId, int exerciseStepsCount)
        {
            if (serverFeedback == null)
                throw new ArgumentNullException();

            if (exerciseStepsCount <= 0)
                throw new ArgumentException();

            m_exerciseId = exerciseId;
            m_serverFeedback = serverFeedback;
            m_exerciseCount = exerciseStepsCount;

            m_processedStepsCount = 0;
            m_currentScore = 0;
        }

        public void SetExerciseStepScore(int score)
        {
            if ((m_processedStepsCount >= m_exerciseCount) || (score < 0))
                return;

            m_currentScore += score;

            if (++m_processedStepsCount == m_exerciseCount)
                Save();
        }

        public void Save()
        {
            m_serverFeedback.ReportExerciseResult(m_exerciseId, m_currentScore / m_processedStepsCount);
        }

        private int m_exerciseId;
        private int m_exerciseCount;
        private IServerFeedback m_serverFeedback;

        private int m_processedStepsCount;
        private int m_currentScore;
    }
}
