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
    public partial class ExerciseTableEditForm : Form
    {
        public ExerciseTableEditForm(ru.poznaika.database.Alphabet.AlphabetDatabase database)
        {
            InitializeComponent();

            m_database = database;
        }       

        private void dataGridView1_RowsAdded(object sender, DataGridViewRowsAddedEventArgs e)
        {
            
        }

        private ru.poznaika.database.Alphabet.AlphabetDatabase m_database;
    }
}
