package ru.po_znaika.alphabet;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an adapter for images display
 */
class ImageAdapter extends BaseAdapter
{
    public ImageAdapter(Context _context, int _layoutContainerId)
    {
        m_context = _context;
        m_layoutContainerId = _layoutContainerId;

        m_uiElements = new ArrayList<Drawable>();
    }

    public void add(Drawable itemImage)
    {
        m_uiElements.add(itemImage);
    }

    @Override
    public int getCount()
    {
        return m_uiElements.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

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
            elementView = layoutInflater.inflate(m_layoutContainerId, null, false);
        }

        ImageView imageView = (ImageView) elementView.findViewById(R.id.imageView);

        final Drawable CurrentElementImage = m_uiElements.get(position);
        if (CurrentElementImage != null)
        {
            imageView.setImageDrawable(CurrentElementImage);
            imageView.setVisibility(View.VISIBLE);
        }

        return elementView;
    }

    private Context m_context;
    private int m_layoutContainerId;
    private List<Drawable> m_uiElements;
}
