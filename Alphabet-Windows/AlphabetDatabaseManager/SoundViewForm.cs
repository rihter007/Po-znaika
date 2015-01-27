using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Forms;
using System.Media;

namespace AlphabetDatabaseManager
{
    public partial class SoundViewForm : Form, IDisposable
    {
        public SoundViewForm(Stream soundContent)
        {
            InitializeComponent();

            if (soundContent != null)
            {
                try
                {
                    m_soundPlayer = new SoundPlayer(soundContent);
                }
                catch
                {
                    MessageBox.Show("Invalid sound content");
                }
            }
        }

        private void ui_playButton_Click(object sender, EventArgs e)
        {
            if (m_soundPlayer == null)
            {
                MessageBox.Show("No sound is selected");
                return;
            }

            try
            {
                m_soundPlayer.PlaySync();
            }
            catch
            {
                MessageBox.Show("Failed to play sound");
            }
        }

        private void ui_selectButton_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        private void ui_changeButton_Click(object sender, EventArgs e)
        {
            DialogResult dialogResult = ui_openFileDialog.ShowDialog();
            if (dialogResult != System.Windows.Forms.DialogResult.OK)
                return;

            if (string.IsNullOrEmpty(ui_openFileDialog.FileName))
            {
                MessageBox.Show("File must be selected");
                return;
            }

            if (m_selectedSound != null)
            {
                m_selectedSound.Dispose();
                m_selectedSound = null;
            }

            try
            {
                m_selectedSound = File.OpenRead(ui_openFileDialog.FileName);
            }
            catch (Exception exp)
            {
                MessageBox.Show(string.Format("Failed to open file \"{0}\" exception \"{1}\" has occured", ui_openFileDialog.FileName, exp.Message));
                return;
            }

            if (m_soundPlayer != null)
            {
                m_soundPlayer.Dispose();
                m_soundPlayer = null;
            }

            try
            {
                m_soundPlayer = new SoundPlayer(m_selectedSound);
            }
            catch (Exception exp)
            {
                m_selectedSound.Dispose();
                m_selectedSound = null;
                MessageBox.Show(string.Format("Failed to create sound player, exception \"{0}\" has occured", exp.Message));
                return;
            }            
        }

        void IDisposable.Dispose()
        {
            if (m_soundPlayer != null)
            {
                m_soundPlayer.Dispose();
                m_soundPlayer = null;
            }

            base.Dispose();
        }

        public FileStream SelectedFile
        {
            get
            {
                return m_selectedSound;
            }
        }

        private FileStream m_selectedSound;
        private SoundPlayer m_soundPlayer;        
    }
}
