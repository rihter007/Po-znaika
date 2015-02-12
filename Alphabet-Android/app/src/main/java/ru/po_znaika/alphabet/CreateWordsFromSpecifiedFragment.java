package ru.po_znaika.alphabet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.ru.po_znaika.common.helpers.AlertDialogHelper;

/**
 * A fragment for the Game: Create sub words from the given
 */
public class CreateWordsFromSpecifiedFragment extends Fragment
{
    /**
     * Represents the game state
     */
    private static class CreateWordsFromSpecifiedState implements Parcelable
    {
        /* Information about the main word from which others are being constructed  */
        public AlphabetDatabase.WordInfo mainWord;

        public ArrayList<String> allSubWords;
        public ArrayList<Integer> foundSubWords;
        public ArrayList<Integer> usedHints;

        public CreateWordsFromSpecifiedState() {}

        public CreateWordsFromSpecifiedState(Parcel _in)
        {
            // get main word
            {
                this.mainWord = new AlphabetDatabase.WordInfo();

                this.mainWord.id = _in.readInt();
                this.mainWord.alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(_in.readInt());
                this.mainWord.complexity = _in.readInt();
                this.mainWord.word = _in.readString();
            }

            this.allSubWords = new ArrayList<String>();
            _in.readStringList(this.allSubWords);

            this.foundSubWords = _in.readArrayList(Integer.class.getClassLoader());

            this.usedHints = _in.readArrayList(Integer.class.getClassLoader());
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel container, int flags)
        {
            // write main word
            {
                container.writeInt(this.mainWord.id);
                container.writeInt(this.mainWord.alphabetType.getValue());
                container.writeInt(this.mainWord.complexity);
                container.writeString(this.mainWord.word);
            }

            container.writeStringList(this.allSubWords);

            container.writeList(this.foundSubWords);

            container.writeList(this.usedHints);
        }

        public static final Creator CREATOR = new Creator()
        {
            public CreateWordsFromSpecifiedState createFromParcel(Parcel in)
            {
                return new CreateWordsFromSpecifiedState(in);
            }

            public CreateWordsFromSpecifiedState[] newArray(int size)
            {
                return new CreateWordsFromSpecifiedState[size];
            }
        };
    }

    /**
     * Represents the user specific selection of Grid control: Which grid and which grid element are selected
     */
    private static class GridSelection
    {
        public static final int InvalidSelectionIndex = -1;

        public GridSelection()
        {
            clear();
        }

        public boolean isValid()
        {
            return (m_gridSelectionIndex != InvalidSelectionIndex) && (m_gridElementSelectionIndex != InvalidSelectionIndex);
        }

        public void clear()
        {
            m_gridSelectionIndex = m_gridElementSelectionIndex = InvalidSelectionIndex;
        }

        public int getGridSelectionIndex()
        {
            return m_gridSelectionIndex;
        }

        public int getGridElementSelectionIndex()
        {
            return m_gridElementSelectionIndex;
        }

        public void setGridSelectionIndex(int gridSelectionIndex)
        {
            m_gridSelectionIndex = gridSelectionIndex;
        }

        public void setGridElementSelectionIndex(int gridElementSelectionIndex)
        {
            m_gridElementSelectionIndex = gridElementSelectionIndex;
        }

        /* Index of selected Grid */
        private int m_gridSelectionIndex;

        /* Index of selected item in Grid */
        private int m_gridElementSelectionIndex;
    }

    private static final String StateTag = "state";

    private static final int MinWordLength = 8;
    private static final int MaxWordLength = 10;

    private static final int SingleCharacterScore = 4;
    private static final int SingleCharacterHintScore = 2;

    private int SelectionColor;
    private int NoSelectionColor;

    public static CreateWordsFromSpecifiedFragment createFragment(@NonNull AlphabetDatabase.AlphabetType alphabetType)
    {
        CreateWordsFromSpecifiedFragment fragment = new CreateWordsFromSpecifiedFragment();

        Bundle arguments = new Bundle();
        arguments.putInt(Constant.AlphabetTypeTag, alphabetType.getValue());
        fragment.setArguments(arguments);
        return fragment;
    }

    public CreateWordsFromSpecifiedFragment()
    {
        // Required empty public constructor
    }

    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        NoSelectionColor = getResources().getColor(android.R.color.holo_blue_light);
        SelectionColor = getResources().getColor(android.R.color.holo_green_light);

