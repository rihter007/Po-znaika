using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
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
    /// Interaction logic for CreateWordsFromSpecifiedPage.xaml
    /// </summary>
    public partial class CreateWordsFromSpecifiedPage : Page, IExerciseStep
    {
        private readonly Brush SelectionColor = Brushes.Red;
        private readonly Brush NoSelectionColor = Brushes.WhiteSmoke;

        private static readonly string RemainWordsText = "Осталось слов {0}";

        class DenyRectangleElementSelection : IAllowElementSelect
        {
            public bool IsSelectionAllowed(UIElement element)
            {
                return !(element is Rectangle);
            }
        }

        public CreateWordsFromSpecifiedPage(NavigationWindow mainWindow, AlphabetDatabase alphabetDatabase, int exerciseId, IServerFeedback serverFeedback)
        {
            InitializeComponent();

            if ((mainWindow == null) || (alphabetDatabase == null))
                throw new ArgumentException();

            if (exerciseId == 0)
                throw new ArgumentException("Invalid exercise id");

            m_mainWindow = mainWindow;
            m_localDatabase = alphabetDatabase;
            m_soundPlayer = new GeneralSoundPlayer(m_localDatabase);

            SubWords? wordInfo = m_localDatabase.GetRandomSubwordsByAlphabetAndLength(AlphabetType.Russian, 6, ui_initialWordGrid.ColumnDefinitions.Count);
            if ((!wordInfo.HasValue) || (wordInfo.Value.subwords == null) || (wordInfo.Value.subwords.Length == 0))
                throw new Exception("Error in getting word info from database");

            m_mainWord = wordInfo.Value.mainWord.word;

            {
                m_allSubWords = new SortedDictionary<string, int>();
                foreach (var subwordInfo in wordInfo.Value.subwords)
                    m_allSubWords.Add(subwordInfo.word, subwordInfo.wordComplexity);

                if (wordInfo.Value.imageSubwords != null)
                {
                    foreach (var subwordInfo in wordInfo.Value.imageSubwords)
                        m_allSubWords.Add(subwordInfo.word.word, subwordInfo.word.wordComplexity);
                }
            }

            {
                m_imageHintWords = new List<CharacterObjectImage>();

                if (wordInfo.Value.imageSubwords != null)
                    m_imageHintWords.AddRange(wordInfo.Value.imageSubwords);
                else
                    ui_hintButton.Visibility = System.Windows.Visibility.Hidden;
            }

            m_foundSubWords = new SortedSet<string>();
            m_usedHints = new SortedSet<string>();

            m_exerciseId = exerciseId;
            m_serverFeedback = serverFeedback;

            UIElement[][] initialGridElements = new UIElement[1][];
            UIElement[][] createdWordGridElements = new UIElement[1][];

            ///
            /// Create graphics elements
            ///            
            {
                UIElement[] initialGridRowElements = new UIElement[m_mainWord.Length];
                UIElement[] createdWordGridRowElements = new UIElement[m_mainWord.Length];

                initialGridElements[0] = initialGridRowElements;
                createdWordGridElements[0] = createdWordGridRowElements;

                double gridColumnWidth = (ui_initialWordGrid.Width / ui_initialWordGrid.ColumnDefinitions.Count) - 5;
                double gridColumnHeight = ui_initialWordGrid.Height;

                m_labelElements = new Label[m_mainWord.Length];
                m_rectangleElements = new Rectangle[m_mainWord.Length];

                for (int charIndex = 0; charIndex < m_mainWord.Length; ++charIndex)
                {
                    {
                        Label label = new Label();
                        label.HorizontalAlignment = System.Windows.HorizontalAlignment.Center;
                        label.VerticalAlignment = System.Windows.VerticalAlignment.Center;
                        label.Content = m_mainWord[charIndex];
                        label.Width = gridColumnWidth;
                        label.Height = gridColumnHeight;
                        label.FontSize = 38;
                        Helpers.PaintUiElement(label, NoSelectionColor);

                        initialGridRowElements[charIndex] = label;
                        m_labelElements[charIndex] = label;
                    }

                    {
                        Rectangle rect = new Rectangle();
                        rect.Fill = Brushes.LightBlue;
                        rect.Width = gridColumnWidth;
                        rect.Height = gridColumnHeight;
                        Helpers.PaintUiElement(rect, NoSelectionColor);

                        createdWordGridRowElements[charIndex] = rect;
                        m_rectangleElements[charIndex] = rect;
                    }
                }
            }

            ///
            /// Refresh remain words label
            ///
            {
                ui_remainWordsTextBlock.Text = string.Format(RemainWordsText, m_allSubWords.Count - m_foundSubWords.Count);
            }

            ///
            /// Paint graphic
            ///
            {
                PaintMoveGraphic();
            }

            ///
            /// Create graphics engine
            ///
            {
                GridDescription[] canvasGrids = new GridDescription[2];

                {
                    canvasGrids[0] = new GridDescription();
                    canvasGrids[0].grid = ui_initialWordGrid;
                    canvasGrids[0].gridElements = initialGridElements;
                    canvasGrids[0].firstRowIndex = canvasGrids[0].lastRowIndex = 0;
                    canvasGrids[0].firstColumnIndex = 0;
                    canvasGrids[0].lastColumnIndex = m_mainWord.Length - 1;
                    canvasGrids[0].isSelfMoveAllowed = false;
                }
                m_initialWordGridDescription = canvasGrids[0];

                {
                    canvasGrids[1] = new GridDescription();
                    canvasGrids[1].grid = ui_newWordGrid;
                    canvasGrids[1].gridElements = createdWordGridElements;
                    canvasGrids[1].firstRowIndex = canvasGrids[1].lastRowIndex = 0;
                    canvasGrids[1].firstColumnIndex = 0;
                    canvasGrids[1].lastColumnIndex = m_mainWord.Length - 1;
                    canvasGrids[1].isSelfMoveAllowed = true;
                }
                m_newWordGridDescription = canvasGrids[1];

                RectangleArea canvasMoveArea = new RectangleArea()
                {
                    topLeftVertex = new Point((int)(double)ui_canvasMoveRectangle.GetValue(Canvas.LeftProperty), (int)(double)ui_canvasMoveRectangle.GetValue(Canvas.TopProperty)),
                    width = ui_canvasMoveRectangle.Width,
                    height = ui_canvasMoveRectangle.Height
                };

                DenyRectangleElementSelection denyRectangleSelection = new DenyRectangleElementSelection();

                m_canvasElementsMoveLogic = new CanvasGridElementMoveLogic(ui_charMoveCanvas, canvasMoveArea, canvasGrids, Brushes.WhiteSmoke, Brushes.Red, denyRectangleSelection);
            }
        }

        private void ui_charMoveCanvas_MouseMove(object sender, MouseEventArgs e)
        {
            m_canvasElementsMoveLogic.OnMouseMove(e.GetPosition(ui_charMoveCanvas));
        }        

        private void ui_charMoveCanvas_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            m_canvasElementsMoveLogic.OnMouseLeftButtonDown(e.GetPosition(ui_charMoveCanvas));
        }

        private void ui_charMoveCanvas_MouseLeftButtonUp(object sender, MouseButtonEventArgs e)
        {
            m_canvasElementsMoveLogic.OnMouseLeftButtonUp(e.GetPosition(ui_charMoveCanvas));
        }       

        private void ui_addWordButton_Click(object sender, RoutedEventArgs e)
        {
            string newWord = string.Empty;

            ///
            /// Build newWord from grid elements disposition
            ///

            {
                bool isCorrectElementsPosition = true; // is true if we got: [label, label, rect, rect] - no labels inside rectangles array
                bool areRectanglesFound = false;
                foreach (UIElement resultElement in m_newWordGridDescription.gridElements[0])
                {
                    if (resultElement is Label)
                    {
                        if (areRectanglesFound)
                            isCorrectElementsPosition = false;

                        newWord += ((Label)(resultElement)).Content;
                    }
                    else
                    {
                        areRectanglesFound = true;
                    }
                }

                if (newWord.Length == 0)
                {
                    MessageBox.Show("Указано пустое слово");
                    return;
                }

                if (!isCorrectElementsPosition)
                {
                    MessageBox.Show("Неправильное расположение элементов. Попробуйте ещё раз");
                    return;
                }
            }

            ///
            /// Check if word already exists
            ///
            if (m_foundSubWords.Contains(newWord))
            {
                MessageBox.Show("Вы уже ввели данное слово");
                return;
            }

            ///
            /// Check if word is correct
            ///
            if (!m_allSubWords.ContainsKey(newWord))
            {
                MessageBox.Show("Данного слово не существует");
                return;
            }

            ///
            /// Add word to list
            ///
            m_foundSubWords.Add(newWord);

            ///
            /// Remove word from possible hints
            ///
            {
                for (int hintIndex = 0; hintIndex < m_imageHintWords.Count; ++hintIndex)
                {
                    if (m_imageHintWords[hintIndex].word.word == newWord)
                    {
                        m_imageHintWords.RemoveAt(hintIndex);
                        break;
                    }
                }
            }

            ///
            /// Refresh remain words label
            ///
            {
                ui_remainWordsTextBlock.Text = string.Format(RemainWordsText, m_allSubWords.Count - m_foundSubWords.Count);
            }

            ///
            /// Add word to ListView
            ///
            {
                TextBlock newWordTextBlock = new TextBlock();
                newWordTextBlock.Text = newWord;
                newWordTextBlock.FontSize = 24;
                ui_foundWordsListBox.Items.Add(newWordTextBlock);
            }

            ///
            /// Repaint move graphics engine
            ///
            PaintMoveGraphic();
        }

        private void ui_backButton_Click(object sender, RoutedEventArgs e)
        {
            m_prevExerciseStep.Process();
        }

        private void ui_checkButton_Click(object sender, RoutedEventArgs e)
        {
            bool AreAllWordsFound = (m_foundSubWords.Count == m_allSubWords.Count);
            if (AreAllWordsFound)
            {
                MessageBoxResult messageBoxResult = MessageBox.Show("Введены не всевозможные слова. Продолить?", "Внимание", MessageBoxButton.YesNoCancel);

                if (messageBoxResult != MessageBoxResult.Yes)
                    return;
            }
            
            m_soundPlayer.PlaySoundByType(AreAllWordsFound ? SoundType.Good : SoundType.Bad);

            ///
            /// Report score
            ///

            double maxPoints = m_allSubWords.Values.Sum();
            double achievedPoints = 0;

            foreach (string foundWord in m_foundSubWords)
            {
                double hintMult = 1.0;
                if (m_usedHints.Contains(foundWord))
                    hintMult = 0.5;

                achievedPoints += hintMult * (double)m_allSubWords[foundWord];
            }

            int totalScore = (int)((double)ExerciseConstant.MaxScore * (achievedPoints / maxPoints));

            if (m_serverFeedback != null)
                m_serverFeedback.ReportExerciseResult(m_exerciseId, totalScore);

            ResultsPage resultsPage = new ResultsPage(totalScore, m_nextExerciseStep);
            m_mainWindow.Navigate(resultsPage);
        }

        private void InitGridRowElements(Grid grid, int rowIndex, UIElement[] elements)
        {
            for (int elementIndex = 0; elementIndex < elements.Length; ++elementIndex)
            {
                grid.Children.Add(elements[elementIndex]);

                elements[elementIndex].SetValue(Grid.ColumnProperty, elementIndex);
                elements[elementIndex].SetValue(Grid.RowProperty, rowIndex);
            }
        }

        private void PaintMoveGraphic()
        {
            ui_initialWordGrid.Children.Clear();
            ui_newWordGrid.Children.Clear();

            InitGridRowElements(ui_initialWordGrid, 0, m_labelElements);
            InitGridRowElements(ui_newWordGrid, 0, m_rectangleElements);

            if (m_canvasElementsMoveLogic != null)
            {
                // Revert any move action
                m_canvasElementsMoveLogic.RevertMoveAction();

                // Reinit grid array of items
                Array.Copy(m_labelElements, m_initialWordGridDescription.gridElements[0], m_labelElements.Length);
                Array.Copy(m_rectangleElements, m_newWordGridDescription.gridElements[0], m_rectangleElements.Length);
            }
        }

        private void ui_hintButton_Click(object sender, RoutedEventArgs e)
        {
            if (m_imageHintWords.Count == 0)
            {
                MessageBox.Show("Подсказок больше нет");
                return;
            }

            int hintIndex = m_random.Next(m_imageHintWords.Count);

            using (MemoryStream imageContent = m_localDatabase.GetImageById(m_imageHintWords[hintIndex].imageId))
            {
                if (imageContent != null)
                {
                    ImageSource imageSource = Helpers.CreateImageFromStream(imageContent);
                    if (imageSource != null)
                    {
                        if (!m_usedHints.Contains(m_imageHintWords[hintIndex].word.word))
                            m_usedHints.Add(m_imageHintWords[hintIndex].word.word);

                        ObjectImageHintPage hintPage = new ObjectImageHintPage(m_mainWindow, this, imageSource);
                        m_mainWindow.Navigate(hintPage);
                    }
                }
            }
        }

        public void Process()
        {
            m_mainWindow.Navigate(this);
        }

        public void SetPreviousExerciseStep(IExerciseStep prevExerciseStep)
        {
            m_prevExerciseStep = prevExerciseStep;
        }

        public void SetNextExerciseStep(IExerciseStep nextExerciseStep)
        {
            m_nextExerciseStep = nextExerciseStep;
        }

        private IExerciseStep m_prevExerciseStep;
        private IExerciseStep m_nextExerciseStep;

        private NavigationWindow m_mainWindow;
        private AlphabetDatabase m_localDatabase;
        private GeneralSoundPlayer m_soundPlayer;

        /// <summary>
        /// Keeps all created label elements for characters from m_mainWord in m_mainWord order
        /// </summary>
        private Label[] m_labelElements;
        /// <summary>
        /// Keeps all rectnagle elements for paintint empty spaces for word creation
        /// </summary>
        private Rectangle[] m_rectangleElements;

        private GridDescription m_initialWordGridDescription;
        private GridDescription m_newWordGridDescription;

        private Random m_random;

        /// <summary>
        /// Main word. All words are constructed from it
        /// </summary>
        private string m_mainWord;

        /// <summary>
        /// All possible subwords. (Value=word, Key=complexity)
        /// </summary>
        private IDictionary<string, int> m_allSubWords;

        /// <summary>
        /// All words that can be used as image hint.
        /// </summary>
        private List<CharacterObjectImage> m_imageHintWords;

        /// <summary>
        /// Found words by user
        /// </summary>
        private ISet<string> m_foundSubWords;

        /// <summary>
        /// Object words showed as hints
        /// </summary>
        private ISet<string> m_usedHints;

        private int m_exerciseId;
        private IServerFeedback m_serverFeedback;

        private CanvasGridElementMoveLogic m_canvasElementsMoveLogic;             
    }
}
