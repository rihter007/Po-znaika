package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Created by Rihter on 06.03.2015.
 * Represents database description of exercise step
 */
final class CharacterExerciseItemStep implements Parcelable
{
    /**
     * Kind of action to perform
     */
    public AlphabetDatabase.CharacterExerciseActionType actionType;
    /**
     * Special parameter wich depends on action type
     */
    public int value;

    public CharacterExerciseItemStep(AlphabetDatabase.CharacterExerciseActionType _actionType, int _value)
    {
        actionType = _actionType;
        value = _value;
    }

    public CharacterExerciseItemStep(Parcel _in)
    {
        actionType = AlphabetDatabase.CharacterExerciseActionType.getTypeByValue(_in.readInt());
        value = _in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel container, int flags)
    {
        container.writeInt(actionType.getValue());
        container.writeInt(value);
    }

    public static final Creator CREATOR = new Creator()
    {
        public CharacterExerciseItemStep createFromParcel(Parcel in)
        {
            return new CharacterExerciseItemStep(in);
        }

        public CharacterExerciseItemStep[] newArray(int size)
        {
            return new CharacterExerciseItemStep[size];
        }
    };
}
