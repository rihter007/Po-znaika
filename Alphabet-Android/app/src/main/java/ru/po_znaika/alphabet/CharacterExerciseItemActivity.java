package ru.po_znaika.alphabet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

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

    private static final String LogTag = CharacterExerciseItemActivity.class.getName();

    private static final String CharacterExerciseItemIdTag = "character_exercise_id";
    private static final String InternalStateTag = "internal_state";
    private static final String FragmentTag = "current_fragment";

    public static void startActivity(@NonNull Context context, int characterExerciseItemId)
    {
        Intent intent = new Intent(context, CharacterExerciseItemActivity.class);
        intent.putExtra(CharacterExerciseItemIdTag, characterExerciseItemId);
        context.startActivity(intent);
    }

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

                        ArrayList<ImageSelectionSingleExerciseState> imageSelectionExercises = new ArrayList<>();

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
                                    int imageIdentifier, soundIdentifier;
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

                                    final int ImageResourceId = DatabaseHelpers.getDrawableIdByName(resources, m_alphabetDatabase.getImageFileNameById(imageIdentifier));
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
     * @throws ru.po_znaika.common.CommonException
     */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        // Restore state
        {
            if (savedInstanceState != null)
            {
                m_state = savedInstanceState.getParcelable(InternalStateTag);
                if (m_state == null)
                {
                    Log.e(LogTag, "Failed to restore internal state");
                    throw new CommonException(CommonResultCode.InvalidInternalState);
                }
            }
            else
            {
                Bundle intentParams = getIntent().getExtras();

                // get initial parameters from intent

                final int characterExerciseItemId = intentParams.getInt(Constant.CharacterExerciseIdTag);
                if (characterExerciseItemId == DatabaseConstant.InvalidDatabaseIndex)
                {
                    Log.e(LogTag, "Invalid character exercise id");
                    throw new CommonException(CommonResultCode.InvalidInternalState);
                }

                // get additional parameters about item steps from database
                m_alphabetDatabase = new AlphabetDatabase(this, false);

                final AlphabetDatabase.CharacterExerciseItemInfo characterExerciseItemInfo =
                        m_alphabetDatabase.getCharacterExerciseItemById(characterExerciseItemId);
                if (characterExerciseItemInfo == null)
                {
                    Log.e(LogTag, String.format("Failed to obtain character item info from database, id: \"%d\"",
                            characterExerciseItemId));
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                final AlphabetDatabase.CharacterExerciseInfo characterExerciseInfo =
                        m_alphabetDatabase.getCharacterExerciseById(characterExerciseItemInfo.characterExerciseId);
                if (characterExerciseInfo == null)
                {
                    Log.e(LogTag, String.format("Failed to obtain character exercise info from database, id: \"%d\"",
                            characterExerciseItemInfo.characterExerciseId));
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                Map<Integer, CharacterExerciseItemStepState> sortedExerciseSteps = new HashMap<>();
                {
                    final AlphabetDatabase.CharacterExerciseItemStepInfo[] exerciseSteps =
                            m_alphabetDatabase.getAllCharacterExerciseStepsByCharacterExerciseItemId(characterExerciseItemId);
                    for (AlphabetDatabase.CharacterExerciseItemStepInfo exerciseStepInfo : exerciseSteps)
                    {
                        sortedExerciseSteps.put(exerciseStepInfo.stepNumber,
                                new CharacterExerciseItemStepState(exerciseStepInfo.action, exerciseStepInfo.value));
                    }
                }

                m_state = new CharacterExerciseItemState(
                        0,
                        characterExerciseInfo.id,
                        characterExerciseInfo.character,
                        characterExerciseItemId,
                        characterExerciseItemInfo.type,
                        getResources().getString(R.string.title_activity_character_exercise_item) + " \"" + characterExerciseItemInfo.displayName + "\"",
                        sortedExerciseSteps.values(), null);
            }
        }

        // Restore exerciseStepsManager
        {
            CharacterExerciseStepManager.ICustomExerciseStepFactory factory = null;

            switch (m_state.characterExerciseItemType)
            {
                case General:
                    factory = new SoundGeneralExerciseFactory();
                    break;

                case Sound:
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);
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
            Log.e(LogTag, String.format("Failed to process fragment, exp: \"%s\"", exp.getMessage()));
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

                // TODO: save score
                /*{
                    IServerFeedback serverFeedback = new ServerCacheFeedback(this);
                    serverFeedback.reportExerciseResult(m_state.characterExerciseItemId, totalScore);
                }*/

                // Show result fragment
                {
                    final Fragment finishFragment = ScoreFragment.createFragment(totalScore);
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
            Log.e(LogTag, String.format("Fatal error. Failed to process exercise step fragment, exp:\"%s\"", exp.getMessage()));
            MessageBox.Show(this, "Fatal error", "Fatal error");
            finish();
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
