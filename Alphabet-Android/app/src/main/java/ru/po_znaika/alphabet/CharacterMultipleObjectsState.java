package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents state of exercise step with multiple selectionVariants with specified sound
 */
public class CharacterMultipleObjectsState implements Parcelable
{
    public int characterExerciseId;
    public char exerciseCharacter;

    public SoundObject[] soundObjects;

    public CharacterMultipleObjectsState() {}

    public CharacterMultipleObjectsState(Parcel _in)
    {
        this.characterExerciseId = _in.readInt();
        this.exerciseCharacter = _in.readString().charAt(0);

        // get array of sound selectionVariants
        {
            final int SoundObjectsNumber = _in.readInt();
            if (SoundObjectsNumber > 0)
            {
                this.soundObjects = new SoundObject[SoundObjectsNumber];
                for (int soundObjectIndex = 0; soundObjectIndex < SoundObjectsNumber; ++soundObjectIndex)
                    this.soundObjects[soundObjectIndex] = _in.readParcelable(SoundObject.class.getClassLoader());
            }
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel container, int flags)
    {
        container.writeInt(this.characterExerciseId);
        container.writeString(((Character)this.exerciseCharacter).toString());

        container.writeInt(soundObjects.length);
        for (int soundObjectIndex = 0; soundObjectIndex < soundObjects.length; ++soundObjectIndex)
            container.writeParcelable(soundObjects[soundObjectIndex], 0);
    }

    public static final Creator CREATOR = new Creator()
    {
        public CharacterMultipleObjectsState createFromParcel(Parcel in)
        {
            return new CharacterMultipleObjectsState(in);
        }

        public CharacterMultipleObjectsState[] newArray(int size)
        {
            return new CharacterMultipleObjectsState[size];
        }
    };
}
