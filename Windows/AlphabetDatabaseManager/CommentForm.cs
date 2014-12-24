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
    public partial class CommentForm : Form
    {
        public CommentForm(string oldComment)
        {
            InitializeComponent();

            if (oldComment != null)
                ui_commentTextBox.Text = oldComment;
        }

        private void button1_Click(object sender, EventArgs e)
        {
            m_newComment = ui_commentTextBox.Text;
            this.Close();
        }

        public string SelectedComment
        {
            get
            {
                return m_newComment;
            }
        }

        private string m_newComment;
    }
}
