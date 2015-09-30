package com.arz_x.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.support.annotation.NonNull;

/**
 * Created by Rihter on 16.09.2015.
 * Responsible for drawing, releasing UI elements at certain coordinates and size
 * and releasing
 */
public class DynamicUIManager
{
    public enum CreateOptions
    {
        CreateIfNotExist,
        FailIfNotExist,
    }

    private static final double MaxProportionDelta = 0.1;

    public DynamicUIManager(@NonNull Resources _resources
            , @NonNull RelativeLayout _mainView
            , @NonNull CreateOptions _createOptions
            , @NonNull UIElementInfo[] _viewElements
            , DrawOptions _backgroundDrawOption
            , Integer _cleanColor)
    {
       this(_resources, _mainView, _createOptions, _viewElements, MaxProportionDelta, _backgroundDrawOption, _cleanColor);
    }

    public DynamicUIManager(@NonNull Resources _resources
            , @NonNull RelativeLayout _mainView
            , @NonNull CreateOptions _createOptions
            , @NonNull UIElementInfo[] _viewElements
            , double _maxProportionDelta
            , DrawOptions _backgroundDrawOption
            , Integer _cleanColor)
    {
        m_resources = _resources;
        m_mainView = _mainView;
        m_createOptions = _createOptions;
        m_viewElements = _viewElements;
        m_maxProportionDelta = _maxProportionDelta;
        m_backgroundDrawOptions = _backgroundDrawOption;
        m_cleanColor = _cleanColor;
    }

    public void drawElements()
    {
        setDrawOptions(m_backgroundDrawOptions, m_mainView);

        for (UIElementInfo uiElementInfo : m_viewElements)
        {
            ///
            /// Find or create view
            ///
            View view = m_mainView.findViewById(uiElementInfo.elementId);
            if (view == null)
            {
                if (m_createOptions == CreateOptions.FailIfNotExist)
                    throw new RuntimeException(String.format("Failed to find %d view", uiElementInfo.elementId));

                view = createElement(uiElementInfo.elementType, uiElementInfo.elementId, true);
                if (view == null)
                    throw new RuntimeException(String.format("Failed to create %d view", uiElementInfo.elementId));
            }

            ///
            /// Set position and size
            ///
            {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();

                layoutParams.height = uiElementInfo.heightPx;
                layoutParams.width = uiElementInfo.widthPx;
                if (uiElementInfo.heightWidthProportion != null)
                {
                    // If proportions do not match, set width or height to reach proportions
                    // making them smaller if needed

                    final double actualHeightWidthProportion = (double)layoutParams.height / layoutParams.width;
                    if (actualHeightWidthProportion < uiElementInfo.heightWidthProportion - m_maxProportionDelta)
                    {
                        layoutParams.width = (int)(layoutParams.height / uiElementInfo.heightWidthProportion);
                    }
                    else if (actualHeightWidthProportion > uiElementInfo.heightWidthProportion + m_maxProportionDelta)
                    {
                        layoutParams.height = (int)(layoutParams.width * uiElementInfo.heightWidthProportion);
                    }
                }

                if (uiElementInfo.leftCoordinatePx != null)
                    layoutParams.leftMargin = uiElementInfo.leftCoordinatePx;
                if (uiElementInfo.bottomCoordinatePx != null)
                    layoutParams.bottomMargin = uiElementInfo.bottomCoordinatePx;
                if (uiElementInfo.rightCoordinatePx != null)
                    layoutParams.rightMargin = uiElementInfo.rightCoordinatePx;
                if (uiElementInfo.topCoordinatePx != null)
                    layoutParams.topMargin = uiElementInfo.topCoordinatePx;

                view.setLayoutParams(layoutParams);
            }

            ///
            /// Set color & image
            ///
            setDrawOptions(uiElementInfo.drawOptions, view);
        }
    }

    public void hideElements()
    {
        if (m_cleanColor == null)
            return;

        m_mainView.setBackgroundColor(m_cleanColor);

        for (UIElementInfo uiElementInfo : m_viewElements)
        {
            View view = m_mainView.findViewById(uiElementInfo.elementId);
            view.setBackgroundColor(m_cleanColor);
        }
    }

    private View createElement(@NonNull UIElementType elementType, int elementId
            , boolean doAttachToMainView)
    {
        return null;
    }

    private void setDrawOptions(DrawOptions drawOptions, @NonNull View view)
    {
        if (drawOptions == null)
            return;

        if (drawOptions instanceof DrawOptions.ImageDrawOption)
        {
            final DrawOptions.ImageDrawOption imageDrawOptions = (DrawOptions.ImageDrawOption) drawOptions;

            ViewGroup.LayoutParams viewLayoutParams = view.getLayoutParams();
            final int width = viewLayoutParams.width > 0 ? viewLayoutParams.width : m_resources.getDisplayMetrics().widthPixels;
            final int height = viewLayoutParams.height > 0 ? viewLayoutParams.height : m_resources.getDisplayMetrics().heightPixels;
            Bitmap bmp = ImageHelper.getImageForSpecifiedDimensions(m_resources
                    , imageDrawOptions.getImageResourceId()
                    , width
                    , height);
            if (bmp == null)
            {
                throw new RuntimeException(String.format("Failed to set %d image to view"
                        , imageDrawOptions.getImageResourceId()));
            }
            view.setBackground(new BitmapDrawable(m_resources, bmp));
        }
        else if (drawOptions instanceof DrawOptions.ColorDrawOption)
        {
            final DrawOptions.ColorDrawOption colorDrawOption = (DrawOptions.ColorDrawOption) drawOptions;
            view.setBackgroundColor(m_resources.getColor(colorDrawOption.getColorResourceId()));
        }
        else if (drawOptions instanceof DrawOptions.ColorValueDrawOption)
        {
            final DrawOptions.ColorValueDrawOption colorDrawOptions = (DrawOptions.ColorValueDrawOption) drawOptions;
            view.setBackgroundColor(colorDrawOptions.getColorValue());
        }
        else
        {
            throw new RuntimeException("Unknown drawOptions: " + drawOptions.getClass().getName());
        }
    }

    private Resources m_resources;
    private RelativeLayout m_mainView;
    private CreateOptions m_createOptions;
    private UIElementInfo[] m_viewElements;
    private double m_maxProportionDelta;
    private DrawOptions m_backgroundDrawOptions;
    private Integer m_cleanColor;
}
