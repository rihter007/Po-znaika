using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Forms;

namespace AlphabetDatabaseManager
{
    public partial class ImageViewForm : Form
    {
        public ImageViewForm(System.IO.Stream currentImageContent)
        {
            InitializeComponent();

            if (currentImageContent != null)
                ShowPicture(currentImageContent);                
        }

        private void ui_changeButton_Click(object sender, EventArgs e)
        {
            DialogResult dialogResult = ui_openFileDialog.ShowDialog();

            if (dialogResult != System.Windows.Forms.DialogResult.OK)
                return;
                        
            if (string.IsNullOrEmpty(ui_openFileDialog.FileName))
            {
                MessageBox.Show("Filename must not be empty");
                return;
            }
                        
            FileStream selected = File.OpenRead(ui_openFileDialog.FileName);
            if (ShowPicture(selected))
            {
                if (m_selectedFile != null)
                    m_selectedFile.Dispose();
                m_selectedFile = selected;
            }
        }

        private bool ShowPicture(Stream pictureStream)
        {
            try
            {
                ui_pictureBox.Image = Image.FromStream(pictureStream);
                return true;
            }
            catch// (Exception e)
            {
                MessageBox.Show("Selected file contains unsupported image format");
            }

            return false;
        }

        public FileStream SelectedFile
        {
            get
            {
                return m_selectedFile;
            }
        }

        private void ui_selectButton_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private FileStream m_selectedFile;        
    }
}
