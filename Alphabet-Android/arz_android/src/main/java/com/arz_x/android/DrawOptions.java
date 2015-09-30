package com.arz_x.android;

/**
 * Created by Rihter on 17.09.2015.
 * Container for possible drawing types
 */
public class DrawOptions
{
    public static class ImageDrawOption extends DrawOptions
    {
        public ImageDrawOption(int _imageResourceId)
        {
            this.m_imageResourceId = _imageResourceId;
        }

        public int getImageResourceId()
        {
            return m_imageResourceId;
        }

        private int m_imageResourceId;
    }

    public static class ColorDrawOption extends DrawOptions
    {
        public ColorDrawOption(int _colorResourceId)
        {
            this.m_colorResourceId = _colorResourceId;
        }

        public int getColorResourceId()
        {
            return m_colorResourceId;
        }

        private int m_colorResourceId;
    }

    public static class ColorValueDrawOption extends DrawOptions
    {
        public ColorValueDrawOption(int _color)
        {
            this.m_color = _color;
        }

        public int getColorValue()
        {
            return m_color;
        }

        public int m_color;
    }
}
