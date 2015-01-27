package ru.po_znaika.alphabet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.database.DatabaseConstant;
import ru.po_znaika.database.alphabet.AlphabetDatabase;
import ru.po_znaika.server_feedback.IServerFeedback;
import ru.po_znaika.server_feedback.ServerCacheFeedback;

public class CharacterExerciseItemActivity extends Activity implements IExerciseStepCallback, IScoreNotification
{
    private static class SoundObjectExerciseContainer
    {
        public int soundFlag;
        public int exercisesCount;
        public int exerciseTitleResourceId;

        public SoundObjectExerciseContainer(int _soundFlag, int _exercisesCount, int _exerciseTitleResourceId)
        {
            this.soundFlag = _soundFlag;
            this.exercisesCount = _exercisesCount;
            this.exerciseTitleResourceId = _exerciseTitleResourceId;
        }
    }

    private static final String StateTag = "State";
    private static final String FragmentTag = "MainWindowFragment";

    private class SoundGeneralExerciseFactory implements CharacterExerciseStepManager.ICustomExerciseStepFactory
    {
        private static final int ContainsSoundImageExercisesCount = 1;
        private static final int BeginsSoundImageExercisesCount = 1;
        private static final int EndsSoundImageExercisesCount = 1;

        public Fragment createExerciseStep(CharacterExerciseItemStepState data)
        {
            Fragment resultFragment = null;

            try
            {
                switch (data.value)
                {
                    case 0:
                        // Multiple objects fragment
                        resultFragment = CharacterMultipleObjectsFragment.CreateFragment(m_state.characterExerciseId, m_state.exerciseCharacter);
                        break;

                    case 1:

                        ArrayList<ImageSelectionSingleExerciseState> imageSelectionExercises = new ArrayList<ImageSelectionSingleExerciseState>();

                        final SoundObjectExerciseContainer[] soundObjectExercises = new SoundObjectExerciseContainer[]
                                {
                                        new SoundObjectExerciseContainer(AlphabetDatabase.SoundObjectInfo.Contain, ContainsSoundImageExercisesCount, R.string.caption_sound_object_contains_selection),
                                        new SoundObjectExerciseContainer(AlphabetDatabase.SoundObjectInfo.Begin, BeginsSoundImageExercisesCount, R.string.caption_sound_object_begins_selection),
                                        new SoundObjectExerciseContainer(AlphabetDatabase.SoundObjectInfo.End, EndsSoundImageExercisesCount, R.string.caption_sound_object_ends_selection)
                                };

                        Random rand = new Random(System.currentTimeMillis());
                        Resources resources = getResources();
                        for (SoundObjectExerciseContainer soundObjectExercise : soundObjectExercises)
                        {
                            final int ThisSoundObjectsCount = soundObjectExercise.exercisesCount;
                            final int OtherSoundObjectsCount = ThisSoundObjectsCount * (ImageSelectionFragment.ImagesCount - ThisSoundObjectsCount);

                            AlphabetDatabase.SoundObjectInfo thisSoundObjects[] = m_alphabetDatabase.getCharacterSoundObjectsByCharacterExerciseIdAndMatchFlag(
                                    m_state.characterExerciseId, soundObjectExercise.soundFlag, ThisSoundObjectsCount);
                            if ((thisSoundObjects == null) || (thisSoundObjects.length != ThisSoundObjectsCount))
                                throw new IllegalStateException();

                            AlphabetDatabase.SoundObjectInfo otherSoundObjects[] = m_alphabetDatabase.getCharacterSoundObjectsByCharacterExerciseIdAndNotMatchFlag(
                                    m_state.characterExerciseId, soundObjectExercise.soundFlag, OtherSoundObjectsCount);
                            if ((otherSoundObjects == null) || (otherSoundObjects.length != OtherSoundObjectsCount))
                                throw new IllegalStateException();

                            for (int subExerciseIndex = 0; subExerciseIndex < ThisSoundObjectsCount; ++subExerciseIndex)
                            {
                                ImageSelectionSingleExerciseState singleExerciseState = new ImageSelectionSingleExerciseState();

                                singleExerciseState.exerciseTitle = String.format(resources.getString(soundObjectExercise.exerciseTitleResourceId), m_state.exerciseCharacter);
                                singleExerciseState.objects = new ObjectDescription[ImageSelectionFragment.ImagesCount];
                                singleExerciseState.answer = rand.nextInt(ImageSelectionFragment.ImagesCount);

                                int otherObjectsIndex = 0;
                                for (int imageIndex = 0; imageIndex < ImageSelectionFragment.ImagesCount; ++imageIndex)
                                {
                                    int imageIdentifier = DatabaseConstant.InvalidDatabaseIndex;
                                    int soundIdentifier = DatabaseConstant.InvalidDatabaseIndex;
                                    if (imageIndex == singleExerciseState.answer)
                                    {
                                        imageIdentifier = thisSoundObjects[subExerciseIndex].imageId;
                                        soundIdentifier = thisSoundObjects[subExerciseIndex].soundId;
                                    }
                                    else
                                    {
                                        final AlphabetDatabase.SoundObjectInfo OtherSoundObjectInfo = otherSoundObjects[(ImageSelectionFragment.ImagesCount - 1) * subExerciseIndex + otherObjectsIndex++];
                                        imageIdentifier = OtherSoundObjectInfo.imageId;
                                        soundIdentifier = OtherSoundObjectInfo.soundId;
                                    }

                                    final int ImageResourceId = resources.getIdentifier(m_alphabetDatabase.getImageFileNameById(imageIdentifier),
                                            Constant.DrawableResourcesTag, getPackageName());
                                    final int SoundResourceId = resources.getIdentifier(m_alphabetDatabase.getSoundFileNameById(soundIdentifier),
                                            Constant.RawResourcesTag, getPackageName());

                                    if ((ImageResourceId == 0) || (SoundResourceId == 0))
                                        throw new IllegalStateException();

                                    singleExerciseState.objects[imageIndex] = new ObjectDescription(ImageResourceId, SoundResourceId, null);
                                }

                                imageSelectionExercises.add(singleExerciseState);
                            }
                        }

                        resultFragment = ImageSelectionFragment.CreateFragment(imageSelectionExercises);
                        break;
                }
            }
            catch (Exception exp)
            {
                resultFragment = null;
            }

            return resultFragment;
        }
    }

