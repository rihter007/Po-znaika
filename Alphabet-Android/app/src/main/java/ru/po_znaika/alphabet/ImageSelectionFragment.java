package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Fragment of correct image selection exercise
 */
public class ImageSelectionFragment extends Fragment
{
    /**
     * Represents state for image selection exercise fragment
     */
    private static class ImageSelectionState implements Parcelable
    {
        /**
         * The number of try after which correct variant was selected
         *
         */
        private int[] exercisesTryCount;

        /**
         * Incorrect variants selected by user
         */
        public boolean[] currentStepVariants;

        /**
         * Current exercise number
         */
        private int currentStepNumber;

        public ImageSelectionState(int exerciseCount)
        {
            this.exercisesTryCount = new int[exerciseCount];
            this.currentStepVariants = new boolean[ImagesCount];
            this.currentStepNumber = 0;
        }

        public ImageSelectionState(@NonNull Parcel _in)
        {
            {
                final int exerciseCount = _in.readInt();
                exercisesTryCount = new int[exerciseCount];
                _in.readIntArray(exercisesTryCount);
            }

            this.currentStepVariants = new boolean[4];
            _in.readBooleanArray(this.currentStepVariants);

            this.currentStepNumber = _in.readInt();
        }

        public int getCurrentStepNumber()
        {
            return this.currentStepNumber;
        }

        public void newStep()
        {
            ++this.currentStepNumber;
            for (int variantIndex = 0; variantIndex < this.currentStepVariants.length; ++variantIndex)
                this.currentStepVariants[variantIndex] = false;
        }

        public void incrementTriesCount()
        {
            ++this.exercisesTryCount[this.currentStepNumber];
        }

        public int getTriesCount()
        {
            return this.exercisesTryCount[this.currentStepNumber];
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel container, int flags)
        {
            container.writeInt(this.exercisesTryCount.length);
            container.writeIntArray(this.exercisesTryCount);
            container.writeBooleanArray(this.currentStepVariants);
            container.writeInt(this.currentStepNumber);
        }

        public static final Creator CREATOR = new Creator()
        {
            public ImageSelectionState createFromParcel(Parcel in)
            {
                return new ImageSelectionState(in);
            }

            public ImageSelectionState[] newArray(int size)
            {
                return new ImageSelectionState[size];
            }
        };
    }

    private static class SingleImageArea
    {
        public SingleImageArea() { }

        public SingleImageArea(int _layoutId, int _imageViewId, int _textViewId)
        {
            this.layoutId = _layoutId;
            this.imageViewId = _imageViewId;
            this.textViewId = _textViewId;
        }

        int layoutId;
        int imageViewId;
        int textViewId;
    }

    private static final String LogTag = ImageSelectionFragment.class.getName();

    public static final int ImagesCount = 4;

    public static final SingleImageArea ImageAreas[] = new SingleImageArea[]
            {
                    new SingleImageArea(R.id.topLeftLayout, R.id.topLeftImageView, R.id.topLeftHintTextView),
                    new SingleImageArea(R.id.topRightLayout, R.id.topRightImageView, R.id.topRightHintTextView),
                    new SingleImageArea(R.id.bottomLeftLayout, R.id.bottomLeftImageView, R.id.bottomLeftHintTextView),
                    new SingleImageArea(R.id.bottomRightLayout, R.id.bottomRightImageView, R.id.bottomRightHintTextView),
            };

    private static final String ExercisesTag = "image_selection_exercises";
    private static final String StateTag = "state";

    private static final int ScoreStep = 10;

    private static final int NoSelectionColor = Constant.Color.BackgroundBlue;
    private static final int CorrectSelectionColor = Constant.Color.LightGreen;
    private static final int IncorrectSelectionColor = Constant.Color.LightRed;

    public static ImageSelectionFragment createFragment(@NonNull Collection<ImageSelectionSingleExerciseState> selectionExercises)
            throws CommonException
    {
        for (ImageSelectionSingleExerciseState exerciseState : selectionExercises)
        {
            if ((exerciseState.selectionVariants == null) || (exerciseState.selectionVariants.length != ImagesCount))
                throw new CommonException(CommonResultCode.InvalidArgument);
        }
        // Image selection fragment
        ImageSelectionFragment resultFragment = new ImageSelectionFragment();

        {
            Bundle arguments = new Bundle();

            final ArrayList<ImageSelectionSingleExerciseState> exerciseStates = new ArrayList<>(selectionExercises);
            arguments.putParcelableArrayList(ExercisesTag, exerciseStates);
            resultFragment.setArguments(arguments);
        }

        return resultFragment;
    }

