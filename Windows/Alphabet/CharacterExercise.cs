using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Controls;
using System.Windows.Navigation;

using ru.po_znaika.database.Alphabet;
using ru.po_znaika.server_feedback;
using ru.po_znaika.common;

namespace ru.po_znaika.alphabet
{
    class CharacterExercise : IExercise, Tracer.ITracerVisitor
    {
        public CharacterExercise(NavigationWindow mainWindow, Page returnPage, IServerFeedback serverFeedback, int exerciseId, string name, string displayName, int displayImageId, AlphabetDatabase localDatabase)
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
            m_exerciseName = name;
            m_displayExerciseName = displayName;
            m_localDatabase = localDatabase;
            m_displayImageId = displayImageId;
        }

        public void SetTracer(Tracer.ITracer tracer)
        {
            m_tracer = tracer;
        }

        private IExerciseStep CreateExerciseSteps(po_znaika.server_feedback.IServerFeedback serverFeedback)
        {
            ExitExerciseStep exitStep = new ExitExerciseStep(m_mainWindow, m_returnPage, null);

            IExerciseStep startStep = exitStep;
            IExerciseStep lastStep = null;

            int characterId = m_localDatabase.GetCharacterIdByExerciseId(m_exerciseId);
            if (characterId == ru.po_znaika.database.Constant.InvalidDatabaseIndex)
                throw new Exception("Failed to extract character exercise id from database");

            ///
            /// Create theory exercises
            ///
            {
                List<CharacterTheoryExerciseInfo> exerciseTheorySteps = m_localDatabase.GetCharacterTheoryInfoByExerciseId(characterId);
                if (exerciseTheorySteps == null)
                    throw new Exception("Failed to extract theory exercises from database");

                Tracer.helpers.TraceString(m_tracer, Tracer.helpers.NormalTraceLevel, "Create theory exercises");

                SortedDictionary<int, CharacterTheoryPage> theoryExercises = new SortedDictionary<int, CharacterTheoryPage>();
                foreach (CharacterTheoryExerciseInfo stepInfo in exerciseTheorySteps)
                {
                    try
                    {
                        Tracer.helpers.TraceString(m_tracer, Tracer.helpers.LowTraceLevel,
                            string.Format("Create theory exercice page with imageId: \"{0}\", soundId \"{1}\"",
                            stepInfo.imageId,
                            stepInfo.soundId));
                     
                        using (Stream theoryImageStream = m_localDatabase.GetImageById(stepInfo.imageId))
                        {
                            using (Stream theorySoundStream = m_localDatabase.GetSoundById(stepInfo.soundId))
                            {
                                CharacterTheoryPage theoryPage = new CharacterTheoryPage(m_mainWindow, theoryImageStream, theorySoundStream);
                                theoryExercises.Add(stepInfo.stepNumber, theoryPage);

                                Tracer.helpers.SetTracer(m_tracer, theoryPage);                                
                            }
                        }
                    }
                    catch
                    {
                        foreach (var exercise in theoryExercises.Values)
                            exercise.Dispose();

                        throw;
                    }
                }

                // set previous steps ieratchy
                for (int exerciseNum = 1; exerciseNum < theoryExercises.Count; ++exerciseNum)
                    theoryExercises[exerciseNum].SetPreviousExerciseStep(theoryExercises[exerciseNum - 1]);

                // set next steps ierarchy
                for (int exerciseNum = 0; exerciseNum < theoryExercises.Count - 1; ++exerciseNum)
                    theoryExercises[exerciseNum].SetNextExerciseStep(theoryExercises[exerciseNum + 1]);

                // fill exercises
                theoryExercises.First().Value.SetPreviousExerciseStep(startStep);

                startStep = theoryExercises.First().Value;
                lastStep = theoryExercises.Last().Value;
            }

            Tracer.helpers.TraceString(m_tracer, Tracer.helpers.NormalTraceLevel, "Create practical exercises");

            ///
            /// Create practical exercises
            ///
            {
                AverageExerciseStepsScoreNotification averageScoreNotification = new AverageExerciseStepsScoreNotification(m_serverFeedback, m_exerciseId, 2);

                {
                    ImageGuessExerciseStep imageGuessExercise = new ImageGuessExerciseStep(m_mainWindow, characterId, m_localDatabase, averageScoreNotification);
                    lastStep.SetNextExerciseStep(imageGuessExercise);
                    lastStep = imageGuessExercise;

                    Tracer.helpers.SetTracer(m_tracer, imageGuessExercise);
                }

                {
                    CharacterSelectionExerciseStep characterSelectionExercise = new CharacterSelectionExerciseStep(m_exerciseId, m_mainWindow, m_localDatabase, averageScoreNotification);
                    characterSelectionExercise.SetNextExerciseStep(exitStep);
                    lastStep.SetNextExerciseStep(characterSelectionExercise);
                    lastStep = characterSelectionExercise;

                    Tracer.helpers.SetTracer(m_tracer, characterSelectionExercise);
                }
            }

            lastStep.SetNextExerciseStep(exitStep);

            return startStep;
        }

        public void Process()
        {
            Tracer.helpers.TraceString(m_tracer, Tracer.helpers.HighTraceLevel, 
                string.Format("Character exercise id: \"{0}\", name: \"{1}\" is selected",
                m_exerciseId,
                m_exerciseName));

            IExerciseStep startStep = CreateExerciseSteps(m_serverFeedback);
            if (startStep == null)
                throw new Exception("Failed to create exercise steps");
            startStep.Process();
        }
                
        public int GetId()
        {
            return m_exerciseId;
        }

        public string GetName()
        {
            return m_exerciseName;
        }

        public string GetDisplayName()
        {
            return m_displayExerciseName;
        }

        public Stream GetDisplayImage()
        {
            return m_localDatabase.GetImageById(m_displayImageId);
        }

        private Tracer.ITracer m_tracer;

        /// <summary>
        /// Navigation back to menu window
        /// </summary>

        private NavigationWindow m_mainWindow;
        private Page m_returnPage;

        /// <summary>
        /// Main class data
        /// </summary>

        private int m_exerciseId;
        
        private string m_exerciseName;
        private string m_displayExerciseName;
        private int m_displayImageId;

        private AlphabetDatabase m_localDatabase;
        private IServerFeedback m_serverFeedback;        
    }
}
