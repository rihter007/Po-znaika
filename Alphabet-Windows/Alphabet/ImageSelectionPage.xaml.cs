using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

using ru.po_znaika.server_feedback;
using ru.po_znaika.database.Alphabet;

namespace ru.po_znaika.alphabet
{    
    /// <summary>
    /// Interaction logic for ImageSelectionPage.xaml
    /// </summary>
    public partial class ImageSelectionPage : Page
    {
        private const int PicturesCount = 4; 

        public ImageSelectionPage(ISingleOptionSelectionCallback selectionCallback, string hintText, ImageSource[] images)
        {
            InitializeComponent();

            if (selectionCallback == null)
                throw new ArgumentNullException("selectionCallback is null");

            if ((images == null) || (images.Length != PicturesCount))
                throw new ArgumentNullException("Insufficient images");

            m_selectionCallback = selectionCallback;
            
            ui_firstVariantImage.Source = images[0];
            ui_secondVariantImage.Source = images[1];
            ui_thirdVariantImage.Source = images[2];
            ui_fourthVariantImage.Source = images[3];

            if (!string.IsNullOrEmpty(hintText))
                ui_explainttextBox.Text = hintText;
        }

        private void ui_firstVariantImage_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            m_selectionCallback.OnSelection(0);
        }

        private void ui_secondVariantImage_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            m_selectionCallback.OnSelection(1);
        }

        private void ui_thirdVariantImage_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            m_selectionCallback.OnSelection(2);
        }

        private void ui_fourthVariantImage_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            m_selectionCallback.OnSelection(3);
        }

        private ISingleOptionSelectionCallback m_selectionCallback;        
    }
}
