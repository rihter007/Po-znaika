using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Media;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Shapes;

namespace ru.po_znaika.alphabet
{
    static class Helpers
    {
        public static string ReplaceNullStringWithEmpty(string str)
        {
            if (str == null)
                return string.Empty;

            return str;
        }

        public static ImageSource CreateImageFromStream(Stream stream)
        {
            stream.Seek(0, SeekOrigin.Begin);

            BitmapDecoder decoder = BitmapDecoder.Create(stream, BitmapCreateOptions.PreservePixelFormat, BitmapCacheOption.OnLoad);
            if (decoder.Frames.Count == 0)
                return null;

            return decoder.Frames[0];
        }

        public static SoundPlayer CreateSoundPlayerFromStream(Stream stream)
        {
            stream.Seek(0, SeekOrigin.Begin);

            SoundPlayer player = new SoundPlayer(stream);
            return player;
        }

        public static void PaintUiElement(UIElement element, Brush paintBrush)
        {
            if (element is TextBlock)
            {
                TextBlock selectedTextBlock = element as TextBlock;
                selectedTextBlock.Background = paintBrush;
            }
            else if (element is Label)
            {
                Label selectedLabel = element as Label;
                selectedLabel.Background = paintBrush;
            }
            else if (element is Rectangle)
            {
                Rectangle selectedRectangle = element as Rectangle;
                selectedRectangle.Stroke = paintBrush;
            }            
        }
    }
}
