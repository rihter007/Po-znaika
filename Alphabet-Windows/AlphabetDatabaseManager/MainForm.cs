using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Windows.Forms;

namespace AlphabetDatabaseManager
{
    public partial class MainForm : Form
    {
        public MainForm()
        {
            InitializeComponent();
        }

        private void MainForm_Load(object sender, EventArgs e)
        {
            DialogResult dbDialogResult = ui_openFileDialog.ShowDialog();
            if ((string.IsNullOrEmpty(ui_openFileDialog.FileName)) || (dbDialogResult != System.Windows.Forms.DialogResult.OK))
            {
                MessageBox.Show("Выерите файл для базы даных");
                this.Close();
                return;
            }

            try
            {
                m_database = new ru.poznaika.database.Alphabet.AlphabetDatabase(ui_openFileDialog.FileName, true);
            }
            catch (Exception exp)
            {
                MessageBox.Show(string.Format("Failed to open database. Reason: \"{0}\"", exp.Message));
                this.Close();
            }

            string[] tableNames = m_database.GetTableNames();
            if (tableNames != null)
            {
                for (int tableNameIndex = 0; tableNameIndex < tableNames.Length; ++tableNameIndex)
                    ui_tablesListBox.Items.Add(tableNames[tableNameIndex]);
            }
        }

        private void ui_tableSelectButton_Click(object sender, EventArgs e)
        {
            ui_tablesListBox_MouseDoubleClick(sender, null);
        }

        private void ui_tablesListBox_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            if (ui_tablesListBox.SelectedIndex != -1)
            {
                string selectedTableName = ui_tablesListBox.SelectedItem as string;

                Form selectedForm = null;
                switch (selectedTableName.ToLower())
                {
                    case "exercise":
                        selectedForm = new ExerciseTableEditForm(m_database);
                        break;

                    case "character_theory":
                        selectedForm = new CharacterTheoryTableEditForm(m_database);
                        break;

                    case "image":
                        selectedForm = new ImageTableEditForm(m_database);
                        break;

                    case "sound":
                        selectedForm = new SoundTableEditForm(m_database);
                        break;
                }

                if (selectedForm != null)
                    selectedForm.ShowDialog();
                else
                    MessageBox.Show("No edit mechanism is found");
            }
            else
            {
                MessageBox.Show("Table is not selected");
            }
        }

        private ru.poznaika.database.Alphabet.AlphabetDatabase m_database;
    }
}
