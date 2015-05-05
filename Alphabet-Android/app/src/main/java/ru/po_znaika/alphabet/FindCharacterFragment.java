package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.common.ru.po_znaika.common.helpers.AlertDialogHelper;

/**
 * Fragment for selecting specified character in text exercise
 */
public class FindCharacterFragment extends Fragment
{
    private enum ExerciseStage
    {
        Active(0),
        Processed(1);

        private static final Map<Integer, ExerciseStage> ValuesMap = new HashMap<Integer, ExerciseStage>()
        {
            {
                put(Active.getValue(), Active);
                put(Processed.getValue(), Processed);
            }
        };

        public static ExerciseStage getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }

        private ExerciseStage(int _value)
        {
            m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private int m_value;
    }

    private static class InternalState implements Parcelable
    {
        public ExerciseStage stage;
        public int columnsCount;
        public MatrixAccessor<Boolean> elementsSelection;

        public InternalState(@NonNull ExerciseStage _stage, int _elementsCount, int _columnsCount)
        {
            this.stage = _stage;
            this.columnsCount = _columnsCount;

            Boolean[] selectionArray = new Boolean[_elementsCount];
            for (int i = 0; i < selectionArray.length; ++i)
                selectionArray[i] = false;

            this.elementsSelection = new MatrixAccessor<>(selectionArray, this.columnsCount);
        }

        public InternalState(@NonNull Parcel _in)
        {
            this.stage = ExerciseStage.getTypeByValue(_in.readInt());
            this.columnsCount = _in.readInt();

            Boolean[] elements = new Boolean[_in.readInt()];
            for (int elemIndex = 0; elemIndex < elements.length; ++elemIndex)
                elements[elemIndex] = _in.readByte() == 1;
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel container, int flags)
        {
            container.writeInt(this.stage.getValue());
            container.writeInt(this.columnsCount);

            final Boolean[] elements = this.elementsSelection.get();
            container.writeInt(elements.length);
            for (int elemIndex = 0; elemIndex < elements.length; ++elemIndex)
                container.writeByte((byte)(elements[elemIndex] ? 1 : 0));
        }

        public static final Creator CREATOR = new Creator()
        {
            public InternalState createFromParcel(Parcel in)
            {
                return new InternalState(in);
            }

            public InternalState[] newArray(int size)
            {
                return new InternalState[size];
            }
        };
    }

    private static final String InternalStateTag = "internal_state";
    private static final String TextTag = "text";
    private static final String SearchCharacterTag = "search_character";

    private static final int ScoreDelta = 5;

    private static final int NoSelectionColorId = android.R.color.transparent;
    private static final int SelectionColorId = android.R.color.holo_blue_light;
    private static final int CorrectSelectionColorId = android.R.color.holo_green_light;
    private static final int IncorrectSelectionColorId = android.R.color.holo_red_light;

    public static FindCharacterFragment createFragment(@NonNull String text, char searchChar)
    {
        FindCharacterFragment fragment = new FindCharacterFragment();

        Bundle args = new Bundle();
        args.putString(TextTag, text);
        args.putChar(SearchCharacterTag, searchChar);
        fragment.setArguments(args);
        return fragment;
    }

    public FindCharacterFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final Bundle arguments = getArguments();
        if (arguments == null)
            throw new NullPointerException();

        final String exerciseText = arguments.getString(TextTag);
        m_text = exerciseText.split(Constant.NewLineDelimiter);
        m_searchCharacter = arguments.getChar(SearchCharacterTag);

        final int maxRowLength = Helpers.getMaxRowLength(m_text);
        final int totalElementsCount = maxRowLength * (m_text.length - 1) + m_text[m_text.length - 1].length();
        if (savedInstanceState != null)
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        else
            m_state = new InternalState(ExerciseStage.Active, totalElementsCount, maxRowLength);

