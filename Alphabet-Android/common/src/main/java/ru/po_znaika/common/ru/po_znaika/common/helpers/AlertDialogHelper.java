package ru.po_znaika.common.ru.po_znaika.common.helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

/**
 * Created by Rihter on 12.02.2015.
 * Contains helpers for showing dialogs
 */
public final class AlertDialogHelper
{
    public static enum DialogResult
    {
        PositiveSelected,
        NegativeSelected
    }

    public static interface IDialogResultListener
    {
        void onDialogProcessed(@NonNull DialogResult dialogResult);
    }

    public static void showAlertDialog(@NonNull Context context, String title, String message,
                                               String positiveButtonCaption, String negativeButtonCaption,
                                               @NonNull IDialogResultListener dialogListener)
    {
        class SimpleClickListener implements DialogInterface.OnClickListener
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
}
