package ru.po_znaika.alphabet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Fragment of correct image selection exercise
 *
 */
public class ImageSelectionFragment extends Fragment
{
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
    private static final int TextViewIds[] = new int[]
            {
                    R.id.topLeftTextView,
                    R.id.topRightTextView,
                    R.id.bottomLeftTextView,
                    R.id.bottomRightTextView
            };

    private static final String ExercisesTag = "image_selection_exercises";
    private static final String StateTag = "state";

    private static final int ScoreStep = 10;

    public static ImageSelectionFragment CreateFragment(ArrayList<ImageSelectionSingleExerciseState> singleSelectionExercises)
    {
        // Image selection fragment
        ImageSelectionFragment resultFragment = new ImageSelectionFragment();

        {
            Bundle arguments = new Bundle();

            arguments.putParcelableArrayList(ExercisesTag, singleSelectionExercises);

            resultFragment.setArguments(arguments);
        }

        return resultFragment;
    }

    public ImageSelectionFragment()
    {
        // Required empty public constructor
    }

    /**
     * Restores all internal objects
     * @param savedInstanceState activity saved state
     */
    void restoreInternalState(Bundle savedInstanceState) throws IOException
    {
        m_alphabetDatabase = new AlphabetDatabase(getActivity(), false);

        if (savedInstanceState == null)
        {
            Bundle arguments = getArguments();

            List<ImageSelectionSingleExerciseState> exerciseStates = arguments.getParcelableArrayList(ExercisesTag);

            m_state = new ImageSelectionState(exerciseStates);
        }
        else
        {
            m_state = savedInstanceState.getParcelable(StateTag);
        }

        m_mediaPlayer = null;
        m_isPlayerResumed = false;
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface(View fragmentView, boolean doSetUserInteraction)
    {
        ImageSelectionSingleExerciseState currentExerciseInfo = m_state.exerciseStates[m_state.currentExerciseStepNumber];

        // place exercise title
        {
            String newExerciseCaption = "";
            if (!TextUtils.isEmpty(currentExerciseInfo.exerciseTitle))
                newExerciseCaption = currentExerciseInfo.exerciseTitle;

            TextView exerciseCaption = (TextView) fragmentView.findViewById(R.id.exerciseCaptionTextView);
            exerciseCaption.setText(newExerciseCaption);
        }

        // place images
        {
            if (currentExerciseInfo.objects.length != ImagesCount)
                throw new IllegalArgumentException();

            ImageView[] uiImages = new ImageView[]
                    {
                            (ImageView) fragmentView.findViewById(R.id.topLeftImageView),
                            (ImageView) fragmentView.findViewById(R.id.topRightImageView),
                            (ImageView) fragmentView.findViewById(R.id.bottomLeftImageView),
                            (ImageView) fragmentView.findViewById(R.id.bottomRightImageView)
                    };

            for (int imageIndex = 0; imageIndex < currentExerciseInfo.objects.length; ++imageIndex)
            {
                uiImages[imageIndex].setImageDrawable(getResources().getDrawable(currentExerciseInfo.objects[imageIndex].imageResourceIndex));

                if (doSetUserInteraction)
                {
                    final int ImageViewId = imageIndex;
                    uiImages[imageIndex].setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            OnImageSelected(ImageViewId);
                        }
                    });
                }
            }
        }

        // clear images text hints
        {
            TextView hintTextViews[] = new TextView[]
                {
                        (TextView) fragmentView.findViewById(R.id.topLeftTextView),
                        (TextView) fragmentView.findViewById(R.id.topRightTextView),
                        (TextView) fragmentView.findViewById(R.id.bottomLeftTextView),
                        (TextView) fragmentView.findViewById(R.id.bottomRightTextView)
                };

            for (TextView hintTextView : hintTextViews)
            {
                hintTextView.setText("");
            }
        }

        // clear selection
        {
            View selectionViews[] = new View[]
                    {
                            fragmentView.findViewById(R.id.topLeftLayout),
                            fragmentView.findViewById(R.id.topRightLayout),
                            fragmentView.findViewById(R.id.bottomLeftLayout),
                            fragmentView.findViewById(R.id.bottomRightLayout)
                    };

            for (View hintTextView : selectionViews)
            {
                hintTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
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

        if ((m_mediaPlayer != null) && (m_mediaPlayer.isPlaying()))
        {
            m_isPlayerResumed = true;
            m_mediaPlayer.pause();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if ((m_mediaPlayer != null) && (m_isPlayerResumed))
        {
            m_isPlayerResumed = false;
            m_mediaPlayer.start();
        }
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

        if (m_mediaPlayer != null)
        {
            m_mediaPlayer.stop();
            m_mediaPlayer.release();
        }
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
            AlertDialog msgBox = MessageBox.CreateDialog(getActivity(), resources.getString(R.string.failed_exercise_step),
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
        m_stepsCallback.processNextStep();
    }

    private void onBackButtonClick()
    {
        m_stepsCallback.processPreviousStep();
    }

    private void OnImageSelected(int selectedImageId)
    {
        // increment tries count
        m_state.exercisesTryCount[m_state.currentExerciseStepNumber]++;

        ImageSelectionSingleExerciseState currentExercise = m_state.exerciseStates[m_state.currentExerciseStepNumber];

        // Decide how to react on user answer
        if (m_mediaPlayer != null)
        {
            m_mediaPlayer.stop();
            m_mediaPlayer.release();
        }
        m_mediaPlayer = null;

        final boolean IsCorrectAnswer = currentExercise.answer == selectedImageId;
        if (IsCorrectAnswer)
        {
            // set color
            {
                View linearLayoutView = getView().findViewById(LayoutViewIds[selectedImageId]);
                linearLayoutView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            }

            final String CorrectSoundFileName = m_alphabetDatabase.getRandomSoundFileNameByType(AlphabetDatabase.SoundType.Correct);
            if (!TextUtils.isEmpty(CorrectSoundFileName))
            {
                final int ResourceId = getResources().getIdentifier(CorrectSoundFileName, Constant.RawResourcesTag, getActivity().getPackageName());
                if (ResourceId != 0)
                {
                    m_mediaPlayer = MediaPlayer.create(getActivity(), ResourceId);
                }
            }
        }
        else
        {
            // set color
            {
                View linearLayoutView = getView().findViewById(LayoutViewIds[selectedImageId]);
                linearLayoutView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            }

            // sound hint
            if (currentExercise.objects[selectedImageId].soundResourceIndex != 0)
            {
                m_mediaPlayer = MediaPlayer.create(getActivity(), currentExercise.objects[selectedImageId].soundResourceIndex);
            }

            // text hint
            if (!TextUtils.isEmpty(currentExercise.objects[selectedImageId].name))
            {
                TextView hintTextView = (TextView)getView().findViewById(TextViewIds[selectedImageId]);
                hintTextView.setText(currentExercise.objects[selectedImageId].name);
            }
        }

        m_isPlayerResumed = false;
        if (m_mediaPlayer != null)
        {
            m_mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
            {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer)
                {
                    if (IsCorrectAnswer)
                        processNextStep();
                }
            });
            m_mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener()
            {
                @Override
                public boolean onError(MediaPlayer mediaPlayer, int i, int i2)
                {
                    if (IsCorrectAnswer)
                        processNextStep();
                    return false;
                }
            });
            m_mediaPlayer.start();
        }

        if (m_mediaPlayer == null)
            processNextStep();
    }

    private void processNextStep()
    {
        if (m_state.currentExerciseStepNumber == m_state.exerciseStates.length - 1)
        {
            // rename button
            {
                Button nextButton = (Button) getView().findViewById(R.id.forwardButton);
                nextButton.setText(getResources().getString(R.string.caption_finish));
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
            m_state.currentExerciseStepNumber++;
            constructUserInterface(getView(), false);
        }
    }

    private AlphabetDatabase m_alphabetDatabase;

    private IExerciseStepCallback m_stepsCallback;
    private IScoreNotification m_scoreNotification;

    private ImageSelectionState m_state;

    private MediaPlayer m_mediaPlayer;
    private boolean m_isPlayerResumed;
}
