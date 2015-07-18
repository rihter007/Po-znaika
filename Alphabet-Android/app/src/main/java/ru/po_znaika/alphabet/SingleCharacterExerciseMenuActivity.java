package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;
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

    private class CharacterExerciseItem
    {
        public CharacterExerciseItem(int _id, @NonNull ExerciseScoreType _scoreType)
        {
            this.id = _id;
            this.scoreType = _scoreType;
        }

        public int id;
        public ExerciseScoreType scoreType;
    }

    private static int getExerciseImageId(@NonNull AlphabetDatabase.CharacterExerciseItemType exerciseItemType
            ,@NonNull ExerciseScoreType scoreType)
    {
        switch (exerciseItemType)
        {
            case CharacterHandWrite:
            {
                switch (scoreType)
                {
                    case Initial:
                        return R.drawable.cloud_handwrite_character_initial;
                    case Started:
                        return R.drawable.cloud_handwrite_character_started;
                    case Completed:
                        return R.drawable.cloud_handwrite_character_completed;
                }
            }
            break;

            case CharacterPrint:
            {
                switch (scoreType)
                {
                    case Initial:
                        return R.drawable.cloud_print_character_initial;
                    case Started:
                        return R.drawable.cloud_print_character_started;
                    case Completed:
                        return R.drawable.cloud_print_character_completed;
                }
            }
            break;

            case CharacterSound:
            {
                switch (scoreType)
                {
                    case Initial:
                        return R.drawable.cloud_character_sound_intial;
                    case Started:
                        return R.drawable.cloud_character_sound_started;
                    case Completed:
                        return R.drawable.cloud_character_sound_completed;
                }
            }
            break;

            case FindCharacter:
            {
                switch (scoreType)
                {
                    case Initial:
                        return R.drawable.cloud_select_character_initial;
                    case Started:
                        return R.drawable.cloud_select_character_started;
                    case Completed:
                        return R.drawable.cloud_select_character_completed;
                }
            }
            break;

            case FindPictureWithCharacter:
            {
                switch (scoreType)
                {
                    case Initial:
                        return R.drawable.cloud_select_image_initial;
                    case Started:
                        return R.drawable.cloud_select_image_started;
                    case Completed:
                        return R.drawable.cloud_select_image_completed;
                }
            }
            break;
        }

        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        try
        {
            setContentView(R.layout.activity_single_character_exercise_menu);

            setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

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
        int characterExerciseId;
        {
            final Bundle intentInfo = getIntent().getExtras();

            characterExerciseId = intentInfo.getInt(CharacterExerciseIdTag);
            if (characterExerciseId == DatabaseConstant.InvalidDatabaseIndex)
            {
                ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Failed to get character exercise id");
                throw new CommonException(CommonResultCode.InvalidInternalState);
            }
        }

        // Prepare databases
        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(this, false);
        DiaryDatabase diaryDatabase = new DiaryDatabase(this);

        // get character icon
        final AlphabetDatabase.CharacterExerciseInfo characterExerciseInfo =
                alphabetDatabase.getCharacterExerciseById(characterExerciseId);
        if (characterExerciseInfo == null)
        {
            ProductTracer.traceMessage(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , String.format("Failed to get character exercise info for id: '%d'", characterExerciseId));
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        }

        m_characterIconId = DatabaseHelpers.getDrawableIdByName(getResources(), characterExerciseInfo.passedImageName);
        if (m_characterIconId == 0)
        {
            ProductTracer.traceMessage(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , String.format("Failed to get character icon image id for '%s'", characterExerciseInfo.passedImageName));
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        }

        // Prepare menu exercises
        final AlphabetDatabase.CharacterExerciseItemInfo[] exercises =
                alphabetDatabase.getAllCharacterExerciseItemsByCharacterExerciseId(characterExerciseId);
        if (exercises == null)
        {
            ProductTracer.traceMessage(m_tracer
                    , TraceLevel.Error
                    , LogTag
                    , String.format("Failed to get character exercise items for '%d'", characterExerciseId));
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        }

        m_characterExerciseItems = new HashMap<>();
        for (AlphabetDatabase.CharacterExerciseItemInfo exerciseItemInfo : exercises)
        {
            ExerciseScoreType scoreType = ExerciseScoreType.Completed;

            final int currentExerciseScore = diaryDatabase.getExerciseScore(exerciseItemInfo.exerciseInfo.name);
            if (diaryDatabase.getExerciseScore(exerciseItemInfo.exerciseInfo.name) <
                    exerciseItemInfo.exerciseInfo.maxScore)
            {
                scoreType = currentExerciseScore == 0 ? ExerciseScoreType.Initial : ExerciseScoreType.Started;
            }

            if (m_characterExerciseItems.containsKey(exerciseItemInfo.menuElementType))
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Menu element '%s' already exists", exerciseItemInfo.menuElementType.toString()));
            }

            m_characterExerciseItems.put(exerciseItemInfo.menuElementType
                    , new CharacterExerciseItem(exerciseItemInfo.id, scoreType));
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface()
    {
        {
            ImageView characterIconImageView = (ImageView)findViewById(R.id.exerciseIconImageView);
            characterIconImageView.setImageDrawable(getResources().getDrawable(m_characterIconId));
        }

        {
            ImageView backImageView = (ImageView)findViewById(R.id.backImageView);
            backImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    finish();
                }
            });
        }

        {
            final List<Pair<AlphabetDatabase.CharacterExerciseItemType, Integer>> exercisesInfo =
                    new ArrayList<Pair<AlphabetDatabase.CharacterExerciseItemType, Integer>>()
                    {
                        {
                            add(new Pair<>(AlphabetDatabase.CharacterExerciseItemType.CharacterSound
                                    , R.id.characterSoundImageView));
                            add(new Pair<>(AlphabetDatabase.CharacterExerciseItemType.CharacterPrint
                                    , R.id.characterPrintImageView));
                            add(new Pair<>(AlphabetDatabase.CharacterExerciseItemType.CharacterHandWrite
                                    , R.id.characterHandwriteImageView));
                            add(new Pair<>(AlphabetDatabase.CharacterExerciseItemType.FindCharacter
                                    , R.id.selectCharacterImageView));
                            add(new Pair<>(AlphabetDatabase.CharacterExerciseItemType.FindPictureWithCharacter
                                    , R.id.selectImageImageView));
                        }
                    };

            for (Pair<AlphabetDatabase.CharacterExerciseItemType, Integer> exerciseUI : exercisesInfo)
            {
                ImageView characterExerciseImageView = (ImageView)findViewById(exerciseUI.second);
                final CharacterExerciseItem exerciseItem = m_characterExerciseItems.get(exerciseUI.first);
                if (exerciseItem != null)
                {
                    characterExerciseImageView.setVisibility(View.VISIBLE);
                    characterExerciseImageView.setImageDrawable(getResources().getDrawable(
                            getExerciseImageId(exerciseUI.first, exerciseItem.scoreType)));
                    final int characterExerciseItemId = exerciseItem.id;
                    characterExerciseImageView.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            CharacterExerciseItemActivity.startActivity(SingleCharacterExerciseMenuActivity.this
                                    , characterExerciseItemId);
                        }
                    });
                }
                else
                {
                    characterExerciseImageView.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private FileTracerInstance m_tracer;
    private int m_characterIconId;
    private Map<AlphabetDatabase.CharacterExerciseItemType, CharacterExerciseItem> m_characterExerciseItems;
}
