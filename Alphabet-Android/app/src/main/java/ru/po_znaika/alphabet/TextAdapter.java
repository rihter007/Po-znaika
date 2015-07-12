package ru.po_znaika.alphabet;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * For displaying text as elements
 */
public class TextAdapter extends BaseAdapter
{
    public enum TextAlign
    {
        Center,
        Right,
        Left
    }

    public TextAdapter(Context _context, float _textSize, @NonNull TextAlign _textAlign)
    {
        m_context = _context;
        m_textSize = _textSize;
        m_textAlign = _textAlign;
        m_textElements = new ArrayList<>();
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
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)elementView.getLayoutParams();

        switch (m_textAlign)
        {
            case Right:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                break;
            case Left:
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                break;
        }

        elementView.setLayoutParams(layoutParams);
        textView.setTextSize(m_textSize);
        textView.setText(m_textElements.get(position));

        return elementView;
    }

    private Context m_context;
    private float m_textSize;
    private TextAlign m_textAlign;
    private List<String> m_textElements;
}
