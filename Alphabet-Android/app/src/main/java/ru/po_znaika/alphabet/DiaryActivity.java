package ru.po_znaika.alphabet;

import android.content.res.Resources;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.ExerciseScore;

public final class DiaryActivity extends ActionBarActivity
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
            return false;
        }
        return true;
    }

    private void constructUserInterface() throws CommonException
    {
        TextAdapter textAdapter;

        // fill grid caption
        {
            final Resources resources = getResources();

            textAdapter = new TextAdapter(this, resources.getDimension(R.dimen.small_text_size));
            textAdapter.add(resources.getString(R.string.caption_date));
            textAdapter.add(resources.getString(R.string.caption_exercise_name));
            textAdapter.add(resources.getString(R.string.caption_exercise_score));
        }

        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(this, false);

        DiaryDatabase diaryDatabase = new DiaryDatabase(this);
        ExerciseScore diaryNotes[] = diaryDatabase.getAllDiaryRecordsOrderedByDate();

        if ((diaryNotes != null) && (diaryNotes.length > 0))
        {
            Map<String, String> exerciseNamesMap = new HashMap<>();
            for (ExerciseScore diaryNote : diaryNotes)
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

                String exerciseTitle;
                if (exerciseNamesMap.containsKey(diaryNote.exerciseName))
                {
                    exerciseTitle = exerciseNamesMap.get(diaryNote.exerciseName);
                }
                else
                {
                    exerciseTitle = alphabetDatabase.getExerciseDisplayNameByIdName(diaryNote.exerciseName);
                    if (exerciseTitle == null)
                        exerciseTitle = alphabetDatabase.getCharacterItemDisplayNameByIdName(diaryNote.exerciseName);
                    exerciseNamesMap.put(diaryNote.exerciseName, exerciseTitle);
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
