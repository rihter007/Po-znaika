package ru.po_znaika.alphabet.database;

import android.content.res.Resources;
import android.support.annotation.NonNull;

/**
 * Created by Rihter on 08.02.2015.
 * Contains common helpers for database
 */
public final class DatabaseHelpers
{
    private static final String DatabasePackageName = DatabaseHelpers.class.getPackage().getName(); // "ru.po_znaika.alphabet.database";

    public static int getDrawableIdByName(@NonNull Resources resources, @NonNull String imageName)
    {
        return resources.getIdentifier(imageName, "drawable-content", DatabasePackageName);
    }

    public static int getSoundIdByName(@NonNull Resources resources, @NonNull String soundName)
    {
        throw new UnsupportedOperationException();
        //return resources.get
    }
}
