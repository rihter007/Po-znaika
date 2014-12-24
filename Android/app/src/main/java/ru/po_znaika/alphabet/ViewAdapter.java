package ru.po_znaika.alphabet;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rihter on 29.08.2014.
 */
public class ViewAdapter extends BaseAdapter
{
    public ViewAdapter()
    {
        m_elementViews = new ArrayList<View>();
    }

    public void add(View v)
    {
        m_elementViews.add(v);
    }

    @Override
    public int getCount()
    {
        return m_elementViews.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
     *
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position)
    {
        return position;
    }

    /**
     * Get the row id associated with the specified position in the list.
     *
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        return m_elementViews.get(position);
    }

    private List<View> m_elementViews;
}
