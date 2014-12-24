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
using System.Media;

using ru.po_znaika.database.Alphabet;

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for CharacterSelectionPage.xaml
    /// </summary>
    public partial class CharacterSelectionPage : Page
    {
        public const int MaxTextCharacters = 15 * 7;

        private static readonly Brush SelectionBackgroundColor = Brushes.LightSkyBlue;
        private static readonly Brush NoSelectionBackgroundColor = Brushes.WhiteSmoke;

        private static readonly Brush CorrectCharacterBackgroundColor = Brushes.LightGreen;
        private static readonly Brush InCorrectCharacterBackgroundcolor = Brushes.Red;

        private const string ButtonNameAfterResultsVerification = "Далее";

        public CharacterSelectionPage(string displayText, char selectionChar, AlphabetDatabase database, IMultipleOptionSelectionCallback multipleOptionsSelection)
        {
            if ((string.IsNullOrWhiteSpace(displayText)) || (multipleOptionsSelection == null))
                throw new ArgumentNullException();

            if (!Char.IsLetterOrDigit(selectionChar))
                throw new ArgumentException("selection character should be number or letter");

            InitializeComponent();

            m_database = database;
            m_soundPlayer = new GeneralSoundPlayer(m_database);
            m_searchedCharacter = selectionChar;
            m_multipleOptionsSelection = multipleOptionsSelection;

            if (!DisplayText(displayText))
                throw new Exception("failed to display specified text");            
        }

        private void label_MouseDown(object sender, MouseButtonEventArgs e)
        {
            TextBlock textBlock = sender as TextBlock;

            if (string.IsNullOrWhiteSpace(textBlock.Text))
                return;

            if (textBlock.Background == NoSelectionBackgroundColor)
                textBlock.Background = SelectionBackgroundColor;
            else
                textBlock.Background = NoSelectionBackgroundColor;
        }

        public int GetMaximumCharacters()
        {
            return ui_textGrid.ColumnDefinitions.Count * ui_textGrid.RowDefinitions.Count;
        }

        private bool DisplayText(string text)
        {
            if (text.Length > GetMaximumCharacters())
                return false;

            {
                ui_taskLabel.Content = string.Format("Выделите все буквы \"{0}\"", m_searchedCharacter);
            }

            {
                double columnWidth = (double)ui_textGrid.Width / (double)ui_textGrid.ColumnDefinitions.Count - 2;
                double rowHeight = (double)ui_textGrid.Height / (double)ui_textGrid.RowDefinitions.Count - 2;

                m_characterTextBlocks = new TextBlock[text.Length];
                for (int textCharIndex = 0; textCharIndex < text.Length; ++textCharIndex)
                {
                    TextBlock textBlock = new TextBlock();
                    textBlock.HorizontalAlignment = System.Windows.HorizontalAlignment.Center;
                    textBlock.FontSize = 24;
                    textBlock.Width = columnWidth;
                    textBlock.Height = rowHeight;
                    textBlock.Text = text[textCharIndex].ToString();
                    textBlock.Background = NoSelectionBackgroundColor;
                    //textBlock.SetValue(Border.BorderThicknessProperty, 1.0);
                    textBlock.MouseDown += new MouseButtonEventHandler(label_MouseDown);
                    m_characterTextBlocks[textCharIndex] = textBlock;

                    ui_textGrid.Children.Add(textBlock);

                    textBlock.SetValue(Grid.RowProperty, textCharIndex / ui_textGrid.ColumnDefinitions.Count);
                    textBlock.SetValue(Grid.ColumnProperty, textCharIndex % ui_textGrid.ColumnDefinitions.Count);
                    textBlock.Visibility = System.Windows.Visibility.Visible;
                }
            }

            return true;
        }

        private void button1_Click(object sender, RoutedEventArgs e)
        {
            if (m_selectedVariants == null)
            {
                m_selectedVariants = new List<int>();

                bool areTotalMistakesFound = false;
                for (int labelIndex = 0; labelIndex < m_characterTextBlocks.Length; ++labelIndex)
                {
                    TextBlock textBlock = m_characterTextBlocks[labelIndex];

                    if (string.IsNullOrEmpty(textBlock.Text))
                        continue;

                    char verifiedCharacter = textBlock.Text.ToLower()[0];
                    if (!Char.IsLetterOrDigit(verifiedCharacter))
                        continue;

                    bool isMistake = (verifiedCharacter == m_searchedCharacter) ? textBlock.Background == NoSelectionBackgroundColor : textBlock.Background == SelectionBackgroundColor;
                    textBlock.Background = isMistake ? InCorrectCharacterBackgroundcolor : CorrectCharacterBackgroundColor;

                    areTotalMistakesFound |= isMistake;

                    if (verifiedCharacter == m_searchedCharacter)
                        m_selectedVariants.Add(labelIndex);
                }

                ui_checkButton.Content = ButtonNameAfterResultsVerification;

                // play sound
                m_soundPlayer.PlaySoundByType(areTotalMistakesFound ? SoundType.Bad : SoundType.Good);                
            }
            else
            {
                m_multipleOptionsSelection.OnSelection(m_selectedVariants);
            }
        }

        private AlphabetDatabase m_database;
        private GeneralSoundPlayer m_soundPlayer;
        private char m_searchedCharacter;
        private IMultipleOptionSelectionCallback m_multipleOptionsSelection;

        private TextBlock[] m_characterTextBlocks;
        private List<int> m_selectedVariants;
    }
}
