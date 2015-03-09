package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Represents internal state of single selection exercise for ImageSelectionFragment
 */
class ImageSelectionSingleExerciseState implements Parcelable
{
    /* Describes title of an exercise*/
    public String exerciseTitle;
    /* Array of selectionVariants to be represented in exercise */
    public ObjectDescription[] selectionVariants;

    /* Index of answer image  */
    public int answer;

    public ImageSelectionSingleExerciseState() {}

    public ImageSelectionSingleExerciseState(Parcel _in)
    {
        this.exerciseTitle = _in.readString();

        final int ObjectsCount = _in.readInt();
        if (ObjectsCount > 0)
        {
            this.selectionVariants = new ObjectDescription[ObjectsCount];

            for (int objectIndex = 0; objectIndex < ObjectsCount; ++objectIndex)
                this.selectionVariants[objectIndex] = _in.readParcelable(ObjectDescription.class.getClassLoader());
        }

        this.answer = _in.readInt();
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel container, int flags)
    {
        container.writeString(this.exerciseTitle);

        if (this.selectionVariants != null)
        {
            container.writeInt(this.selectionVariants.length);
            for (int objectIndex = 0; objectIndex < this.selectionVariants.length; ++objectIndex)
                container.writeParcelable(this.selectionVariants[objectIndex], 0);
        }
        else
        {
            container.writeInt(0);
        }

        container.writeInt(this.answer);
    }

    public static final Creator CREATOR = new Creator()
    {
        public ImageSelectionSingleExerciseState createFromParcel(Parcel in)
        {
            return new ImageSelectionSingleExerciseState(in);
        }

        public ImageSelectionSingleExerciseState[] newArray(int size)
        {
            return new ImageSelectionSingleExerciseState[size];
        }
    };
}
