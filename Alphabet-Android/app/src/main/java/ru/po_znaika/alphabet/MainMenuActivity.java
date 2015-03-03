package ru.po_znaika.alphabet;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;
import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.ru.po_znaika.common.helpers.AlertDialogHelper;
import ru.po_znaika.licensing.LicenseType;
import ru.po_znaika.network.LoginPasswordCredentials;
import ru.po_znaika.network.NetworkException;
import ru.po_znaika.network.NetworkResultCode;

public class MainMenuActivity extends ActionBarActivity
{
    private static final String LogTag = "MainMenuActivity";

    private class LicenseProcessingTask extends AsyncTask<String, Integer, LicenseType>
    {
        @Override
        protected LicenseType doInBackground(String... credentialParts)
        {
            if (!m_serviceLocator.getExerciseScoreProcessor().syncCacheData())
            {
                m_networkErrorCode = NetworkResultCode.NoConnection;
                return null;
            }

            LoginPasswordCredentials credentials = new LoginPasswordCredentials();
            credentials.login = credentialParts[0];
            credentials.password = credentialParts[1];

            try
            {
                m_serviceLocator.getAuthenticationProvider().setLoginPasswordCredentials(credentials.login, credentials.password);
                final LicenseType accountLicense = m_serviceLocator.getLicensing().getCurrentLicenseInfo(credentials);
                return accountLicense;
            }
            catch (NetworkException exp)
            {
                Log.e(LogTag, "License processing network exception: " + exp.getMessage());
                m_networkErrorCode = exp.getResultCode();
            }
            catch (CommonException exp)
            {
                Log.e(LogTag, "License processing common exception: " + exp.getMessage());
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
                    MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_no_active_license),
                            resources.getString(R.string.alert_title));
                    return;
                }

                MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_application_activated),
                        resources.getString(R.string.alert_title));
                return;
            }

            switch (m_networkErrorCode)
            {
                case AuthenticationError:
                {
                    MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_login_password_incorrect),
                            resources.getString(R.string.alert_title));
                    return;
                }

                case Unknown:
                case NoConnection:
                {
                    MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_no_connection),
                            resources.getString(R.string.alert_title));
                    return;
                }
            }
            if (m_commonErrorCode == CommonResultCode.InvalidArgument)
            {
                MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_bad_credentials),
                        resources.getString(R.string.alert_title));
                return;
            }

            MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.failed_action),
                    resources.getString(R.string.alert_title));
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
                Log.i(LogTag, "Licensing get exception message: " + exp.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(LicenseType licenseType)
        {
            // TODO: remove in future!!! hack for testing
            {
                final LoginPasswordCredentials credentials = m_serviceLocator.getAuthenticationProvider().getLoginPasswordCredentials();
                if ((credentials != null) && (credentials.login.equalsIgnoreCase("test")))
                {
                    licenseType = LicenseType.Commercial;
                }
            }
            if (!LicenseType.isActive(licenseType))
            {
                Resources resources = getResources();
                MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_no_license),
                        resources.getString(R.string.alert_title));
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        try
        {
            m_serviceLocator = new CoreServiceLocator(this);
            restoreInternalState();
            constructUserInterface();
        }
        catch (CommonException exp)
        {
            MessageBox.Show(this, exp.getMessage(), getResources().getString(R.string.assert_error));
        }
        catch (Exception exp)
        {
            Log.e(MainMenuActivity.class.getName(), "onCreate: Unknown exception occurred");
            MessageBox.Show(this, exp.getMessage(), getResources().getString(R.string.assert_error));
        }
    }

    void restoreInternalState() throws CommonException
    {
        // Print account name
        {
            final String accountName = m_serviceLocator.getAuthenticationProvider().getAccountName();
            if (!TextUtils.isEmpty(accountName))
            {
                // TODO:
            }
        }

        ExerciseFactory exerciseFactory = new ExerciseFactory(this, m_serviceLocator.getAlphabetDatabase());

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
                    Log.e(MainMenuActivity.class.getName(), String.format("Failed to create exercise with id \"%d\"", exerciseInfo.id));
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
        ListView uiListView = (ListView) findViewById(R.id.menuListView);
        uiListView.setAdapter(null);

       ImageTextAdapter listViewItemsAdapter = new ImageTextAdapter(this, R.layout.large_image_menu_item);

        // add first element - alphabet
        {
            Resources resources = getResources();
            listViewItemsAdapter.add(resources.getDrawable(R.drawable.alphabet), resources.getString(R.string.caption_abc_book));
        }

        for (IExercise currentExercise : m_menuExercises)
        {
            listViewItemsAdapter.add(currentExercise.getDisplayImage(), currentExercise.getDisplayName());
        }

        uiListView.setAdapter(listViewItemsAdapter);
        uiListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId)
            {
                onListViewItemSelected((int)rowId);
            }
        });
    }

    private void onListViewItemSelected(int itemSelectedIndex)
    {
        try
        {
            new ExerciseStartTask(itemSelectedIndex).execute();
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            MessageBox.Show(this, resources.getString(R.string.failed_exercise_start), resources.getString(R.string.alert_title));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int SelectedItemId = item.getItemId();
        switch (SelectedItemId)
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
                                    MessageBox.Show(MainMenuActivity.this, resources.getString(R.string.alert_login_password_not_empty),
                                            resources.getString(R.string.alert_title));
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

    private CoreServiceLocator m_serviceLocator;
    private List<IExercise> m_menuExercises;
}
