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

public final class WordGatherActivity extends Activity implements IExerciseStepCallback, IScoreNotification
{
    private static final class WordGatherActivityState implements Parcelable
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

        public WordGatherActivityState()
        {
            this.totalScore = 0;
            this.stage = GameStage.GameIsActive;
        }

        public WordGatherActivityState(@NonNull Parcel _in)
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

    private static final String ExerciseNameTag = "exercise_name";
    private static final String AlphabetTypeTag = "alphabet_type";
    private static final String InternalStateTag = "internal_state";
    private static final String FragmentTag = "main_window_fragment";

    public static void startActivity(@NonNull Context context, @NonNull String exerciseName, @NonNull AlphabetDatabase.AlphabetType alphabetType)
    {
        Intent intent = new Intent(context, WordGatherActivity.class);
        intent.putExtra(ExerciseNameTag, exerciseName);
        intent.putExtra(AlphabetTypeTag, alphabetType.getValue());

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_gather);

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

    private void restoreInternalState(Bundle savedInstanceState)
    {
        final Bundle arguments = getIntent().getExtras();
        m_exerciseName = arguments.getString(ExerciseNameTag);
        m_alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(AlphabetTypeTag));

        m_state = (savedInstanceState == null) ? new WordGatherActivityState() :
                (WordGatherActivityState)savedInstanceState.getParcelable(InternalStateTag);
    }

    private void constructUserInterface(Bundle savedInstanceState)
    {
        Fragment currentFragment = null;
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
                    currentFragment = ScoreFragment.createFragment(m_state.totalScore);
                }
                break;

                default:
                {
                    Log.e(LogTag, String.format("Unknown game stage: \"%s\"", m_state.stage.name()));
                }
                break;
            }
        }
        else
        {
            currentFragment = getFragmentManager().getFragment(savedInstanceState, FragmentTag);
        }

        ProcessFragment(currentFragment);
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
    public void processNextStep()
    {
        if (m_state.stage == WordGatherActivityState.GameStage.GameIsActive)
        {
            // exercise has just finished
            m_state.stage = WordGatherActivityState.GameStage.GameIsFinished;

            // Save score
            try
            {
               m_serviceLocator.getExerciseScoreProcessor().reportExerciseScore(m_exerciseName, m_state.totalScore);
            }
            catch (CommonException exp)
            {
                Log.e(LogTag, String.format("Failed to save exercise score," +
                        " exercise name: \"%s\", score: \"%d\"", m_exerciseName, m_state.totalScore));
            }

            // process score fragment
            {
                final ScoreFragment scoreFragment = ScoreFragment.createFragment(m_state.totalScore);
                ProcessFragment(scoreFragment);
            }
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
    public void setScore(int score)
    {
        m_state.totalScore = score;
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

    private String m_exerciseName;
    private AlphabetDatabase.AlphabetType m_alphabetType;

    private WordGatherActivityState m_state;

    private CoreServiceLocator m_serviceLocator;
}
