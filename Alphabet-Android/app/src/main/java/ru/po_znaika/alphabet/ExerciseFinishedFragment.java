package ru.po_znaika.alphabet;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Random;

import ru.po_znaika.common.IExerciseStepCallback;

public class ExerciseFinishedFragment extends Fragment
{
    private static class State implements Parcelable
    {
        public Integer score;
        public int imageId;

        public State(Integer _score, int _imageId)
        {
            score = _score;
            imageId = _imageId;
        }

        public State(@NonNull Parcel _in)
        {
            if (_in.readByte() != 0)
                score = _in.readInt();
            imageId = _in.readInt();
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags)
        {
            if (this.score != null)
            {
                out.writeByte((byte) 1);
                out.writeInt(score);
            }
            out.writeInt(imageId);
        }

        public static final Creator CREATOR = new Creator()
        {
            public State createFromParcel(Parcel in)
            {
                return new State(in);
            }

            public State[] newArray(int size)
            {
                return new State[size];
            }
        };
    }

    private static final String ScoreTag = "score_tag";
    private static final String InternalStateTag = "internal_state_tag";

    public static Fragment createFragment(Integer score)
    {
        ExerciseFinishedFragment fragment = new ExerciseFinishedFragment();

        if (score != null)
        {
            Bundle bundle = new Bundle();
            bundle.putInt(ScoreTag, score);
            fragment.setArguments(bundle);
        }
        return fragment;
    }

    public ExerciseFinishedFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        if (savedInstanceState != null)
        {
            m_internalState = savedInstanceState.getParcelable(InternalStateTag);
        }
        else
        {
            int finishImagesArray[] = new int[]
                    {
                            R.drawable.exercise_finished_1,
                            R.drawable.exercise_finished_2
                    };

            Random random = new Random();
            final int imageId = finishImagesArray[random.nextInt(finishImagesArray.length)];

            Integer score = null;
            if (getArguments() != null)
                score = getArguments().getInt(ScoreTag);

            m_internalState = new State(score, imageId);
        }

        final View fragmentView = inflater.inflate(R.layout.fragment_exercise_finished, container, false);

        ImageView imageView = (ImageView)fragmentView.findViewById(R.id.imageView);
        imageView.setImageDrawable(getResources().getDrawable(m_internalState.imageId));

        if (m_internalState.score != null)
        {
            TextView scoreTextView = (TextView) fragmentView.findViewById(R.id.scoreTextView);
            scoreTextView.setText(getResources().getString(R.string.caption_exercise_score) + ": " + m_internalState.score);
        }

        Button finishButton = (Button)fragmentView.findViewById(R.id.finishButton);
        finishButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_callback.processNextStep();
            }
        });

        Button repeatButton = (Button) fragmentView.findViewById(R.id.repeatButton);
        repeatButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_callback.repeatExercise();
            }
        });

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_internalState);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_callback = (IExerciseStepCallback) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_callback = null;
    }

    private IExerciseStepCallback m_callback;
    private State m_internalState;
}
