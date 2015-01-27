package ru.po_znaika.alphabet;

import android.os.Parcelable;
import android.os.Parcel;

/**
 * Represents a full description to word of sound exercise
 */
public class SoundObject implements Parcelable
{
    /*The word itself*/
    public String word;
    /*Object representing the word*/
    public int wordImageResourceId;
    /*Sound representing pronunciation of the word*/
    public int wordSoundResourceId;

    public SoundObject() { }

    public SoundObject(String _word, int _wordImageId, int _wordSoundId)
    {
        this.word = _word;
        this.wordImageResourceId = _wordImageId;
        this.wordSoundResourceId = _wordSoundId;
    }

    public SoundObject(Parcel _in)
    {
        this.word = _in.readString();
        this.wordImageResourceId = _in.readInt();
        this.wordSoundResourceId = _in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel container, int flags)
    {
        container.writeString(this.word);
        container.writeInt(this.wordImageResourceId);
        container.writeInt(this.wordSoundResourceId);
    }

    public static final Creator CREATOR = new Creator()
    {
        public SoundObject createFromParcel(Parcel in)
        {
            return new SoundObject(in);
        }

        public SoundObject[] newArray(int size)
        {
            return new SoundObject[size];
        }
    };
}
