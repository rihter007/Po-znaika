package ru.po_znaika.alphabet;

import java.util.List;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.os.Bundle;
import android.app.Fragment;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.ru.po_znaika.common.helpers.ProcessUrl;
import ru.po_znaika.common.ru.po_znaika.common.helpers.TextFormatBlock;
import ru.po_znaika.common.ru.po_znaika.common.helpers.TextFormatter;

/**
 * Describes theory page
 */
public class TheoryPageFragment extends Fragment
{
    private static final String LogTag = TheoryPageFragment.class.getName();

    private static final String TheoryTableIndexTag = "theory_table_index";
    private static final String ExerciseIconImageIdTag = "exercise_icon_image_id";
    private static final String InternalStateTag = "internal_state";

    public static TheoryPageFragment createFragment(int theoryPageDatabaseId, int exerciseIconImageId)
    {
        TheoryPageFragment pageFragment = new TheoryPageFragment();
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putInt(TheoryTableIndexTag, theoryPageDatabaseId);
        if (exerciseIconImageId != 0)
            fragmentArguments.putInt(ExerciseIconImageIdTag, exerciseIconImageId);
        pageFragment.setArguments(fragmentArguments);

        return pageFragment;
    }

    private static class TheoryPageState implements Parcelable
    {
        public int theoryImageResourceId;
        public String theoryImageRedirectUrl;
        public String theoryMessage;
        public int[] theorySoundsResourceId;

        public TheoryPageState()
        {
        }

        public TheoryPageState(@NonNull Parcel parcel)
        {
            theoryImageResourceId = parcel.readInt();
            theoryImageRedirectUrl = (String)parcel.readValue(String.class.getClassLoader());
            theoryMessage = (String)parcel.readValue(String.class.getClassLoader());

            final int soundsNumber = parcel.readInt();
            if (soundsNumber > 0)
            {
                this.theorySoundsResourceId = new int[soundsNumber];
                parcel.readIntArray(this.theorySoundsResourceId);
            }
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags)
        {
            out.writeInt(theoryImageResourceId);
            out.writeValue(theoryImageRedirectUrl);
            out.writeValue(theoryMessage);
            if (theorySoundsResourceId == null)
            {
                out.writeInt(0);
            }
            else
            {
                out.writeInt(theorySoundsResourceId.length);
                out.writeIntArray(theorySoundsResourceId);
            }
        }

        public static final Creator CREATOR = new Creator()
        {
            public TheoryPageState createFromParcel(Parcel in)
            {
                return new TheoryPageState(in);
            }

            public TheoryPageState[] newArray(int size)
            {
                return new TheoryPageState[size];
            }
        };
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_stepsCallback = (IExerciseStepCallback) activity;
        if (activity instanceof ITracerGetter)
            m_tracer = ((ITracerGetter)activity).getTracer();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_stepsCallback = null;
        m_tracer = null;
        m_theorySoundPlayer.stop();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        m_theorySoundPlayer.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        m_theorySoundPlayer.resume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_theory_page, container, false);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            AlertDialogHelper.showMessageBox(getActivity(),
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.failed_action),
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

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     * @throws com.arz_x.CommonException
    */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(getActivity(), false);
        m_theorySoundPlayer = new MediaPlayerManager(getActivity(), alphabetDatabase);

