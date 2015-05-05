package ru.po_znaika.alphabet;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

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
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class CharacterExerciseItemActivity extends Activity implements IExerciseStepCallback, IScoreNotification
{
    /**
     * Describes CharacterExerciseItem activity state
     */
    private static final class CharacterExerciseItemState implements Parcelable
    {
        /**
         * Current exercise step (starts from 0)
         */
        public int currentStep;

        public int characterExerciseId;
        public char exerciseCharacter;

        public int characterExerciseItemId;
        public String characterExerciseItemName;
        public String characterExerciseItemDisplayName;

        public CharacterExerciseItemStep[] exerciseSteps;
        public Map<Integer, Integer> exerciseStepsScore;

        public CharacterExerciseItemState(int _characterExerciseId,
                                          char _exerciseCharacter,
                                          int _characterExerciseItemId,
                                          @NonNull String _characterExerciseItemName,
                                          @NonNull String _characterExerciseItemDisplayName,
                                          @NonNull Collection<CharacterExerciseItemStep> _exerciseSteps)

        {
            if ((_characterExerciseId == DatabaseConstant.InvalidDatabaseIndex) ||
                    (_characterExerciseItemId == DatabaseConstant.InvalidDatabaseIndex))
            {
                throw new IllegalArgumentException();
            }

            this.currentStep = 0;

            this.characterExerciseId = _characterExerciseId;
            this.exerciseCharacter = _exerciseCharacter;

            this.characterExerciseItemId = _characterExerciseItemId;
            this.characterExerciseItemName = _characterExerciseItemName;
            this.characterExerciseItemDisplayName = _characterExerciseItemDisplayName;

            this.exerciseSteps = new CharacterExerciseItemStep[_exerciseSteps.size()];
            _exerciseSteps.toArray(this.exerciseSteps);

            this.exerciseStepsScore = new HashMap<>();
        }

        public CharacterExerciseItemState(@NonNull Parcel _in)
        {
            this.currentStep = _in.readInt();

            this.characterExerciseId = _in.readInt();
            this.exerciseCharacter = _in.readString().charAt(0);

            this.characterExerciseItemId = _in.readInt();
            this.characterExerciseItemName = _in.readString();
            this.characterExerciseItemDisplayName = _in.readString();

            {
                final int exerciseStepsNumber = _in.readInt();

                if (exerciseStepsNumber > 0)
                {
                    this.exerciseSteps = new CharacterExerciseItemStep[exerciseStepsNumber];

                    for (int exerciseStepIndex = 0; exerciseStepIndex < exerciseStepsNumber; ++exerciseStepIndex)
                    {
                        this.exerciseSteps[exerciseStepIndex] = _in.readParcelable(CharacterExerciseItemStep.class.getClassLoader());
                    }
                }
            }

            {
                this.exerciseStepsScore = new HashMap<>();
                _in.readMap(this.exerciseStepsScore, HashMap.class.getClassLoader());
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
            out.writeInt(this.currentStep);

            out.writeInt(this.characterExerciseId);
            out.writeString(((Character)this.exerciseCharacter).toString());

            out.writeInt(this.characterExerciseItemId);
            out.writeString(this.characterExerciseItemName);

            out.writeInt(this.exerciseSteps.length);
            for (CharacterExerciseItemStep exerciseItemStepState : this.exerciseSteps)
                out.writeParcelable(exerciseItemStepState, 0);

            out.writeMap(this.exerciseStepsScore);
        }

        public static final Creator CREATOR = new Creator()
        {
            public CharacterExerciseItemState createFromParcel(Parcel in)
            {
                return new CharacterExerciseItemState(in);
            }

            public CharacterExerciseItemState[] newArray(int size)
            {
                return new CharacterExerciseItemState[size];
            }
        };
    }

    private static final String LogTag = CharacterExerciseItemActivity.class.getName();

    private static final String CharacterExerciseItemIdTag = "character_exercise_item_id";
    private static final String InternalStateTag = "internal_state";
    private static final String FragmentTag = "current_fragment";

    public static void startActivity(@NonNull Context context, int characterExerciseItemId)
    {
        Intent intent = new Intent(context, CharacterExerciseItemActivity.class);
        intent.putExtra(CharacterExerciseItemIdTag, characterExerciseItemId);
        context.startActivity(intent);
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

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        new CloseAsyncTask(m_serviceLocator).execute();
    }

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     * @throws com.arz_x.CommonException
     */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        m_serviceLocator = new CoreServiceLocator(this);

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
                m_currentFragment = getFragmentManager().getFragment(savedInstanceState, FragmentTag);
            }
            else
            {
                m_currentFragment = null;
                Bundle intentParams = getIntent().getExtras();

                // get initial parameters from intent

                final int characterExerciseItemId = intentParams.getInt(CharacterExerciseItemIdTag);
                if (characterExerciseItemId == DatabaseConstant.InvalidDatabaseIndex)
                {
                    Log.e(LogTag, "Invalid character exercise id");
                    throw new CommonException(CommonResultCode.InvalidInternalState);
                }

                // get additional parameters about item steps from database
                final AlphabetDatabase.CharacterExerciseItemInfo characterExerciseItemInfo =
                        m_serviceLocator.getAlphabetDatabase().getCharacterExerciseItemById(characterExerciseItemId);
                if (characterExerciseItemInfo == null)
                {
                    Log.e(LogTag, String.format("Failed to obtain character item info from database, id: \"%d\"",
                            characterExerciseItemId));
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                final AlphabetDatabase.CharacterExerciseInfo characterExerciseInfo =
                        m_serviceLocator.getAlphabetDatabase().getCharacterExerciseById(characterExerciseItemInfo.characterExerciseId);
                if (characterExerciseInfo == null)
                {
                    Log.e(LogTag, String.format("Failed to obtain character exercise info from database, id: \"%d\"",
                            characterExerciseItemInfo.characterExerciseId));
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                Map<Integer, CharacterExerciseItemStep> sortedExerciseSteps = new TreeMap<>();
                {
                    final AlphabetDatabase.CharacterExerciseItemStepInfo[] exerciseSteps =
                            m_serviceLocator.getAlphabetDatabase().getAllCharacterExerciseStepsByCharacterExerciseItemId(characterExerciseItemId);
                    for (AlphabetDatabase.CharacterExerciseItemStepInfo exerciseStepInfo : exerciseSteps)
                    {
                        sortedExerciseSteps.put(exerciseStepInfo.stepNumber,
                                new CharacterExerciseItemStep(exerciseStepInfo.action, exerciseStepInfo.value));
                    }
                }

                m_state = new CharacterExerciseItemState(characterExerciseInfo.id,
                        characterExerciseInfo.character,
                        characterExerciseItemId,
                        characterExerciseItemInfo.name,
                        getResources().getString(R.string.title_activity_character_exercise_item) + " \"" + characterExerciseItemInfo.displayName + "\"",
                        sortedExerciseSteps.values());
            }
        }

        final CharacterExerciseActionsFactory actionsFactory = new CharacterExerciseActionsFactory(m_state.characterExerciseId,
                this, m_serviceLocator.getAlphabetDatabase());
        m_exerciseStepManager = new CharacterExerciseStepManager(m_state.currentStep, m_state.exerciseSteps, actionsFactory);
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface()
    {
        setTitle(m_state.characterExerciseItemDisplayName);
        if (m_currentFragment == null)
            m_currentFragment = m_exerciseStepManager.getCurrentExerciseStep();
        ProcessFragment(m_currentFragment);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);

        final FragmentManager fragmentManager = getFragmentManager();
        final Fragment currentFragment = fragmentManager.findFragmentByTag(FragmentTag);
        fragmentManager.putFragment(savedInstanceState, FragmentTag, currentFragment);
    }

    @Override
    public void processPreviousStep()
    {
        Fragment exerciseStep = m_exerciseStepManager.getPreviousExerciseStep();
        if (exerciseStep == null)
            finish();

        m_currentFragment = exerciseStep;
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

    @Override
    public void processNextStep()
    {
        if (m_exerciseStepManager.isFinished())
        {
            // finish button is clicked in final fragment
            if (m_state.currentStep >= m_state.exerciseSteps.length)
            {
                finish();
                return;
            }

            Integer score = null;
            // save score
            if (!m_state.exerciseStepsScore.isEmpty())
            {
                // calculate score
                int totalScore = 0;
                {
                    Collection<Integer> values = m_state.exerciseStepsScore.values();
                    for (Integer singleScore : values)
                        totalScore += singleScore;
                }
                score = totalScore;

                try
                {
                    m_serviceLocator.getExerciseScoreProcessor().reportExerciseScore(m_state.characterExerciseItemName, totalScore);
                }
                catch (CommonException exp)
                {
                    Log.e(LogTag, "Failed to report exercise score, exception message:" + exp.getMessage());
                }
            }
            ++m_state.currentStep;

            m_currentFragment = ExerciseFinishedFragment.createFragment(score);
            ProcessFragment(m_currentFragment);
            return;
        }

        try
        {
            ++m_state.currentStep;
            m_currentFragment = m_exerciseStepManager.getNextExerciseStep();
            ProcessFragment(m_currentFragment);
        }
        catch (Exception exp)
        {
            Log.e(LogTag, String.format("Fatal error. Failed to process exercise step fragment, exp:\"%s\"", exp.getMessage()));
            MessageBox.Show(this, "Fatal error", "Fatal error");
            finish();
        }
    }

    @Override
    public void repeatExercise()
    {
        try
        {
            restoreInternalState(null);
            constructUserInterface();
        }
        catch (Exception exp)
        {
            Log.e(LogTag, String.format("Failed to repeat exercise: \"%s\"", exp.getMessage()));
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
    private Fragment m_currentFragment;
    private CharacterExerciseStepManager m_exerciseStepManager;

    private CoreServiceLocator m_serviceLocator;
}
