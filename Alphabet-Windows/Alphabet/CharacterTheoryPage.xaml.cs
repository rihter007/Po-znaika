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

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for CharacterTheoryPage.xaml
    /// </summary>
    public partial class CharacterTheoryPage : Page, IExerciseStep, IDisposable
    {
        public CharacterTheoryPage(NavigationWindow drawWindow, Stream theoryPicture, Stream theoryOralDescription)
        {
            if (drawWindow == null)
                throw new ArgumentNullException("DrawWindow cannot be null");

            if (theoryPicture == null)
                throw new ArgumentNullException("Theory picture cannot be null");
            
            m_drawWindow = drawWindow;

            m_theoryOralDescription = null;
            if (theoryOralDescription != null)
            {
                m_theoryStream = new MemoryStream();
                theoryOralDescription.CopyTo(m_theoryStream);

                m_theoryOralDescription = Helpers.CreateSoundPlayerFromStream(m_theoryStream);       
            }
           
            InitializeComponent();

            ui_theoryPicture.Source = Helpers.CreateImageFromStream(theoryPicture);
        }

        public void SetPreviousExerciseStep(IExerciseStep prevExerciseStep)
        {
            m_prevExerciseStep = prevExerciseStep;
        }

        public void SetNextExerciseStep(IExerciseStep nextExerciseStep)
        {
            m_nextExerciseStep = nextExerciseStep;
        }

        public void Process()
        {            
            m_drawWindow.Navigate(this);

            if (m_theoryOralDescription != null)
                m_theoryOralDescription.Play();
        }

        private void ui_backButton_Click(object sender, RoutedEventArgs e)
        {
            m_prevExerciseStep.Process();
        }

        private void ui_forwardButton_Click(object sender, RoutedEventArgs e)
        {
            m_nextExerciseStep.Process();
        }

        public void Dispose()
        {
            // remove circle links
            m_prevExerciseStep = null;
            m_nextExerciseStep = null;

            if (m_theoryOralDescription != null)
                m_theoryOralDescription.Dispose();

            if (m_theoryStream != null)
                m_theoryStream = null;

            m_theoryOralDescription = null;
            m_theoryStream = null;
        }

        private NavigationWindow m_drawWindow;

        private System.Media.SoundPlayer m_theoryOralDescription;
        private MemoryStream m_theoryStream;

        private IExerciseStep m_prevExerciseStep;
        private IExerciseStep m_nextExerciseStep;
    }
}