        Bundle fragmentArguments = getArguments();
        final int theoryTableIndex = fragmentArguments.getInt(TheoryTableIndexTag);
        m_exerciseIconResourceId = fragmentArguments.getInt(ExerciseIconImageIdTag, 0);

        ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, String.format("Theory table id: '%d'", theoryTableIndex));

        // Restore essential internal members
        if (savedInstanceState == null)
        {
            AlphabetDatabase.TheoryPageInfo theoryPageInfo = alphabetDatabase.getTheoryPageById(theoryTableIndex);
            if (theoryPageInfo == null)
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Failed to get theory page by '%d'", theoryTableIndex));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            if ((TextUtils.isEmpty(theoryPageInfo.imageName))
                    && (TextUtils.isEmpty(theoryPageInfo.message)))
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , "Both theory image and message are empty");
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            m_state = new TheoryPageState();
            if (!TextUtils.isEmpty(theoryPageInfo.imageName))
            {
                m_state.theoryImageResourceId = DatabaseHelpers.getDrawableIdByName(getResources()
                        , theoryPageInfo.imageName);
                if (m_state.theoryImageResourceId == 0)
                {
                    ProductTracer.traceMessage(m_tracer
                            , TraceLevel.Error
                            , LogTag
                            , String.format("Failed to get image resource id for '%s'", theoryPageInfo.imageName));
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }
            }

            if (theoryPageInfo.soundsName != null)
            {
                int[] theorySounds = new int[theoryPageInfo.soundsName.length];

                for (int i = 0; i < theoryPageInfo.soundsName.length; ++i)
                {
                    final String soundName = theoryPageInfo.soundsName[i];
                    if (TextUtils.isEmpty(soundName))
                    {
                        ProductTracer.traceMessage(m_tracer
                                , TraceLevel.Error
                                , LogTag
                                , "Empty sound name");
                        throw new CommonException(CommonResultCode.InvalidExternalSource);
                    }

                    final int theorySoundResourceId = DatabaseHelpers.getSoundIdByName(getResources(), soundName);
                    if (theorySoundResourceId == 0)
                    {
                        ProductTracer.traceMessage(m_tracer
                                , TraceLevel.Error
                                , LogTag
                                , String.format("Failed to get sound resource id for '%s'", soundName));
                        throw new CommonException(CommonResultCode.InvalidExternalSource);
                    }

                    theorySounds[i] = theorySoundResourceId;
                }

                m_state.theorySoundsResourceId = theorySounds;
            }

            m_state.theoryImageRedirectUrl = theoryPageInfo.imageRedirectUrl;
            m_state.theoryMessage = theoryPageInfo.message;
        }
        else
        {
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface(View fragmentView) throws CommonException
    {
        {
            ImageView exerciseIconImageView = (ImageView)fragmentView.findViewById(R.id.exerciseIconImageView);
            if (m_exerciseIconResourceId != 0)
                exerciseIconImageView.setImageDrawable(getResources().getDrawable(m_exerciseIconResourceId));
            else
                exerciseIconImageView.setVisibility(View.INVISIBLE);
        }

        // process image
        if (m_state.theoryImageResourceId != 0)
        {
            ImageView theoryImageView = (ImageView) fragmentView.findViewById(R.id.imageInformationImageView);

            // programmatically scale to square size
            {
                DisplayMetricsHelper displayMetricsHelper = new DisplayMetricsHelper(getActivity());
                final int imageHeight = displayMetricsHelper.getHeightInProportionPx(3 * (1.0 / 7.0), 0);
                final int imageWidth = displayMetricsHelper.getDisplayWidth()
                        - 2 * (int) getResources().getDimension(R.dimen.small_margin);

                ViewGroup.LayoutParams imageLayoutParams = theoryImageView.getLayoutParams();
                imageLayoutParams.height = imageLayoutParams.width = Math.min(imageHeight, imageWidth);
                theoryImageView.setLayoutParams(imageLayoutParams);
            }
            theoryImageView.setImageDrawable(getResources().getDrawable(m_state.theoryImageResourceId));

            if (!TextUtils.isEmpty(m_state.theoryImageRedirectUrl))
            {
                theoryImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            ProcessUrl.openUrl(getActivity(), m_state.theoryImageRedirectUrl);
                        }
                        catch (Exception exp)
                        {
                            ProductTracer.traceException(m_tracer
                                    , TraceLevel.Warning
                                    , LogTag
                                    , exp);
                            AlertDialogHelper.showMessageBox(getActivity(),
                                    getResources().getString(R.string.alert_title),
                                    getResources().getString(R.string.error_unknown_error));
                        }
                    }
                });
            }
        }
        else
        {
            ImageView theoryImageView = (ImageView) fragmentView.findViewById(R.id.imageInformationImageView);
            theoryImageView.setVisibility(View.INVISIBLE);

            ViewGroup.LayoutParams layoutParams = theoryImageView.getLayoutParams();
            layoutParams.height = layoutParams.width = 0;
            theoryImageView.setLayoutParams(layoutParams);
        }

        // process sound
        if (m_state.theorySoundsResourceId != null)
        {
            ImageView soundPlayImageView = (ImageView)fragmentView.findViewById(R.id.soundImageView);
            soundPlayImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    onPlaySoundButtonClick();
                }
            });

            onPlaySoundButtonClick();
        }
        else
        {
            ImageView soundPlayImageView = (ImageView)fragmentView.findViewById(R.id.soundImageView);
            soundPlayImageView.setVisibility(View.INVISIBLE);
        }

        // process text
        if (!TextUtils.isEmpty(m_state.theoryMessage))
        {
            TextView theoryTextView = (TextView) fragmentView.findViewById(R.id.textInformationTextView);
            theoryTextView.setText(null);
            final List<TextFormatBlock> formatBlocks = TextFormatter.processText(m_state.theoryMessage);

            for (TextFormatBlock formatBlock : formatBlocks)
            {
                Spannable spanText = new SpannableString(formatBlock.getText());
                if (formatBlock.isColorSet())
                {
                    spanText.setSpan(new ForegroundColorSpan(formatBlock.getARGBColor()),
                            0,
                            formatBlock.getText().length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                theoryTextView.append(spanText);
            }
        }
        else
        {
            TextView theoryTextView = (TextView) fragmentView.findViewById(R.id.textInformationTextView);
            theoryTextView.setVisibility(View.INVISIBLE);
        }

        // set buttons callbacks
        {
            {
                ImageView forwardButton = (ImageView) fragmentView.findViewById(R.id.forwardImageView);
                forwardButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        m_stepsCallback.processNextStep();
                    }
                });
            }

            {
                ImageView backButton = (ImageView) fragmentView.findViewById(R.id.backImageView);
                backButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        m_stepsCallback.processPreviousStep();
                    }
                });
            }
        }
    }

    private void onPlaySoundButtonClick()
    {
        if (m_state.theorySoundsResourceId == null)
            return;

        try
        {
            m_theorySoundPlayer.playSequentially(m_state.theorySoundsResourceId);
        }
        catch (CommonException exp)
        {
            ProductTracer.traceException(m_tracer
                    , TraceLevel.Assert
                    , LogTag
                    , exp);
        }
    }

    /* Performs switching between exercise steps */
    private IExerciseStepCallback m_stepsCallback;
    private TheoryPageState m_state;
    private int m_exerciseIconResourceId;
    private ITracer m_tracer;

    private IMediaPlayerManager m_theorySoundPlayer;
}
