package ru.po_znaika.alphabet;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.TreeMap;

import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.util.Log;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class MainMenuActivity extends ActionBarActivity
{
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
            if (itemSelectedIndex == 0)
            {
                // ABC-book is selected
                CharacterExerciseMenuActivity.startActivity(this);
            }
            else
            {
                m_menuExercises.get(itemSelectedIndex - 1).process();
            }
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
                Intent intent = new Intent(this, DiaryActivity.class);
                startActivity(intent);
                break;

            case R.id.action_about:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private CoreServiceLocator m_serviceLocator;
    private List<IExercise> m_menuExercises;
}
