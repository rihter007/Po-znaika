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
using ru.po_znaika.common;

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Interaction logic for MenuPage.xaml
    /// </summary>
    public partial class MenuPage : Page
    {
        private const int MenuItemFontSize = 24;
        private const int MenuItemImageSize = 80;

        public MenuPage(NavigationWindow mainWindow, po_znaika.server_feedback.IServerFeedback serverCachedFeedback, AlphabetDatabase mainDatabase, DiaryDatabase diaryDatabase)
        {
            InitializeComponent();

            m_mainWindow = mainWindow;
            m_serverCachedFeedback = serverCachedFeedback;
            m_mainDatabase = mainDatabase;
            m_diaryDatabase = diaryDatabase;

            try
            {
                m_fileTracer = new Tracer.FileTracer("poznaika.log", true, true, 1000, Tracer.helpers.NormalTraceLevel);
            }
            catch { }

            ///
            /// Get exercises
            ///
            {
                // Get all exercises sorted by display name, as UI elements
                SortedDictionary<string, List<IExercise>> collectedExercises = new SortedDictionary<string,List<IExercise>>();

                List<ExerciseInfo> exercises = m_mainDatabase.GetAllExercises();

                if (exercises != null)
                {
                    Tracer.helpers.TraceString(m_fileTracer, Tracer.helpers.LowTraceLevel,
                                string.Format("Extracted \"{0}\" exercies", exercises.Count));

                    foreach (ExerciseInfo info in exercises)
                    {
                        if ((info.id == database.Constant.InvalidDatabaseIndex) || (string.IsNullOrEmpty(info.name) || (string.IsNullOrEmpty(info.displayName))))
                        {
                            Tracer.helpers.TraceString(m_fileTracer, Tracer.helpers.CriticalTraceLevel,
                                string.Format("Error. Bad exercise parameters id=\"{0}\" name=\"{1}\" displayName=\"{2}\"",
                                info.id,
                                Helpers.ReplaceNullStringWithEmpty(info.name),
                                Helpers.ReplaceNullStringWithEmpty(info.displayName)));

                            continue;
                        }

                        IExercise exercise = null;
                        try
                        {
                            switch (info.exerciseType)
                            {
                                case ExerciseType.Character:
                                    exercise = new CharacterExercise(mainWindow, this, m_serverCachedFeedback, info.id, info.name, info.displayName, info.imageId, m_mainDatabase);
                                    break;

                                case ExerciseType.WordGather:
                                    exercise = new WordGatherExercise(mainWindow, this, m_serverCachedFeedback, info.id, info.name, info.displayName, info.imageId, m_mainDatabase);
                                    break;

                                case ExerciseType.CreateWordsFromSpecified:
                                    exercise = new CreateWordsFromSpecifiedExercise(mainWindow, this, m_serverCachedFeedback, info.id, info.name, info.displayName, info.imageId, m_mainDatabase);
                                    break;
                            }
                        }
                        catch (Exception exp)
                        {
                            Tracer.helpers.TraceString(m_fileTracer, Tracer.helpers.CriticalTraceLevel,
                                string.Format("Error. Failed to create exercise. Exercise Id id=\"{0}\" exception message=\"{1}\"",
                                info.id,
                                exp.Message));

                            exercise = null;
                        }

                        if (exercise != null)
                        {
                            // Set tracer
                            Tracer.helpers.SetTracer(m_fileTracer, exercise);                                                   

                            // Add exercise to list
                            List<IExercise> displayNameExercises = null;
                            if (collectedExercises.ContainsKey(info.displayName))
                            {
                                displayNameExercises = collectedExercises[info.displayName];
                            }
                            else
                            {
                                displayNameExercises = new List<IExercise>();
                                collectedExercises.Add(info.displayName, displayNameExercises);
                            }                            

                            displayNameExercises.Add(exercise);                            
                        }
                    }                     
                }

                m_menuExercises = new List<IExercise>();

                // Place exercises in sorted order
                foreach (var sortedExercise in collectedExercises)
                    m_menuExercises.AddRange(sortedExercise.Value);                
            }

            ///
            /// Get all exercises sorted by display name, as UI elements
            /// 

            Tracer.helpers.TraceString(m_fileTracer, Tracer.helpers.NormalTraceLevel,
                string.Format("Total created exercies: \"{0}\"", m_menuExercises.Count));

            for (int exerciseIndex = 0; exerciseIndex < m_menuExercises.Count; ++exerciseIndex)
            {
                IExercise currentExercise = m_menuExercises[exerciseIndex];

                StackPanel graphicMenuItem = new StackPanel();
                graphicMenuItem.Orientation = Orientation.Horizontal;

                System.IO.Stream exerciseImageStream = currentExercise.GetDisplayImage();

                if (exerciseImageStream != null)
                {
                    using (exerciseImageStream)
                    {
                        Image img = new Image();
                        img.Stretch = Stretch.Fill;
                        img.Height = img.Width = MenuItemImageSize;
                        img.Source = Helpers.CreateImageFromStream(exerciseImageStream);

                        graphicMenuItem.Children.Add(img);
                    }
                }

                Label exerciseTextLabel = new Label();
                exerciseTextLabel.FontSize = MenuItemFontSize;
                exerciseTextLabel.Content = currentExercise.GetDisplayName();
                graphicMenuItem.Children.Add(exerciseTextLabel);

                ui_exerciseListView.Items.Add(graphicMenuItem);
            }
        }        

        private void ui_exerciseListView_SelectionChanged(object sender, SelectionChangedEventArgs e)
        {
            if (e.AddedItems.Count > 0)
            {
                int selectedIndex = ui_exerciseListView.SelectedIndex;
                if (selectedIndex != -1)
                {
                    try
                    {
                        m_menuExercises[selectedIndex].Process();
                    }
                    catch (Exception exp)
                    {
                        Tracer.helpers.TraceString(m_fileTracer, Tracer.helpers.CriticalTraceLevel,
                            string.Format("Failed to launch exercise id: \"{0}\" Exception message: \"{1}\"",
                            m_menuExercises[selectedIndex].GetId(),
                            exp.Message));

                        MessageBox.Show("Не удалось запустить упражнение", "Внимание");
                    }                    
                }

                ui_exerciseListView.SelectedItems.Clear();
            }
        }

        private void ui_diaryBbutton_Click(object sender, RoutedEventArgs e)
        {
            List<DiaryRow> diaryRecords = new List<DiaryRow>();

            List<ExerciseDiaryShortInfo> selectedExercisesScores = m_diaryDatabase.GetAllDiaryRecordsOrderedByDate();
            if ((selectedExercisesScores != null) && (selectedExercisesScores.Count > 0))
            {
                List<ExerciseInfo> alphabetExercises = m_mainDatabase.GetAllExercises();
                if (alphabetExercises != null)
                {
                    foreach (ExerciseDiaryShortInfo exerciseScore in selectedExercisesScores)
                    {
                        var exerciseDisplayNameCollection = alphabetExercises.Where(obj => obj.id == exerciseScore.exerciseId).Select(obj => obj.displayName);
                        if (exerciseDisplayNameCollection.Count() > 0)
                        {
                            DiaryRow diaryRow = new DiaryRow()
                            {
                                date = exerciseScore.date,
                                exerciseName = exerciseDisplayNameCollection.First(),
                                score = exerciseScore.score
                            };

                            diaryRecords.Add(diaryRow);
                        }
                    }
                }
                else
                {
                    Tracer.helpers.TraceString(m_fileTracer, Tracer.helpers.CriticalTraceLevel, "Failed to get Alphabet exercises");
                }
            }

            DiaryPage diary = new DiaryPage(m_mainWindow, this, diaryRecords);
            m_mainWindow.Navigate(diary);
        }

        private Tracer.FileTracer m_fileTracer;

        private NavigationWindow m_mainWindow;
        private po_znaika.server_feedback.IServerFeedback m_serverCachedFeedback;
        private AlphabetDatabase m_mainDatabase;
        private DiaryDatabase m_diaryDatabase;
        
        private List<IExercise> m_menuExercises;                              
    }
}
