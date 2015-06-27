package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import java.util.Map;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class SingleCharacterExerciseMenuActivity extends Activity
{
    public static void startActivity(@NonNull Context context, int characterExerciseId)
    {
        Intent intent = new Intent(context, SingleCharacterExerciseMenuActivity.class);
        intent.putExtra(CharacterExerciseIdTag, characterExerciseId);
        context.startActivity(intent);
    }

    private static final String CharacterExerciseIdTag = "character_exercise_id";
    private static final String LogTag = SingleCharacterExerciseMenuActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_character_exercise_menu);

        setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try
        {
            m_tracer = TracerHelper.continueOrCreateFileTracer(this, savedInstanceState);

            restoreInternalState();
            constructUserInterface();
        }
        catch (Throwable exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            AlertDialogHelper.showMessageBox(this,
                    getResources().getString(R.string.alert_title),
                    getResources().getString(R.string.failed_exercise_start),
                    false, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            finish();
                        }
                    });
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        m_tracer.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        try
        {
            m_tracer.resume();
        }
        catch (CommonException exp)
        {
            // this should never happen
            throw new AssertionError();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstance)
    {
        super.onSaveInstanceState(savedInstance);
        FileTracerInstance.saveInstance(m_tracer, savedInstance);
    }

    /**
     * Restores all internal selectionVariants
     * @throws com.arz_x.CommonException
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

            /*m_character = intentInfo.getChar(ExerciseCharacterTag);
            if (m_character == '\0')
            {
                Log.e(LogTag, "Invalid exercise character");
                throw new CommonException(CommonResultCode.InvalidInternalState);
            }*/
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

           /* m_characterExerciseItems = new AlphabetDatabase.CharacterExerciseItemInfo[exercises.length];

            // Sort exercises in m_characterExerciseItems according to menu_position
            {
                Map<Integer, AlphabetDatabase.CharacterExerciseItemInfo> sortedExercises = new TreeMap<>();

                //for (AlphabetDatabase.CharacterExerciseItemInfo exerciseInfo : exercises)
                //    sortedExercises.put(exerciseInfo.menuPosition, exerciseInfo);

                int exerciseIndex = 0;
                for (Map.Entry<Integer, AlphabetDatabase.CharacterExerciseItemInfo> exerciseInfo : sortedExercises.entrySet())
                    m_characterExerciseItems[exerciseIndex++] = exerciseInfo.getValue();
            }
            */
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
            //final String CharacterCaption = "[" + m_character + "]";
           // textView.setText(CharacterCaption);
        }
        // process menu items list view
        {
            ArrayAdapter<String> menuItems = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

            //for (AlphabetDatabase.CharacterExerciseItemInfo characterExerciseInfo : m_characterExerciseItems)
            //    menuItems.add(characterExerciseInfo.displayName);

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
        //CharacterExerciseItemActivity.startActivity(this, m_characterExerciseItems[itemSelectedIndex].id);
    }

    private FileTracerInstance m_tracer;
    private Map<AlphabetDatabase.CharacterExerciseItemType, Integer> m_characterExerciseItems;
    private AlphabetDatabase m_alphabetDatabase;
    private int m_characterExerciseId;
}
