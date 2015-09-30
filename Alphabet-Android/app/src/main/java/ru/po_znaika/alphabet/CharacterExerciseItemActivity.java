package ru.po_znaika.alphabet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.app.Activity;
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
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class CharacterExerciseItemActivity extends Activity implements IExerciseStepCallback
        , ITracerGetter
        , IScoreNotification
{
    /**
     * Describes CharacterExerciseItem activity state
     */
    private static final class CharacterExerciseItemState implements Parcelable
    {
        public String exerciseName;
        public int characterExerciseId;
        public int exerciseMaxScore;
        public int exerciseIconId;

        /* Current exercise step (starts from 0) */
        public int currentStep;
        /* Exercise item steps */
        public CharacterExerciseItemStep[] exerciseSteps;
        /* Scores achieved for processing steps */
        public Map<Integer, Double> exerciseStepsScore;

        public CharacterExerciseItemState(@NonNull String _exerciseName,
                                          int _characterExerciseId,
                                          int _exerciseMaxScore,
                                          int _exerciseIconId,
                                          @NonNull Collection<CharacterExerciseItemStep> _exerciseSteps)

        {
            this.exerciseName = _exerciseName;
            this.characterExerciseId = _characterExerciseId;
            this.exerciseMaxScore = _exerciseMaxScore;
            this.exerciseIconId = _exerciseIconId;

            this.currentStep = 0;
            this.exerciseSteps = new CharacterExerciseItemStep[_exerciseSteps.size()];
            _exerciseSteps.toArray(this.exerciseSteps);

            this.exerciseStepsScore = new HashMap<>();
        }

        public CharacterExerciseItemState(@NonNull Parcel _in)
        {
            this.exerciseName = _in.readString();
            this.characterExerciseId = _in.readInt();
            this.exerciseMaxScore = _in.readInt();
            this.exerciseIconId = _in.readInt();

            this.currentStep = _in.readInt();

            {
                final int exerciseStepsNumber = _in.readInt();
                if (exerciseStepsNumber > 0)
                {
                    this.exerciseSteps = new CharacterExerciseItemStep[exerciseStepsNumber];

                    for (int exerciseStepIndex = 0; exerciseStepIndex < exerciseStepsNumber; ++exerciseStepIndex)
                        this.exerciseSteps[exerciseStepIndex] = _in.readParcelable(CharacterExerciseItemStep.class.getClassLoader());
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
            out.writeString(this.exerciseName);
            out.writeInt(this.characterExerciseId);
            out.writeInt(this.exerciseMaxScore);
            out.writeInt(this.exerciseIconId);

            out.writeInt(this.currentStep);

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
            m_tracer = TracerHelper.continueOrCreateFileTracer(this, savedInstanceState);
            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onCreate");
            restoreInternalState(savedInstanceState);
            constructUserInterface();
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.failed_exercise_start),
                    false,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            finish();
                        }
                    });
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        m_tracer.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        m_tracer.resume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        new CloseAsyncTask(m_serviceLocator).execute();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        FileTracerInstance.saveInstance(m_tracer, savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);

        final FragmentManager fragmentManager = getFragmentManager();
        final Fragment currentFragment = fragmentManager.findFragmentByTag(FragmentTag);
        fragmentManager.putFragment(savedInstanceState, FragmentTag, currentFragment);
    }

    @Override
    public ITracer getTracer()
    {
        return m_tracer;
    }

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     * @throws com.arz_x.CommonException
     */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        m_serviceLocator = new CoreServiceLocator(this);

        Bundle intentParams = getIntent().getExtras();

        // get initial parameters from intent

        final int characterExerciseItemId = intentParams.getInt(CharacterExerciseItemIdTag
                , DatabaseConstant.InvalidDatabaseIndex);
        if (characterExerciseItemId == DatabaseConstant.InvalidDatabaseIndex)
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Invalid character exercise id");
            throw new CommonException(CommonResultCode.InvalidInternalState);
        }

        ProductTracer.traceMessage(m_tracer
                , TraceLevel.Info
                , LogTag
                , String.format("Character exercise item id: '%d'", characterExerciseItemId));

        // Restore state
        {
            if (savedInstanceState != null)
            {
                m_state = savedInstanceState.getParcelable(InternalStateTag);
                if (m_state == null)
                {
                    ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Failed to restore internal state");
                    throw new CommonException(CommonResultCode.InvalidInternalState);
                }
                m_currentFragment = getFragmentManager().getFragment(savedInstanceState, FragmentTag);
            }
            else
            {
                m_currentFragment = null;

                // get additional parameters about item steps from database
                final AlphabetDatabase.CharacterExerciseItemInfo characterExerciseItemInfo =
                        m_serviceLocator.getAlphabetDatabase().getCharacterExerciseItemById(characterExerciseItemId);
                if (characterExerciseItemInfo == null)
                {
                    ProductTracer.traceMessage(m_tracer
                            , TraceLevel.Error
                            , LogTag
                            , "Failed to obtain character item info from database");
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                final int exerciseIconId = DatabaseHelpers.getDrawableIdByName(getResources()
                        , characterExerciseItemInfo.iconImageName);
                if (exerciseIconId == 0)
                {
                    ProductTracer.traceMessage(m_tracer
                            , TraceLevel.Error
                            , LogTag
                            , String.format("Failed to exercise icon resource id by '%s'"
                                , characterExerciseItemInfo.iconImageName));
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                final AlphabetDatabase.CharacterExerciseItemStepInfo[] exerciseSteps =
                        m_serviceLocator.getAlphabetDatabase()
                                .getAllCharacterExerciseStepsByCharacterExerciseItemId(characterExerciseItemId);
                if (exerciseSteps == null)
                {
                    ProductTracer.traceMessage(m_tracer
                            , TraceLevel.Error
                            , LogTag
                            , "Failed to obtain character item info from database");
                    throw new CommonException(CommonResultCode.InvalidExternalSource);
                }

                Arrays.sort(exerciseSteps, new Comparator<AlphabetDatabase.CharacterExerciseItemStepInfo>()
                {
                    @Override
                    public int compare(AlphabetDatabase.CharacterExerciseItemStepInfo lhs, AlphabetDatabase.CharacterExerciseItemStepInfo rhs)
                    {
                        if (lhs.stepNumber == rhs.stepNumber)
                            return 0;
                        return lhs.stepNumber < rhs.stepNumber ? -1 : 1;
                    }
                });

                List<CharacterExerciseItemStep> itemSteps = new ArrayList<>();
                for (AlphabetDatabase.CharacterExerciseItemStepInfo stepInfo : exerciseSteps)
                    itemSteps.add(new CharacterExerciseItemStep(stepInfo.action, stepInfo.value));

                m_state = new CharacterExerciseItemState(characterExerciseItemInfo.exerciseInfo.name
                        , characterExerciseItemInfo.characterExercise.id
                        , characterExerciseItemInfo.exerciseInfo.maxScore
                        , exerciseIconId
                        , itemSteps);
            }
        }

        final CharacterExerciseActionsFactory actionsFactory = new CharacterExerciseActionsFactory(m_state.characterExerciseId
                , m_state.exerciseIconId
                , this
                , m_serviceLocator.getAlphabetDatabase());
        m_exerciseStepManager = new CharacterExerciseStepManager(m_state.currentStep, m_state.exerciseSteps, actionsFactory);
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface()
    {
        if (m_currentFragment == null)
            m_currentFragment = m_exerciseStepManager.getCurrentExerciseStep();
        ProcessFragment(m_currentFragment);
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
                double averageCompleteness = 0;
                {
                    Collection<Double> values = m_state.exerciseStepsScore.values();
                    for (Double singleScore : values)
                        averageCompleteness += singleScore;
                    averageCompleteness /= m_state.exerciseStepsScore.size();
                }
                score = (int)(m_state.exerciseMaxScore * averageCompleteness);
            }
            else
            {
                score = m_state.exerciseMaxScore;
            }

            try
            {
                m_serviceLocator.getExerciseScoreProcessor().reportExerciseScore(m_state.exerciseName, score);
            }
            catch (Throwable exp)
            {
                ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
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
            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.error_unknown_error));
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
            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.error_unknown_error));
            finish();
        }
    }

    @Override
    public void setCompletionRate(double completeness)
    {
        m_state.exerciseStepsScore.put(m_state.currentStep, completeness);
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

    private FileTracerInstance m_tracer;
    private CoreServiceLocator m_serviceLocator;
    private CharacterExerciseStepManager m_exerciseStepManager;

    private CharacterExerciseItemState m_state;
    private Fragment m_currentFragment;
}
