package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
         */
        public int[] exercisesTryCount;

        /**
         * Incorrect variants selected by user
         */
        public boolean[] currentStepVariants;

        /**
         * Current exercise number
         */
        public int currentStepNumber;

        public ImageSelectionState(int exerciseCount)
        {
            exercisesTryCount = new int[exerciseCount];

            currentStepVariants = new boolean[ImagesCount];
            currentStepNumber = 0;
        }

        public ImageSelectionState(@NonNull Parcel _in)
        {
            {
                final int exerciseCount = _in.readInt();
                exercisesTryCount = new int[exerciseCount];
                _in.readIntArray(exercisesTryCount);
            }

            currentStepVariants = new boolean[4];
            _in.readBooleanArray(currentStepVariants);
            currentStepNumber = _in.readInt();
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel container, int flags)
        {
            container.writeInt(exercisesTryCount.length);
            container.writeIntArray(exercisesTryCount);

            container.writeBooleanArray(currentStepVariants);
            container.writeInt(currentStepNumber);
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

    private static final String LogTag = ImageSelectionFragment.class.getName();

    public static final int ImagesCount = 4;

    private static final int LayoutViewIds[] = new int[]
            {
                    R.id.topLeftLayout,
                    R.id.topRightLayout,
                    R.id.bottomLeftLayout,
                    R.id.bottomRightLayout
            };
    private static final int ImageViewIds[] = new int[]
            {
                    R.id.topLeftImageView,
                    R.id.topRightImageView,
                    R.id.bottomLeftImageView,
                    R.id.bottomRightImageView
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
            constructUserInterface(fragmentView, true);
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
    void constructUserInterface(@NonNull View fragmentView, boolean doSetUserInteraction) throws CommonException
    {
        final ImageSelectionSingleExerciseState currentExerciseInfo = m_exerciseStates.get(m_state.currentStepNumber);

        // place exercise caption
        {
            String newExerciseCaption = "";
            if (!TextUtils.isEmpty(currentExerciseInfo.exerciseTitle))
                newExerciseCaption = currentExerciseInfo.exerciseTitle;

            TextView exerciseCaption = (TextView) fragmentView.findViewById(R.id.exerciseCaptionTextView);
            exerciseCaption.setText(newExerciseCaption);
        }

        final float selectionImageSize = calculateSelectionImageSize(fragmentView);

        // set images
        for (int imageIndex = 0; imageIndex < LayoutViewIds.length; ++imageIndex)
        {
            View selectionView = fragmentView.findViewById(LayoutViewIds[imageIndex]);
            {
                ViewGroup.LayoutParams layoutParams = selectionView.getLayoutParams();
                layoutParams.height = layoutParams.width = (int) selectionImageSize;
                selectionView.setLayoutParams(layoutParams);
            }
            ImageView uiImage = (ImageView)fragmentView.findViewById(ImageViewIds[imageIndex]);

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
            if (doSetUserInteraction)
            {
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

        // process buttons
        if (doSetUserInteraction)
        {
            {
                Button forwardButton = (Button) fragmentView.findViewById(R.id.forwardButton);
                forwardButton.setOnClickListener(new View.OnClickListener()
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
                Button backButton = (Button) fragmentView.findViewById(R.id.backButton);
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

    float calculateSelectionImageSize(@NonNull View fragmentView)
    {
        final RelativeLayout layout = (RelativeLayout)fragmentView.findViewById(R.id.leftLayout);
        final ViewGroup.LayoutParams layoutParams = layout.getLayoutParams();

        final float imageMargin = getResources().getDimension(R.dimen.small_margin);
        if (layoutParams.height >= imageMargin + 2*layoutParams.width)
            return layoutParams.width;

        return (layoutParams.height - imageMargin) / 2;
    }

    @Override
    public void onPause()
    {
        super.onPause();

       m_mediaPlayerManager.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

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

    private void onImageSelected(int selectedImageId) throws CommonException
    {
        final ImageSelectionSingleExerciseState currentExercise = m_exerciseStates.get(m_state.currentStepNumber);

        if (m_state.currentStepVariants[selectedImageId])
            return;
        ++m_state.exercisesTryCount[m_state.currentStepNumber];

        final View fragmentView = getView();
        if (fragmentView == null)
        {
            Log.e(LogTag, "Fragment view is null");
            throw new CommonException(CommonResultCode.InvalidInternalState);
        }

        // Decide how to react on user answerIndex
        final boolean isCorrectAnswer = currentExercise.answerIndex == selectedImageId;
        if (isCorrectAnswer)
        {
            // set color
            {
                View linearLayoutView = fragmentView.findViewById(LayoutViewIds[selectedImageId]);
                linearLayoutView.setBackgroundColor(CorrectSelectionColor);
            }

            try
            {
                m_mediaPlayerManager.play(AlphabetDatabase.SoundType.Correct);
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
                View linearLayoutView = fragmentView.findViewById(LayoutViewIds[selectedImageId]);
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
                    Log.e(LogTag, "Failed to play sound hint " + exp.getMessage());
                }
            }
        }
    }

    private void processNextStep() throws CommonException
    {
        final View fragmentView = getView();
        if (fragmentView == null)
            throw new CommonException(CommonResultCode.InvalidInternalState);

        if (m_state.currentStepNumber >= m_exerciseStates.size() - 1)
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

            {
                for (int tryCount : m_state.exercisesTryCount)
                {
                    if (tryCount == 1)
                        resultScore += ScoreStep;
                    else if (tryCount == 2)
                        resultScore += ScoreStep / 2;
                    // if we got more than 2 tries... than 0 is the mark!!
                }
            }

            // remember results
            m_scoreNotification.setCompletionRate(resultScore);
        }
        else
        {
            ++m_state.currentStepNumber;
            constructUserInterface(fragmentView, false);
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
