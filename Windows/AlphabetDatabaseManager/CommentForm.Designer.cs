namespace AlphabetDatabaseManager
{
    partial class CommentForm
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
            this.ui_commentTextBox = new System.Windows.Forms.TextBox();
            this.ui_selectButton = new System.Windows.Forms.Button();
            this.label1 = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // ui_commentTextBox
            // 
            this.ui_commentTextBox.Location = new System.Drawing.Point(13, 37);
            this.ui_commentTextBox.Name = "ui_commentTextBox";
            this.ui_commentTextBox.Size = new System.Drawing.Size(259, 20);
            this.ui_commentTextBox.TabIndex = 0;
            // 
            // ui_selectButton
            // 
            this.ui_selectButton.Location = new System.Drawing.Point(197, 63);
            this.ui_selectButton.Name = "ui_selectButton";
            this.ui_selectButton.Size = new System.Drawing.Size(75, 23);
            this.ui_selectButton.TabIndex = 1;
            this.ui_selectButton.Text = "select";
            this.ui_selectButton.UseVisualStyleBackColor = true;
            this.ui_selectButton.Click += new System.EventHandler(this.button1_Click);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(13, 13);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(78, 13);
            this.label1.TabIndex = 2;
            this.label1.Text = "New comment:";
            // 
            // CommentForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(284, 101);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.ui_selectButton);
            this.Controls.Add(this.ui_commentTextBox);
            this.Name = "CommentForm";
            this.Text = "Edit Comment";
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox ui_commentTextBox;
        private System.Windows.Forms.Button ui_selectButton;
        private System.Windows.Forms.Label label1;
    }
}