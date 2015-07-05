package ru.po_znaika.alphabet;

import android.app.Fragment;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Created by Rihter on 06.03.2015.
 * Creates steps fragments for CharacterExercise
 */
public final class CharacterExerciseActionsFactory implements CharacterExerciseStepManager.IExerciseStepFactory
{
    private enum CustomAction
    {
        ObjectsContainingSound(0),
        SelectPictureWithSound(1),
        SelectPictureWithCharacter(2),
        FindCharacter(3);

        private static Map<Integer, CustomAction> ValuesMap = new HashMap<Integer, CustomAction>()
        {
            { put(ObjectsContainingSound.getValue(), ObjectsContainingSound); }
            { put(SelectPictureWithSound.getValue(), SelectPictureWithSound); }
            { put(SelectPictureWithCharacter.getValue(), SelectPictureWithCharacter); }
            { put(FindCharacter.getValue(), FindCharacter); }
        };

        public static CustomAction parse(int value)
        {
            return ValuesMap.get(value);
        }

        CustomAction(int _value)
        {
            m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private int m_value;
    }

    private static class ObjectExerciseContainer
    {
        public AlphabetDatabase.ContainRelationship containRelationship;
        public int exercisesCount;
        public int exerciseTitleResourceId;

        public ObjectExerciseContainer(AlphabetDatabase.ContainRelationship _containRelationship,
                                       int _exercisesCount,
                                       int _exerciseTitleResourceId)
        {
            this.containRelationship = _containRelationship;
            this.exercisesCount = _exercisesCount;
            this.exerciseTitleResourceId = _exerciseTitleResourceId;
        }
    }

    private static final int CorrectImagesInExerciseStep = 1;

    private static final int ContainsSoundImageExercisesCount = 1;
    private static final int BeginsSoundImageExercisesCount = 1;
    private static final int EndsSoundImageExercisesCount = 1;

    private static final int VerseMinSearchCharactersCount = 3;
    private static final int VerseMaxCharactersCount = 100;

    public CharacterExerciseActionsFactory(int _characterExerciseId, @NonNull Context _context,
                                           @NonNull AlphabetDatabase _alphabetDatabase)
    {
        this(_characterExerciseId, 0, _context, _alphabetDatabase);
    }

    public CharacterExerciseActionsFactory(int _characterExerciseId
            , int exerciseLogoId
            , @NonNull Context _context
            , @NonNull AlphabetDatabase _alphabetDatabase)
    {
        m_characterExerciseId = _characterExerciseId;
        m_exerciseLogoId = exerciseLogoId;
        m_context = _context;
        m_alphabetDatabase = _alphabetDatabase;
    }

    public Fragment createExerciseStep(@NonNull AlphabetDatabase.CharacterExerciseActionType actionType,
                                int value) throws CommonException
    {
        Fragment resultFragment = null;
        switch (actionType)
        {
            case CustomAction:
                final CustomAction customAction = CustomAction.parse(value);
                if (customAction == null)
                    throw new CommonException(CommonResultCode.InvalidArgument);

                resultFragment = createCustomActionFragment(customAction);
                break;

            case TheoryPage:
                resultFragment = TheoryPageFragment.createFragment(value, m_exerciseLogoId);
                break;
        }

        return resultFragment;
    }

