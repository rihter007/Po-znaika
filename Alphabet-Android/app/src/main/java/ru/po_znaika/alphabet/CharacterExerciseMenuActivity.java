package ru.po_znaika.alphabet;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.IExercise;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class CharacterExerciseMenuActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_exercise_menu);

        try
        {
            restoreInternalState();
            constructUserInterface();
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

    private void restoreInternalState() throws CommonException
    {
        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(this, false);
        AlphabetDatabase.ExerciseShortInfo[] characterExercisesInfo = alphabetDatabase.getAllExercisesShortInfoByType(AlphabetDatabase.ExerciseType.Character);
        if (characterExercisesInfo == null)
            throw new IllegalArgumentException();

        final ExerciseFactory exerciseFactory = new ExerciseFactory(this, alphabetDatabase);

        Map<String, IExercise> exerciseMap = new TreeMap<>();
        for (AlphabetDatabase.ExerciseShortInfo exerciseShortInfo : characterExercisesInfo)
        {
            IExercise characterExercise = exerciseFactory.CreateExerciseFromId(exerciseShortInfo.id, exerciseShortInfo.type);
            if (characterExercise != null)
            {
                final String ExerciseDisplayName = characterExercise.getDisplayName();
                if (!TextUtils.isEmpty(ExerciseDisplayName))
                    exerciseMap.put(ExerciseDisplayName, characterExercise);
            }
        }

        {
            m_characterExercises = new IExercise[exerciseMap.size()];

            int exerciseIndex = 0;
            final Set<Map.Entry<String, IExercise>> values = exerciseMap.entrySet();
            for (Map.Entry<String, IExercise> exercise : values)
                m_characterExercises[exerciseIndex++] = exercise.getValue();
        }
    }

    private void constructUserInterface()
    {
        {
            LayoutInflater inflater = getLayoutInflater();
            ViewAdapter adapter = new ViewAdapter();
            for (int exerciseId = 0; exerciseId < m_characterExercises.length; ++exerciseId)
            {
                LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.small_image_item, null, false);
                ImageView imageView = (ImageView) layout.findViewById(R.id.imageView);
                imageView.setImageDrawable(m_characterExercises[exerciseId].getDisplayImage());

                final int exerciseIndex = exerciseId;
                layout.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onGridItemClicked(exerciseIndex);
                    }
                });

                adapter.add(layout);
            }

            GridView menuGrid = (GridView) findViewById(R.id.gridView);
            menuGrid.setAdapter(adapter);
        }

        {
            ImageButton backButton = (ImageButton) findViewById(R.id.backImageButton);
            backButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    finish();
                }
            });
        }
    }

    private void onGridItemClicked(int itemId)
    {
        try
        {
            m_characterExercises[itemId].process();
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            MessageBox.Show(this, resources.getString(R.string.failed_exercise_start), resources.getString(R.string.alert_title));
        }
    }

    private IExercise[] m_characterExercises;
}
