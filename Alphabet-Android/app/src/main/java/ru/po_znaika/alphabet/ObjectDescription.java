package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents single object description in different aspects
 */
class ObjectDescription implements Parcelable
{
    public String imageFilePath;
    public String soundFilePath;

    public String name;

    public ObjectDescription() {}

    public ObjectDescription(String _imageResourceIndex, String _soundResourceIndex, String _name)
    {
        this.imageFilePath = _imageResourceIndex;
        this.soundFilePath = _soundResourceIndex;
        this.name = _name;
    }

    public ObjectDescription(Parcel _in)
    {
        this.imageFilePath = _in.readString();
        this.soundFilePath = _in.readString();

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
        container.writeString(this.imageFilePath);
        container.writeString(this.soundFilePath);
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
