package ru.po_znaika.alphabet;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ru.po_znaika.database.alphabet.AlphabetDatabase;

/**
 * Keeps state of character exercise class object
 */
public class CharacterExerciseItemState implements Parcelable
{
    public int currentStep;

    public int characterExerciseId;
    public char exerciseCharacter;

    public int characterExerciseItemId;
    public AlphabetDatabase.CharacterExerciseItemType characterExerciseItemType;
    public String characterExerciseItemTitle;
    public CharacterExerciseItemStepState[] exerciseSteps;
    public Map<Integer, Integer> exerciseStepsScore;

    public CharacterExerciseItemState()
    {}

    public CharacterExerciseItemState(int _currentStep,
                                      int _characterExerciseId, char _exerciseCharacter,
                                      int _characterExerciseItemId,
                                      final AlphabetDatabase.CharacterExerciseItemType _characterExerciseItemType,
                                      final String _characterExerciseItemTitle,
                                      final Collection<CharacterExerciseItemStepState> _exerciseSteps,
                                      final Map<Integer, Integer> _exerciseStepsScore)

    {
        this.currentStep = _currentStep;

        this.characterExerciseId = _characterExerciseId;
        this.exerciseCharacter = _exerciseCharacter;

        this.characterExerciseItemId = _characterExerciseItemId;
        this.characterExerciseItemType = _characterExerciseItemType;

        this.characterExerciseItemTitle = _characterExerciseItemTitle;

        if (_exerciseSteps != null)
        {
            this.exerciseSteps = new CharacterExerciseItemStepState[_exerciseSteps.size()];
            _exerciseSteps.toArray(this.exerciseSteps);
        }

        if (_exerciseStepsScore != null)
            this.exerciseStepsScore = new HashMap<Integer, Integer>(_exerciseStepsScore);
        else
            this.exerciseStepsScore = new HashMap<Integer, Integer>();
    }

    // Parcelling part
    public CharacterExerciseItemState(Parcel _in)
    {
        this.currentStep = _in.readInt();

        this.characterExerciseId = _in.readInt();
        this.exerciseCharacter = _in.readString().charAt(0);

        this.characterExerciseItemId = _in.readInt();
        this.characterExerciseItemType = AlphabetDatabase.CharacterExerciseItemType.getTypeByValue(_in.readInt());
        this.characterExerciseItemTitle = _in.readString();

        {
            final int ExerciseStepsNumber = _in.readInt();

            if (ExerciseStepsNumber > 0)
            {
                this.exerciseSteps = new CharacterExerciseItemStepState[ExerciseStepsNumber];

                for (int exerciseStepIndex = 0; exerciseStepIndex < ExerciseStepsNumber; ++exerciseStepIndex)
                {
                    this.exerciseSteps[exerciseStepIndex] = _in.readParcelable(CharacterExerciseItemStepState.class.getClassLoader());
                }
            }
        }

        {
            this.exerciseStepsScore = new HashMap<Integer, Integer>();
            _in.readMap(this.exerciseStepsScore, HashMap.class.getClassLoader());
        }
    }

    @Override
    public int describeContents()
    {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags)
    {
        out.writeInt(this.currentStep);
        out.writeInt(this.characterExerciseId);
        out.writeString(((Character)this.exerciseCharacter).toString());
        out.writeInt(this.characterExerciseItemId);
        out.writeInt(this.characterExerciseItemType.getValue());
        out.writeString(this.characterExerciseItemTitle);

        if (this.exerciseSteps != null)
        {
            out.writeInt(this.exerciseSteps.length);
            for (CharacterExerciseItemStepState exerciseItemStepState : this.exerciseSteps)
            {
                out.writeParcelable(exerciseItemStepState, 0);
            }
        }
        else
        {
            out.writeInt(0);
        }

        out.writeMap(this.exerciseStepsScore);
    }

    public static final Creator CREATOR = new Creator()
    {
        public CharacterExerciseItemState createFromParcel(Parcel in)
        {
            return new CharacterExerciseItemState(in);
        }

        public CharacterExerciseItemState[] newArray(int size)
        {
            return new CharacterExerciseItemState[size];
        }
    };
}
