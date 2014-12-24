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

using ru.po_znaika.database.Alphabet;
using ru.po_znaika.database.Diary;
using ru.po_znaika.server_feedback;

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : NavigationWindow
    {
        private const string ExerciseAlphabetDatabasePath = "alphabet.db";
        private const string ExerciseDiaryDatabasePath = "exercise-diary.db";

        public MainWindow()
        {
            InitializeComponent();            
        }

        private void Window_Initialized(object sender, EventArgs e)
        {            
            ///
            /// Initialize main objects
            ///
          
            try
            {
                m_mainDatabase = new AlphabetDatabase(ExerciseAlphabetDatabasePath, true);
                m_diaryDatabase = new DiaryDatabase(ExerciseDiaryDatabasePath, false);

                m_serverCachedFeedback = new ServerCacheFeedback(ExerciseDiaryDatabasePath, "https://feedback.po-znaika.ru");
            }
            catch
            {
                MessageBox.Show("Ошибка инициализации, отсутствует файл БД");
                this.Close();
                return;
            }            

            m_menuPage = new MenuPage(this, m_serverCachedFeedback, m_mainDatabase, m_diaryDatabase);
            this.Navigate(m_menuPage);
        }

        private ServerCacheFeedback m_serverCachedFeedback;
        private AlphabetDatabase m_mainDatabase;
        private DiaryDatabase m_diaryDatabase;

        private MenuPage m_menuPage;
    }
}
