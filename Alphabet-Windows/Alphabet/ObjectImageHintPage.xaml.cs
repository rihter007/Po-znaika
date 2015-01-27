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

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for ObjectImageHintPage.xaml
    /// </summary>
    public partial class ObjectImageHintPage : Page
    {
        public ObjectImageHintPage(NavigationWindow mainWindow, Page returnPage, ImageSource objectImage)
        {
            InitializeComponent();

            m_mainWindow = mainWindow;
            m_returnPage = returnPage;
            ui_objectHintImage.Source = objectImage;
        }

        private void ui_returnButton_Click(object sender, RoutedEventArgs e)
        {
            m_mainWindow.Navigate(m_returnPage);
        }

        private NavigationWindow m_mainWindow;
        private Page m_returnPage;
    }
}
