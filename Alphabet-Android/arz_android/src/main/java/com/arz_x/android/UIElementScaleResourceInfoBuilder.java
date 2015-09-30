package com.arz_x.android;

/**
 * Created by Rihter on 27.09.2015.
 * Useful helper for constructing UIElementScaleResourceInfo
 */
public class UIElementScaleResourceInfoBuilder
{
    public UIElementScaleResourceInfoBuilder()
    {
        m_result = new UIElementScaleResourceInfo();
    }

    public UIElementScaleResourceInfo build()
    {
        return m_result;
    }

    public UIElementScaleResourceInfoBuilder setElementId(int elementId)
    {
        m_result.elementId = elementId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setElementType(UIElementType elementType)
    {
        m_result.elementType = elementType;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setLeft(Integer leftCoordinateResourceId)
    {
        m_result.leftCoordinateResourceId = leftCoordinateResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setRight(Integer rightCoordinateResourceId)
    {
        m_result.rightCoordinateResourceId = rightCoordinateResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setTop(Integer topCoordinateResourceId)
    {
        m_result.topCoordinateResourceId = topCoordinateResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setBottom(Integer bottomCoordinateResourceId)
    {
        m_result.bottomCoordinateResourceId = bottomCoordinateResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setWidth(int widthResourceId)
    {
        m_result.widthResourceId = widthResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setHeight(int heightResourceId)
    {
        m_result.heightResourceId = heightResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setHeightWidthProportion(Integer heightWidthProportionResourceId)
    {
        m_result.heightWidthProportionResourceId = heightWidthProportionResourceId;
        return this;
    }

    public UIElementScaleResourceInfoBuilder setImageResourceId(int imageResourceId)
    {
        m_result.drawOptions = new DrawOptions.ImageDrawOption(imageResourceId);
        return this;
    }

    public UIElementScaleResourceInfoBuilder setColorResourceId(int colorResourceId)
    {
        m_result.drawOptions = new DrawOptions.ColorDrawOption(colorResourceId);
        return this;
    }

    public UIElementScaleResourceInfoBuilder setColorValue(int color)
    {
        m_result.drawOptions = new DrawOptions.ColorValueDrawOption(color);
        return this;
    }

    private UIElementScaleResourceInfo m_result;
}
