package ru.po_znaika.alphabet;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;

public class SingleCharacterExerciseMenuActivity extends Activity
{
    private static final String LogTag = SingleCharacterExerciseMenuActivity.class.getName();

    private static final String CharacterExerciseIdTag = "character_exercise_id";
    private static final String ExerciseCharacterTag = "exercise_character";

    public static void startActivity(@NonNull Context context, int characterExerciseId, char exerciseCharacter)
    {
        Intent intent = new Intent(context, SingleCharacterExerciseMenuActivity.class);
        intent.putExtra(CharacterExerciseIdTag, characterExerciseId);
        intent.putExtra(ExerciseCharacterTag, exerciseCharacter);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_character_exercise_menu);

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

    /**
     * Restores all internal selectionVariants
     * @throws ru.po_znaika.common.CommonException
     */
    void restoreInternalState() throws CommonException
    {
        // Restore exercise id from Bundle
        {
            Bundle intentInfo = getIntent().getExtras();

            m_characterExerciseId = intentInfo.getInt(CharacterExerciseIdTag);
            if (m_characterExerciseId == DatabaseConstant.InvalidDatabaseIndex)
            {
                Log.e(LogTag, "Invalid character exercise id");
                throw new CommonException(CommonResultCode.InvalidInternalState);
            }

            m_character = intentInfo.getChar(ExerciseCharacterTag);
            if (m_character == '\0')
            {
                Log.e(LogTag, "Invalid exercise character");
                throw new CommonException(CommonResultCode.InvalidInternalState);
            }
        }

        // Prepare database
        {
            m_alphabetDatabase = new AlphabetDatabase(this, false);
        }

        // Prepare menu exercises
        {
            AlphabetDatabase.CharacterExerciseItemInfo[] exercises = m_alphabetDatabase.getAllCharacterExerciseItemsByCharacterExerciseId(m_characterExerciseId);
            if (exercises == null)
            {
                Log.e(LogTag, "Failed to get character exercises from database");
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            m_characterExerciseItems = new AlphabetDatabase.CharacterExerciseItemInfo[exercises.length];

            // Sort exercises in m_characterExerciseItems according to menu_position
            {
                Map<Integer, AlphabetDatabase.CharacterExerciseItemInfo> sortedExercises = new TreeMap<>();

                for (AlphabetDatabase.CharacterExerciseItemInfo exerciseInfo : exercises)
                    sortedExercises.put(exerciseInfo.menuPosition, exerciseInfo);

                int exerciseIndex = 0;
                for (Map.Entry<Integer, AlphabetDatabase.CharacterExerciseItemInfo> exerciseInfo : sortedExercises.entrySet())
                    m_characterExerciseItems[exerciseIndex++] = exerciseInfo.getValue();
            }
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface()
    {
        // process caption
        {
            TextView textView = (TextView) findViewById(R.id.charcterTextView);
            final String CharacterCaption = "[" + m_character + "]";
            textView.setText(CharacterCaption);
        }
        // process menu items list view
        {
            ArrayAdapter<String> menuItems = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

            for (AlphabetDatabase.CharacterExerciseItemInfo characterExerciseInfo : m_characterExerciseItems)
                menuItems.add(characterExerciseInfo.displayName);

            ListView menuListView = (ListView) findViewById(R.id.menuListView);
            menuListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
                {
                    onListViewItemSelected((int) l);
                }
            });
            menuListView.setAdapter(menuItems);
        }
    }

    private void onListViewItemSelected(int itemSelectedIndex)
    {
        CharacterExerciseItemActivity.startActivity(this, m_characterExerciseItems[itemSelectedIndex].id);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    private AlphabetDatabase m_alphabetDatabase;
    private int m_characterExerciseId;
    private char m_character;

    private AlphabetDatabase.CharacterExerciseItemInfo[] m_characterExerciseItems;
}
