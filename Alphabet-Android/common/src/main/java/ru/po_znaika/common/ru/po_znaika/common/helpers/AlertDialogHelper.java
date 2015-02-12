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

    public static DialogResult showAlertDialog(@NonNull Context context, String title, String message,
                                               String positiveButtonCaption, String negativeButtonCaption)
    {
        class SimpleClickListener implements DialogInterface.OnClickListener
        {
            public SimpleClickListener()
            {
                m_isClicked = false;
            }

            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                m_isClicked = true;
            }

            public boolean isClicked()
            {
                return m_isClicked;
            }

            private boolean m_isClicked;
        }

        SimpleClickListener positiveButtonClicker = new SimpleClickListener();
        SimpleClickListener negativeButtonClicker = new SimpleClickListener();

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setCancelable(false);
        dialogBuilder.setTitle(title);
        dialogBuilder.setMessage(message);
        dialogBuilder.setPositiveButton(positiveButtonCaption, positiveButtonClicker);
        dialogBuilder.setNegativeButton(negativeButtonCaption, negativeButtonClicker);
        dialogBuilder.show();

        return positiveButtonClicker.isClicked() ? DialogResult.PositiveSelected : DialogResult.NegativeSelected;
    }
}