    private class SoundPronunciationExerciseFactory implements CharacterExerciseStepManager.ICustomExerciseStepFactory
    {
        public Fragment createExerciseStep( CharacterExerciseItemStepState data)
        {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_exercise_item);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface();
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            AlertDialog msgBox = MessageBox.CreateDialog(this, resources.getString(R.string.failed_exercise_start),
                    resources.getString(R.string.alert_title), false, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            finish();
                        }
                    });
            msgBox.show();
        }
    }

    /**
     * Restores all internal objects
     * @param savedInstanceState activity saved state
     * @throws java.io.IOException
     */
    void restoreInternalState(Bundle savedInstanceState) throws IOException
    {
        // Restore state
        {
            if (savedInstanceState != null)
            {
                m_state = savedInstanceState.getParcelable(StateTag);
            }

            if (m_state == null)
            {
                Bundle intentParams = getIntent().getExtras();

                // get initial parameters from intent

                final int CharacterExerciseId = intentParams.getInt(Constant.CharacterExerciseIdTag);
                final char ExerciseCharacter = intentParams.getChar(Constant.CharacterTag);

                final int CharacterExerciseItemId = intentParams.getInt(Constant.CharacterExerciseItemIdTag);
                final AlphabetDatabase.CharacterExerciseItemType CharacterExerciseItemType =
                        AlphabetDatabase.CharacterExerciseItemType.getTypeByValue(intentParams.getInt(Constant.CharacterExerciseItemTypeTag));
                final String CharacterExerciseItemTitle = getResources().getString(R.string.title_activity_character_exercise_item) + " \"" +
                        intentParams.getString(Constant.CharacterExerciseItemTitleTag) + "\"";

                if ((CharacterExerciseId == DatabaseConstant.InvalidDatabaseIndex) || (CharacterExerciseItemId == DatabaseConstant.InvalidDatabaseIndex))
                    throw new IllegalArgumentException();

                // get additional parameters about item steps from database
                m_alphabetDatabase = new AlphabetDatabase(this, false);
                AlphabetDatabase.CharacterExerciseItemStepInfo[] exerciseSteps = m_alphabetDatabase.getAllCharacterExerciseStepsByCharacterExerciseItemId(CharacterExerciseItemId);

                Map<Integer, CharacterExerciseItemStepState> sortedExerciseSteps = new HashMap<Integer, CharacterExerciseItemStepState>();
                for (AlphabetDatabase.CharacterExerciseItemStepInfo exerciseStepInfo : exerciseSteps)
                {
                    sortedExerciseSteps.put(exerciseStepInfo.stepNumber,
                            new CharacterExerciseItemStepState(exerciseStepInfo.action, exerciseStepInfo.value));
                }

                m_state = new CharacterExerciseItemState(
                        0,
                        CharacterExerciseId, ExerciseCharacter,
                        CharacterExerciseItemId, CharacterExerciseItemType, CharacterExerciseItemTitle,
                        sortedExerciseSteps.values(), null);
            }
        }

        // Restore exerciseStepsManager
        {
            CharacterExerciseStepManager.ICustomExerciseStepFactory factory = null;

            switch (m_state.characterExerciseItemType)
            {
                case SoundGeneral:
                    factory = new SoundGeneralExerciseFactory();
                    break;

                case SoundPronunciation:
                    factory = new SoundPronunciationExerciseFactory();
                    break;
            }

            m_exerciseStepManager = new CharacterExerciseStepManager(m_state.currentStep, m_state.exerciseSteps, factory);
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface()
    {
        setTitle(m_state.characterExerciseItemTitle);
        ProcessFragment(m_exerciseStepManager.getCurrentExerciseStep());
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(StateTag, m_state);
    }

    public void processPreviousStep()
    {
        Fragment exerciseStep = m_exerciseStepManager.getPreviousExerciseStep();
        if (exerciseStep == null)
            finish();

        ++m_state.currentStep;
        try
        {
            ProcessFragment(exerciseStep);
        }
        catch (Exception exp)
        {
        }
    }

    public void processNextStep()
    {
        if (m_exerciseStepManager.isFinished())
        {
            if (!m_state.exerciseStepsScore.isEmpty())
            {
                // calculate score
                int totalScore = 0;
                {
                    Collection<Integer> values = m_state.exerciseStepsScore.values();
                    for (Integer singleScore : values)
                        totalScore += singleScore;
                }

                // save score
                {
                    IServerFeedback serverFeedback = new ServerCacheFeedback(this);
                    serverFeedback.reportExerciseResult(m_state.characterExerciseItemId, totalScore);
                }

                // Show result fragment
                {
                    Fragment finishFragment = new ScoreFragment();

                    Bundle fragmentArguments = new Bundle();
                    fragmentArguments.putInt(ScoreFragment.ScoreTag, totalScore);
                    finishFragment.setArguments(fragmentArguments);

                    ProcessFragment(finishFragment);
                }
            }
            else
            {
                finish();
            }

            return;
        }
        ++m_state.currentStep;

        Fragment exerciseStep = m_exerciseStepManager.getNextExerciseStep();
        assert exerciseStep != null;

        try
        {
            ProcessFragment(exerciseStep);
        }
        catch (Exception exp)
        {
            // todo: wtf!!!
        }
    }

    public void setScore(int score)
    {
        m_state.exerciseStepsScore.put(m_state.currentStep, score);
    }

    /**
     * Renders fragment in user interface
     * @param fragment to apply in User Interface
     */
    private void ProcessFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, FragmentTag);
        fragmentTransaction.commit();
    }

    private CharacterExerciseItemState m_state;
    private CharacterExerciseStepManager m_exerciseStepManager;

    private AlphabetDatabase m_alphabetDatabase;
}
