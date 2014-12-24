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

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for ResultsPage.xaml
    /// </summary>
    public partial class ResultsPage : Page
    {
        public ResultsPage(int totalScore, IExerciseStep nextExerciseStep)
        {
            InitializeComponent();

            if (nextExerciseStep == null)
                throw new ArgumentNullException("nextExerciseStep must not be null");

            ui_resultTextBlock.Text = string.Format("Заработано {0} баллов!", totalScore);
            m_nextExerciseStep = nextExerciseStep;
        }

        private void ui_nextButton_Click(object sender, RoutedEventArgs e)
        {
            m_nextExerciseStep.Process();
        }

        private IExerciseStep m_nextExerciseStep;
    }
}
