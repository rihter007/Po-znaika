package ru.po_znaika.alphabet;

import android.os.Parcelable;
import android.os.Parcel;

import java.util.Collection;

/**
 * Represents state for image selection exercise fragment
 */
public class ImageSelectionState implements Parcelable
{
    public ImageSelectionSingleExerciseState[] exerciseStates;
    public int[] exercisesTryCount;

    public int currentExerciseStepNumber;

    public ImageSelectionState(Collection<ImageSelectionSingleExerciseState> _exerciseStates)
    {
        this.exerciseStates = new ImageSelectionSingleExerciseState[_exerciseStates.size()];
        _exerciseStates.toArray(this.exerciseStates);

        this.exercisesTryCount = new int[_exerciseStates.size()];

        currentExerciseStepNumber = 0;
    }

    public ImageSelectionState(Parcel _in)
    {
        final int ExercisesCount = _in.readInt();
        if (ExercisesCount > 0)
        {
            this.exerciseStates = new ImageSelectionSingleExerciseState[ExercisesCount];

            for (int i = 0; i < ExercisesCount; ++i)
            {
                this.exerciseStates[i] = _in.readParcelable(ImageSelectionState.class.getClassLoader());
            }

            this.exercisesTryCount = new int[ExercisesCount];
            _in.readIntArray(this.exercisesTryCount);

            this.currentExerciseStepNumber = _in.readInt();
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
        if (exerciseStates != null)
        {
            container.writeInt(this.exerciseStates.length);
            for (int i = 0; i < this.exerciseStates.length; ++i)
            {
                container.writeParcelable(this.exerciseStates[i], 0);
            }

            container.writeIntArray(this.exercisesTryCount);

            container.writeInt(this.currentExerciseStepNumber);
        }
        else
        {
            container.writeInt(0);
        }
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
    {
        public ImageSelectionState createFromParcel(Parcel in)
        {
            return new ImageSelectionState(in);
        }

        public ImageSelectionState[] newArray(int size)
        {
            return new ImageSelectionState[size];
        }
    };
}
