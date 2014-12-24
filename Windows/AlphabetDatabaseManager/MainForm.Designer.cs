namespace AlphabetDatabaseManager
{
    partial class MainForm
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
            this.ui_tablesListBox = new System.Windows.Forms.ListBox();
            this.ui_tableSelectButton = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.ui_openFileDialog = new System.Windows.Forms.OpenFileDialog();
            this.SuspendLayout();
            // 
            // ui_tablesListBox
            // 
            this.ui_tablesListBox.FormattingEnabled = true;
            this.ui_tablesListBox.Location = new System.Drawing.Point(12, 32);
            this.ui_tablesListBox.Name = "ui_tablesListBox";
            this.ui_tablesListBox.Size = new System.Drawing.Size(260, 186);
            this.ui_tablesListBox.TabIndex = 0;
            this.ui_tablesListBox.MouseDoubleClick += new System.Windows.Forms.MouseEventHandler(this.ui_tablesListBox_MouseDoubleClick);
            // 
            // ui_tableSelectButton
            // 
            this.ui_tableSelectButton.Location = new System.Drawing.Point(196, 226);
            this.ui_tableSelectButton.Name = "ui_tableSelectButton";
            this.ui_tableSelectButton.Size = new System.Drawing.Size(75, 23);
            this.ui_tableSelectButton.TabIndex = 1;
            this.ui_tableSelectButton.Text = "Select";
            this.ui_tableSelectButton.UseVisualStyleBackColor = true;
            this.ui_tableSelectButton.Click += new System.EventHandler(this.ui_tableSelectButton_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(63, 13);
            this.label1.TabIndex = 2;
            this.label1.Text = "Select table";
            // 
            // ui_openFileDialog
            // 
            this.ui_openFileDialog.CheckFileExists = false;
            this.ui_openFileDialog.CheckPathExists = false;
            // 
            // MainForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(284, 261);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.ui_tableSelectButton);
            this.Controls.Add(this.ui_tablesListBox);
            this.Name = "MainForm";
            this.Text = "Table edit form";
            this.Load += new System.EventHandler(this.MainForm_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.ListBox ui_tablesListBox;
        private System.Windows.Forms.Button ui_tableSelectButton;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.OpenFileDialog ui_openFileDialog;
    }
}

