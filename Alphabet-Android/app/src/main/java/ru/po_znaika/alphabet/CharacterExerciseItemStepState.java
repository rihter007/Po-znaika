package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Created by Rihter on 14.08.2014.
 */
public class CharacterExerciseItemStepState implements Parcelable
{
    public AlphabetDatabase.CharacterExerciseActionType actionType;
    public int value;

    public CharacterExerciseItemStepState(AlphabetDatabase.CharacterExerciseActionType _actionType, int _value)
    {
        actionType = _actionType;
        value = _value;
    }

    public CharacterExerciseItemStepState(Parcel _in)
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
        public CharacterExerciseItemStepState createFromParcel(Parcel in)
        {
            return new CharacterExerciseItemStepState(in);
        }

        public CharacterExerciseItemStepState[] newArray(int size)
        {
            return new CharacterExerciseItemStepState[size];
        }
    };
}
