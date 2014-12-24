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

using ru.po_znaika.database.Alphabet;
using ru.po_znaika.database.Diary;

namespace ru.po_znaika.alphabet
{
    public class DiaryRow
    {
        public DateTime date { get; set; }
        public string exerciseName { get; set; }
        public int score { get; set; }
    }
    
    /// <summary>
    /// Interaction logic for DiaryPage.xaml
    /// </summary>
    public partial class DiaryPage : Page
    {
        public DiaryPage(NavigationWindow mainWindow, Page returnPage, IEnumerable<DiaryRow> diaryRecords)
        {
            if ((mainWindow == null) || (returnPage == null))
                throw new ArgumentNullException();

            InitializeComponent();

            ui_diaryDataGrid.ItemsSource = diaryRecords;

            m_mainWindow = mainWindow;
            m_returnPage = returnPage;            
        }

        private void ui_backButton_Click(object sender, RoutedEventArgs e)
        {
            m_mainWindow.Navigate(m_returnPage);
        }

        private NavigationWindow m_mainWindow;
        private Page m_returnPage;
    }
}
