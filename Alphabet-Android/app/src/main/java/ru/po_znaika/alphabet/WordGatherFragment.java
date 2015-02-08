package ru.po_znaika.alphabet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Fragment for processing word gather exercise
 */
public class WordGatherFragment extends Fragment
{
    private static class WordGatherState implements Parcelable
    {
        /* resources identifier in drawable folder of object image to show */
        public int imageResourceId;

        /* Initial word */
        public String word;

        /* specifies current position of the characters */
        public char[] currentGather;

        public boolean isExerciseChecked;

        public WordGatherState() {}

        public WordGatherState(Parcel _in)
        {
            this.imageResourceId = _in.readInt();
            this.word = _in.readString();
            this.currentGather = _in.createCharArray();
            this.isExerciseChecked = _in.readByte() != 0;
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel container, int flags)
        {
            container.writeInt(this.imageResourceId);
            container.writeString(this.word);
            container.writeCharArray(this.currentGather);

            container.writeByte((byte)(this.isExerciseChecked ? 1 : 0));
        }

        public static final Creator CREATOR = new Creator()
        {
            public WordGatherState createFromParcel(Parcel in)
            {
                return new WordGatherState(in);
            }

            public WordGatherState[] newArray(int size)
            {
                return new WordGatherState[size];
            }
        };
    }

    private static final String StateTag = "state";

    private static final int MinWordLength = 6;
    private static final int MaxWordLength = 10;

    private static final int SingleCharacterScore = 5;

    private static final int InvalidIndexSelectionValue = -1;


    public WordGatherFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_stepsCallback = (IExerciseStepCallback) activity;
        m_scoreNotification = (IScoreNotification) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        m_stepsCallback = null;
        m_scoreNotification = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_word_gather, container, false);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            AlertDialog msgBox = MessageBox.CreateDialog(getActivity(), resources.getString(R.string.failed_exercise_start),
                    resources.getString(R.string.alert_title), false, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            getActivity().finish();
                        }
                    });
            msgBox.show();
        }

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(StateTag, m_state);
    }

    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        if (savedInstanceState == null)
        {
            final Bundle arguments = getArguments();
            final AlphabetDatabase.AlphabetType AlphabetId = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(Constant.AlphabetTypeTag));

            AlphabetDatabase alphabetDatabase = new AlphabetDatabase(getActivity(), false);
            Pair<AlphabetDatabase.WordInfo, Integer> wordInfo = alphabetDatabase.getRandomWordAndImageByAlphabetAndLength(AlphabetId, MinWordLength, MaxWordLength);
            if (wordInfo == null)
                throw new IllegalArgumentException();

            final int ImageResourceId = getResources().getIdentifier(alphabetDatabase.getImageFileNameById(wordInfo.second), Constant.DrawableResourcesTag, getActivity().getPackageName());
            if (ImageResourceId == 0)
                throw new IllegalArgumentException();

            m_state = new WordGatherState();
            m_state.imageResourceId = ImageResourceId;
            m_state.word = wordInfo.first.word;
            m_state.currentGather = m_state.word.toCharArray();
            m_state.isExerciseChecked = false;

            // random snuffle as long as we do not get the same word
            while (m_state.word.equalsIgnoreCase(new String(m_state.currentGather)))
                Helpers.randomSnuffle(m_state.currentGather);
        }
        else
        {
            m_state = savedInstanceState.getParcelable(StateTag);
        }

        m_selectedItemIndex = InvalidIndexSelectionValue;
    }

    private void constructUserInterface(View fragmentView)
    {
        // process Button onclick listener
        {
            Button button = (Button) fragmentView.findViewById(R.id.finishButton);
            button.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onCheckButtonClicked();
                }
            });
        }

        // Set image hint
        {
            ImageView imageHint = (ImageView) fragmentView.findViewById(R.id.imageView);
            imageHint.setImageDrawable(getResources().getDrawable(m_state.imageResourceId));
        }

        // process GridView
        {
            m_gridElements = new LinearLayout[m_state.currentGather.length];

            ViewAdapter viewAdapter = new ViewAdapter();
            {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                for (int chIndex = 0; chIndex < m_state.currentGather.length; ++chIndex)
                {
                    LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.character_item, null, false);
                    TextView textView = (TextView) layout.findViewById(R.id.textView);
                    textView.setText(((Character)m_state.currentGather[chIndex]).toString());

                    final int CharacterIndex = chIndex;
                    layout.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            onGridElementClicked(CharacterIndex);
                        }
                    });

                    m_gridElements[chIndex] = layout;
                    viewAdapter.add(layout);
                }
            }

            GridView gridView = (GridView) fragmentView.findViewById(R.id.gridView);
            ViewGroup.LayoutParams gridLayoutParameters = gridView.getLayoutParams();
            gridLayoutParameters.width =  (int)getResources().getDimension(R.dimen.character_width) * m_gridElements.length;
            gridView.setLayoutParams(gridLayoutParameters);
            gridView.setNumColumns(viewAdapter.getCount());
            gridView.setAdapter(viewAdapter);
        }
    }

    private void onGridElementClicked(int index)
    {
        if (m_selectedItemIndex != InvalidIndexSelectionValue)
        {
            if (m_selectedItemIndex != index)
            {
                final Character SourceChar = m_state.currentGather[m_selectedItemIndex];
                final Character DestinationChar = m_state.currentGather[index];

                // change characters in internal state
                {
                    m_state.currentGather[m_selectedItemIndex] = DestinationChar;
                    m_state.currentGather[index] = SourceChar;
                }

                // change characters in UI
                {
                    TextView sourceItem = (TextView) m_gridElements[m_selectedItemIndex].findViewById(R.id.textView);
                    TextView destinationItem = (TextView) m_gridElements[index].findViewById(R.id.textView);

                    sourceItem.setText(DestinationChar.toString());
                    destinationItem.setText(SourceChar.toString());
                }
            }

            m_gridElements[m_selectedItemIndex].setBackgroundColor(getResources().getColor(android.R.color.transparent));
            m_selectedItemIndex = InvalidIndexSelectionValue;
        }
        else
        {
            m_gridElements[index].setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            m_selectedItemIndex = index;
        }
    }

    private void onCheckButtonClicked()
    {
        if (m_state.isExerciseChecked)
        {
            int totalScore = 0;
            for (int characterIndex = 0; characterIndex < m_state.currentGather.length; ++characterIndex)
            {
                if (m_state.currentGather[characterIndex] == m_state.word.charAt(characterIndex))
                    totalScore += SingleCharacterScore;
            }

            m_scoreNotification.setScore(totalScore);
            m_stepsCallback.processNextStep();
            return;
        }

        // underline all characters
        {
            for (int characterIndex = 0; characterIndex < m_state.currentGather.length; ++characterIndex)
            {
                final int ColorId = m_state.currentGather[characterIndex] == m_state.word.charAt(characterIndex)
                        ? android.R.color.holo_green_light : android.R.color.holo_red_light;

                m_gridElements[characterIndex].setBackgroundColor(getResources().getColor(ColorId));
            }
        }

        // rename "check" button to "finish"
        {
            Button checkButton = (Button) getView().findViewById(R.id.finishButton);
            checkButton.setText(getResources().getString(R.string.caption_finish));
        }

        m_state.isExerciseChecked = true;
    }

    private IExerciseStepCallback m_stepsCallback;
    private IScoreNotification m_scoreNotification;

    private WordGatherState m_state;

    private LinearLayout m_gridElements[];
    private int m_selectedItemIndex;
}
