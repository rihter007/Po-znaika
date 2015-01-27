package ru.po_znaika.alphabet;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Rihter on 20.08.2014.
 */
class ImageTextAdapter extends BaseAdapter
{
    private class ItemContainer
    {
        public Drawable image;
        public String text;

        public ItemContainer(Drawable _image, String _text)
        {
            this.image = _image;
            this.text = _text;
        }
    }

    public ImageTextAdapter(Context _context, int _layoutContainerId)
    {
        m_context = _context;
        m_layoutContainerId = _layoutContainerId;

        m_uiElements = new ArrayList<ItemContainer>();
    }

    public void add(Drawable itemImage, String itemText)
    {
        m_uiElements.add(new ItemContainer(itemImage, itemText));
    }

    @Override
    public int getCount()
    {
        return m_uiElements.size();
    }

    /**
     * Get the data item associated with the specified position in the data set.
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
            elementView = layoutInflater.inflate(m_layoutContainerId, null, false);
        }

        ImageView imageView = (ImageView) elementView.findViewById(R.id.imageView);
        TextView textView = (TextView) elementView.findViewById(R.id.textView);

       final ItemContainer CurrentElementData = m_uiElements.get(position);
        if (CurrentElementData.image != null)
        {
            imageView.setImageDrawable(CurrentElementData.image);
            imageView.setVisibility(View.VISIBLE);
        }

        if (!TextUtils.isEmpty(CurrentElementData.text))
        {
            textView.setText(CurrentElementData.text);
            textView.setVisibility(View.VISIBLE);
        }

        return elementView;
    }

    private Context m_context;
    private int m_layoutContainerId;
    private List<ItemContainer> m_uiElements;
}
