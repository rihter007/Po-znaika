package ru.po_znaika.alphabet;

import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import ru.po_znaika.database.alphabet.AlphabetDatabase;
import ru.po_znaika.database.diary.DiaryDatabase;


public class DiaryActivity extends ActionBarActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diary);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.diary, menu);

        try
        {
            constructUserInterface();
        }
        catch (Exception exp)
        {

        }
        return true;
    }

    private void constructUserInterface() throws IOException
    {
        TextAdapter textAdapter = new TextAdapter(this);

        // fill grid caption
        {
            Resources resources = getResources();

            textAdapter.add(resources.getString(R.string.caption_date));
            textAdapter.add(resources.getString(R.string.caption_exercise_name));
            textAdapter.add(resources.getString(R.string.caption_exercise_score));
        }

        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(this, false);

        DiaryDatabase diaryDatabase = new DiaryDatabase(this);
        DiaryDatabase.ExerciseDiaryShortInfo diaryNotes[] = diaryDatabase.getAllDiaryRecordsOrderedByDate();

        if ((diaryNotes != null) && (diaryNotes.length > 0))
        {
            Map<Integer, String> exerciseNamesMap = new HashMap<Integer, String>();
            for (DiaryDatabase.ExerciseDiaryShortInfo diaryNote : diaryNotes)
            {
                Calendar noteTime = new GregorianCalendar();
                noteTime.setTime(diaryNote.date);

                textAdapter.add(String.format("%02d.%02d.%d %02d:%02d",
                        noteTime.get(Calendar.DAY_OF_MONTH),
                        noteTime.get(Calendar.MONTH) + 1,
                        noteTime.get(Calendar.YEAR),
                        noteTime.get(Calendar.HOUR_OF_DAY),
                        noteTime.get(Calendar.MINUTE)
                ));

                String exerciseTitle = null;
                if (exerciseNamesMap.containsKey(diaryNote.exerciseId))
                {
                    exerciseTitle = exerciseNamesMap.get(diaryNote.exerciseId);
                }
                else
                {
                    exerciseTitle = alphabetDatabase.getExerciseDisplayNameById(diaryNote.exerciseId);
                    if (exerciseTitle == null)
                        exerciseTitle = alphabetDatabase.getCharacterItemDisplayNameById(diaryNote.exerciseId);
                    exerciseNamesMap.put(diaryNote.exerciseId, exerciseTitle);
                }

                if (!TextUtils.isEmpty(exerciseTitle))
                    textAdapter.add(exerciseTitle);
                else
                    textAdapter.add("");

                textAdapter.add(((Integer)diaryNote.score).toString());
            }
        }

        GridView gridView = (GridView)findViewById(R.id.gridView);
        gridView.setAdapter(textAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        final int ItemId = item.getItemId();
        switch (ItemId)
        {
            case R.id.action_synchronize:
                break;

            case R.id.action_clear:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
