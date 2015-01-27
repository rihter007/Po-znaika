using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Media;
using System.Windows.Controls;
using System.Windows.Navigation;
using System.Media;

using ru.po_znaika.server_feedback;
using ru.po_znaika.database.Alphabet;

using ru.po_znaika.common;

namespace ru.po_znaika.alphabet
{
    class ImageGuessExerciseStep : IExerciseStep, ISingleOptionSelectionCallback, Tracer.ITracerVisitor
    {
        private class ExerciseInfo
        {
            public string taskCaption;

            public CharacterObjectImage[] images;
            public int correctAnswer;
        }

        private struct ImageExerciseTypeDescription
        {
            public CharacterDispositionFlag characterDisposition;
            public string taskCaption;
            public int exerciseCount;
        }

        private const int ContainCharacterExerciseCount = 1;
        private const int BeginCharacterExerciseCount = 1;
        private const int EndCharacterExerciseCount = 1;

        /// <summary>
        /// Number of images in exercise (1-correct, 3-non correct)
        /// </summary>
        private const int ExerciseImagesCount = 4;

        public ImageGuessExerciseStep(NavigationWindow mainWindow, int characterExerciseId, AlphabetDatabase database, IExerciseStepsScoreNotification scoreNotification)
        {
            if ((mainWindow == null) || (database == null) || (scoreNotification == null))
                throw new ArgumentNullException();

            if (characterExerciseId == ru.po_znaika.database.Constant.InvalidDatabaseIndex)
                throw new ArgumentException("Invalid character index");

            m_mainWindow = mainWindow;

            m_characterExerciseId = characterExerciseId;
            m_database = database;
            m_soundPlayer = new GeneralSoundPlayer(m_database);

            m_scoreNotification = scoreNotification;            

            if (!GetContent())
                throw new Exception("Failed to prepare exercise");

            m_currentExerciseIndex = 0;
        }

        public void SetTracer(Tracer.ITracer tracer)
        {
            m_tracer = tracer;
        }

        public void SetPreviousExerciseStep(IExerciseStep prevExerciseStep)
        {
            return;
        }

        public void SetNextExerciseStep(IExerciseStep nextExerciseStep)
        {
            m_nextExerciseStep = nextExerciseStep;
        }

        /// <summary>
        /// Gets content without actual loading for all sub exercies
        /// </summary>
        /// <returns></returns>
        private bool GetContent()
        {
            char exerciseCharacterValue = '\0';

            {
                CharacterExerciseInfo? characterExerciseInfo = m_database.GetCharacterValueById(m_characterExerciseId);
                if (!characterExerciseInfo.HasValue)
                    return false;

                exerciseCharacterValue = characterExerciseInfo.Value.value;
            }

            int placedExerciseIndex = 0;
            m_exercisesInfo = new ExerciseInfo[ContainCharacterExerciseCount + BeginCharacterExerciseCount + EndCharacterExerciseCount];

            Random rand = new Random(DateTime.Now.Millisecond);

            ImageExerciseTypeDescription[] imageExerciseTypes = new ImageExerciseTypeDescription[]
            {
                new ImageExerciseTypeDescription()
                {
                    characterDisposition = CharacterDispositionFlag.Contains,
                    taskCaption = string.Format("Выбери предмет с буквой \'{0}\'", exerciseCharacterValue),
                    exerciseCount = ContainCharacterExerciseCount
                },

                new ImageExerciseTypeDescription()
                {
                    characterDisposition = CharacterDispositionFlag.Begining,
                    taskCaption = string.Format("Выбери предмет начинающийся на букву \'{0}\'", exerciseCharacterValue),
                    exerciseCount = BeginCharacterExerciseCount
                },

                new ImageExerciseTypeDescription()
                {
                    characterDisposition = CharacterDispositionFlag.End,
                    taskCaption = string.Format("Выбери предмет заканчивающийся на букву \'{0}\'", exerciseCharacterValue),
                    exerciseCount = EndCharacterExerciseCount
                }
            };

            // Create exercies using specified description above
            for (int imageExerciseTypeIndex = 0; imageExerciseTypeIndex < imageExerciseTypes.Length; ++imageExerciseTypeIndex)
            {
                ExerciseInfo[] typeExercises = GetSingleCharacterExerciseContent(rand, imageExerciseTypes[imageExerciseTypeIndex].characterDisposition,
                    imageExerciseTypes[imageExerciseTypeIndex].taskCaption, ContainCharacterExerciseCount);
                if (typeExercises == null)
                {
                    Tracer.helpers.TraceString(m_tracer, Tracer.helpers.CriticalTraceLevel,
                        string.Format("Failed to get exercise content for char disposition: \"{0}\"",
                        imageExerciseTypes[imageExerciseTypeIndex].characterDisposition));

                    return false;
                }

                Array.Copy(typeExercises, 0, m_exercisesInfo, placedExerciseIndex, typeExercises.Length);
                placedExerciseIndex += typeExercises.Length;
            }

            return true;
        }

