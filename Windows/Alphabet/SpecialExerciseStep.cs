using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Windows.Controls;
using System.Windows.Navigation;

using ru.po_znaika.common;

namespace ru.po_znaika.alphabet
{
    class ExitExerciseStep : IExerciseStep
    {
        public ExitExerciseStep(NavigationWindow mainWindow, Page navigationPage, IEnumerable<IDisposable> exerciseResources)
        {
            if ((mainWindow == null) || (navigationPage == null))
                throw new ArgumentNullException("MainWindow and navigationPage cannot be null");

            m_mainWindow = mainWindow;
            m_navigationPage = navigationPage;
            m_exerciseResources = exerciseResources;
        }

        public void SetExerciseDisposableResources(IEnumerable<IDisposable> exerciseResources)
        {
            m_exerciseResources = exerciseResources;
        }

        public void Process()
        {
            if (m_exerciseResources != null)
            {
                foreach (IDisposable dispObject in m_exerciseResources)
                    dispObject.Dispose();
            }

            m_mainWindow.Navigate(m_navigationPage);
        }

        public void SetPreviousExerciseStep(IExerciseStep prevExerciseStep)
        {
            throw new NotImplementedException();
        }

        public void SetNextExerciseStep(IExerciseStep nextExerciseStep)
        {
            throw new NotImplementedException();
        }

        private NavigationWindow m_mainWindow;
        private Page m_navigationPage;
        private IEnumerable<IDisposable> m_exerciseResources;        
    }
}
