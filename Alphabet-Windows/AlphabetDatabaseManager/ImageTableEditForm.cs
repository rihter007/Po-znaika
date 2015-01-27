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
    public partial class ImageTableEditForm : Form
    {
        private const int DataColumnIndex = 2;
        private const int CommentColumnIndex = 3;

        public ImageTableEditForm(ru.poznaika.database.Alphabet.AlphabetDatabase database)
        {
            InitializeComponent();

            m_database = database;
            RenewDataGrid();
        }

        private void RenewDataGrid()
        {
            ui_dataGridView.Rows.Clear();

            var allImages = m_database.GetAllImagesShortInfo();
            if (allImages == null)
            {
                MessageBox.Show("Failed to obtain images from databse");
                return;
            }

            foreach (var imageInfo in allImages)
                ui_dataGridView.Rows.Add(imageInfo.id, imageInfo.hash, "double click to edit", imageInfo.comment);            
        }

        private void dataGridView1_CellDoubleClick(object sender, DataGridViewCellEventArgs e)
        {
            if (e.RowIndex >= ui_dataGridView.Rows.Count -1)
                return;

            int imageId = (int)ui_dataGridView.Rows[e.RowIndex].Cells[0].Value;
            
            bool isOperationProcess = false;
            bool isOperationSuccessful = false;
            
            switch (e.ColumnIndex)
            {
                case DataColumnIndex:                    
                    ImageViewForm imageViewForm = new ImageViewForm(m_database.GetImageById(imageId));
                    imageViewForm.ShowDialog();

                    if (imageViewForm.SelectedFile != null)
                    {
                        using (System.IO.FileStream selectedFile = imageViewForm.SelectedFile)
                        {
                            selectedFile.Seek(0, System.IO.SeekOrigin.Begin);

                            byte[] fileContent = new byte[selectedFile.Length];
                            selectedFile.Read(fileContent, 0, fileContent.Length);

                            selectedFile.Seek(0, System.IO.SeekOrigin.Begin);
                            string hashValue = CustomCryptography.Hash.CalculateSHA1Literal(selectedFile);

                            isOperationProcess = true;
                            isOperationSuccessful = m_database.UpdateImageData(imageId, hashValue, fileContent);
                        }                        
                    }
                    break;

                case CommentColumnIndex:
                    string oldComment = (string)ui_dataGridView.Rows[e.RowIndex].Cells[CommentColumnIndex].Value;

                    CommentForm commentViewForm = new CommentForm((string)ui_dataGridView.Rows[e.RowIndex].Cells[CommentColumnIndex].Value);
                    commentViewForm.ShowDialog();

                    if (commentViewForm.SelectedComment != oldComment)
                    {
                        isOperationProcess = true;
                        isOperationSuccessful = m_database.UpdateImageComment(imageId, commentViewForm.SelectedComment);
                    }

                    break;

                default:
                    return;
            }

            if (isOperationSuccessful)
            {
                MessageBox.Show("Row is succesfully updated");
                RenewDataGrid();
            }
            else if (isOperationProcess)
            {
                MessageBox.Show("Failed to update row");
            }
        }

        private void ui_addButton_Click(object sender, EventArgs e)
        {
            ImageViewForm imageViewForm = new ImageViewForm(null);
            imageViewForm.ShowDialog();

            if (imageViewForm.SelectedFile != null)
            {
                using (System.IO.FileStream selectedFile = imageViewForm.SelectedFile)
                {
                    selectedFile.Seek(0, System.IO.SeekOrigin.Begin);

                    byte[] fileContent = new byte[selectedFile.Length];
                    selectedFile.Read(fileContent, 0, fileContent.Length);

                    selectedFile.Seek(0, System.IO.SeekOrigin.Begin);
                    string hashValue = CustomCryptography.Hash.CalculateSHA1Literal(selectedFile);
                    if (m_database.InsertImage(hashValue, fileContent) != ru.poznaika.database.Constant.InvalidDatabaseIndex)
                        MessageBox.Show("Item is succesfully inserted");
                    else
                        MessageBox.Show("Failed to insert item");                    
                }

                RenewDataGrid();
            }
        }

        private void ui_renewButton_Click(object sender, EventArgs e)
        {
            RenewDataGrid();
        }

        private void ui_dataGridView_UserDeletingRow(object sender, DataGridViewRowCancelEventArgs e)
        {
            if (MessageBox.Show("Delete items?", "Warning", MessageBoxButtons.YesNo) != System.Windows.Forms.DialogResult.Yes)
            {
                e.Cancel = true;
                return;
            }

            if (m_database.DeleteImageById((int)e.Row.Cells[0].Value))
                MessageBox.Show("Item has been deleted");
            else
                MessageBox.Show("Failed to delete an item");
        }       
                
        private ru.poznaika.database.Alphabet.AlphabetDatabase m_database;                               
    }
}