        /// <summary>
        /// Loads content for single chaarcter type exercises: [Begins, Contans, Ends]
        /// </summary>
        /// <param name="rand">Random numbers generator for randomly selecting image position for correct answer</param>
        /// <param name="charDisposition">Indicates what object pictures to choose: Begining with that character, containg this character, ending with this character</param>
        /// <param name="taskCaption">Task caption shown in exercise page</param>
        /// <param name="exerciseCount">How many exercises to create</param>
        /// <returns>If succeeds returns array of created exercies, null otherwise</returns>
        private ExerciseInfo[] GetSingleCharacterExerciseContent(Random rand, CharacterDispositionFlag charDisposition, string taskCaption, int exerciseCount)
        {
            List<CharacterObjectImage> characterCotainingObjects = m_database.GetRandomCharacterObjectImages(m_characterExerciseId, charDisposition, exerciseCount);
            if ((characterCotainingObjects == null) || (characterCotainingObjects.Count !=exerciseCount))
                return null;

            List<CharacterObjectImage> nonContainingCharacterObjects = m_database.GetRandomOtherCharacterObjectImages(m_characterExerciseId, charDisposition, (ExerciseImagesCount - 1) * exerciseCount);
            if ((nonContainingCharacterObjects == null) || (nonContainingCharacterObjects.Count != (ExerciseImagesCount - 1) * exerciseCount))
                return null;            

            ExerciseInfo[] resultExercies = new ExerciseInfo[exerciseCount];
            for (int exerciseIndex = 0; exerciseIndex < ContainCharacterExerciseCount; ++exerciseIndex)
            {
                ExerciseInfo exerciseInfo = new ExerciseInfo();
                exerciseInfo.taskCaption = taskCaption;
                exerciseInfo.correctAnswer = rand.Next(ExerciseImagesCount);

                exerciseInfo.images = new CharacterObjectImage[ExerciseImagesCount];
                int otherCharImageIndex = 0;
                for (int imageIndex = 0; imageIndex < ExerciseImagesCount; ++imageIndex)
                {
                    if (imageIndex == exerciseInfo.correctAnswer)
                    {
                        exerciseInfo.images[imageIndex] = characterCotainingObjects[exerciseIndex];
                    }
                    else
                    {
                        exerciseInfo.images[imageIndex] = nonContainingCharacterObjects[(ExerciseImagesCount - 1) * exerciseIndex + otherCharImageIndex];
                        ++otherCharImageIndex;
                    }
                }

                resultExercies[exerciseIndex] = exerciseInfo;
            }

            return resultExercies;
        }

        /// <summary>
        /// Loads actual images from database
        /// </summary>
        /// <param name="objectImages"></param>
        /// <returns></returns>
        private ImageSource[] CreateImagesFromDatabase(IList<CharacterObjectImage> objectImages)
        {
            ImageSource[] images = new ImageSource[objectImages.Count];

            for (int imageIndex = 0; imageIndex < objectImages.Count; ++imageIndex)
            {
                MemoryStream ms = m_database.GetImageById(objectImages[imageIndex].imageId);
                if (ms == null)
                    return null;

                using (ms)
                {
                    ImageSource imgSrc = Helpers.CreateImageFromStream(ms);
                    if (imgSrc == null)
                        return null;

                    images[imageIndex] = imgSrc;
                }
            }

            return images;
        }

        public void Process()
        {
            if (m_currentExerciseIndex < m_exercisesInfo.Length)
            {
                try
                {
                    ImageSelectionPage selectionPage = new ImageSelectionPage(this, m_exercisesInfo[m_currentExerciseIndex].taskCaption, CreateImagesFromDatabase(m_exercisesInfo[m_currentExerciseIndex].images));
                    m_mainWindow.Navigate(selectionPage);
                }
                catch (Exception e)
                {
                    Tracer.helpers.TraceString(m_tracer, Tracer.helpers.CriticalTraceLevel,
                        string.Format("Failed to process image selection page. Exce[tion message: \"{0}\"", e.Message));

                    ++m_currentExerciseIndex;
                    Process();
                }
            }
            else
            {
                Tracer.helpers.TraceString(m_tracer, Tracer.helpers.NormalTraceLevel, "Image selection exercise is finished");

                // calc statistics
                int totalScore = (int)((((double)m_correctAnswersCount) / ((double)(ContainCharacterExerciseCount + BeginCharacterExerciseCount + EndCharacterExerciseCount))) * ExerciseConstant.MaxScore);
                if (m_scoreNotification != null)
                    m_scoreNotification.SetExerciseStepScore(totalScore);

                ResultsPage resultsPage = new ResultsPage(totalScore, m_nextExerciseStep);
                m_mainWindow.Navigate(resultsPage);
            }
        }

        /// <summary>
        /// Callback on user image selection
        /// </summary>
        /// <param name="variant"></param>
        public void OnSelection(int variant)
        {
            bool isAnswerCorrect = variant == m_exercisesInfo[m_currentExerciseIndex].correctAnswer;
            if (isAnswerCorrect)
                ++m_correctAnswersCount;

            m_soundPlayer.PlaySoundByType(isAnswerCorrect ? SoundType.Good : SoundType.Bad);           
            ++m_currentExerciseIndex;

            Process();
        }

        private Tracer.ITracer m_tracer;

        private NavigationWindow m_mainWindow;

        private IExerciseStep m_nextExerciseStep;

        private AlphabetDatabase m_database;
        private int m_characterExerciseId;

        private GeneralSoundPlayer m_soundPlayer;
        
        private int m_currentExerciseIndex;
        private ExerciseInfo[] m_exercisesInfo;
        private IExerciseStepsScoreNotification m_scoreNotification;

        private int m_correctAnswersCount;        
    }
}
