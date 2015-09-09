package com.arz_x.android;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Rihter on 30.08.2015.
 * Helper for fitting image size to certain proportions
 */
public class ImageHelper
{
    public static Bitmap getImageForSpecifiedDimensions(@NonNull Resources res, int imageResourceId,
                                                         int reqWidth, int reqHeight)
    {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, imageResourceId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, imageResourceId, options);
    }

    public static Bitmap getImageForSpecifiedView(@NonNull Resources res, int imageResourceId, @NonNull View view)
    {
        final ViewGroup.LayoutParams viewParams = view.getLayoutParams();
        // TODO: throw exception if height or width are special values
        return getImageForSpecifiedDimensions(res, imageResourceId, viewParams.width, viewParams.height);
    }

    private static int calculateInSampleSize(@NonNull BitmapFactory.Options options, int reqWidth, int reqHeight)
    {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
}
