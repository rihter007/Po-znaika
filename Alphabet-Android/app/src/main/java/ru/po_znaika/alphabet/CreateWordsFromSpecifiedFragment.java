package ru.po_znaika.alphabet;

import android.app.Activity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * A fragment for the Game: Create sub words from the given
 */
public class  CreateWordsFromSpecifiedFragment extends Fragment
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

    private static final String LogTag = CreateWordsFromSpecifiedFragment.class.getName();

    private static final String AlphabetTypeTag = "alphabet_type";
    private static final String InternalStateTag = "state";

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
        arguments.putInt(AlphabetTypeTag, alphabetType.getValue());
        fragment.setArguments(arguments);
        return fragment;
    }

    public CreateWordsFromSpecifiedFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_create_words_from_specified, container, false);

        try
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "onCreateView()");

            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            AlertDialogHelper.showMessageBox(getActivity(),
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.failed_exercise_start),
                    false, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            getActivity().finish();
                        }
                    });
        }

        return fragmentView;
    }

    @Override
    public void onAttach(@NonNull Activity activity)
    {
        super.onAttach(activity);

        m_exerciseCallback = (IExerciseStepCallback) activity;
        m_scoreNotification = (IScoreNotification) activity;
        m_displayMetrics = new DisplayMetricsHelper(activity);

        if (activity instanceof ITracerGetter)
            m_tracer = ((ITracerGetter)activity).getTracer();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        m_exerciseCallback = null;
        m_scoreNotification = null;
        m_tracer = null;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);
    }

    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        NoSelectionColor = getResources().getColor(R.color.internal_elements_background);
        SelectionColor = getResources().getColor(R.color.selection);

        if (savedInstanceState == null)
        {
            final Bundle arguments = getArguments();
            final int AlphabetTypeValue = arguments.getInt(AlphabetTypeTag);

            m_state = new CreateWordsFromSpecifiedState();

            AlphabetDatabase alphabetDatabase = new AlphabetDatabase(getActivity(), false);
            m_state.mainWord = alphabetDatabase.getRandomCreationWordExerciseByAlphabetAndLength(
                    AlphabetDatabase.AlphabetType.getTypeByValue(AlphabetTypeValue), MinWordLength, MaxWordLength);
            if (m_state.mainWord == null)
            {
                ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Failed to extract main word");
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            final AlphabetDatabase.WordInfo[] allSubWords = alphabetDatabase.getSubWords(m_state.mainWord);
            if (allSubWords == null)
            {
                ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Failed to extract subwords");
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            m_state.allSubWords = new ArrayList<>();
            for (AlphabetDatabase.WordInfo wordInfo : allSubWords)
            {
                m_state.allSubWords.add(wordInfo.word);
            }

            m_state.foundSubWords = new ArrayList<>();
            m_state.usedHints = new ArrayList<>();
        }
        else
        {
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        }

        m_selection = new GridSelection();
    }

    private void constructUserInterface(View fragmentView) throws CommonException
    {
        // fill destination grid
        {
            LinearLayout destinationLayout = (LinearLayout)fragmentView.findViewById(R.id.resultTextLayout);
            destinationLayout.removeAllViews();

            m_destinationGridElements = new RelativeLayout[m_state.mainWord.word.length()];
            for (int charIndex = 0; charIndex < m_destinationGridElements.length; ++charIndex)
            {
                RelativeLayout characterLayout = createCharacterItem();
                FramedTextItem.setTextSize(characterLayout
                        , getResources().getDimension(R.dimen.create_words_from_specified_item_text_size));

                final int elementIndex = charIndex;
                characterLayout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onGridItemClicked(0, elementIndex);
                    }
                });
                destinationLayout.addView(characterLayout);
                m_destinationGridElements[charIndex] = characterLayout;
            }
        }

        // fill source grid
        {
            LinearLayout sourceLayout = (LinearLayout)fragmentView.findViewById(R.id.sourceTextLayout);
            sourceLayout.removeAllViews();

            m_sourceGridElements = new RelativeLayout[m_state.mainWord.word.length()];
            for (int charIndex = 0; charIndex < m_state.mainWord.word.length(); ++charIndex)
            {
                RelativeLayout characterLayout = createCharacterItem();
                FramedTextItem.setText(characterLayout, m_state.mainWord.word.charAt(charIndex));
                FramedTextItem.setTextSize(characterLayout
                        , getResources().getDimension(R.dimen.create_words_from_specified_item_text_size));

                final int elementIndex = charIndex;
                characterLayout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onGridItemClicked(1, elementIndex);
                    }
                });
                sourceLayout.addView(characterLayout);
                m_sourceGridElements[charIndex] = characterLayout;
            }
        }

        // process characters text view
        printWordsFound(fragmentView, m_state.foundSubWords.size());

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

            ViewGroup.LayoutParams layoutParams = finishButton.getLayoutParams();
            layoutParams.width = m_displayMetrics.getWidthInProportionPx(0.45
                    , 2 * (int) getResources().getDimension(R.dimen.small_margin));
            finishButton.setLayoutParams(layoutParams);
        }

        // process left arrow
        {
            ImageView backImageView = (ImageView) fragmentView.findViewById(R.id.backImageView);
            backImageView.setOnClickListener(new View.OnClickListener()
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
            Button skipButton = (Button) fragmentView.findViewById(R.id.skipButton);
            skipButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onSkipButtonClicked();
                }
            });
        }

        // process found sub words list
        printFoundSubWords(fragmentView);
    }

    private RelativeLayout createCharacterItem()
    {
        RelativeLayout characterLayout = (RelativeLayout)getActivity().getLayoutInflater().inflate(R.layout.framed_text_item, null, false);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                m_displayMetrics.getWidthInProportionPx(1.0 / MaxWordLength
                        , 2 * (int) getResources().getDimension(R.dimen.small_margin))
                , ViewGroup.LayoutParams.MATCH_PARENT);
        characterLayout.setLayoutParams(layoutParams);
        FramedTextItem.setInternalColorWithNoBorder(characterLayout, NoSelectionColor);
        return characterLayout;
    }

    private void printWordsFound(View view, int wordsCount) throws CommonException
    {
        final View fragmentView = view == null ? getView() : view;
        if (fragmentView == null)
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "printWordsFound for null view");
            throw new CommonException(CommonResultCode.AssertError);
        }

        TextView textView = (TextView) fragmentView.findViewById(R.id.wordsFoundNumberTextView);
        textView.setText(String.format(getResources().getString(R.string.caption_current_words_count), wordsCount));
    }

    private void printFoundSubWords(View view) throws CommonException
    {
        final View fragmentView = view == null ? getView() : view;
        if (fragmentView == null)
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "printFoundSubWords for null view");
            throw new CommonException(CommonResultCode.AssertError);
        }

        ListView foundWordsListView = (ListView) fragmentView.findViewById(R.id.resultWordsListView);
        TextAdapter adapter = new TextAdapter(getActivity()
                , getResources().getDimension(R.dimen.create_words_from_specified_found_word_text_size)
                , TextAdapter.TextAlign.Left);
        for (int i = 0; i < m_state.foundSubWords.size(); ++i)
        {
            final int foundWordIndex = m_state.foundSubWords.get(i);
            adapter.add(String.format("%d. %s", i+1, m_state.allSubWords.get(foundWordIndex)));
        }
        foundWordsListView.setAdapter(adapter);
    }

    private void onGridItemClicked(int gridId, int elementId)
    {
        if (m_selection.isValid())
        {
            if ((m_selection.getGridSelectionIndex() == gridId) && (m_selection.getGridElementSelectionIndex() == elementId))
            {
                // deselect
                RelativeLayout[] selectedGrid = getGridElementsArrayById(m_selection.getGridSelectionIndex());
                FramedTextItem.setInternalColorWithNoBorder(selectedGrid[m_selection.getGridElementSelectionIndex()],
                        NoSelectionColor);
                m_selection.clear();
            }
            else
            {
                // swap elements
                // do not allow exchanging elements in source grid
                if (!((m_selection.getGridSelectionIndex() == gridId) && (gridId == 1)))
                {
                    RelativeLayout sourceElement = getGridElementsArrayById(m_selection.getGridSelectionIndex())[m_selection.getGridElementSelectionIndex()];
                    RelativeLayout destinationElement = getGridElementsArrayById(gridId)[elementId];

                    final String sourceElementText = FramedTextItem.getText(sourceElement);
                    final String destinationElementText = FramedTextItem.getText(destinationElement);

                    FramedTextItem.setText(sourceElement, destinationElementText);
                    FramedTextItem.setText(destinationElement, sourceElementText);

                    // clear selection
                    FramedTextItem.setInternalColorWithNoBorder(sourceElement, NoSelectionColor);
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
            RelativeLayout[] selectedGrid = getGridElementsArrayById(m_selection.getGridSelectionIndex());
            RelativeLayout selectedElement = selectedGrid[m_selection.getGridElementSelectionIndex()];
            FramedTextItem.setInternalColorWithNoBorder(selectedElement, SelectionColor);
        }
    }

    private void onCreateWordButtonClicked()
    {
        try
        {
            String word = "";

            // get new word
            {
                boolean isWordCorrect = true;

                boolean isPreviousElementEmpty = false;
                for (RelativeLayout element : m_destinationGridElements)
                {
                    final String textCharacters = FramedTextItem.getText(element);

                    if (TextUtils.isEmpty(textCharacters))
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

                        word += textCharacters;
                    }
                }

                if (!isWordCorrect)
                {
                    AlertDialogHelper.showMessageBox(getActivity(),
                            getResources().getString(R.string.alert_title),
                            getResources().getString(R.string.alert_incorrect_word_creation));
                    return;
                }
            }

            final int subWordIndex = m_state.allSubWords.indexOf(word);
            if (subWordIndex == -1)
            {
                Resources resources = getResources();
                AlertDialogHelper.showMessageBox(getActivity(),
                        resources.getString(R.string.alert_title),
                        resources.getString(R.string.alert_incorrect_word_creation));
                return;
            }

            if (m_state.foundSubWords.contains(subWordIndex))
            {
                AlertDialogHelper.showMessageBox(getActivity(),
                        getResources().getString(R.string.alert_title),
                        getResources().getString(R.string.alert_word_already_exists));
                return;
            }

            //
            // word is ok
            //

            m_state.foundSubWords.add(subWordIndex);

            // return grids to initial state
            {
                for (int charIndex = 0; charIndex < m_sourceGridElements.length; ++charIndex)
                    FramedTextItem.setText(m_sourceGridElements[charIndex], m_state.mainWord.word.charAt(charIndex));

                for (RelativeLayout layout : m_destinationGridElements)
                    FramedTextItem.setText(layout, "");
            }

            // clear
            if (m_selection.isValid())
            {
                RelativeLayout sourceElement = getGridElementsArrayById(m_selection.getGridSelectionIndex())[m_selection.getGridElementSelectionIndex()];
                FramedTextItem.setInternalColorWithNoBorder(sourceElement, NoSelectionColor);
                m_selection.clear();
            }

            // renew list of created words
            printFoundSubWords(null);

            // renew found characters text
            printWordsFound(null, m_state.foundSubWords.size());
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            AlertDialogHelper.showMessageBox(getActivity()
                    , getResources().getString(R.string.alert_title)
                    , getResources().getString(R.string.error_unknown_error));
        }
    }

    private void onFinishButtonClicked()
    {
        final Resources resources = getResources();
        AlertDialogHelper.showAlertDialog(getActivity(),
                resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_finish_exercise),
                resources.getString(R.string.yes),
                resources.getString(R.string.no),
                new AlertDialogHelper.IDialogResultListener()
                {
                    @Override
                    public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                    {
                        if (dialogResult == AlertDialogHelper.DialogResult.PositiveSelected)
                        {
                            int totalScore = 0;
                            for (Integer wordId : m_state.foundSubWords)
                            {
                                final String WordLiteral = m_state.allSubWords.get(wordId);
                                totalScore += WordLiteral.length() * (m_state.usedHints.contains(wordId) ? SingleCharacterHintScore : SingleCharacterScore);
                            }

                            m_scoreNotification.setCompletionRate(totalScore);
                            m_exerciseCallback.processNextStep();
                        }
                    }
                });
    }

    private void onBackButtonClicked()
    {
        final Resources resources = getResources();
        AlertDialogHelper.showAlertDialog(getActivity(),
                resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_exit_exercise),
                resources.getString(R.string.yes),
                resources.getString(R.string.no),
                new AlertDialogHelper.IDialogResultListener()
                {
                    @Override
                    public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                    {
                        if (dialogResult == AlertDialogHelper.DialogResult.PositiveSelected)
                            getActivity().finish();
                    }
                });
    }

    private void onSkipButtonClicked()
    {
        final Resources resources = getResources();
        AlertDialogHelper.showAlertDialog(getActivity(),
                resources.getString(R.string.alert_title),
                resources.getString(R.string.alert_sure_word_skip),
                resources.getString(R.string.yes),
                resources.getString(R.string.no),
                new AlertDialogHelper.IDialogResultListener()
                {
                    @Override
                    public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                    {
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
                });
    }

    private RelativeLayout[] getGridElementsArrayById(int id)
    {
        return (id == 1) ? m_sourceGridElements : m_destinationGridElements;
    }

    private IExerciseStepCallback m_exerciseCallback;
    private IScoreNotification m_scoreNotification;
    private DisplayMetricsHelper m_displayMetrics;
    private ITracer m_tracer;

    private CreateWordsFromSpecifiedState m_state;

    private RelativeLayout[] m_sourceGridElements;
    private RelativeLayout[] m_destinationGridElements;

    private GridSelection m_selection;
}
