package ru.po_znaika.alphabet;

import java.util.Arrays;
import java.util.Comparator;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.alphabet.database.diary.DiaryDatabase;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

public class CharacterExerciseMenuActivity extends Activity
{
    public static void startActivity(@NonNull Context context)
    {
        Intent intent = new Intent(context, CharacterExerciseMenuActivity.class);
        context.startActivity(intent);
    }

    private static final String LogTag = CharacterExerciseMenuActivity.class.getName();
    private static final String PageNumberTag = "page_number";

    private static final int ExercisesPerPage = 3*4;

    private static class ExerciseDisplayInfo
    {
        public String displayImage;
        public int characterExerciseId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_character_exercise_menu);

        setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try
        {
            m_tracer = TracerHelper.continueOrCreateFileTracer(this, savedInstanceState);
            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onCreate");

            restoreInternalState(savedInstanceState);
            constructUserInterface();
        }
        catch (Throwable exp)
        {
            Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.failed_exercise_start),
                    resources.getString(R.string.alert_title),
                    false,
                    new DialogInterface.OnClickListener()
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

        m_tracer.resume();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstance)
    {
        super.onSaveInstanceState(savedInstance);

        ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onSaveInstanceState");

        try
        {
            savedInstance.putInt(PageNumberTag, m_pageNumber);
            FileTracerInstance.saveInstance(m_tracer, savedInstance);
        }
        catch (Exception exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            throw exp;
        }
    }

    private void restoreInternalState(Bundle savedInstance) throws CommonException
    {
        if (savedInstance == null)
            m_pageNumber = 0;
        else
            m_pageNumber = savedInstance.getInt(PageNumberTag, 0);

        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(this, false);
        final AlphabetDatabase.CharacterExerciseInfo[] characterExercises =
                alphabetDatabase.getAllCharacterExercisesByAlphabetType(AlphabetDatabase.AlphabetType.Russian);
        if (characterExercises == null)
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Error, LogTag, "Failed to get character exercises");
            throw new CommonException(CommonResultCode.AssertError);
        }

        Arrays.sort(characterExercises, new Comparator<AlphabetDatabase.CharacterExerciseInfo>()
        {
            @Override
            public int compare(@NonNull AlphabetDatabase.CharacterExerciseInfo lhs
                    , @NonNull AlphabetDatabase.CharacterExerciseInfo rhs)
            {
                // Character.compare
                if (lhs.character == rhs.character)
                    return 0;
                return lhs.character < rhs.character ? -1 : 1;
            }
        });

        final DiaryDatabase diaryDatabase = new DiaryDatabase(this);
        m_characterExercises = new ExerciseDisplayInfo[characterExercises.length];
        for (int chExerciseIndex = 0; chExerciseIndex < characterExercises.length; ++chExerciseIndex)
        {
            final AlphabetDatabase.CharacterExerciseInfo currentCharacterExercise = characterExercises[chExerciseIndex];
            final AlphabetDatabase.CharacterExerciseItemInfo[] characterExerciseItems =
                    alphabetDatabase.getAllCharacterExerciseItemsByCharacterExerciseId(currentCharacterExercise.id);

            if ((characterExerciseItems == null) || (characterExerciseItems.length == 0))
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("No character exercise items for %d", currentCharacterExercise.id));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            boolean areAllExercisesComplete = true;
            for (AlphabetDatabase.CharacterExerciseItemInfo characterExerciseItem : characterExerciseItems)
            {
                final int exerciseScore = diaryDatabase.getExerciseScore(characterExerciseItem.exerciseInfo.name);
                if (exerciseScore < characterExerciseItem.exerciseInfo.maxScore)
                {
                    areAllExercisesComplete = false;
                    break;
                }
            }

            final ExerciseDisplayInfo currentExercise = new ExerciseDisplayInfo();
            currentExercise.displayImage = areAllExercisesComplete
                    ? currentCharacterExercise.passedImageName : currentCharacterExercise.notPassedImageName;
            currentExercise.characterExerciseId = currentCharacterExercise.id;
            m_characterExercises[chExerciseIndex] = currentExercise;
        }
    }

    private void constructUserInterface() throws CommonException
    {
        drawCharacterExercises();

        ImageView backImageView = (ImageView)findViewById(R.id.backImageView);
        backImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (m_pageNumber == 0)
                {
                    finish();
                    return;
                }

                --m_pageNumber;
                try
                {
                    drawCharacterExercises();
                }
                catch (CommonException exp)
                {
                    ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
                    AlertDialogHelper.showMessageBox(CharacterExerciseMenuActivity.this
                            , getResources().getString(R.string.error_unknown_error)
                            , getResources().getString(R.string.alert_title)
                            , false
                            , new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    finish();
                                }
                            }
                    );
                }
            }
        });

        final boolean isForwardAvailable = (m_pageNumber + 1) * ExercisesPerPage < m_characterExercises.length;

        ImageView forwardImageView = (ImageView)findViewById(R.id.forwardImageView);
        forwardImageView.setVisibility(isForwardAvailable ? View.VISIBLE : View.INVISIBLE);
        forwardImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!isForwardAvailable)
                    return;
                ++m_pageNumber;

                try
                {
                    drawCharacterExercises();
                }
                catch (Exception exp)
                {
                    ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
                    AlertDialogHelper.showMessageBox(CharacterExerciseMenuActivity.this
                            , getResources().getString(R.string.error_unknown_error)
                            , getResources().getString(R.string.alert_title)
                            , false
                            , new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    finish();
                                }
                            }
                    );
                }
            }
        });
    }

    private void drawCharacterExercises() throws CommonException
    {
        ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "Draw menu, page number: " + m_pageNumber);

        final int[] imageViewIds = new int[]
                {
                        R.id.characterImageView1,
                        R.id.characterImageView2,
                        R.id.characterImageView3,
                        R.id.characterImageView4,
                        R.id.characterImageView5,
                        R.id.characterImageView6,
                        R.id.characterImageView7,
                        R.id.characterImageView8,
                        R.id.characterImageView9,
                        R.id.characterImageView10,
                        R.id.characterImageView11,
                        R.id.characterImageView12,
                };

        final int ExerciseIterateIndex = Math.min(m_characterExercises.length - ExercisesPerPage * m_pageNumber, imageViewIds.length);
        final Resources resources = getResources();
        for (int exerciseIndex = 0; exerciseIndex < ExerciseIterateIndex; ++exerciseIndex)
        {
            final int exerciseOffsetIndex = ExercisesPerPage * m_pageNumber + exerciseIndex;
            final ExerciseDisplayInfo exerciseDisplayInfo = m_characterExercises[exerciseOffsetIndex];
            if (exerciseDisplayInfo == null)
                continue;

            final int drawableId = DatabaseHelpers.getDrawableIdByName(resources, exerciseDisplayInfo.displayImage);
            if (drawableId == 0)
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Failed to get image resource id for '%s'", exerciseDisplayInfo.displayImage));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            final Drawable drawable = resources.getDrawable(drawableId);
            if (drawable == null)
            {
                ProductTracer.traceMessage(m_tracer
                        , TraceLevel.Error
                        , LogTag
                        , String.format("Failed to get drawable for id '%d", drawableId));
            }

            ImageView imageView = (ImageView)findViewById(imageViewIds[exerciseIndex]);
            imageView.setImageDrawable(drawable);

            final int characterExerciseId = exerciseDisplayInfo.characterExerciseId;
            imageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        ProductTracer.traceMessage(m_tracer
                                , TraceLevel.Info
                                , LogTag
                                , String.format("Start exercise for character exercise id: '%d'", characterExerciseId));
                        SingleCharacterExerciseMenuActivity.startActivity(CharacterExerciseMenuActivity.this
                                , characterExerciseId);
                    }
                    catch (Exception exp)
                    {
                        ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
                        AlertDialogHelper.showMessageBox(CharacterExerciseMenuActivity.this,
                                getResources().getString(R.string.alert_title),
                                getResources().getString(R.string.failed_exercise_start));
                    }
                }
            });
        }
    }

    private int m_pageNumber;
    private FileTracerInstance m_tracer;
    private ExerciseDisplayInfo[] m_characterExercises;
}
