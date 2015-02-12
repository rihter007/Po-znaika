package ru.po_znaika.alphabet;

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

import java.util.HashMap;
import java.util.Map;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
//import ru.po_znaika.server_feedback.IServerFeedback;
//import ru.po_znaika.server_feedback.ServerCacheFeedback;

public class CreateWordsFromSpecifiedActivity extends Activity implements IExerciseStepCallback, IScoreNotification
{
    private static class ActivityInternalState implements Parcelable
    {
        public static enum GameStage
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

            private GameStage(int _value)
            {
                m_value = _value;
            }

            public int getValue()
            {
                return m_value;
            }

            private int m_value;
        }

        public int totalScore;
        public GameStage stage;

        public ActivityInternalState()
        {
            this.totalScore = 0;
            this.stage = GameStage.GameIsActive;
        }

        public ActivityInternalState(Parcel _in)
        {
            this.totalScore = _in.readInt();
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
            container.writeInt(this.totalScore);
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

    private static final String ExerciseNameTag = "exercise_name";
    private static final String AlphabetTypeTag = "alphabet_type";
    private static final String InternalStateTag = "internal_state";
    private static final String FragmentTag = "fragment";

    public static void startActivity(@NonNull Context context, @NonNull String exerciseName, @NonNull AlphabetDatabase.AlphabetType alphabetType)
    {
        Intent intent = new Intent(context, CreateWordsFromSpecifiedActivity.class);
        intent.putExtra(ExerciseNameTag, exerciseName);
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
            m_serviceLocator = new CoreServiceLocator(this);
            restoreInternalState(savedInstanceState);
            constructUserInterface(savedInstanceState);
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
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);

        final FragmentManager fragmentManager = getFragmentManager();
        final Fragment currentFragment = fragmentManager.findFragmentByTag(FragmentTag);
        fragmentManager.putFragment(savedInstanceState, FragmentTag, currentFragment);
    }

    private void restoreInternalState(Bundle savedInstanceState)
    {
        final Bundle arguments = getIntent().getExtras();
        m_exerciseName = arguments.getString(ExerciseNameTag);
        m_alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(Constant.AlphabetTypeTag));

        if (savedInstanceState == null)
            m_state = new ActivityInternalState();
        else
            m_state = savedInstanceState.getParcelable(InternalStateTag);
    }

    private void constructUserInterface(Bundle savedInstanceState)
    {
        Fragment currentFragment;

        if (savedInstanceState == null)
        {
            if (m_state.stage == ActivityInternalState.GameStage.GameIsActive)
                currentFragment = CreateWordsFromSpecifiedFragment.createFragment(m_alphabetType);
            else
                currentFragment = ScoreFragment.createFragment(m_state.totalScore);
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
    public void setScore(int score)
    {
        m_state.totalScore = score;
    }

    @Override
    public void processNextStep()
    {
        if (m_state.stage == ActivityInternalState.GameStage.GameIsActive)
        {
            m_state.stage = ActivityInternalState.GameStage.GameIsFinished;

            try
            {
                m_serviceLocator.getexerciseScoreProcessor().reportExerciseScore(m_exerciseName, m_state.totalScore);
            }
            catch (CommonException exp)
            {
                Log.e(LogTag, String.format("An exception while saving exercise score occurred, exp: \"%s\"", exp.getMessage()));
            }

            final ScoreFragment scoreFragment = ScoreFragment.createFragment(m_state.totalScore);
            ProcessFragment(scoreFragment);
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

    private String m_exerciseName;
    private AlphabetDatabase.AlphabetType m_alphabetType;
    private ActivityInternalState m_state;

    private CoreServiceLocator m_serviceLocator;
}
