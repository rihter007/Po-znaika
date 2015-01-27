using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Navigation;

using ru.po_znaika.database.Alphabet;
using ru.po_znaika.common;

namespace ru.po_znaika.alphabet
{
    class CharacterSelectionExerciseStep : IExerciseStep, IMultipleOptionSelectionCallback
    {
        public CharacterSelectionExerciseStep(int exerciseId, NavigationWindow mainWindow, AlphabetDatabase database, IExerciseStepsScoreNotification scoreNotification)
        {
            if ((mainWindow == null) || (database == null))
                throw new ArgumentException();

            m_mainWindow = mainWindow;
            m_database = database;
            m_scoreNotification = scoreNotification;

            if (!LoadContent(exerciseId, database))
                throw new Exception("Failed to prepare exercise");
        }

        private bool LoadContent(int exerciseId, AlphabetDatabase database)
        {
            int characterId = database.GetCharacterIdByExerciseId(exerciseId);
            if (characterId == ru.po_znaika.database.Constant.InvalidDatabaseIndex)
                return false;

            CharacterExerciseInfo? verifiedChar = database.GetCharacterValueByExerciseId(exerciseId);
            if (verifiedChar == null)
                return false;
            m_verifiedCharacter = verifiedChar.Value.value.ToString().ToLower()[0];

            m_verifiedText = database.GetRandomVerseByCharacterIdAndMaxLength(characterId, CharacterSelectionPage.MaxTextCharacters);
            if (string.IsNullOrWhiteSpace(m_verifiedText))
                return false;

            return true;
        }

        public void Process()
        {
            CharacterSelectionPage charSelectionPage = new CharacterSelectionPage(m_verifiedText, m_verifiedCharacter, m_database, this);
            m_mainWindow.Navigate(charSelectionPage);
        }

        public void SetPreviousExerciseStep(IExerciseStep prevExerciseStep)
        {
            return;
        }

        public void SetNextExerciseStep(IExerciseStep nextExerciseStep)
        {
            m_nextExerciseStep = nextExerciseStep;
        }

        public void OnSelection(IList<int> variant)
        {
            int totalScore = 0;
                        
            {
                int matchedCharactersCount = 0;
                int missedCharactersCount = 0;
                int wrongSelectedCharactersCount = 0;
                int totalProcessedCharactersCount = 0;

                SortedSet<int> remainCharacters = new SortedSet<int>(variant);

                string normalizedVerifiedText = m_verifiedText.ToLower();
                for (int textIndex = 0; textIndex < normalizedVerifiedText.Length; ++textIndex)
                {
                    if (!Char.IsLetterOrDigit(m_verifiedText[textIndex]))
                        continue;

                    ++totalProcessedCharactersCount;

                    if (m_verifiedText[textIndex] == m_verifiedCharacter)
                    {
                        if (remainCharacters.Contains(textIndex))
                            ++matchedCharactersCount;
                        else
                            ++missedCharactersCount;
                    }
                    else
                    {
                        if (remainCharacters.Contains(textIndex))
                            ++wrongSelectedCharactersCount;
                    }
                }

                totalScore = (int)(ExerciseConstant.MaxScore * (1.0 - (((double)(wrongSelectedCharactersCount + missedCharactersCount)) / ((double)(totalProcessedCharactersCount)))));
            }

            if (m_scoreNotification != null)
                m_scoreNotification.SetExerciseStepScore(totalScore);

            ResultsPage resultsPage = new ResultsPage(totalScore, m_nextExerciseStep);
            m_mainWindow.Navigate(resultsPage);
        }

        private NavigationWindow m_mainWindow;
        private AlphabetDatabase m_database;

        private IExerciseStep m_nextExerciseStep;
        private char m_verifiedCharacter;
        private string m_verifiedText;

        private IExerciseStepsScoreNotification m_scoreNotification;
    }
}
