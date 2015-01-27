package ru.po_znaika.alphabet;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

/**
 * Helpers for easy message box manipulating
 */
public class MessageBox
{
    public static void Show(Context context, String message, String title)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);

        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public static AlertDialog CreateDialog(Context context,String message, String title,
                                           boolean isCancelable, DialogInterface.OnClickListener okButtonListener)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);

        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton("OK", okButtonListener);
        dlgAlert.setCancelable(isCancelable);

        return dlgAlert.create();
    }
}
