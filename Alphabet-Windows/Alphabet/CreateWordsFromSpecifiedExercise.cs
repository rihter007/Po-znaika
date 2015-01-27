using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Controls;
using System.Windows.Navigation;

using ru.po_znaika.common;
using ru.po_znaika.server_feedback;
using ru.po_znaika.database.Alphabet;

namespace ru.po_znaika.alphabet
{
    class CreateWordsFromSpecifiedExercise : IExercise
    {
        public CreateWordsFromSpecifiedExercise(NavigationWindow mainWindow, Page returnPage, IServerFeedback serverFeedback, int exerciseId, string name, string displayName, int displayImageId, AlphabetDatabase localDatabase)
        {
            if ((mainWindow == null) || (returnPage == null) || (localDatabase == null))
                throw new ArgumentNullException();

            if (exerciseId == ru.po_znaika.database.Constant.InvalidDatabaseIndex)
                throw new ArgumentException("Exercise id is invalid");

            if (displayImageId == ru.po_znaika.database.Constant.InvalidDatabaseIndex)
                throw new ArgumentException("Display image id is invalid");

            m_mainWindow = mainWindow;
            m_returnPage = returnPage;
            m_serverFeedback = serverFeedback;

            m_exerciseId = exerciseId;
            m_exercsieName = name;
            m_displayExerciseName = displayName;
            m_displayImageId = displayImageId;

            m_localDatabase = localDatabase;
        }

        public void Process()
        {
            ExitExerciseStep lastExerciseStep = new ExitExerciseStep(m_mainWindow, m_returnPage, null);

            CreateWordsFromSpecifiedPage exerciseStep = new CreateWordsFromSpecifiedPage(m_mainWindow, m_localDatabase, m_exerciseId, m_serverFeedback);
            exerciseStep.SetPreviousExerciseStep(lastExerciseStep);
            exerciseStep.SetNextExerciseStep(lastExerciseStep);
            exerciseStep.Process();
        }

        public int GetId()
        {
            return m_exerciseId;
        }

        public string GetName()
        {
            return m_exercsieName;
        }

        public string GetDisplayName()
        {
            return m_displayExerciseName;
        }

        public System.IO.Stream GetDisplayImage()
        {
            return m_localDatabase.GetImageById(m_displayImageId);
        }

        private NavigationWindow m_mainWindow;
        private Page m_returnPage;
        private IServerFeedback m_serverFeedback;

        private int m_exerciseId;
        private string m_exercsieName;
        private string m_displayExerciseName;
        private int m_displayImageId;

        private AlphabetDatabase m_localDatabase;
    }
}
