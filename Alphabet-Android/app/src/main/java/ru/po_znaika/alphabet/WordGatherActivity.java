package ru.po_znaika.alphabet;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

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

public final class WordGatherActivity extends Activity implements IExerciseStepCallback
        , IScoreNotification, ITracerGetter
{
    public static void startActivity(@NonNull Context context
            , int exerciseId
            , @NonNull AlphabetDatabase.AlphabetType alphabetType)
    {
        Intent intent = new Intent(context, WordGatherActivity.class);
        intent.putExtra(ExerciseIdTag, exerciseId);
        intent.putExtra(AlphabetTypeTag, alphabetType.getValue());

        context.startActivity(intent);
    }

    private static final class WordGatherActivityState implements Parcelable
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

        public int maxExerciseScore;
        public GameStage stage;
        public double completionRate;

        public WordGatherActivityState(int _maxExerciseScore
                , GameStage _stage)
        {
            this.maxExerciseScore = _maxExerciseScore;
            this.stage = _stage;
            this.completionRate = 0.0;
        }

        public WordGatherActivityState(@NonNull Parcel _in)
        {
            this.maxExerciseScore = _in.readInt();
            this.stage = GameStage.getTypeByValue(_in.readInt());
            this.completionRate = _in.readDouble();
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel container, int flags)
        {
            container.writeInt(this.maxExerciseScore);
            container.writeInt(this.stage.getValue());
            container.writeDouble(this.completionRate);
        }

        public static final Creator CREATOR = new Creator()
        {
            public WordGatherActivityState createFromParcel(Parcel in)
            {
                return new WordGatherActivityState(in);
            }

            public WordGatherActivityState[] newArray(int size)
            {
                return new WordGatherActivityState[size];
            }
        };
    }

    private static final String LogTag = WordGatherActivity.class.getName();

    private static final String ExerciseIdTag = "exercise_name";
    private static final String AlphabetTypeTag = "alphabet_type";
    private static final String InternalStateTag = "internal_state";
    private static final String FragmentTag = "main_window_fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_gather);
        setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try
        {
            m_tracer = TracerHelper.continueOrCreateFileTracer(this, savedInstanceState);
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "onCreate()");

            m_serviceLocator = new CoreServiceLocator(this);
            restoreInternalState(savedInstanceState);
            constructUserInterface(savedInstanceState);
        }
        catch (Exception exp)
        {
            AlertDialogHelper.showMessageBox(this,
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.failed_exercise_start),
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

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        FileTracerInstance.saveInstance(m_tracer, savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);

        final FragmentManager fragmentManager = getFragmentManager();
        final Fragment currentFragment = fragmentManager.findFragmentByTag(FragmentTag);
        fragmentManager.putFragment(savedInstanceState, FragmentTag, currentFragment);
    }

    private void restoreInternalState(Bundle savedInstanceState)
    {
        final Bundle arguments = getIntent().getExtras();
        final int exerciseId = arguments.getInt(ExerciseIdTag);
        m_alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(AlphabetTypeTag));

        AlphabetDatabase.ExerciseInfo exerciseInfo = m_serviceLocator.getAlphabetDatabase().getExerciseInfoById(exerciseId);
        m_exerciseName = exerciseInfo.name;

        m_state = (savedInstanceState == null) ? new WordGatherActivityState(exerciseInfo.maxScore
                , WordGatherActivityState.GameStage.GameIsActive) :
                (WordGatherActivityState)savedInstanceState.getParcelable(InternalStateTag);
    }

    private void constructUserInterface(Bundle savedInstanceState) throws CommonException
    {
        Fragment currentFragment;
        if (savedInstanceState == null)
        {
            switch (m_state.stage)
            {
                case GameIsActive:
                {
                    currentFragment = WordGatherFragment.createFragment(m_alphabetType);
                }
                break;

                case GameIsFinished:
                {
                    currentFragment = ExerciseFinishedFragment.createFragment((int) (m_state.maxExerciseScore * m_state.completionRate));
                }
                break;

                default:
                {
                    ProductTracer.traceMessage(m_tracer
                            , TraceLevel.Error
                            , LogTag
                            , String.format("Unknown game stage: \"%s\"", m_state.stage.name()));
                    throw new CommonException(CommonResultCode.AssertError);
                }
            }
        }
        else
        {
            currentFragment = getFragmentManager().getFragment(savedInstanceState, FragmentTag);
        }

        ProcessFragment(currentFragment);
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
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);

            AlertDialogHelper.showMessageBox(this,
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.error_unknown_error));
            finish();
        }
    }

    @Override
    public void processNextStep()
    {
        if (m_state.stage == WordGatherActivityState.GameStage.GameIsActive)
        {
            // exercise has just finished
            m_state.stage = WordGatherActivityState.GameStage.GameIsFinished;

            final int totalScore = (int)(m_state.maxExerciseScore * m_state.completionRate);

            // Save score
            try
            {
               m_serviceLocator.getExerciseScoreProcessor().reportExerciseScore(m_exerciseName, totalScore);
            }
            catch (Exception exp)
            {
                ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            }

            // process score fragment
            {
                final Fragment finishFragment = ExerciseFinishedFragment.createFragment(totalScore);
                ProcessFragment(finishFragment);
            }
        }
        else
        {
            finish();
        }
    }

    @Override
    public ITracer getTracer()
    {
        return m_tracer;
    }

    @Override
    public void processPreviousStep()
    {
        finish();
    }

    @Override
    public void setCompletionRate(double completionRate)
    {
        m_state.completionRate = completionRate;
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

    private String m_exerciseName;
    private AlphabetDatabase.AlphabetType m_alphabetType;

    private WordGatherActivityState m_state;

    private CoreServiceLocator m_serviceLocator;
}
