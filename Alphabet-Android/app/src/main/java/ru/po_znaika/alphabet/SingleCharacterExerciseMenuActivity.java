package ru.po_znaika.alphabet;

import java.util.Map;
import java.util.HashMap;
import java.io.IOException;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import ru.po_znaika.database.DatabaseConstant;
import ru.po_znaika.database.alphabet.AlphabetDatabase;


public class SingleCharacterExerciseMenuActivity extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_character_exercise_menu);

        try
        {
            restoreInternalState(savedInstanceState);
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
     * Restores all internal objects
     * @param savedInstanceState activity saved state
     * @throws java.io.IOException
     */
    void restoreInternalState(Bundle savedInstanceState) throws IOException
    {
        // Restore exercise id from Bundle
        {
            Bundle intentInfo = getIntent().getExtras();

            m_characterExerciseId = intentInfo.getInt(Constant.CharacterExerciseIdTag);
            if (m_characterExerciseId == DatabaseConstant.InvalidDatabaseIndex)
                throw new IllegalArgumentException();

            m_character = intentInfo.getChar(Constant.CharacterTag);
            if (m_character == '\0')
                throw new IllegalArgumentException();
        }

        // Prepare database
        {
            m_alphabetDatabase = new AlphabetDatabase(this, false);
        }

        // Prepare menu exercises
        {
            AlphabetDatabase.CharacterExerciseItemInfo[] exercises = m_alphabetDatabase.getAllCharacterExerciseItemsByCharacterExerciseId(m_characterExerciseId);
            if (exercises == null)
                throw new IllegalArgumentException("Failed to get character exercises from database");

            m_characterExerciseItems = new AlphabetDatabase.CharacterExerciseItemInfo[exercises.length];

            // Sort exercises in m_characterExerciseItems according to menu_position
            {
                Map<Integer, AlphabetDatabase.CharacterExerciseItemInfo> sortedExercises = new HashMap<Integer, AlphabetDatabase.CharacterExerciseItemInfo>();

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
            ArrayAdapter<String> menuItems = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

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
        Intent intent = new Intent(this,CharacterExerciseItemActivity.class);

        intent.putExtra(Constant.CharacterExerciseIdTag, m_characterExerciseId);
        intent.putExtra(Constant.CharacterTag, m_character);
        intent.putExtra(Constant.CharacterExerciseItemIdTag, m_characterExerciseItems[itemSelectedIndex].id);
        intent.putExtra(Constant.CharacterExerciseItemTypeTag, m_characterExerciseItems[itemSelectedIndex].type.getValue());
        intent.putExtra(Constant.CharacterExerciseItemTitleTag, m_characterExerciseItems[itemSelectedIndex].displayName);
        this.startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
    }

    private AlphabetDatabase m_alphabetDatabase;
    private int m_characterExerciseId;
    private char m_character;

    private AlphabetDatabase.CharacterExerciseItemInfo[] m_characterExerciseItems;
}
