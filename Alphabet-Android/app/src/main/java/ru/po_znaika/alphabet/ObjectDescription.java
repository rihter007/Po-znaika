package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents single object description in different aspects
 */
class ObjectDescription implements Parcelable
{
    public int imageResourceIndex;
    public int soundResourceIndex;

    public String name;

    public ObjectDescription() {}

    public ObjectDescription(int _imageResourceIndex, int _soundResourceIndex, String _name)
    {
        this.imageResourceIndex = _imageResourceIndex;
        this.soundResourceIndex = _soundResourceIndex;
        this.name = _name;
    }

    public ObjectDescription(Parcel _in)
    {
        this.imageResourceIndex = _in.readInt();
        this.soundResourceIndex = _in.readInt();

        this.name = _in.readString();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel container, int flags)
    {
        container.writeInt(this.imageResourceIndex);
        container.writeInt(this.soundResourceIndex);
        container.writeString(this.name);
    }

    public static final Creator CREATOR = new Creator()
    {
        public ObjectDescription createFromParcel(Parcel in)
        {
            return new ObjectDescription(in);
        }

        public ObjectDescription[] newArray(int size)
        {
            return new ObjectDescription[size];
        }
    };
}
