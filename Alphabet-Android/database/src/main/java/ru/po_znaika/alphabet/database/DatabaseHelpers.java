package ru.po_znaika.alphabet.database;

import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Created by Rihter on 08.02.2015.
 * Contains common helpers for database
 */
public final class DatabaseHelpers
{
    /**
     * All resources at last are collected to the root namespace of the [app] project
     * Thats why here is 'ru.po_znaika.alphabet' instead of 'ru.po_znaika.alphabet.database'
     */
    private static final String ResourcesPackageName = "ru.po_znaika.alphabet";

    public static int getDrawableIdByDatabaseId(@NonNull AlphabetDatabase database
            , @NonNull Resources resources
            , int imageId)
    {
        final String resourceFileName = database.getImageFileNameById(imageId);
        if (TextUtils.isEmpty(resourceFileName))
            return 0;
        return getDrawableIdByName(resources, resourceFileName);
    }

    public static int getSoundIdByDatabaseId(@NonNull AlphabetDatabase database
            , @NonNull Resources resources
            , int soundId)
    {
        final String resourceFileName = database.getSoundFileNameById(soundId);
        if (TextUtils.isEmpty(resourceFileName))
            return 0;
        return getSoundIdByName(resources, resourceFileName);
    }

    public static int getDrawableIdByName(@NonNull Resources resources, @NonNull String imageName)
    {
        return resources.getIdentifier(imageName, "drawable", ResourcesPackageName);
    }

    public static int getSoundIdByName(@NonNull Resources resources, @NonNull String soundName)
    {
        return resources.getIdentifier(soundName, "raw", ResourcesPackageName);
    }
}
