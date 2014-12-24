namespace AlphabetDatabaseManager
{
    partial class ImageViewForm
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.ui_pictureBox = new System.Windows.Forms.PictureBox();
            this.ui_changeButton = new System.Windows.Forms.Button();
            this.ui_openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.ui_selectButton = new System.Windows.Forms.Button();
            ((System.ComponentModel.ISupportInitialize)(this.ui_pictureBox)).BeginInit();
            this.SuspendLayout();
            // 
            // ui_pictureBox
            // 
            this.ui_pictureBox.Location = new System.Drawing.Point(13, 13);
            this.ui_pictureBox.Name = "ui_pictureBox";
            this.ui_pictureBox.Size = new System.Drawing.Size(286, 210);
            this.ui_pictureBox.SizeMode = System.Windows.Forms.PictureBoxSizeMode.Zoom;
            this.ui_pictureBox.TabIndex = 0;
            this.ui_pictureBox.TabStop = false;
            // 
            // ui_changeButton
            // 
            this.ui_changeButton.Location = new System.Drawing.Point(224, 230);
            this.ui_changeButton.Name = "ui_changeButton";
            this.ui_changeButton.Size = new System.Drawing.Size(75, 43);
            this.ui_changeButton.TabIndex = 1;
            this.ui_changeButton.Text = "Change picture";
            this.ui_changeButton.UseVisualStyleBackColor = true;
            this.ui_changeButton.Click += new System.EventHandler(this.ui_changeButton_Click);
            // 
            // ui_selectButton
            // 
            this.ui_selectButton.Location = new System.Drawing.Point(12, 240);
            this.ui_selectButton.Name = "ui_selectButton";
            this.ui_selectButton.Size = new System.Drawing.Size(75, 23);
            this.ui_selectButton.TabIndex = 2;
            this.ui_selectButton.Text = "Select";
            this.ui_selectButton.UseVisualStyleBackColor = true;
            this.ui_selectButton.Click += new System.EventHandler(this.ui_selectButton_Click);
            // 
            // ImageViewForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(311, 292);
            this.Controls.Add(this.ui_selectButton);
            this.Controls.Add(this.ui_changeButton);
            this.Controls.Add(this.ui_pictureBox);
            this.Name = "ImageViewForm";
            this.Text = "Image selection";
            ((System.ComponentModel.ISupportInitialize)(this.ui_pictureBox)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.PictureBox ui_pictureBox;
        private System.Windows.Forms.Button ui_changeButton;
        private System.Windows.Forms.OpenFileDialog ui_openFileDialog;
        private System.Windows.Forms.Button ui_selectButton;
    }
}