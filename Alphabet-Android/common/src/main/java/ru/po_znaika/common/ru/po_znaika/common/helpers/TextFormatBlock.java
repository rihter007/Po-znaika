package ru.po_znaika.common.ru.po_znaika.common.helpers;

import android.support.annotation.NonNull;

/**
 * Created by Rihter on 08.04.2015.
 * Represents a single block of formatted text
 */
public class TextFormatBlock
{
    public TextFormatBlock(@NonNull String _text)
    {
        this.text = _text;
    }

    public TextFormatBlock(@NonNull String _text, int _textSize, int _rgbColor)
    {
        this.text = _text;
        this.textSize = _textSize;
        this.rgbColor = _rgbColor;
    }

    public void setTextSize(int textSize)
    {
        this.textSize = textSize;
    }

    public void setARGBColor(int rgbColor)
    {
        this.rgbColor = rgbColor;
    }

    public String getText()
    {
        return this.text;
    }

    public int getTextSize()
    {
        return this.textSize;
    }

    public int getARGBColor()
    {
        return this.rgbColor;
    }

    public boolean isTextSizeSet()
    {
        return this.textSize != null;
    }

    public boolean isColorSet()
    {
        return this.rgbColor != null;
    }

    private Integer textSize;
    private Integer rgbColor;

    private String text;
}