    private Fragment createCustomActionFragment(@NonNull CustomAction customAction)
    {
        Fragment resultFragment = null;

        try
        {
            switch (customAction)
            {
                case ObjectsContainingSound:
                {
                    resultFragment = CharacterMultipleObjectsFragment.CreateFragment(m_characterExerciseId);
                }
                break;

                case SelectPictureWithCharacter:
                {
                    final ObjectExerciseContainer[] objectInfoExercises = new ObjectExerciseContainer[]
                            {
                                    new ObjectExerciseContainer(AlphabetDatabase.ContainRelationship.Contain, ContainsSoundImageExercisesCount, R.string.caption_object_contains_character),
                                    new ObjectExerciseContainer(AlphabetDatabase.ContainRelationship.Begin, BeginsSoundImageExercisesCount, R.string.caption_object_begins_character),
                                    new ObjectExerciseContainer(AlphabetDatabase.ContainRelationship.End, EndsSoundImageExercisesCount, R.string.caption_object_ends_character)
                            };

                    final AlphabetDatabase.CharacterExerciseInfo characterExerciseInfo = m_alphabetDatabase.getCharacterExerciseById(m_characterExerciseId);
                    if (characterExerciseInfo == null)
                        throw new CommonException(CommonResultCode.InvalidExternalSource);

                    List<ImageSelectionSingleExerciseState> imageSelectionExercises = new ArrayList<>();

                    Resources resources = m_context.getResources();
                    Random random = new Random(System.currentTimeMillis());
                    for (ObjectExerciseContainer objectExercise : objectInfoExercises)
                    {
                        AlphabetDatabase.WordObjectInfo[] thisTypeWords = m_alphabetDatabase.getRandomImageWords(characterExerciseInfo.alphabetType,
                                characterExerciseInfo.character,
                                objectExercise.containRelationship,
                                false,
                                CorrectImagesInExerciseStep);
                        if ((thisTypeWords == null) || (thisTypeWords.length != CorrectImagesInExerciseStep))
                            throw new CommonException(CommonResultCode.InvalidExternalSource);
                        AlphabetDatabase.WordObjectInfo[] otherTypeWords = m_alphabetDatabase.getRandomImageWords(characterExerciseInfo.alphabetType,
                                characterExerciseInfo.character,
                                objectExercise.containRelationship,
                                true,
                                ImageSelectionFragment.ImagesCount - CorrectImagesInExerciseStep);
                        if ((otherTypeWords == null) || (otherTypeWords.length != ImageSelectionFragment.ImagesCount - CorrectImagesInExerciseStep))
                            throw new CommonException(CommonResultCode.InvalidExternalSource);

                        final int correctAnswerIndex = random.nextInt(ImageSelectionFragment.ImagesCount);

                        ImageSelectionSingleExerciseState exerciseStep = new ImageSelectionSingleExerciseState();
                        final String exerciseTitleTemplate = resources.getString(objectExercise.exerciseTitleResourceId);
                        if (exerciseTitleTemplate == null)
                            throw new CommonException(CommonResultCode.InvalidExternalSource);

                        exerciseStep.exerciseTitle = String.format(exerciseTitleTemplate, characterExerciseInfo.character);

                        exerciseStep.selectionVariants = new ObjectDescription[ImageSelectionFragment.ImagesCount];
                        int otherTypeWordIndex = 0;
                        for (int imageIndex = 0; imageIndex < ImageSelectionFragment.ImagesCount; ++imageIndex)
                        {
                            exerciseStep.answerIndex = correctAnswerIndex;

                            final AlphabetDatabase.WordObjectInfo processedWord = (imageIndex == correctAnswerIndex) ?
                                    thisTypeWords[0] : otherTypeWords[otherTypeWordIndex++];

                            ObjectDescription objectDescription = new ObjectDescription(processedWord.imageFilePath,
                                    processedWord.soundFilePath,
                                    processedWord.word.word);

                            exerciseStep.selectionVariants[imageIndex] = objectDescription;
                        }
                        imageSelectionExercises.add(exerciseStep);
                    }
                    resultFragment = ImageSelectionFragment.createFragment(imageSelectionExercises);
                }
                break;

                case FindCharacter:
                {
                    final AlphabetDatabase.CharacterExerciseInfo exerciseInfo =
                            m_alphabetDatabase.getCharacterExerciseById(m_characterExerciseId);
                    if (exerciseInfo == null)
                        throw new CommonException(CommonResultCode.InvalidExternalSource);

                    final String verseText = m_alphabetDatabase.getVerseTextByAlphabet(exerciseInfo.alphabetType,
                            exerciseInfo.character, VerseMinSearchCharactersCount, VerseMaxCharactersCount);
                    if (TextUtils.isEmpty(verseText))
                        throw new CommonException(CommonResultCode.InvalidExternalSource);

                    resultFragment = FindCharacterFragment.createFragment(verseText, exerciseInfo.character);
                }
                break;
            }
        }
        catch (Exception exp)
        {
            resultFragment = null;
        }

        return resultFragment;
    }

    private int m_characterExerciseId;
    private int m_exerciseLogoId;
    private Context m_context;
    private AlphabetDatabase m_alphabetDatabase;
}