    public ImageSelectionFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_image_selection, container, false);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
            setUserInteractionControllers(fragmentView);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , exp);

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

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        CoreServiceLocator serviceLocator = new CoreServiceLocator(getActivity());
        m_mediaPlayerManager = serviceLocator.getMediaPlayerManager();

        m_exerciseStates = getArguments().getParcelableArrayList(ExercisesTag);
        if (savedInstanceState == null)
        {
            m_state = new ImageSelectionState(m_exerciseStates.size());
        }
        else
        {
            m_state = savedInstanceState.getParcelable(StateTag);
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface(@NonNull View fragmentView) throws CommonException
    {
        final ImageSelectionSingleExerciseState currentExerciseInfo = m_exerciseStates.get(m_state.getCurrentStepNumber());

        // place exercise caption
        {
            String newExerciseCaption = "";
            if (!TextUtils.isEmpty(currentExerciseInfo.exerciseTitle))
                newExerciseCaption = currentExerciseInfo.exerciseTitle;

            TextView exerciseCaption = (TextView) fragmentView.findViewById(R.id.exerciseCaptionTextView);
            exerciseCaption.setText(newExerciseCaption);
        }

        for (int imageIndex = 0; imageIndex < ImageAreas.length; ++imageIndex)
        {
            // process main single image area view
            {
                View selectionView = fragmentView.findViewById(ImageAreas[imageIndex].layoutId);

                final boolean isVariantProcessed = m_state.currentStepVariants[imageIndex];
                if (isVariantProcessed)
                {
                    if (currentExerciseInfo.answerIndex == imageIndex)
                        selectionView.setBackgroundColor(CorrectSelectionColor);
                    else
                        selectionView.setBackgroundColor(IncorrectSelectionColor);
                }
                else
                {
                    selectionView.setBackgroundColor(NoSelectionColor);
                }
            }

            // process hint text
            {
                TextView hintTextView = (TextView) fragmentView.findViewById(ImageAreas[imageIndex].textViewId);
                final int wordLength = currentExerciseInfo.selectionVariants[imageIndex].name.length();

                String hintText = "";
                if (wordLength > 0)
                {
                    hintText = "_";
                    for (int wordIndex = 1; wordIndex < wordLength; ++wordIndex)
                        hintText += " _";
                }
                hintTextView.setText(hintText);
            }
        }

        // draw object images
        drawImages(fragmentView);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        clearImages(getView());
        m_mediaPlayerManager.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        try
        {
            View fragmentView = getView();
            if (fragmentView != null)
                drawImages(fragmentView);
        }
        catch (CommonException exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
        }
        m_mediaPlayerManager.resume();
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_stepsCallback = (IExerciseStepCallback) activity;
        m_scoreNotification = (IScoreNotification) activity;

        m_displayMetrics = new DisplayMetricsHelper(activity);

        if (activity instanceof ITracerGetter)
            m_tracer = ((ITracerGetter)activity).getTracer();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_stepsCallback = null;
        m_scoreNotification = null;
        m_displayMetrics = null;
        m_tracer = null;

        m_mediaPlayerManager.stop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(StateTag, m_state);
    }

    private void drawImages(@NonNull View fragmentView) throws CommonException
    {
        final ImageSelectionSingleExerciseState currentExerciseInfo = m_exerciseStates.get(m_state.currentStepNumber);

        // process image
        for (int imageIndex = 0; imageIndex < ImageAreas.length; ++imageIndex)
        {
            ImageView uiImage = (ImageView) fragmentView.findViewById(ImageAreas[imageIndex].imageViewId);
            final int imageResourceId = DatabaseHelpers.getDrawableIdByName(getResources()
                    , currentExerciseInfo.selectionVariants[imageIndex].imageFilePath);
            if (imageResourceId == DatabaseConstant.InvalidDatabaseIndex)
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Failed to get resources id for '%s'"
                        , currentExerciseInfo.selectionVariants[imageIndex].imageFilePath));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            uiImage.setImageDrawable(getResources().getDrawable(imageResourceId));
        }
    }

    private void clearImages(@NonNull View fragmentView)
    {
        final int backgroundColor = getResources().getColor(R.color.standard_background);
        for (int imageIndex = 0; imageIndex < ImageAreas.length; ++imageIndex)
            fragmentView.findViewById(ImageAreas[imageIndex].imageViewId).setBackgroundColor(backgroundColor);
    }

    private void setUserInteractionControllers(@NonNull View fragmentView)
    {
        for (int imageIndex = 0; imageIndex < ImageAreas.length; ++imageIndex)
        {
            ImageView uiImage = (ImageView) fragmentView.findViewById(ImageAreas[imageIndex].imageViewId);
            final int imageViewId = imageIndex;
            uiImage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try
                    {
                        onImageSelected(imageViewId);
                    }
                    catch (CommonException exp)
                    {
                        ProductTracer.traceException(m_tracer
                                , TraceLevel.Error
                                , LogTag
                                , exp);
                        getActivity().finish();
                    }
                }
            });
        }

        {
            ImageView soundImageView = (ImageView)fragmentView.findViewById(R.id.soundImageView);
            soundImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    // TODO: Add exercise caption
                    //m_mediaPlayerManager.
                }
            });
        }

        {
            ImageView forwardImageView = (ImageView) fragmentView.findViewById(R.id.forwardImageView);
            forwardImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    try
                    {
                        processNextStep();
                    }
                    catch (CommonException exp)
                    {
                        AlertDialogHelper.showMessageBox(getActivity()
                                , getResources().getString(R.string.alert_title)
                                , getResources().getString(R.string.error_unknown_error)
                                , false
                                , new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                getActivity().finish();
                            }
                        });
                        ProductTracer.traceException(m_tracer
                                , TraceLevel.Error
                                , LogTag
                                , exp);
                    }
                }
            });
        }

        {
            ImageView backImageView = (ImageView) fragmentView.findViewById(R.id.backImageView);
            backImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    m_stepsCallback.processPreviousStep();
                }
            });
        }
    }

    private void onImageSelected(int selectedImageId) throws CommonException
    {
        final ImageSelectionSingleExerciseState currentExercise = m_exerciseStates.get(m_state.currentStepNumber);

        if (m_state.currentStepVariants[selectedImageId])
            return;
        m_state.incrementTriesCount();
        m_state.currentStepVariants[selectedImageId] = true;

        final View fragmentView = getView();
        if (fragmentView == null)
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Fragment view is null");
            throw new CommonException(CommonResultCode.InvalidInternalState);
        }

        // Decide how to react on user answerIndex
        final boolean isCorrectAnswer = currentExercise.answerIndex == selectedImageId;
        if (isCorrectAnswer)
        {
            // set color
            {
                View linearLayoutView = fragmentView.findViewById(ImageAreas[selectedImageId].layoutId);
                linearLayoutView.setBackgroundColor(CorrectSelectionColor);
            }

            try
            {
                m_mediaPlayerManager.play(AlphabetDatabase.SoundType.Cheer);
            }
            catch (CommonException exp)
            {
                ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            }
        }
        else
        {
            // set color
            {
                View linearLayoutView = fragmentView.findViewById(ImageAreas[selectedImageId].layoutId);
                linearLayoutView.setBackgroundColor(IncorrectSelectionColor);
            }

            // sound hint
            if (!TextUtils.isEmpty(currentExercise.selectionVariants[selectedImageId].soundFilePath))
            {
                try
                {
                    final int soundResourceId = DatabaseHelpers.getSoundIdByName(getResources(),
                            currentExercise.selectionVariants[selectedImageId].soundFilePath);
                    if (soundResourceId == 0)
                        throw new CommonException(CommonResultCode.InvalidArgument);
                    m_mediaPlayerManager.play(soundResourceId);
                }
                catch (CommonException exp)
                {
                    ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
                }
            }
            else
            {
                m_mediaPlayerManager.play(AlphabetDatabase.SoundType.TryAgain);
            }
        }
    }

    private void processNextStep() throws CommonException
    {
        final View fragmentView = getView();
        if (fragmentView == null)
            throw new CommonException(CommonResultCode.InvalidInternalState);

        if (m_state.getCurrentStepNumber() >= m_exerciseStates.size() - 1)
        {
            // rename button
            {
                Button nextButton = (Button) fragmentView.findViewById(R.id.forwardButton);
                nextButton.setText(getResources().getString(R.string.caption_finish));
                nextButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        m_stepsCallback.processNextStep();
                    }
                });
            }
            // calculate results
            int resultScore = 0;

            /// TODO: process scores according to the latest algorithm
            /*{
                for (int tryCount : m_state.exercisesTryCount)
                {
                    if (tryCount == 1)
                        resultScore += ScoreStep;
                    else if (tryCount == 2)
                        resultScore += ScoreStep / 2;
                    // if we got more than 2 tries... than 0 is the mark!!
                }
            }*/

            // remember results
            m_scoreNotification.setCompletionRate(resultScore);
        }
        else
        {
            m_state.newStep();
            constructUserInterface(fragmentView);
        }
    }

    private ITracer m_tracer;
    private DisplayMetricsHelper m_displayMetrics;
    private IMediaPlayerManager m_mediaPlayerManager;

    private IExerciseStepCallback m_stepsCallback;
    private IScoreNotification m_scoreNotification;

    private List<ImageSelectionSingleExerciseState> m_exerciseStates;
    private ImageSelectionState m_state;
}
