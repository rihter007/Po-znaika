package ru.po_znaika.alphabet;

import android.os.AsyncTask;
import android.support.annotation.NonNull;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Rihter on 09.03.2015.
 */
public class CloseAsyncTask extends AsyncTask<Void, Void, Void>
{
    public CloseAsyncTask(@NonNull Closeable closeable)
    {
        List<Closeable> closeables = new ArrayList<>();
        closeables.add(closeable);
        m_closeCollection = closeables;
    }

    public CloseAsyncTask(@NonNull Collection<Closeable> closeableCollection)
    {
        m_closeCollection = closeableCollection;
    }

    @Override
    public Void doInBackground(Void... params)
    {
        for (Closeable closeable : m_closeCollection)
        {
            try
            {
                closeable.close();
            }
            catch (Exception exp) { }
        }

        return null;
    }

    private Collection<Closeable> m_closeCollection;
}
