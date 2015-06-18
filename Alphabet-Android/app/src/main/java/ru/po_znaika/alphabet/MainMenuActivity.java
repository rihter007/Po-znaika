package ru.po_znaika.alphabet;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.NetworkException;
import com.arz_x.NetworkResultCode;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;

import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.licensing.LicenseType;
import ru.po_znaika.network.LoginPasswordCredentials;

public class MainMenuActivity extends Activity
{
    public static void startActivity(@NonNull Context context)
    {
        Intent intent = new Intent(context, MainMenuActivity.class);
        context.startActivity(intent);
    }

    private static final String LogTag = "MainMenuActivity";

    private class LicenseProcessingTask extends AsyncTask<String, Integer, LicenseType>
    {
        @Override
        protected LicenseType doInBackground(String... credentialParts)
        {
            try
            {
                m_serviceLocator.getExerciseScoreProcessor().syncCache();
            }
            catch (CommonException | NetworkException exp)
            {
                if ((exp instanceof  NetworkException) &&
                        (((NetworkException)exp).getResultCode() == NetworkResultCode.AuthenticationError))
                {
                    m_serviceLocator.getExerciseScoreProcessor().clearCache();
                }
                else
                {
                    m_networkErrorCode = NetworkResultCode.NoConnection;
                    return null;
                }
            }

            LoginPasswordCredentials credentials = new LoginPasswordCredentials();
            credentials.login = credentialParts[0];
            credentials.password = credentialParts[1];

            try
            {
                m_serviceLocator.getAuthenticationProvider().setLoginPasswordCredentials(credentials.login, credentials.password);
                return m_serviceLocator.getLicensing().getCurrentLicenseInfo(credentials);
            }
            catch (NetworkException exp)
            {
                //Log.e(LogTag, "License processing network exception: " + exp.getMessage());
                m_networkErrorCode = exp.getResultCode();
            }
            catch (CommonException exp)
            {
                //Log.e(LogTag, "License processing common exception: " + exp.getMessage());
                m_commonErrorCode = exp.getResultCode();
            }
            return null;
        }

        @Override
        protected void onPostExecute(LicenseType accountLicense)
        {
            Resources resources = getResources();
            if (accountLicense != null)
            {
                if (!LicenseType.isActive(accountLicense))
                {
                    AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                            resources.getString(R.string.alert_title),
                            resources.getString(R.string.alert_no_active_license));
                    return;
                }

                AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                        resources.getString(R.string.alert_title),
                        resources.getString(R.string.alert_application_activated));
                return;
            }

            switch (m_networkErrorCode)
            {
                case AuthenticationError:
                {
                    AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                            resources.getString(R.string.alert_title),
                            resources.getString(R.string.alert_login_password_incorrect));
                    return;
                }

                case UnknownReason:
                case NoConnection:
                {
                    AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                            resources.getString(R.string.alert_title),
                            resources.getString(R.string.alert_no_connection));

