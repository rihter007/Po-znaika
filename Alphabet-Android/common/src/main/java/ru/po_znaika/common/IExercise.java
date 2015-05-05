package ru.po_znaika.common;

import android.graphics.drawable.Drawable;

import java.lang.String;
import java.io.ByteArrayOutputStream;

/**
 * Created by Rihter on 07.08.2014.
 *
 * Main interface which represents an entire exercise.
 * Include methods for representing exercise to user and programmatically manage it
 */
public interface IExercise
{
    /*
    Launches the exercise
    May throw exceptions
    */
    void process();

    /* Returns a unique id of the exercise */
    int getId();

    /* Returns exercise internal format name */
    String getName();

    /* Returns user-friendly exercise name to display */
    String getDisplayName();

    /* Returns an exercise icon */
    Drawable getDisplayImage();
}
