using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using ru.po_znaika.common;
using ru.po_znaika.server_feedback;
using ru.po_znaika.database.Alphabet;

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for WordGatherPage.xaml
    /// </summary>
    public partial class WordGatherPage : Page, IExerciseStep
    {
        struct CoordinateDelta
        {
            public CoordinateDelta(int x, int y)
            {
                X = x;
                Y = y;
            }

            public int X;
            public int Y;
        }

        private const int GridCharacterFontSize = 54;

        private readonly Brush NoCharacterSelectionColor = Brushes.LightBlue;
        private readonly Brush SelectionColor = Brushes.Red;
        private readonly Brush NoSelectionColor = Brushes.WhiteSmoke;//White;

        public WordGatherPage(NavigationWindow mainWindow, int exerciseId, AlphabetDatabase alphabetDatabase, IServerFeedback serverFeedback)
        {
            InitializeComponent();

            m_mainWindow = mainWindow;
            m_exerciseId = exerciseId;
            
            m_alphabetDatabase = alphabetDatabase;
            m_soundPlayer = new GeneralSoundPlayer(m_alphabetDatabase);

            m_serverFeedback = serverFeedback;

            m_previousExerciseStep = m_nextExerciseStep = null;

            InitializeInternals();    

            m_verificationStage = true;
        }

        private void InitializeInternals()
        {
            ///
            /// Process word object extraction
            ///

            CharacterObjectImage? charObject = m_alphabetDatabase.GetRandomWordImageByALphabetAndMaxLength(AlphabetType.Russian, ui_resultTextGrid.ColumnDefinitions.Count);
            if (!charObject.HasValue)
                throw new Exception("Word of specified type is not found");            
            
            // Verify common errors in database
            if (string.IsNullOrEmpty(charObject.Value.word.word))
                throw new ArgumentNullException("Display word must not be null");
            if (charObject.Value.word.word.Contains(" \r\n\t"))
                throw new ArgumentException("Display word must not containt whitespace");
            
            // Remember correct answer
            m_correctAnswer = charObject.Value.word.word.ToLower();

            // Display picture
            using (System.IO.Stream imageStream = m_alphabetDatabase.GetImageById(charObject.Value.imageId))
            {
                if (imageStream == null)
                    throw new ArgumentNullException("No image data is found");

                System.Windows.Media.ImageSource imageSource = Helpers.CreateImageFromStream(imageStream);
                if (imageSource == null)
                    throw new ArgumentNullException("Can`t convert image data to picture");

                ui_ojectImage.Source = imageSource;
            }

            ///
            /// Process graphics engine
            ///

            {
                // random snuffle characters
                Random rand = new Random(DateTime.Now.Millisecond);
                List<char> characters = null;

                while (true)
                {
                    string randomModifiedWord = string.Concat(m_correctAnswer.OrderBy(obj => rand.Next()));
                    if (string.Compare(randomModifiedWord, m_correctAnswer) != 0)
                    {
                        characters = randomModifiedWord.ToList();
                        break;
                    }
                }                

                // Maybe we should clear previous results
                if (m_charMoveGridDescription != null)
                {
                    foreach (UIElement guiElement in m_charMoveGridDescription.gridElements[0])
                        ui_resultTextGrid.Children.Remove(guiElement);                    
                }

                // calculate borders of grid
                int firstUsedColumnIndex = 0;
                int lastUsedColumnIndex = 0;
                
                {
                    int lengthDelta = (ui_resultTextGrid.ColumnDefinitions.Count - characters.Count) / 2;
                    firstUsedColumnIndex = lengthDelta;
                    lastUsedColumnIndex = firstUsedColumnIndex + characters.Count - 1;
                }

                UIElement[] resultTextGridElements = new UIElement[characters.Count];

                // Set elements
                for (int columnIndex = firstUsedColumnIndex; columnIndex <= lastUsedColumnIndex; ++columnIndex)
                {
                    TextBlock tb = new TextBlock();
                    tb.Width = (ui_resultTextGrid.Width / ui_resultTextGrid.ColumnDefinitions.Count) - 5;
                    tb.Height = ui_resultTextGrid.Height;
                    tb.FontSize = GridCharacterFontSize;
                    tb.HorizontalAlignment = System.Windows.HorizontalAlignment.Center;
                    tb.Text = characters[columnIndex - firstUsedColumnIndex].ToString();
                    Helpers.PaintUiElement(tb, NoSelectionColor);

                    ui_resultTextGrid.Children.Add(tb);
                    tb.SetValue(Grid.RowProperty, 0);
                    tb.SetValue(Grid.ColumnProperty, columnIndex);

                    resultTextGridElements[columnIndex - firstUsedColumnIndex] = tb;
                }

                m_charMoveGridDescription = new GridDescription();
                m_charMoveGridDescription.firstRowIndex = m_charMoveGridDescription.lastRowIndex = 0;
                m_charMoveGridDescription.firstColumnIndex = firstUsedColumnIndex;
                m_charMoveGridDescription.lastColumnIndex = lastUsedColumnIndex;
                m_charMoveGridDescription.isSelfMoveAllowed = true;
                m_charMoveGridDescription.grid = ui_resultTextGrid;
                m_charMoveGridDescription.gridElements = new UIElement[1][];
                m_charMoveGridDescription.gridElements[0] = resultTextGridElements;

                m_gridElementMoveEngine = new CanvasGridElementMoveLogic(ui_charMoveCanvas, null, new GridDescription[] { m_charMoveGridDescription }, NoSelectionColor, SelectionColor, null);
            }
        }

        public void Process()
        {
            m_mainWindow.Navigate(this);
        }

        public void SetPreviousExerciseStep(IExerciseStep prevExerciseStep)
        {
            m_previousExerciseStep = prevExerciseStep;
        }

        public void SetNextExerciseStep(IExerciseStep nextExerciseStep)
        {
            m_nextExerciseStep = nextExerciseStep;
        }

        private void ui_charMoveCanvas_MouseMove(object sender, MouseEventArgs e)
        {
            m_gridElementMoveEngine.OnMouseMove(e.GetPosition(ui_charMoveCanvas));
        }

        private void ui_charMoveCanvas_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            m_gridElementMoveEngine.OnMouseLeftButtonDown(e.GetPosition(ui_charMoveCanvas));
        }

        private void ui_charMoveCanvas_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
        {
            m_gridElementMoveEngine.OnMouseLeftButtonUp(e.GetPosition(ui_charMoveCanvas));
        }

        private void ui_checkButton_Click(object sender, RoutedEventArgs e)
        {
            if (!m_verificationStage)
            {
                m_nextExerciseStep.Process();
                return;
            }

            // Check exercise
            string resultMessage = string.Empty;
            for (int charIndex = 0; charIndex < m_charMoveGridDescription.gridElements[0].Length; ++charIndex)
                resultMessage += ((TextBlock)m_charMoveGridDescription.gridElements[0][charIndex]).Text;

            int correctCharactersCount = 0;
            for (int charIndex = 0; charIndex < resultMessage.Length; ++charIndex)
            {
                if (resultMessage[charIndex] == m_correctAnswer[charIndex])
                    ++correctCharactersCount;
            }

            if (correctCharactersCount == m_correctAnswer.Length)
            {
                ui_informationTextBlock.Text = "Молодец, слово составлено верно!";
                m_soundPlayer.PlaySoundByType(SoundType.Good);
            }
            else
            {
                ui_informationTextBlock.Text = string.Format("Ошибка, ответ \"{0}\"", m_correctAnswer);
                m_soundPlayer.PlaySoundByType(SoundType.Bad);
            }

             int totalScore = (int)(correctCharactersCount / m_correctAnswer.Length) * ExerciseConstant.MaxScore;

            if (m_serverFeedback != null)
                m_serverFeedback.ReportExerciseResult(m_exerciseId, totalScore);
            
            ui_backButton.Visibility = System.Windows.Visibility.Hidden;
            ui_repeatButton.Visibility = System.Windows.Visibility.Visible;
            ui_checkButton.Content = "Завершить";

            m_verificationStage = false;

            // Redirect to results page:
            ResultsPage resultsPage = new ResultsPage(totalScore, this);
            m_mainWindow.Navigate(resultsPage);
        }

        private void ui_repeatButton_Click(object sender, RoutedEventArgs e)
        {
            InitializeInternals();

            ui_informationTextBlock.Text = "Переставь местами буквы";

            ui_backButton.Visibility = System.Windows.Visibility.Visible;
            ui_repeatButton.Visibility = System.Windows.Visibility.Hidden;
            
            ui_checkButton.Content = "Проверить";

            m_verificationStage = true;
        }

        private void ui_backButton_Click(object sender, RoutedEventArgs e)
        {
            m_previousExerciseStep.Process();
        }

        private NavigationWindow m_mainWindow;
        private int m_exerciseId;

        private IServerFeedback m_serverFeedback;

        private AlphabetDatabase m_alphabetDatabase;        
        private GeneralSoundPlayer m_soundPlayer;

        private IExerciseStep m_previousExerciseStep;
        private IExerciseStep m_nextExerciseStep;

        private string m_correctAnswer;

        private bool m_verificationStage;

        private GridDescription m_charMoveGridDescription;
        private CanvasGridElementMoveLogic m_gridElementMoveEngine;
    }
}
