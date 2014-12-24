namespace AlphabetDatabaseManager
{
    partial class SoundViewForm
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
            this.ui_playButton = new System.Windows.Forms.Button();
            this.ui_selectButton = new System.Windows.Forms.Button();
            this.ui_changeButton = new System.Windows.Forms.Button();
            this.ui_openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.SuspendLayout();
            // 
            // ui_playButton
            // 
            this.ui_playButton.Font = new System.Drawing.Font("Microsoft Sans Serif", 14F, System.Drawing.FontStyle.Regular, System.Drawing.GraphicsUnit.Point, ((byte)(204)));
            this.ui_playButton.Location = new System.Drawing.Point(83, 55);
            this.ui_playButton.Name = "ui_playButton";
            this.ui_playButton.Size = new System.Drawing.Size(113, 54);
            this.ui_playButton.TabIndex = 0;
            this.ui_playButton.Text = "Play";
            this.ui_playButton.UseVisualStyleBackColor = true;
            this.ui_playButton.Click += new System.EventHandler(this.ui_playButton_Click);
            // 
            // ui_selectButton
            // 
            this.ui_selectButton.Location = new System.Drawing.Point(12, 190);
            this.ui_selectButton.Name = "ui_selectButton";
            this.ui_selectButton.Size = new System.Drawing.Size(75, 23);
            this.ui_selectButton.TabIndex = 1;
            this.ui_selectButton.Text = "Select";
            this.ui_selectButton.UseVisualStyleBackColor = true;
            this.ui_selectButton.Click += new System.EventHandler(this.ui_selectButton_Click);
            // 
            // ui_changeButton
            // 
            this.ui_changeButton.Location = new System.Drawing.Point(197, 190);
            this.ui_changeButton.Name = "ui_changeButton";
            this.ui_changeButton.Size = new System.Drawing.Size(75, 40);
            this.ui_changeButton.TabIndex = 2;
            this.ui_changeButton.Text = "Change sound";
            this.ui_changeButton.UseVisualStyleBackColor = true;
            this.ui_changeButton.Click += new System.EventHandler(this.ui_changeButton_Click);
            // 
            // SoundViewForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(284, 261);
            this.Controls.Add(this.ui_changeButton);
            this.Controls.Add(this.ui_selectButton);
            this.Controls.Add(this.ui_playButton);
            this.Name = "SoundViewForm";
            this.Text = "SoundViewForm";
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.Button ui_playButton;
        private System.Windows.Forms.Button ui_selectButton;
        private System.Windows.Forms.Button ui_changeButton;
        private System.Windows.Forms.OpenFileDialog ui_openFileDialog;
    }
}