package ru.po_znaika.alphabet;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.ImageHelper;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.common.IExerciseStepCallback;

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

        ExerciseStage(int _value)
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
            this.elementsSelection = new MatrixAccessor<>(elements, this.columnsCount);
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
            for (Boolean elem : elements)
                container.writeByte((byte)(elem ? 1 : 0));
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

    private enum SelectionType
    {
        NoSelection,
        Selected,
        Correct,
        InCorrect
    }

    private static final String LogTag = FindCharacterFragment.class.getName();

    private static final int MaxCharactersInRowCount = 20;
    private static final int MaxMistakesCount = 5;

    private static final String InternalStateTag = "internal_state";
    private static final String TextTag = "text";
    private static final String SearchCharacterTag = "search_character";
    private static final String ExerciseIconTag = "exercise_icon";

    private static int NoSelectionColor = Constant.Color.BackgroundBlue;
    private static final int SelectionColor = Constant.Color.LightBlue;
    private static final int CorrectSelectionColor = Constant.Color.LightGreen;
    private static final int IncorrectSelectionColor = Constant.Color.LightRed;

    public static FindCharacterFragment createFragment(@NonNull String text, char searchChar, int exerciseIconId)
    {
        FindCharacterFragment fragment = new FindCharacterFragment();

        Bundle args = new Bundle();
        args.putString(TextTag, text);
        args.putChar(SearchCharacterTag, searchChar);
        if (exerciseIconId != 0)
            args.putInt(ExerciseIconTag, exerciseIconId);
        fragment.setArguments(args);
        return fragment;
    }

    public FindCharacterFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        final View fragmentView = inflater.inflate(R.layout.fragment_find_character, container, false);
        try
        {
            calculateDimensions(fragmentView);
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView, inflater);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , exp);
            AlertDialogHelper.showMessageBox(getActivity()
                    , getResources().getString(R.string.error_unknown_error)
                    , getResources().getString(R.string.alert_title)
                    , false
                    , new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
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

        m_stepCallback = (IExerciseStepCallback)activity;
        m_exerciseScoreNotificator = (IScoreNotification)activity;

        m_displayMetrics = new DisplayMetricsHelper(activity);

        if (activity instanceof ITracerGetter)
            m_tracer = ((ITracerGetter)activity).getTracer();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        m_tracer = null;
        m_displayMetrics = null;

        m_stepCallback = null;
        m_exerciseScoreNotificator = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        clearImages(getView());
        //m_mediaPlayerManager.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        View fragmentView = getView();
        if (fragmentView != null)
            drawImages(fragmentView);
        //m_mediaPlayerManager.resume();
    }

    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        // colors
        NoSelectionColor = getResources().getColor(R.color.no_selection);

        // restore internal state
        final Bundle arguments = getArguments();
        final String exerciseText = arguments.getString(TextTag);
        if (exerciseText == null)
            throw new CommonException(CommonResultCode.InvalidArgument);
        m_text = exerciseText.split(Constant.NewLineDelimiter);
        m_searchCharacter = arguments.getChar(SearchCharacterTag);
        m_exerciseIconId = arguments.getInt(ExerciseIconTag, 0);

        final int maxRowLength = Helpers.getMaxRowLength(m_text);
        if (maxRowLength > MaxCharactersInRowCount)
        {
            ProductTracer.traceMessage(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , String.format("Exceeded row length, current '%d', max '%d'", maxRowLength, MaxCharactersInRowCount));
            throw new CommonException(CommonResultCode.InvalidArgument);
        }

        final int totalElementsCount = maxRowLength * (m_text.length - 1) + m_text[m_text.length - 1].length();
        if (savedInstanceState != null)
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        else
            m_state = new InternalState(ExerciseStage.Active, totalElementsCount, maxRowLength);
    }

    void constructUserInterface(@NonNull View fragmentView, @NonNull LayoutInflater inflater) throws CommonException
    {
        final int totalElementsCount = m_state.elementsSelection.getElementsCount();
        final int maxRowLength = m_state.elementsSelection.getColumnsCount();

        RelativeLayout[] elements = new RelativeLayout[totalElementsCount];
        for (int elementIndex = 0; elementIndex < elements.length; ++elementIndex)
        {
            elements[elementIndex] = (RelativeLayout) inflater.inflate(R.layout.framed_text_item, null, false);
            final int rowIndex = elementIndex / maxRowLength;
            final int columnIndex = elementIndex % maxRowLength;

            if ((columnIndex < m_text[rowIndex].length()) &&
                    (!Character.isWhitespace(m_text[rowIndex].charAt(columnIndex))))
            {
                FramedTextItem.setText(elements[elementIndex], m_text[rowIndex].charAt(columnIndex));

                elements[elementIndex].setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        onItemSelected((LinearLayout) v, rowIndex, columnIndex);
                    }
                });
            }
        }

        m_uiElements = new MatrixAccessor<>(elements, maxRowLength);

        // process selection
        for (int columnIndex = 0; columnIndex < m_uiElements.getColumnsCount(); ++columnIndex)
        {
            for (int rowIndex = 0; rowIndex < m_uiElements.getRowsCount(); ++rowIndex)
            {
                if (m_state.stage == ExerciseStage.Active)
                {
                    markCharacterElement(rowIndex, columnIndex, SelectionType.NoSelection);
                }
                else
                {
                    final boolean isCorrect = m_state.elementsSelection.get(rowIndex, columnIndex) ==
                            (m_text[rowIndex].charAt(columnIndex) == m_searchCharacter);
                    markCharacterElement(rowIndex, columnIndex
                            , isCorrect ? SelectionType.Correct : SelectionType.InCorrect);
                }
            }
        }

        // put characters
        {
            final int elementWidth = m_displayMetrics.getWidthInProportionPx(1.0 / MaxCharactersInRowCount,
                    2 * (int) getResources().getDimension(R.dimen.small_margin));
            final int gridWidth = maxRowLength * elementWidth;

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
            ImageView backImageView = (ImageView) fragmentView.findViewById(R.id.backImageView);
            backImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onBackButtonPressed();
                }
            });
        }

        {
            ImageView forwardImageView = (ImageView) fragmentView.findViewById(R.id.forwardImageView);
            forwardImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    onForwardButtonPressed();
                }
            });
        }

        // exercise title
        {
            TextView titleTextView = (TextView)fragmentView.findViewById(R.id.textInformationTextView);
            titleTextView.setText(String.format(getResources().getString(R.string.caption_find_character), m_searchCharacter));
        }
    }

    void calculateDimensions(@NonNull View fragmentView)
    {
        // TODO: auto calculation of all dimensions
    }

    void drawImages(@NonNull View fragmentView)
    {
        if (m_exerciseIconId != 0)
        {
            final ImageView exerciseIconImageView = (ImageView)fragmentView.findViewById(R.id.exerciseIconImageView);

            final Bitmap iconImage = ImageHelper.getImageForSpecifiedView(getResources()
                , m_exerciseIconId
                , exerciseIconImageView);
            exerciseIconImageView.setBackground(new BitmapDrawable(getResources(), iconImage));
        }
    }

    void clearImages(@NonNull View fragmentView)
    {
        final int backgroundColor = getResources().getColor(R.color.standard_background);
        fragmentView.findViewById(R.id.exerciseIconImageView).setBackgroundColor(backgroundColor);
    }

    public void markCharacterElement(int rowIndex, int columnIndex
            , @NonNull SelectionType selectionType) throws CommonException
    {
        RelativeLayout layout = m_uiElements.get(rowIndex, columnIndex);
        if (layout == null)
        {
            ProductTracer.traceMessage(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , String.format("Invalid rowIndex, columnIndex: '%d', '%d'", rowIndex, columnIndex));
            throw new CommonException(CommonResultCode.AssertError);
        }

        int color;
        switch (selectionType)
        {
            case NoSelection:
                color = NoSelectionColor;
                break;
            case Selected:
                color = SelectionColor;
                break;
            case Correct:
                color = CorrectSelectionColor;
                break;
            case InCorrect:
                color = IncorrectSelectionColor;
                break;
            default:
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Unknown selection type: '%s'", selectionType));
                throw new CommonException(CommonResultCode.AssertError);
            }
        }

        FramedTextItem.setTextColor(layout, color);
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
        try
        {
            if (m_state.stage == ExerciseStage.Active)
            {
                // TODO: place messagebox here
                m_state.stage = ExerciseStage.Processed;

                final String searchCharacter = ((Character) m_searchCharacter).toString();

                int mistakesCount = 0;
                for (int columnIndex = 0; columnIndex < m_uiElements.getColumnsCount(); ++columnIndex)
                {
                    for (int rowIndex = 0; rowIndex < m_uiElements.getRowsCount(); ++columnIndex)
                    {
                        if ((columnIndex < m_text[rowIndex].length()) &&
                                (!Character.isWhitespace(m_text[rowIndex].charAt(columnIndex))))
                        {
                            String currentCharacter = ((Character) m_text[rowIndex].charAt(columnIndex)).toString();
                            final boolean isCorrect = m_state.elementsSelection.get(rowIndex, columnIndex) ==
                                    currentCharacter.equalsIgnoreCase(searchCharacter);
                            markCharacterElement(rowIndex, columnIndex
                                    , isCorrect ? SelectionType.Correct : SelectionType.InCorrect);

                            if (!isCorrect)
                                ++mistakesCount;
                        }
                    }
                }

                final double completionRate = mistakesCount > MaxMistakesCount
                        ? 0 : 100 * ((MaxMistakesCount - mistakesCount) / (double)MaxMistakesCount);
                m_exerciseScoreNotificator.setCompletionRate(completionRate);
            }
            else
            {
                m_stepCallback.processNextStep();
            }
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
        }
    }

    public void onItemSelected(@NonNull LinearLayout view, int rowId, int columnId)
    {
        if (m_state.stage != ExerciseStage.Active)
            return;

        final boolean isSelected = m_state.elementsSelection.get(rowId, columnId);
        int colorId = isSelected ? NoSelectionColor : SelectionColor;
        view.setBackgroundColor(getResources().getColor(colorId));
        m_state.elementsSelection.set(rowId, columnId, !isSelected);
    }

    private ITracer m_tracer;
    private DisplayMetricsHelper m_displayMetrics;
    private IExerciseStepCallback m_stepCallback;
    private IScoreNotification m_exerciseScoreNotificator;

    private String[] m_text;
    private char m_searchCharacter;
    private int m_exerciseIconId;

    private InternalState m_state;
    private MatrixAccessor<RelativeLayout> m_uiElements;
}
