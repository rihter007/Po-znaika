package ru.po_znaika.alphabet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * For displaying text as elements
 */
public class TextAdapter extends BaseAdapter
{
    public TextAdapter(Context _context)
    {
        m_context = _context;
        m_textElements = new ArrayList<String>();
    }

    public void add(String itemText)
    {
        m_textElements.add(itemText);
    }

    @Override
    public int getCount()
    {
        return m_textElements.size();
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
        View elementView = convertView;
        if (elementView == null)
        {
            LayoutInflater layoutInflater = (LayoutInflater) m_context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            elementView = layoutInflater.inflate(R.layout.center_text_item, null, false);
        }

        TextView textView = (TextView) elementView.findViewById(R.id.textView);
        textView.setText(m_textElements.get(position));

        return elementView;
    }

    private Context m_context;
    private List<String> m_textElements;
}
