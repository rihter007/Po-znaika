package ru.po_znaika.alphabet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.network.IServerOperations;

public class WordGatherActivity extends Activity implements IExerciseStepCallback, IScoreNotification
{
    private static class WordGatherActivityState implements Parcelable
    {
        public int totalScore;
        public boolean isExerciseStep;

        public WordGatherActivityState()
        {
            this.totalScore = 0;
            this.isExerciseStep = true;
        }

        public WordGatherActivityState(Parcel _in)
        {
            this.totalScore = _in.readInt();
            this.isExerciseStep = _in.readByte() > 0;
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
            container.writeByte((byte)(this.isExerciseStep ? 1 : 0));
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

    private static final String StateTag = "StateTag";
    private static final String FragmentTag = "MainWindowFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_gather);

        try
        {
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
        m_exerciseId = arguments.getInt(Constant.ExerciseIdTag);
        m_alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(Constant.AlphabetTypeTag));

        if (savedInstanceState == null)
            m_state = new WordGatherActivityState();
        else
            m_state = savedInstanceState.getParcelable(StateTag);
    }

    private void constructUserInterface(Bundle savedInstanceState)
    {
        Fragment currentFragment = null;

        if (savedInstanceState == null)
        {
            if (m_state.isExerciseStep)
            {
                WordGatherFragment wordGatherFragment = new WordGatherFragment();

                {
                    Bundle arguments = new Bundle();
                    arguments.putInt(Constant.AlphabetTypeTag, m_alphabetType.getValue());
                    wordGatherFragment.setArguments(arguments);
                }

                currentFragment = wordGatherFragment;
            }
            else
            {
                ScoreFragment scoreFragment = new ScoreFragment();

                {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ScoreFragment.ScoreTag, m_state.totalScore);
                    scoreFragment.setArguments(arguments);
                }

                currentFragment = scoreFragment;
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

        savedInstanceState.putParcelable(StateTag, m_state);

        FragmentManager fragmentManager = getFragmentManager();
        final Fragment currentFragment = fragmentManager.findFragmentByTag(FragmentTag);
        fragmentManager.putFragment(savedInstanceState, FragmentTag, currentFragment);
    }

    @Override
    public void processNextStep()
    {
        if (m_state.isExerciseStep)
        {
            m_state.isExerciseStep = false;
            // exercise has just finished

            // Save score
            {
                // serverFeedback = new ServerCacheFeedback(this);
                //serverFeedback.reportExerciseResult(m_exerciseId, m_state.totalScore);
            }

            // process score fragment
            {
                ScoreFragment scoreFragment = new ScoreFragment();

                {
                    Bundle arguments = new Bundle();
                    arguments.putInt(ScoreFragment.ScoreTag, m_state.totalScore);
                    scoreFragment.setArguments(arguments);
                }

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

    private int m_exerciseId;
    private AlphabetDatabase.AlphabetType m_alphabetType;

    private WordGatherActivityState m_state;
}
