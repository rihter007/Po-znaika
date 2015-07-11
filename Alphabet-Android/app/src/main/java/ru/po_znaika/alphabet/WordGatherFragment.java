package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import java.util.ArrayList;
import java.util.List;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Fragment for processing word gather exercise
 */
public final class WordGatherFragment extends Fragment
{
    private static class WordGatherState implements Parcelable
    {
        /* resources identifier in drawable folder of object image to show */
        public int imageHintResourceId;

        /* Initial word */
        public String word;

        /* specifies position of selection elements */
        public char[] selectionElements;

        /* specifies current user selection of elements */
        public List<Integer> currentSelection;

        public boolean isExerciseChecked;

        public WordGatherState(int _imageHintResourceId
                , @NonNull String _word)
        {
            this.imageHintResourceId = _imageHintResourceId;
            this.word = _word;
            this.selectionElements = this.word.toCharArray();
            while (this.word.equalsIgnoreCase(new String(this.selectionElements)))
                Helpers.randomSnuffle(this.selectionElements);
            this.currentSelection = new ArrayList<>();
            this.isExerciseChecked = false;
        }

        public WordGatherState(Parcel _in)
        {
            this.imageHintResourceId = _in.readInt();
            this.word = _in.readString();
            this.selectionElements = _in.createCharArray();

            this.currentSelection = new ArrayList<>();
            final Object[] currentSelectionValues = _in.readArray(Integer.class.getClassLoader());
            for (Object value : currentSelectionValues)
                this.currentSelection.add((Integer)value);

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
            container.writeInt(this.imageHintResourceId);
            container.writeString(this.word);
            container.writeCharArray(this.selectionElements);
            container.writeArray(this.currentSelection.toArray());
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

    private static final String LogTag = WordGatherFragment.class.getName();

    private static final String AlphabetTypeTag = "alphabet_type";
    private static final String InternalStateTag = "internal_state";

    private static final int MinWordLength = 6;
    private static final int MaxWordLength = 10;
    private static final int CharactersPerRow = MaxWordLength / 2;

    private int internalSquareColor = 0;
    private int borderColor = 0;
    private int selectionColor = 0;
    private int correctSelectionColor = 0;
    private int incorrectSelectionColor = 0;

    public static WordGatherFragment createFragment(@NonNull AlphabetDatabase.AlphabetType alphabetType)
    {
        WordGatherFragment wordGatherFragment = new WordGatherFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(AlphabetTypeTag, alphabetType.getValue());
        wordGatherFragment.setArguments(arguments);
        return  wordGatherFragment;
    }

    public WordGatherFragment()
    {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_displayHelper = new DisplayMetricsHelper(activity);
        m_stepsCallback = (IExerciseStepCallback) activity;
        m_scoreNotification = (IScoreNotification) activity;

        if (activity instanceof ITracerGetter)
            m_tracer = ((ITracerGetter)activity).getTracer();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        m_stepsCallback = null;
        m_scoreNotification = null;
        m_tracer = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_word_gather, container, false);

        try
        {
            internalSquareColor = getResources().getColor(R.color.internal_elements_background);
            borderColor = getResources().getColor(R.color.border);
            selectionColor = getResources().getColor(R.color.selection);
            correctSelectionColor = getResources().getColor(R.color.correct);
            incorrectSelectionColor = getResources().getColor(R.color.incorrect);

            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onCreateView()");
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView, inflater);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            AlertDialogHelper.showMessageBox(getActivity(),
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.failed_exercise_start),
                    false,
                    new DialogInterface.OnClickListener()
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
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);
    }

    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        if (savedInstanceState == null)
        {
            final AlphabetDatabase.AlphabetType alphabetId = AlphabetDatabase.AlphabetType.
                    getTypeByValue(getArguments().getInt(AlphabetTypeTag));

            AlphabetDatabase alphabetDatabase = new AlphabetDatabase(getActivity(), false);
            final Pair<AlphabetDatabase.WordInfo, String> wordInfo = alphabetDatabase.
                    getRandomWordAndImageByAlphabetAndLength(alphabetId, MinWordLength, MaxWordLength);
            if (wordInfo == null)
            {
                ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Could not extract word from alphabet database");
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            final int imageResourceId = DatabaseHelpers.getDrawableIdByName(getResources(), wordInfo.second);
            if (imageResourceId == 0)
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Could not extract image for name: \"%s\"", wordInfo.second));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            m_state = new WordGatherState(imageResourceId, wordInfo.first.word);
        }
        else
        {
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        }
    }

    private void constructUserInterface(@NonNull View fragmentView, @NonNull LayoutInflater inflater)
    {
        {
            final ImageView backButton = (ImageView) fragmentView.findViewById(R.id.backImageView);
            backButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "Back button is pressed");
                    m_stepsCallback.processPreviousStep();
                }
            });
        }

        {
            final Button finishButton = (Button) fragmentView.findViewById(R.id.finishButton);
            finishButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try
                    {
                        onCheckButtonClicked();
                    }
                    catch (CommonException exp)
                    {
                        ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
                        getActivity().finish();
                    }
                }
            });

            if (m_state.isExerciseChecked)
                finishButton.setText(getResources().getString(R.string.caption_finish));
        }

        // Set image hint
        {
            final ImageView imageHint = (ImageView) fragmentView.findViewById(R.id.hintImageView);
            imageHint.setImageDrawable(getResources().getDrawable(m_state.imageHintResourceId));
        }

        constructDynamicUserInterface(fragmentView, inflater);
    }

    private void constructDynamicUserInterface(@NonNull View fragmentView, @NonNull LayoutInflater inflater)
    {
        // process result text
        {
            LinearLayout resultTextLayout = (LinearLayout)fragmentView.findViewById(R.id.resultTextLayout);
            resultTextLayout.removeAllViews();

            for (int index = 0; index < m_state.currentSelection.size(); ++index)
            {
                final int selectionIndex = m_state.currentSelection.get(index);

                RelativeLayout itemLayout = createResultFramedItem(m_state.selectionElements[selectionIndex]);
                if (m_state.isExerciseChecked)
                {
                    final int color = (m_state.word.charAt(index) == m_state.selectionElements[selectionIndex]) ?
                            correctSelectionColor : incorrectSelectionColor;
                    FramedTextItem.setBorderColor(itemLayout, color);
                }
                resultTextLayout.addView(itemLayout);
            }
        }

        // process GridView
        {
            m_selectionElements = new RelativeLayout[m_state.selectionElements.length];

            final LinearLayout[] selectionLayouts = new LinearLayout[]
                    {
                            (LinearLayout)fragmentView.findViewById(R.id.firstCharacterSelectionRow),
                            (LinearLayout)fragmentView.findViewById(R.id.secondCharacterSelectionRow)
                    };
            for (LinearLayout containerLayout : selectionLayouts)
                containerLayout.removeAllViews();

            for (int i = 0; i < m_state.selectionElements.length; ++i)
            {
                final RelativeLayout elementLayout = (RelativeLayout)inflater.inflate(R.layout.framed_text_item, null);
                FramedTextItem.setInternalColorWithNoBorder(elementLayout, internalSquareColor);
                FramedTextItem.setTextSize(elementLayout, getResources().getDimension(R.dimen.word_gather_large_text_size));
                FramedTextItem.setText(elementLayout, m_state.selectionElements[i]);

                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        m_displayHelper.getWidthInProportionDp(1.0 / CharactersPerRow
                            , 2 * (int)getResources().getDimension(R.dimen.small_margin))
                        , ViewGroup.LayoutParams.MATCH_PARENT);
                elementLayout.setLayoutParams(layoutParams);

                final int selectionLayoutIndex = i < CharactersPerRow ? 0 : 1;
                selectionLayouts[selectionLayoutIndex].addView(elementLayout);

                if (!m_state.isExerciseChecked)
                {
                    final int elementIndex = i;
                    elementLayout.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            onSelectionElementClicked(elementIndex);
                        }
                    });

                    // set color

                    if (m_state.currentSelection.indexOf(i) >= 0)
                        elementLayout.setBackgroundColor(selectionColor);
                    else
                        elementLayout.setBackgroundColor(borderColor);
                }
                else
                {
                    elementLayout.setBackgroundColor(selectionColor);
                }

                m_selectionElements[i] = elementLayout;
            }
        }
    }

    private void onSelectionElementClicked(int index)
    {
        try
        {
            if (m_state.isExerciseChecked)
                return;

            boolean declineLastSelection = !m_state.currentSelection.isEmpty()
                    && m_state.currentSelection.get(m_state.currentSelection.size() - 1) == index;

            if (declineLastSelection)
            {
                final int lastElementIndex = m_state.currentSelection.size() - 1;
                m_state.currentSelection.remove(lastElementIndex);

                {
                    LinearLayout resultTextLayout = (LinearLayout) getView().findViewById(R.id.resultTextLayout);
                    resultTextLayout.removeViewAt(lastElementIndex);
                }

                FramedTextItem.setInternalColor(m_selectionElements[index], internalSquareColor);
                return;
            }

            // if element is already selected?
            if (m_state.currentSelection.indexOf(index) >= 0)
                return;

            // new element is selected
            {
                m_state.currentSelection.add(index);

                FramedTextItem.setInternalColor(m_selectionElements[index], selectionColor);

                // add new element to characters line
                {
                    LinearLayout resultTextLayout = (LinearLayout) getView().findViewById(R.id.resultTextLayout);
                    resultTextLayout.addView(createResultFramedItem(m_state.selectionElements[index]));
                }
            }
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            throw exp;
        }
    }

    private void onCheckButtonClicked() throws CommonException
    {
        final View fragmentView = getView();
        if (fragmentView == null)
        {
            ProductTracer.traceMessage(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , "onCheckButtonClicked(): 'Assert: null fragment view'");
            return;
        }

        if (!m_state.isExerciseChecked)
        {
            if (m_state.currentSelection.size() != m_state.word.length())
            {
                AlertDialogHelper.showMessageBox(getActivity()
                        , getResources().getString(R.string.alert_title)
                        , getResources().getString(R.string.not_all_characters_are_used));
                return;
            }

            m_state.isExerciseChecked = true;

            double totalScore = 100.0;
            for (int characterIndex = 0; characterIndex < m_state.selectionElements.length; ++characterIndex)
            {
                if (m_state.selectionElements[characterIndex] != m_state.word.charAt(characterIndex))
                {
                    totalScore -= 10.0;
                }
            }

            m_scoreNotification.setCompletionRate(totalScore);
        }
        else
        {
            m_stepsCallback.processNextStep();
            return;
        }

        // underline all characters
        constructDynamicUserInterface(fragmentView, getActivity().getLayoutInflater());

        // rename "check" button to "finish"
        {
            Button checkButton = (Button) fragmentView.findViewById(R.id.finishButton);
            checkButton.setText(getResources().getString(R.string.caption_finish));
        }
    }

    private RelativeLayout createResultFramedItem(char ch)
    {
        return  createInitialFramedItem(
            m_displayHelper.getWidthInProportionDp(1.0 / MaxWordLength
                    , 2*(int)getResources().getDimension(R.dimen.small_margin))
            , (int)getResources().getDimension(R.dimen.word_gather_small_text_size)
            , ch);
    }

    private RelativeLayout createInitialFramedItem(int width, int textSize, char ch)
    {
        RelativeLayout elementLayout = (RelativeLayout)getActivity()
                .getLayoutInflater().inflate(R.layout.framed_text_item, null);

        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(width, ViewGroup.LayoutParams.MATCH_PARENT);
        elementLayout.setLayoutParams(layoutParams);
        FramedTextItem.setInternalColorWithNoBorder(elementLayout, internalSquareColor);
        FramedTextItem.setTextSize(elementLayout, textSize);
        FramedTextItem.setText(elementLayout, ch);
        return elementLayout;
    }

    private ITracer m_tracer;
    private IExerciseStepCallback m_stepsCallback;
    private IScoreNotification m_scoreNotification;

    private DisplayMetricsHelper m_displayHelper;
    private WordGatherState m_state;

    private RelativeLayout[] m_selectionElements;
}
