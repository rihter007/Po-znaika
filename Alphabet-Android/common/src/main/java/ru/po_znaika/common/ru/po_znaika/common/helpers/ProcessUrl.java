package ru.po_znaika.common.ru.po_znaika.common.helpers;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by Rihter on 23.04.2015.
 * Helper for perform of opening url in android browser or other external application
 */
public class ProcessUrl
{
    public static void openUrl(@NonNull Context context, @NonNull String url)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }
}