                    return;
                }
            }
            if (m_commonErrorCode == CommonResultCode.InvalidArgument)
            {
                AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                        resources.getString(R.string.alert_title),
                        resources.getString(R.string.alert_bad_credentials));
                return;
            }

            AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.failed_action));
        }

        private CommonResultCode m_commonErrorCode;
        private NetworkResultCode m_networkErrorCode;
    }

    private class ExerciseStartTask extends AsyncTask<Void, Integer, LicenseType>
    {
        public ExerciseStartTask(int selectedMenuPosition)
        {
            m_selectedMenuPosition = selectedMenuPosition;
        }

        @Override
        protected LicenseType doInBackground(Void... params)
        {
            try
            {
                return m_serviceLocator.getLicensing().getCurrentLicenseInfo();
            }
            catch (CommonException | NetworkException exp)
            {
                //Log.i(LogTag, "Licensing get exception message: " + exp.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(LicenseType licenseType)
        {
            // TODO: remove in future!!! hack for testing
            if (isTestUser())
                licenseType = LicenseType.Commercial;

            if (!LicenseType.isActive(licenseType))
            {
                Resources resources = getResources();
                AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                        resources.getString(R.string.alert_title),
                        resources.getString(R.string.alert_no_license));
                return;
            }

            if (m_selectedMenuPosition == 0)
            {
                // ABC-book is selected
                CharacterExerciseMenuActivity.startActivity(MainMenuActivity.this);
            }
            else
            {
                m_menuExercises.get(m_selectedMenuPosition - 1).process();
            }
        }

        private int m_selectedMenuPosition;
    }

    private boolean isTestUser()
    {
        final LoginPasswordCredentials credentials = m_serviceLocator.getAuthenticationProvider().getLoginPasswordCredentials();
        return (credentials != null) && (credentials.login.equalsIgnoreCase("test"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try
        {
            m_tracer = TracerHelper.continueOrCreateFileTracer(this, savedInstanceState);

            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onCreate");

            m_serviceLocator = new CoreServiceLocator(this);
            restoreInternalState();
            constructUserInterface();
        }
        catch (Throwable exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);

            AlertDialogHelper.showMessageBox(this,
                    getResources().getString(R.string.assert_error),
                    exp.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstance)
    {
        super.onSaveInstanceState(savedInstance);
        ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "onSaveInstance");
        m_tracer.saveInstance(savedInstance);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        m_serviceLocator.close();
    }

    void restoreInternalState() throws CommonException
    {
        final ExerciseFactory exerciseFactory = new ExerciseFactory(this, m_serviceLocator.getAlphabetDatabase());

        // contains processed exercises in sorted order
        Map<String, ArrayList<IExercise>> collectedExercises = new TreeMap<>();

        // contains all exercises
        AlphabetDatabase.ExerciseShortInfo[] exercisesShortInfo = m_serviceLocator.getAlphabetDatabase().getAllExercisesShortInfoExceptType(AlphabetDatabase.ExerciseType.Character);
        if (exercisesShortInfo != null)
        {
            for (AlphabetDatabase.ExerciseShortInfo exerciseInfo : exercisesShortInfo)
            {
                IExercise exercise = exerciseFactory.CreateExerciseFromId(exerciseInfo.id, exerciseInfo.type);
                if (exercise != null)
                {
                    final String exerciseDisplayName = exercise.getDisplayName();

                    // Add exercise to list
                    ArrayList<IExercise> displayNameExercises;
                    if (collectedExercises.containsKey(exerciseDisplayName))
                    {
                        displayNameExercises = collectedExercises.get(exerciseDisplayName);
                    }
                    else
                    {
                        displayNameExercises = new ArrayList<>();
                        collectedExercises.put(exerciseDisplayName, displayNameExercises);
                    }

                    displayNameExercises.add(exercise);
                }
                else
                {
                    ProductTracer.traceMessage(m_tracer
                            , TraceLevel.Error
                            , LogTag
                            , String.format("Failed to create exercise with id \"%d\"", exerciseInfo.id));
                }
            }
        }

        // Place exercises in sorted order
        m_menuExercises = new ArrayList<>();
        for (Map.Entry<String, ArrayList<IExercise>> sortedExercise : collectedExercises.entrySet())
        {
            m_menuExercises.addAll(sortedExercise.getValue());
        }
    }

    private void constructUserInterface()
    {
        {
            ImageView abcBookImageView = (ImageView)findViewById(R.id.abcBookImageView);
            abcBookImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onClick(): abcBookImageView");
                }
            });
        }

        {
            ImageView backImageView = (ImageView)findViewById(R.id.backImageView);
            backImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onClick(): backImageView");
                    finish();
                }
            });
        }
    }

    private void onListViewItemSelected(int itemSelectedIndex)
    {
        try
        {
            new ExerciseStartTask(itemSelectedIndex).execute();
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);

            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.failed_exercise_start));
        }
    }


    //@Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    //@Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int selectedItemId = item.getItemId();

        m_tracer.traceMessage(TraceLevel.Info, String.format("ActionBar item \"%d\" is selected", selectedItemId));

        switch (selectedItemId)
        {
            case R.id.action_diary:
            {
                Intent intent = new Intent(this, DiaryActivity.class);
                startActivity(intent);
            }
            break;

            case R.id.action_authorization:
            {
                final Resources resources = getResources();
                final View signInDialog = getLayoutInflater().inflate(R.layout.dialog_signin, null);
                AlertDialogHelper.showAlertDialog(this
                        , signInDialog
                        , resources.getString(R.string.caption_authorize)
                        , resources.getString(R.string.caption_cancel)
                        , new AlertDialogHelper.IDialogResultListener()
                        {
                            @Override
                            public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                            {
                                if (dialogResult != AlertDialogHelper.DialogResult.PositiveSelected)
                                    return;

                                EditText loginText = (EditText) signInDialog.findViewById(R.id.loginEditText);
                                EditText passwordText = (EditText) signInDialog.findViewById(R.id.passwordEditText);

                                if ((loginText.length() == 0) || (passwordText.length() == 0))
                                {
                                    AlertDialogHelper.showMessageBox(MainMenuActivity.this,
                                            resources.getString(R.string.alert_title),
                                            resources.getString(R.string.alert_login_password_not_empty));
                                    return;
                                }

                                new LicenseProcessingTask().execute(loginText.getText().toString(),
                                        passwordText.getText().toString());
                            }
                        }
                );
            }
            break;

            case R.id.action_about:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private FileTracerInstance m_tracer;
    private CoreServiceLocator m_serviceLocator;
    private List<IExercise> m_menuExercises;
}
