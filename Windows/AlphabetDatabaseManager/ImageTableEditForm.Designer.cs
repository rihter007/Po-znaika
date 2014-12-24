namespace AlphabetDatabaseManager
{
    partial class ImageTableEditForm
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
            this.ui_dataGridView = new System.Windows.Forms.DataGridView();
            this.ui_renewButton = new System.Windows.Forms.Button();
            this.ui_addButton = new System.Windows.Forms.Button();
            this.IdColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.HashColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.DataColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.CommentColumn = new System.Windows.Forms.DataGridViewTextBoxColumn();
            ((System.ComponentModel.ISupportInitialize)(this.ui_dataGridView)).BeginInit();
            this.SuspendLayout();
            // 
            // ui_dataGridView
            // 
            this.ui_dataGridView.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.ui_dataGridView.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.IdColumn,
            this.HashColumn,
            this.DataColumn,
            this.CommentColumn});
            this.ui_dataGridView.Location = new System.Drawing.Point(12, 12);
            this.ui_dataGridView.Name = "ui_dataGridView";
            this.ui_dataGridView.Size = new System.Drawing.Size(645, 208);
            this.ui_dataGridView.TabIndex = 0;
            this.ui_dataGridView.CellDoubleClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.dataGridView1_CellDoubleClick);
            this.ui_dataGridView.UserDeletingRow += new System.Windows.Forms.DataGridViewRowCancelEventHandler(this.ui_dataGridView_UserDeletingRow);
            // 
            // ui_renewButton
            // 
            this.ui_renewButton.Location = new System.Drawing.Point(582, 226);
            this.ui_renewButton.Name = "ui_renewButton";
            this.ui_renewButton.Size = new System.Drawing.Size(75, 23);
            this.ui_renewButton.TabIndex = 1;
            this.ui_renewButton.Text = "Renew";
            this.ui_renewButton.UseVisualStyleBackColor = true;
            this.ui_renewButton.Click += new System.EventHandler(this.ui_renewButton_Click);
            // 
            // ui_addButton
            // 
            this.ui_addButton.Location = new System.Drawing.Point(501, 226);
            this.ui_addButton.Name = "ui_addButton";
            this.ui_addButton.Size = new System.Drawing.Size(75, 23);
            this.ui_addButton.TabIndex = 2;
            this.ui_addButton.Text = "Add";
            this.ui_addButton.UseVisualStyleBackColor = true;
            this.ui_addButton.Click += new System.EventHandler(this.ui_addButton_Click);
            // 
            // IdColumn
            // 
            this.IdColumn.HeaderText = "Id";
            this.IdColumn.Name = "IdColumn";
            this.IdColumn.ReadOnly = true;
            // 
            // HashColumn
            // 
            this.HashColumn.HeaderText = "Hash";
            this.HashColumn.MinimumWidth = 150;
            this.HashColumn.Name = "HashColumn";
            this.HashColumn.ReadOnly = true;
            this.HashColumn.Width = 150;
            // 
            // DataColumn
            // 
            this.DataColumn.AutoSizeMode = System.Windows.Forms.DataGridViewAutoSizeColumnMode.Fill;
            this.DataColumn.HeaderText = "Data";
            this.DataColumn.Name = "DataColumn";
            this.DataColumn.ReadOnly = true;
            // 
            // CommentColumn
            // 
            this.CommentColumn.HeaderText = "Comment";
            this.CommentColumn.MinimumWidth = 200;
            this.CommentColumn.Name = "CommentColumn";
            this.CommentColumn.ReadOnly = true;
            this.CommentColumn.Width = 200;
            // 
            // ImageTableEditForm
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(669, 261);
            this.Controls.Add(this.ui_addButton);
            this.Controls.Add(this.ui_renewButton);
            this.Controls.Add(this.ui_dataGridView);
            this.Name = "ImageTableEditForm";
            this.Text = "ImageEditForm";
            ((System.ComponentModel.ISupportInitialize)(this.ui_dataGridView)).EndInit();
            this.ResumeLayout(false);

        }

        #endregion

        private System.Windows.Forms.DataGridView ui_dataGridView;
        private System.Windows.Forms.Button ui_renewButton;
        private System.Windows.Forms.Button ui_addButton;
        private System.Windows.Forms.DataGridViewTextBoxColumn IdColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn HashColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn DataColumn;
        private System.Windows.Forms.DataGridViewTextBoxColumn CommentColumn;
    }
}