        if (savedInstanceState != null)
            m_state = savedInstanceState.getParcelable(StateTag);

        if (m_state == null)
        {
            final Bundle arguments = getArguments();
            final int AlphabetTypeValue = arguments.getInt(Constant.AlphabetTypeTag);

            m_state = new CreateWordsFromSpecifiedState();

            AlphabetDatabase alphabetDatabase = new AlphabetDatabase(getActivity(), false);
            m_state.mainWord = alphabetDatabase.getRandomCreationWordExerciseByAlphabetAndLength(
                    AlphabetDatabase.AlphabetType.getTypeByValue(AlphabetTypeValue), MinWordLength, MaxWordLength);
            if (m_state.mainWord == null)
                throw new IllegalArgumentException();

            final AlphabetDatabase.WordInfo[] allSubWords = alphabetDatabase.getSubWords(m_state.mainWord);
            if (allSubWords == null)
                throw new IllegalArgumentException();

            m_state.allSubWords = new ArrayList<>();
            for (AlphabetDatabase.WordInfo wordInfo : allSubWords)
            {
                m_state.allSubWords.add(wordInfo.word);
            }

            m_state.foundSubWords = new ArrayList<>();
            m_state.usedHints = new ArrayList<>();
        }

        // calculate current used characters number
        {
            m_currentCharactersFound = 0;
            for (Integer wordIndex : m_state.foundSubWords)
                m_currentCharactersFound += m_state.allSubWords.get(wordIndex).length();
        }

