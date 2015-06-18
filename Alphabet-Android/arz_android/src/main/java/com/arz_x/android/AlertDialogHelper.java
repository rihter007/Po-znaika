package com.arz_x.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by Rihter on 12.02.2015.
 * Contains helpers for showing dialogs
 */
public final class AlertDialogHelper
{
    public enum DialogResult
    {
        PositiveSelected,
        NegativeSelected
    }

    public interface IDialogResultListener
    {
        void onDialogProcessed(@NonNull DialogResult dialogResult);
    }

    private static class SimpleClickListener implements DialogInterface.OnClickListener
    {
        public SimpleClickListener(@NonNull DialogResult result, @NonNull IDialogResultListener listener)
        {
            m_result = result;
            m_listener = listener;
        }

        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            m_listener.onDialogProcessed(m_result);
        }

        private DialogResult m_result;
        private IDialogResultListener m_listener;
    }

    private static final String OkButtonCaption = "OK";

    public static void showAlertDialog(@NonNull Context context, View dialogView,
                                       String positiveButtonCaption, String negativeButtonCaption,
                                       @NonNull IDialogResultListener dialogListener)
    {
        SimpleClickListener positiveButtonClicker = new SimpleClickListener(DialogResult.PositiveSelected, dialogListener);
        SimpleClickListener negativeButtonClicker = new SimpleClickListener(DialogResult.NegativeSelected, dialogListener);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton(positiveButtonCaption, positiveButtonClicker);
        dialogBuilder.setNegativeButton(negativeButtonCaption, negativeButtonClicker);
        dialogBuilder.show();
    }

    public static void showAlertDialog(@NonNull Context context, String title, String message,
                                               String positiveButtonCaption, String negativeButtonCaption,
                                               @NonNull IDialogResultListener dialogListener)
    {
        SimpleClickListener positiveButtonClicker = new SimpleClickListener(DialogResult.PositiveSelected, dialogListener);
        SimpleClickListener negativeButtonClicker = new SimpleClickListener(DialogResult.NegativeSelected, dialogListener);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveButtonCaption, positiveButtonClicker);
        dialogBuilder.setNegativeButton(negativeButtonCaption, negativeButtonClicker);
        dialogBuilder.show();
    }

    public static void showMessageBox(@NonNull Context context, String title, String message)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);

        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton(OkButtonCaption, null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    public static void showMessageBox(@NonNull Context context,String message, String title,
                                           boolean isCancelable, DialogInterface.OnClickListener okButtonListener)
    {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);

        dlgAlert.setMessage(message);
        dlgAlert.setTitle(title);
        dlgAlert.setPositiveButton(OkButtonCaption, okButtonListener);
        dlgAlert.setCancelable(isCancelable);
        dlgAlert.create().show();
    }
}