        LinearLayout[] elements = new LinearLayout[totalElementsCount];
        for (int elementIndex = 0; elementIndex < elements.length; ++elementIndex)
        {
            elements[elementIndex] = (LinearLayout) inflater.inflate(R.layout.character_item, null, false);
            final int rowIndex = elementIndex / maxRowLength;
            final int columnIndex = elementIndex % maxRowLength;

            if ((columnIndex < m_text[rowIndex].length()) &&
                    (!Character.isWhitespace(m_text[rowIndex].charAt(columnIndex))))
            {
                TextView textView = (TextView) elements[elementIndex].findViewById(R.id.textView);
                final String itemText = ((Character)m_text[rowIndex].charAt(columnIndex)).toString();
                textView.setText(itemText);

                elements[elementIndex].setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        onItemSelected((LinearLayout) v, rowIndex, columnIndex);
                    }
                });

                if (m_state.stage == ExerciseStage.Active)
                {
                    elements[elementIndex].setBackgroundColor(getResources().getColor(NoSelectionColorId));
                }
                else
                {
                    final boolean isCorrect = m_state.elementsSelection.get(rowIndex, columnIndex) ==
                            (m_text[rowIndex].charAt(columnIndex) == m_searchCharacter);
                    final int colorId = isCorrect ? CorrectSelectionColorId : IncorrectSelectionColorId;
                    elements[elementIndex].setBackgroundColor(colorId);
                }
            }
            else
            {
                elements[elementIndex].setBackgroundColor(getResources().getColor(NoSelectionColorId));
            }
        }
        m_uiElements = new MatrixAccessor<>(elements, maxRowLength);

        final View fragmentView = inflater.inflate(R.layout.fragment_find_character, container, false);

        // put characters
        {
            final int gridWidth = maxRowLength * (int)getResources().getDimension(R.dimen.single_character_width);

            GridView charactersGridView = (GridView) fragmentView.findViewById(R.id.charactersGridView);
            charactersGridView.setNumColumns(maxRowLength);
            ViewGroup.LayoutParams layoutParams = charactersGridView.getLayoutParams();
            layoutParams.width = gridWidth;
            charactersGridView.setLayoutParams(layoutParams);

            ViewAdapter adapter = new ViewAdapter();
            adapter.add(elements);
            charactersGridView.setAdapter(adapter);
        }

        {
            Button backButton = (Button) fragmentView.findViewById(R.id.backButton);
            backButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onBackButtonPressed();
                }
            });
        }

        {
            Button forwardButton = (Button) fragmentView.findViewById(R.id.forwardButton);
            forwardButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onForwardButtonPressed();
                }
            });

            forwardButton.setText(getResources().getText(R.string.caption_check));
        }

        // exercise title
        {
            TextView titleTextView = (TextView)fragmentView.findViewById(R.id.exerciseCaptionTextView);
            titleTextView.setText(String.format(getResources().getString(R.string.caption_find_character), m_searchCharacter));
        }

        return fragmentView;
    }

    @Override
    public void onAttach(@NonNull Activity activity)
    {
        super.onAttach(activity);

        m_stepCallback = (IExerciseStepCallback)activity;
        m_exerciseScoreNotificator = (IScoreNotification)activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        m_stepCallback = null;
        m_exerciseScoreNotificator = null;
    }

    public void onBackButtonPressed()
    {
        Resources resources = getResources();
        AlertDialogHelper.showAlertDialog(getActivity(), resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_exit_exercise),
                resources.getString(R.string.caption_cancel),
                resources.getString(R.string.ok),
                new AlertDialogHelper.IDialogResultListener()
                {
                    @Override
                    public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                    {
                        if (dialogResult != AlertDialogHelper.DialogResult.PositiveSelected)
                            return;

                        m_stepCallback.processPreviousStep();
                    }
                });
    }

    public void onForwardButtonPressed()
    {
        if (m_state.stage == ExerciseStage.Active)
        {
            m_state.stage = ExerciseStage.Processed;

            final String searchCharacter = ((Character)m_searchCharacter).toString();

            int mistakesCount = 0;
            int charactersCount = 0;

            final int elementsCount = m_state.elementsSelection.get().length;
            for (int elementIndex = 0; elementIndex < elementsCount; ++elementIndex)
            {
                final int rowIndex = elementIndex / m_state.columnsCount;
                final int columnIndex = elementIndex % m_state.columnsCount;

                if ((columnIndex < m_text[rowIndex].length()) &&
                        (!Character.isWhitespace(m_text[rowIndex].charAt(columnIndex))))
                {
                    String currentCharacter = ((Character)m_text[rowIndex].charAt(columnIndex)).toString();
                    final boolean isCorrect = m_state.elementsSelection.get(rowIndex, columnIndex) ==
                            currentCharacter.equalsIgnoreCase(searchCharacter);
                    final int colorId = isCorrect ? CorrectSelectionColorId : IncorrectSelectionColorId;

                    ++charactersCount;

                    if (!isCorrect)
                        ++mistakesCount;

                    m_uiElements.get(rowIndex, columnIndex).setBackgroundColor(getResources().getColor(colorId));
                }
            }

            int score = (int)((double)(charactersCount - mistakesCount) / (double)charactersCount * ScoreDelta);
            m_exerciseScoreNotificator.setScore(score);

            Button forwardButton = (Button)getView().findViewById(R.id.forwardButton);
            forwardButton.setText(getResources().getText(R.string.caption_next));
        }
        else
        {
            m_stepCallback.processNextStep();
        }
    }

    public void onItemSelected(@NonNull LinearLayout view, int rowId, int columnId)
    {
        if (m_state.stage != ExerciseStage.Active)
            return;

        final boolean isSelected = m_state.elementsSelection.get(rowId, columnId);
        int colorId = isSelected ? NoSelectionColorId : SelectionColorId;
        view.setBackgroundColor(getResources().getColor(colorId));
        m_state.elementsSelection.set(rowId, columnId, !isSelected);
    }

    private IExerciseStepCallback m_stepCallback;
    private IScoreNotification m_exerciseScoreNotificator;

    private String[] m_text;
    private char m_searchCharacter;

    private InternalState m_state;
    private MatrixAccessor<LinearLayout> m_uiElements;
}
