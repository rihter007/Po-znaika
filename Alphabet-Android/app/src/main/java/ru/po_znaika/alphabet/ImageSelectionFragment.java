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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Fragment of correct image selection exercise
 *
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

            currentStepVariants = new boolean[exerciseCount];
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
    private static final int HintTextViewIds[] = new int[]
            {
                    R.id.topLeftTextView,
                    R.id.topRightTextView,
                    R.id.bottomLeftTextView,
                    R.id.bottomRightTextView
            };


    private static final String ExercisesTag = "image_selection_exercises";
    private static final String StateTag = "state";

    private static final int ScoreStep = 10;

    private static final int NoSelectionColorId = android.R.color.transparent;
    private static final int CorrectSelectionColorId = android.R.color.holo_green_light;
    private static final int IncorrectSelectionColorId = android.R.color.holo_red_light;

    public static ImageSelectionFragment createFragment(@NonNull Collection<ImageSelectionSingleExerciseState> singleSelectionExercises)
            throws CommonException
    {
        if (singleSelectionExercises.size() != ImagesCount)
            throw new CommonException(CommonResultCode.InvalidArgument);

        // Image selection fragment
        ImageSelectionFragment resultFragment = new ImageSelectionFragment();

        {
            Bundle arguments = new Bundle();

            final ArrayList<ImageSelectionSingleExerciseState> exerciseStates = new ArrayList<>(singleSelectionExercises);
            arguments.putParcelableArrayList(ExercisesTag, exerciseStates);
            resultFragment.setArguments(arguments);
        }

        return resultFragment;
    }

    public ImageSelectionFragment()
    {
        // Required empty public constructor
    }

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        m_serviceLocator = new CoreServiceLocator(getActivity());
        m_mediaPlayerManager = m_serviceLocator.getMediaPlayerManager();

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
    void constructUserInterface(@NonNull View fragmentView, boolean doSetUserInteraction)
    {
        final ImageSelectionSingleExerciseState currentExerciseInfo = m_exerciseStates.get(m_state.currentStepNumber);

        // place exercise title
        {
            String newExerciseCaption = "";
            if (!TextUtils.isEmpty(currentExerciseInfo.exerciseTitle))
                newExerciseCaption = currentExerciseInfo.exerciseTitle;

            TextView exerciseCaption = (TextView) fragmentView.findViewById(R.id.exerciseCaptionTextView);
            exerciseCaption.setText(newExerciseCaption);
        }

        // set images + hints + selection
        for (int imageIndex = 0; imageIndex < LayoutViewIds.length; ++imageIndex)
        {
            ImageView uiImage = (ImageView)fragmentView.findViewById(ImageViewIds[imageIndex]);
            View selectionView = fragmentView.findViewById(LayoutViewIds[imageIndex]);
            TextView hintTextView = (TextView)fragmentView.findViewById(HintTextViewIds[imageIndex]);

            uiImage.setImageDrawable(getResources().
                    getDrawable(currentExerciseInfo.selectionVariants[imageIndex].imageResourceIndex));
            if (doSetUserInteraction)
            {
                final int ImageViewId = imageIndex;
                uiImage.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onImageSelected(ImageViewId);
                    }
                });
            }

            final boolean isVariantProcessed = m_state.currentStepVariants[imageIndex];
            if (isVariantProcessed)
            {
                if (currentExerciseInfo.answer == imageIndex)
                    selectionView.setBackgroundColor(CorrectSelectionColorId);
                else
                {
                    selectionView.setBackgroundColor(IncorrectSelectionColorId);

                    String objectName = currentExerciseInfo.selectionVariants[imageIndex].name;
                    if (objectName == null)
                        objectName = "";
                    hintTextView.setText(objectName);
                }
            }
            else
            {
                selectionView.setBackgroundColor(NoSelectionColorId);
                hintTextView.setText("");
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
                        onForwardButtonClick();
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
                        onBackButtonClick();
                    }
                });
            }
        }
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
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_stepsCallback = null;

        m_mediaPlayerManager.stop();
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
            Resources resources = getResources();
            AlertDialog msgBox = MessageBox.CreateDialog(getActivity(), resources.getString(R.string.failed_action),
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

    private void onForwardButtonClick()
    {
        processNextStep();
    }

    private void onBackButtonClick()
    {
        m_stepsCallback.processPreviousStep();
    }

    private void onImageSelected(int selectedImageId)
    {
        final ImageSelectionSingleExerciseState currentExercise = m_exerciseStates.get(m_state.currentStepNumber);

        if (m_state.currentStepVariants[selectedImageId])
            return;
        ++m_state.exercisesTryCount[m_state.currentStepNumber];

        // Decide how to react on user answer
        final boolean isCorrectAnswer = currentExercise.answer == selectedImageId;
        if (isCorrectAnswer)
        {
            // set color
            {
                View linearLayoutView = getView().findViewById(LayoutViewIds[selectedImageId]);
                linearLayoutView.setBackgroundColor(getResources().getColor(CorrectSelectionColorId));
            }

            try
            {
                m_mediaPlayerManager.play(AlphabetDatabase.SoundType.Correct);
            }
            catch (CommonException exp)
            {
                Log.e(LogTag, "Failed to play correct media " + exp.getMessage());
            }
        }
        else
        {
            // set color
            {
                View linearLayoutView = getView().findViewById(LayoutViewIds[selectedImageId]);
                linearLayoutView.setBackgroundColor(getResources().getColor(IncorrectSelectionColorId));
            }

            // sound hint
            if (currentExercise.selectionVariants[selectedImageId].soundResourceIndex != 0)
            {
                try
                {
                    m_mediaPlayerManager.play(currentExercise.selectionVariants[selectedImageId].soundResourceIndex);
                }
                catch (CommonException exp)
                {
                    Log.e(LogTag, "Failed to play sound hint " + exp.getMessage());
                }
            }

            // text hint
            if (!TextUtils.isEmpty(currentExercise.selectionVariants[selectedImageId].name))
            {
                TextView hintTextView = (TextView)getView().findViewById(HintTextViewIds[selectedImageId]);
                hintTextView.setText(currentExercise.selectionVariants[selectedImageId].name);
            }
        }
    }

    private void processNextStep()
    {
        if (m_state.currentStepNumber >= m_exerciseStates.size() - 1)
        {
            // rename button
            {
                Button nextButton = (Button) getView().findViewById(R.id.forwardButton);
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
            m_scoreNotification.setScore(resultScore);
        }
        else
        {
            ++m_state.currentStepNumber;
            constructUserInterface(getView(), false);
        }
    }

    private CoreServiceLocator m_serviceLocator;
    private IMediaPlayerManager m_mediaPlayerManager;

    private IExerciseStepCallback m_stepsCallback;
    private IScoreNotification m_scoreNotification;

    private List<ImageSelectionSingleExerciseState> m_exerciseStates;
    private ImageSelectionState m_state;
}