        m_selection = new GridSelection();
    }

    private void constructUserInterface(View fragmentView)
    {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // fill source grid
        {
            m_sourceGridElements = new LinearLayout[m_state.mainWord.word.length()];

            ViewAdapter viewAdapter = new ViewAdapter();
            for (int charIndex = 0; charIndex < m_state.mainWord.word.length(); ++charIndex)
            {
                LinearLayout characterLayout = (LinearLayout) inflater.inflate(R.layout.character_item, null, false);
                characterLayout.setBackgroundColor(NoSelectionColor);
                TextView characterTextView = (TextView) characterLayout.findViewById(R.id.textView);
                characterTextView.setText(((Character)m_state.mainWord.word.charAt(charIndex)).toString());

                final int ElementIndex = charIndex;
                characterLayout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onGridItemClicked(0, ElementIndex);
                    }
                });

                viewAdapter.add(characterLayout);

                m_sourceGridElements[charIndex] = characterLayout;
            }

            GridView sourceGrid = (GridView) fragmentView.findViewById(R.id.sourceGridView);
            final ViewGroup.LayoutParams gridLayoutParams = sourceGrid.getLayoutParams();
            gridLayoutParams.width = (int)getResources().getDimension(R.dimen.character_width) * m_state.mainWord.word.length();
            sourceGrid.setLayoutParams(gridLayoutParams);
            sourceGrid.setNumColumns(m_state.mainWord.word.length());
            sourceGrid.setAdapter(viewAdapter);
        }

        // fill destination grid
        {
            m_destinationGridElements = new LinearLayout[m_state.mainWord.word.length()];

            ViewAdapter viewAdapter = new ViewAdapter();
            for (int charIndex = 0; charIndex < m_state.mainWord.word.length(); ++charIndex)
            {
                LinearLayout characterLayout = (LinearLayout) inflater.inflate(R.layout.character_item, null, false);
                final int ElementIndex = charIndex;
                characterLayout.setBackgroundColor(NoSelectionColor);
                characterLayout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onGridItemClicked(1, ElementIndex);
                    }
                });

                viewAdapter.add(characterLayout);

                m_destinationGridElements[charIndex] = characterLayout;
            }

            GridView destinationGrid = (GridView) fragmentView.findViewById(R.id.destinationGridView);
            final ViewGroup.LayoutParams gridLayoutParams = destinationGrid.getLayoutParams();
            gridLayoutParams.width = (int)getResources().getDimension(R.dimen.character_width) * m_state.mainWord.word.length();
            destinationGrid.setLayoutParams(gridLayoutParams);
            destinationGrid.setNumColumns(m_state.mainWord.word.length());
            destinationGrid.setAdapter(viewAdapter);
        }

        // process characters text view
        printCharactersFound(fragmentView, m_currentCharactersFound);

        // process create word button
        {
            Button createWordButton = (Button) fragmentView.findViewById(R.id.createWordButton);
            createWordButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onCreateWordButtonClicked();
                }
            });
        }

        // process finish button
        {
            Button finishButton = (Button) fragmentView.findViewById(R.id.finishButton);
            finishButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onFinishButtonClicked();
                }
            });
        }

        // process left arrow
        {
            ImageButton imageButton = (ImageButton) fragmentView.findViewById(R.id.backImageButton);
            imageButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onBackButtonClicked();
                }
            });
        }

        // process right arrow
        {
            ImageButton imageButton = (ImageButton) fragmentView.findViewById(R.id.skipImageButton);
            imageButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onSkipButtonClicked();
                }
            });
        }

        // process found sub words list
        {
            ListView foundWordsListView = (ListView) fragmentView.findViewById(R.id.wordsListView);
            TextAdapter adapter = new TextAdapter(getActivity(), getResources().getDimension(R.dimen.small_text_size));
            foundWordsListView.setAdapter(adapter);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_create_words_from_specified, container, false);

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
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_exerciseCallback = (IExerciseStepCallback) activity;
        m_scoreNotification = (IScoreNotification) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        m_exerciseCallback = null;
        m_scoreNotification = null;
    }

    private void printCharactersFound(@NonNull View view, int charactersCount)
    {
        TextView textView = (TextView) view.findViewById(R.id.charactersCountTextView);
        textView.setText(String.format(getResources().getString(R.string.caption_current_character_count), charactersCount));

    }

    private void onGridItemClicked(int gridId, int elementId)
    {
        if (m_selection.isValid())
        {
            if ((m_selection.getGridSelectionIndex() == gridId) && (m_selection.getGridElementSelectionIndex() == elementId))
            {
                // deselect
                LinearLayout[] selectedGrid = (m_selection.getGridSelectionIndex() == 0) ? m_sourceGridElements : m_destinationGridElements;
                LinearLayout selectedElement = selectedGrid[m_selection.getGridElementSelectionIndex()];
                selectedElement.setBackgroundColor(NoSelectionColor);

                m_selection.clear();
            }
            else
            {
                // swap elements
                // do not allow exchanging elements in source grid
                if (!((m_selection.getGridSelectionIndex() == gridId) && (gridId == 0)))
                {
                    LinearLayout sourceElement = getGridElementsArrayById(m_selection.getGridSelectionIndex())[m_selection.getGridElementSelectionIndex()];
                    LinearLayout destinationElement = getGridElementsArrayById(gridId)[elementId];

                    TextView sourceElementTextView = (TextView) sourceElement.findViewById(R.id.textView);
                    TextView destinationElementTextView = (TextView) destinationElement.findViewById(R.id.textView);

                    final CharSequence SourceElementText = sourceElementTextView.getText();
                    final CharSequence DestinationElementText = destinationElementTextView.getText();

                    sourceElementTextView.setText(DestinationElementText);
                    destinationElementTextView.setText(SourceElementText);

                    // clear selection
                    sourceElement.setBackgroundColor(NoSelectionColor);
                    m_selection.clear();
                }
            }
        }
        else
        {
           m_selection.setGridSelectionIndex(gridId);
           m_selection.setGridElementSelectionIndex(elementId);
        }

        // UI select element if needed
        if (m_selection.isValid())
        {
            LinearLayout[] selectedGrid = getGridElementsArrayById(m_selection.getGridSelectionIndex());
            LinearLayout selectedElement = selectedGrid[m_selection.getGridElementSelectionIndex()];
            selectedElement.setBackgroundColor(SelectionColor);
        }
    }

    private void onCreateWordButtonClicked()
    {
        String word = "";

        // get new word
        {
            boolean isWordCorrect = true;

            boolean isPreviousElementEmpty = false;
            for (LinearLayout element : m_destinationGridElements)
            {
                TextView textView = (TextView) element.findViewById(R.id.textView);
                final CharSequence TextCharacters = textView.getText();

                if (TextUtils.isEmpty(TextCharacters))
                {
                    isPreviousElementEmpty = true;
                }
                else
                {
                    if (isPreviousElementEmpty)
                    {
                        isWordCorrect = false;
                        break;
                    }

                    word += TextCharacters;
                }
            }

            if (!isWordCorrect)
            {
                Resources resources = getResources();
                AlertDialog msgBox = MessageBox.CreateDialog(getActivity(), resources.getString(R.string.alert_incorrect_word_creation),
                        resources.getString(R.string.alert_title), false, null);
                msgBox.show();
                return;
            }
        }

        final int subWordIndex = m_state.allSubWords.indexOf(word);
        if (subWordIndex == -1)
        {
            Resources resources = getResources();
            MessageBox.Show(getActivity(), resources.getString(R.string.alert_incorrect_word_creation),
                    resources.getString(R.string.alert_title));
            return;
        }

        if (m_state.foundSubWords.contains(subWordIndex))
        {
            Resources resources = getResources();
            MessageBox.Show(getActivity(), resources.getString(R.string.alert_word_already_exists),
                    resources.getString(R.string.alert_title));
            return;
        }

        //
        // word is ok
        //

        m_state.foundSubWords.add(subWordIndex);

        // return grids to initial state
        {
            for (int charIndex = 0; charIndex < m_sourceGridElements.length; ++charIndex)
            {
                TextView textView = (TextView) m_sourceGridElements[charIndex].findViewById(R.id.textView);
                textView.setText(((Character)m_state.mainWord.word.charAt(charIndex)).toString());
            }

            for (LinearLayout layout : m_destinationGridElements)
            {
                TextView textView = (TextView) layout.findViewById(R.id.textView);
                textView.setText("");
            }
        }

        // clear
        if (m_selection.isValid())
        {
            LinearLayout sourceElement = getGridElementsArrayById(m_selection.getGridSelectionIndex())[m_selection.getGridElementSelectionIndex()];
            sourceElement.setBackgroundColor(NoSelectionColor);
            m_selection.clear();
        }

        // renew list of created words
        {
            TextAdapter textAdapter = new TextAdapter(getActivity(), getResources().getDimension(R.dimen.small_text_size));
            for (Integer foundWordIndex : m_state.foundSubWords)
                textAdapter.add(m_state.allSubWords.get(foundWordIndex));

            ListView wordsListView = (ListView) getView().findViewById(R.id.wordsListView);
            wordsListView.setAdapter(textAdapter);
        }

        // renew found characters text
        {
            m_currentCharactersFound += m_state.allSubWords.get(subWordIndex).length();
            printCharactersFound(getView(), m_currentCharactersFound);
        }
    }

    private void onFinishButtonClicked()
    {
        final Resources resources = getResources();
        final AlertDialogHelper.DialogResult dialogResult = AlertDialogHelper.showAlertDialog(getActivity(),
                resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_finish_exercise),
                resources.getString(R.string.yes),
                resources.getString(R.string.no));

        if (dialogResult == AlertDialogHelper.DialogResult.PositiveSelected)
        {
            int totalScore = 0;
            for (Integer wordId : m_state.foundSubWords)
            {
                final String WordLiteral = m_state.allSubWords.get(wordId);
                totalScore += WordLiteral.length() * (m_state.usedHints.contains(wordId) ? SingleCharacterHintScore : SingleCharacterScore);
            }

            m_scoreNotification.setScore(totalScore);
            m_exerciseCallback.processNextStep();
        }
    }

    private void onBackButtonClicked()
    {
        final Resources resources = getResources();
        final AlertDialogHelper.DialogResult dialogResult = AlertDialogHelper.showAlertDialog(getActivity(),
                resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_exit_exercise),
                resources.getString(R.string.yes),
                resources.getString(R.string.no));

        if (dialogResult == AlertDialogHelper.DialogResult.PositiveSelected)
            getActivity().finish();
    }

    private void onSkipButtonClicked()
    {
        final Resources resources = getResources();
        final AlertDialogHelper.DialogResult dialogResult = AlertDialogHelper.showAlertDialog(getActivity(),
                resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_sure_word_skip),
                resources.getString(R.string.yes),
                resources.getString(R.string.no));

        if (dialogResult == AlertDialogHelper.DialogResult.PositiveSelected)
        {
            try
            {
                restoreInternalState(null);
                constructUserInterface(getView());
            }
            catch (Exception exp)
            {
                getActivity().finish();
            }
        }
    }

    private LinearLayout[] getGridElementsArrayById(int id)
    {
        return (id == 0) ? m_sourceGridElements : m_destinationGridElements;
    }

    private IExerciseStepCallback m_exerciseCallback;
    private IScoreNotification m_scoreNotification;

    private CreateWordsFromSpecifiedState m_state;
    private int m_currentCharactersFound;

    private LinearLayout[] m_sourceGridElements;
    private LinearLayout[] m_destinationGridElements;

    private GridSelection m_selection;
}
