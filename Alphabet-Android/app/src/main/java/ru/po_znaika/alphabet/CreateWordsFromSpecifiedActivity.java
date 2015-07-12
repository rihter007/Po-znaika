package ru.po_znaika.alphabet;

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

import java.util.HashMap;
import java.util.Map;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.android.product_tracer.ITracerGetter;
import com.arz_x.tracer.ITracer;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class CreateWordsFromSpecifiedActivity extends Activity
        implements IExerciseStepCallback, IScoreNotification, ITracerGetter
{
    private static class ActivityInternalState implements Parcelable
    {
        public enum GameStage
        {
            GameIsActive(0),
            GameIsFinished(1);

            private static final Map<Integer, GameStage> TypesMap = new HashMap<Integer, GameStage>()
            {
                {
                    put(GameIsActive.getValue(), GameIsActive);
                    put(GameIsFinished.getValue(), GameIsFinished);
                }
            };

            public static GameStage getTypeByValue(int value)
            {
                return TypesMap.get(value);
            }

            GameStage(int _value)
            {
                m_value = _value;
            }

            public int getValue()
            {
                return m_value;
            }

            private int m_value;
        }

        private String exerciseName;
        public int exerciseMaxScore;

        public double completeRate;
        public GameStage stage;

        public ActivityInternalState(@NonNull String exerciseName, int exerciseMaxScore)
        {
            this.exerciseName = exerciseName;
            this.exerciseMaxScore = exerciseMaxScore;
            this.stage = GameStage.GameIsActive;
        }

        public ActivityInternalState(Parcel _in)
        {
            this.exerciseName = _in.readString();
            this.exerciseMaxScore = _in.readInt();
            this.completeRate = _in.readDouble();
            this.stage = GameStage.getTypeByValue(_in.readInt());
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel container, int flags)
        {
            container.writeString(this.exerciseName);
            container.writeInt(this.exerciseMaxScore);
            container.writeDouble(this.completeRate);
            container.writeInt(this.stage.getValue());
        }

        public static final Creator CREATOR = new Creator()
        {
            public ActivityInternalState createFromParcel(Parcel in)
            {
                return new ActivityInternalState(in);
            }

            public ActivityInternalState[] newArray(int size)
            {
                return new ActivityInternalState[size];
            }
        };
    }

    private static final String LogTag = CreateWordsFromSpecifiedActivity.class.getName();

    private static final String ExerciseIdTag = "exercise_id";
    private static final String AlphabetTypeTag = "alphabet_type";
    private static final String InternalStateTag = "internal_state";
    private static final String FragmentTag = "fragment";

    public static void startActivity(@NonNull Context context, @NonNull int exerciseId, @NonNull AlphabetDatabase.AlphabetType alphabetType)
    {
        Intent intent = new Intent(context, CreateWordsFromSpecifiedActivity.class);
        intent.putExtra(ExerciseIdTag, exerciseId);
        intent.putExtra(AlphabetTypeTag, alphabetType.getValue());

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_words_from_specified);

        try
        {
            m_tracer = TracerHelper.continueOrCreateFileTracer(this, savedInstanceState);
            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onCreate()");

            m_serviceLocator = new CoreServiceLocator(this);
            restoreInternalState(savedInstanceState);
            constructUserInterface(savedInstanceState);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            AlertDialogHelper.showMessageBox(this,
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.failed_exercise_start),
                    false, new DialogInterface.OnClickListener()
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
    protected void onPause()
    {
        super.onPause();
        m_tracer.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            m_tracer.resume();
        }
        catch (CommonException exp)
        {
            // this should never happen
            throw new AssertionError();
        }
    }

    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        final Bundle arguments = getIntent().getExtras();
        final int exerciseId = arguments.getInt(ExerciseIdTag);
        m_alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(AlphabetTypeTag));

        if (savedInstanceState == null)
        {
            final AlphabetDatabase.ExerciseInfo exerciseInfo =  m_serviceLocator.getAlphabetDatabase()
                    .getExerciseInfoById(exerciseId);
            if (exerciseInfo == null)
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Failed to get exercise info by id '%d'", exerciseId));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            m_state = new ActivityInternalState(exerciseInfo.name, exerciseInfo.maxScore);
        }
        else
        {
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        }
    }

    private void constructUserInterface(Bundle savedInstanceState)
    {
        Fragment currentFragment;

        if (savedInstanceState == null)
        {
            if (m_state.stage == ActivityInternalState.GameStage.GameIsActive)
                currentFragment = CreateWordsFromSpecifiedFragment.createFragment(m_alphabetType);
            else
                currentFragment = ScoreFragment.createFragment((int)(m_state.exerciseMaxScore * m_state.completeRate));
        }
        else
        {
            currentFragment = getFragmentManager().getFragment(savedInstanceState, FragmentTag);
        }

        ProcessFragment(currentFragment);
    }

    private void ProcessFragment(Fragment fragment)
    {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment, FragmentTag);
        fragmentTransaction.commit();
    }

    @Override
    public void setCompletionRate(double completeness)
    {
        m_state.completeRate = completeness;
    }

    @Override
    public void repeatExercise()
    {
        try
        {
            restoreInternalState(null);
            constructUserInterface(null);
        }
        catch (Exception exp)
        {
            Log.e(LogTag, String.format("Failed to repeat exercise: \"%s\"", exp.getMessage()));
            Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.error_unknown_error));
            finish();
        }
    }

    @Override
    public void processNextStep()
    {
        if (m_state.stage == ActivityInternalState.GameStage.GameIsActive)
        {
            m_state.stage = ActivityInternalState.GameStage.GameIsFinished;

            final int exerciseScore = (int)(m_state.exerciseMaxScore * m_state.completeRate);

            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, String.format("Exercise score: '%d'", exerciseScore));
            try
            {
                m_serviceLocator.getExerciseScoreProcessor().reportExerciseScore(m_state.exerciseName, exerciseScore);
            }
            catch (Exception exp)
            {
                ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            }

            final Fragment finishFragment = ExerciseFinishedFragment.createFragment(exerciseScore);
            ProcessFragment(finishFragment);
        }
        else
        {
            finish();
        }
    }

    @Override
    public void processPreviousStep()
    {
        finish();
    }

    @Override
    public ITracer getTracer()
    {
        return m_tracer;
    }

    private AlphabetDatabase.AlphabetType m_alphabetType;
    private ActivityInternalState m_state;

    private FileTracerInstance m_tracer;
    private CoreServiceLocator m_serviceLocator;
}
