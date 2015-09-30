package com.arz_x.android;

/**
 * Created by Rihter on 17.09.2015.
 * Useful aggregator for constructing UI elements with UIElementInfoBuilder
 */
public class UIElementScaleResourceInfo
{
    public UIElementType elementType;
    public int elementId;

    public Integer leftCoordinateResourceId;
    public Integer rightCoordinateResourceId;
    public Integer topCoordinateResourceId;
    public Integer bottomCoordinateResourceId;

    public int widthResourceId;
    public int heightResourceId;
    public Integer heightWidthProportionResourceId;

    public DrawOptions drawOptions;
}
