package com.arz_x.android;

import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.arz_x.common.NumberHelper;

/**
 * Created by Rihter on 17.09.2015.
 * Clear way to create UIElementInfo
 */
public class UIElementInfoBuilder
{
    public static UIElementInfoBuilder create(@NonNull Resources resources
            , @NonNull DisplayMetricsHelper displayMetricsHelper
            , @NonNull UIElementScaleResourceInfo scaleResourceInfo)
    {
        UIElementInfoBuilder builder = new UIElementInfoBuilder(displayMetricsHelper);

        builder.setElementType(scaleResourceInfo.elementType);
        builder.setElementId(scaleResourceInfo.elementId);

        if (scaleResourceInfo.leftCoordinateResourceId != null)
            builder.setLeftCoordinateInProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.leftCoordinateResourceId)));
        if (scaleResourceInfo.rightCoordinateResourceId != null)
            builder.setRightCoordinateInProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.rightCoordinateResourceId)));
        if (scaleResourceInfo.topCoordinateResourceId != null)
            builder.setTopCoordinateInProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.topCoordinateResourceId)));
        if (scaleResourceInfo.bottomCoordinateResourceId != null)
            builder.setBottomCoordinateInProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.bottomCoordinateResourceId)));
        builder.setWidthInProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.widthResourceId)));
        builder.setHeightInProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.heightResourceId)));
        if (scaleResourceInfo.heightWidthProportionResourceId != null)
            builder.setHeightWidthProportion(NumberHelper.getProportionFromPercent(resources.getInteger(scaleResourceInfo.heightWidthProportionResourceId)));

        if (scaleResourceInfo.drawOptions instanceof DrawOptions.ImageDrawOption)
            builder.setImageResourceId(((DrawOptions.ImageDrawOption)scaleResourceInfo.drawOptions).getImageResourceId());
        else if (scaleResourceInfo.drawOptions instanceof DrawOptions.ColorDrawOption)
            builder.setColorResourceId(((DrawOptions.ColorDrawOption) scaleResourceInfo.drawOptions).getColorResourceId());
        else if (scaleResourceInfo.drawOptions instanceof DrawOptions.ColorValueDrawOption)
            builder.setColorValue(((DrawOptions.ColorValueDrawOption) scaleResourceInfo.drawOptions).getColorValue());

        return builder;
    }

    public UIElementInfoBuilder(@NonNull DisplayMetricsHelper _displayMetricsHelper)
    {
        m_displayMetricsHelper = _displayMetricsHelper;
        m_result = new UIElementInfo();
    }

    public UIElementInfo build()
    {
        return m_result;
    }

    public UIElementInfoBuilder setElementType(UIElementType elementType)
    {
        m_result.elementType = elementType;
        return this;
    }

    public UIElementInfoBuilder setElementId(int elementId)
    {
        m_result.elementId = elementId;
        return this;
    }

    public UIElementInfoBuilder setImageResourceId(int imageResourceId)
    {
        m_result.drawOptions = new DrawOptions.ImageDrawOption(imageResourceId);
        return this;
    }

    public UIElementInfoBuilder setColorResourceId(int colorResourceId)
    {
        m_result.drawOptions = new DrawOptions.ColorDrawOption(colorResourceId);
        return this;
    }

    public UIElementInfoBuilder setColorValue(int color)
    {
        m_result.drawOptions = new DrawOptions.ColorValueDrawOption(color);
        return this;
    }

    public UIElementInfoBuilder setLeftCoordinatePx(Integer leftCoordinatePx)
    {
        m_result.leftCoordinatePx = leftCoordinatePx;
        return this;
    }

    public UIElementInfoBuilder setLeftCoordinateInProportion(double leftCoordinate)
    {
        m_result.leftCoordinatePx = m_displayMetricsHelper.getWidthInProportionPx(leftCoordinate);
        return this;
    }

    public UIElementInfoBuilder setRightCoordinatePx(Integer rightCoordinatePx)
    {
        m_result.rightCoordinatePx = rightCoordinatePx;
        return this;
    }

    public UIElementInfoBuilder setRightCoordinateInProportion(double rightCoordinate)
    {
        m_result.rightCoordinatePx = m_displayMetricsHelper.getWidthInProportionPx(rightCoordinate);
        return this;
    }

    public UIElementInfoBuilder setTopCoordinatePx(Integer topCoordinatePx)
    {
        m_result.topCoordinatePx = topCoordinatePx;
        return this;
    }

    public UIElementInfoBuilder setTopCoordinateInProportion(double topCoordinate)
    {
        m_result.topCoordinatePx = m_displayMetricsHelper.getHeightInProportionPx(topCoordinate);
        return this;
    }

    public UIElementInfoBuilder setBottomCoordinatePx(Integer bottomCoordinatePx)
    {
        m_result.bottomCoordinatePx = bottomCoordinatePx;
        return this;
    }

    public UIElementInfoBuilder setBottomCoordinateInProportion(double bottomCoordinate)
    {
        m_result.bottomCoordinatePx = m_displayMetricsHelper.getHeightInProportionPx(bottomCoordinate);
        return this;
    }

    public UIElementInfoBuilder setHeightPx(int heightPx)
    {
        m_result.heightPx = heightPx;
        return this;
    }

    public UIElementInfoBuilder setHeightInProportion(double height)
    {
        m_result.heightPx = m_displayMetricsHelper.getHeightInProportionPx(height);
        return this;
    }

    public UIElementInfoBuilder setWidthPx(int widthPx)
    {
        m_result.widthPx = widthPx;
        return this;
    }

    public UIElementInfoBuilder setWidthInProportion(double width)
    {
        m_result.widthPx = m_displayMetricsHelper.getWidthInProportionPx(width);
        return this;
    }

    public UIElementInfoBuilder setHeightWidthProportion(Double heightWidthProportion)
    {
        m_result.heightWidthProportion = heightWidthProportion;
        return this;
    }

    private DisplayMetricsHelper m_displayMetricsHelper;
    private UIElementInfo m_result;
}
