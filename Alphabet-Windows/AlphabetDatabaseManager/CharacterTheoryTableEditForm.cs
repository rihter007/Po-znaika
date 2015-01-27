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
    public partial class CharacterTheoryTableEditForm : Form
    {
        public CharacterTheoryTableEditForm(ru.poznaika.database.Alphabet.AlphabetDatabase database)
        {
            InitializeComponent();

            m_database = database;
        }

        private ru.poznaika.database.Alphabet.AlphabetDatabase m_database;
    }
}
