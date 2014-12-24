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
import ru.po_znaika.database.alphabet.AlphabetDatabase;
import ru.po_znaika.server_feedback.IServerFeedback;
import ru.po_znaika.server_feedback.ServerCacheFeedback;

public class CreateWordsFromSpecifiedActivity extends Activity implements IExerciseStepCallback, IScoreNotification
{
    private static class CreateWordsFromSpecifiedActivityState implements Parcelable
    {
        public int totalScore;
        public boolean isExerciseStep;

        public CreateWordsFromSpecifiedActivityState()
        {
            this.totalScore = 0;
            this.isExerciseStep = true;
        }

        public CreateWordsFromSpecifiedActivityState(Parcel _in)
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

        public static final Parcelable.Creator CREATOR = new Parcelable.Creator()
        {
            public CreateWordsFromSpecifiedActivityState createFromParcel(Parcel in)
            {
                return new CreateWordsFromSpecifiedActivityState(in);
            }

            public CreateWordsFromSpecifiedActivityState[] newArray(int size)
            {
                return new CreateWordsFromSpecifiedActivityState[size];
            }
        };
    }

    private static final String StateTag = "state";
    private static final String FragmentTag = "fragment";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_words_from_specified);

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

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        savedInstanceState.putParcelable(StateTag, m_state);
    }

    private void restoreInternalState(Bundle savedInstanceState)
    {
        final Bundle arguments = getIntent().getExtras();
        m_exerciseId = arguments.getInt(Constant.ExerciseIdTag);
        m_alphabetType = AlphabetDatabase.AlphabetType.getTypeByValue(arguments.getInt(Constant.AlphabetTypeTag));

        if (savedInstanceState == null)
            m_state = new CreateWordsFromSpecifiedActivityState();
        else
            m_state = savedInstanceState.getParcelable(StateTag);

        m_serverFeedback = new ServerCacheFeedback(this);
    }

    private void constructUserInterface(Bundle savedInstanceState)
    {
        Fragment currentFragment = null;

        if (savedInstanceState == null)
        {
            if (m_state.isExerciseStep)
            {
                CreateWordsFromSpecifiedFragment createWordsFromSpecifiedFragment = new CreateWordsFromSpecifiedFragment();

                {
                    Bundle arguments = new Bundle();
                    arguments.putInt(Constant.AlphabetTypeTag, m_alphabetType.getValue());
                    createWordsFromSpecifiedFragment.setArguments(arguments);
                }

                currentFragment = createWordsFromSpecifiedFragment;
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
        m_serverFeedback.reportExerciseResult(m_exerciseId, m_state.totalScore);
    }

    @Override
    public void processNextStep()
    {
        if (m_state.isExerciseStep)
        {
            m_state.isExerciseStep = false;

            ScoreFragment scoreFragment = new ScoreFragment();

            {
                Bundle arguments = new Bundle();
                arguments.putInt(ScoreFragment.ScoreTag, m_state.totalScore);
                scoreFragment.setArguments(arguments);
            }

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

    private int m_exerciseId;
    private AlphabetDatabase.AlphabetType m_alphabetType;

    private CreateWordsFromSpecifiedActivityState m_state;

    private IServerFeedback m_serverFeedback;
}
