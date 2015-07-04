package ru.po_znaika.alphabet.database.exercise;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileOutputStream;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

/**
 * Created by Rihter on 07.08.2014.
 * Help tutorial = http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 */
public final class AlphabetDatabase
{
    /**
     * Types declarations
     */
    public enum SoundType
    {
        Correct(78467623),                     // crc32 of 'Correct'
        Praise(-1022835248),                   // crc32 of 'Praise'
        TryAgain(2010528955);                  // crc32 of 'TryAgain'

        private int m_value;

        SoundType(int _value)
        {
            this.m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private static final Map<Integer, SoundType> ValuesMap = new HashMap<Integer, SoundType>()
        {{
                put(78467623, Correct);
                put(-1022835248, Praise);
                put(2010528955, TryAgain);
            }};

        public static SoundType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }
    }

    /**
     * Represents hardcoded type of an exercise in Alphabet studies
     */
    public enum ExerciseType
    {
        Character(294335127),                   // crc32 of 'Character'
        WordGather(402850721),                  // crc32 of 'WordGather'
        CreateWordsFromSpecified(-858355490);   // crc32 of 'CreateWordsFromSpecified'

        private int m_value;

        ExerciseType(int _value)
        {
            this.m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private static final Map<Integer, ExerciseType> ValuesMap = new HashMap<Integer, ExerciseType>()
        {{
                put(294335127, Character);
                put(402850721, WordGather);
                put(-858355490, CreateWordsFromSpecified);
            }};

        public static ExerciseType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }
    }

    public enum CharacterExerciseItemType
    {
        CharacterSound(260157427),            // a crc32 of 'CharacterExerciseItemType.CharacterSound'
        CharacterPrint(-489091055),           // a crc32 of 'CharacterExerciseItemType.CharacterPrint'
        CharacterHandWrite(1022598804),       // a crc32 of 'CharacterExerciseItemType.CharacterHandWrite'
        FindPictureWithCharacter(-954576837), // a crc32 of 'CharacterExerciseItemType.FindPictureWithCharacter'
        FindCharacter(-226661029);            // a crc32 of 'CharacterExerciseItemType.FindCharacter'

        CharacterExerciseItemType(int value)
        {
            m_value = value;
        }

        public int getValue()
        {
            return m_value;
        }

        private static Map<Integer, CharacterExerciseItemType> ValuesMap = new HashMap<Integer, CharacterExerciseItemType>()
        {
            {
                put(CharacterSound.getValue(), CharacterSound);
                put(CharacterPrint.getValue(), CharacterPrint);
                put(CharacterHandWrite.getValue(), CharacterHandWrite);
                put(FindPictureWithCharacter.getValue(), FindPictureWithCharacter);
                put(FindCharacter.getValue(), FindCharacter);
            }
        };

        public static CharacterExerciseItemType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }

        private final int m_value;
    }

    public enum CharacterExerciseActionType
    {
        TheoryPage(1986991965),             // a crc32 of 'TheoryPage'
        CustomAction(291784361);            // a crc32 of 'CustomAction'

        CharacterExerciseActionType(int _value)
        {
            m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private static final Map<Integer, CharacterExerciseActionType> ValuesMap = new HashMap<Integer, CharacterExerciseActionType>()
        {{
                put(1986991965, TheoryPage);
                put(291784361, CustomAction);
            }};

        public static CharacterExerciseActionType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }

        private final int m_value;
    }

    public enum AlphabetType
    {
        Russian(-345575051);               // crc32 of 'russian'

        private int m_value;

        AlphabetType(int _value)
        {
            this.m_value = _value;
        }

        public int getValue()
        {
            return m_value;
        }

        private static final Map<Integer, AlphabetType> ValuesMap = new HashMap<Integer, AlphabetType>()
        {
            {
                put(Russian.getValue(), Russian);
            }
        };

        public static AlphabetType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }
    }

    /**
     * Represents exercise description in alphabet database
     */
    public static class ExerciseInfo
    {
        /* Unique id of an exercise */
        public int id;
        /* Type of an exercise */
        public ExerciseType type;
        /* Unique internal name if an exercise */
        public String name;
        /* Maximum score of the exercise */
        public int maxScore;
    }

    /**
     * Represents a single character exercise
     */
    public static class CharacterExerciseInfo
    {
        /* Unique identifier of character exercise*/
        public int id;
        /* Character that represents an exercise */
        public char character;
        /* Unique identifier that represents the alphabet id of the character */
        public AlphabetType alphabetType;

        /* Image name of exercise icon when exercise is not passed */
        public String notPassedImageName;
        /* Image name of exercise icon when exercise is passed */
        public String passedImageName;
    }

    /**
     * Represents single group of sub-exercises for selected character
     */
    public static class CharacterExerciseItemInfo
    {
        /* Raw element id */
        public int id;
        /* Information about the base exercise */
        public ExerciseInfo exerciseInfo;
        /* Identifier of the parent character exercise */
        public int characterExerciseId;
        /* Unique type of the menu element */
        public CharacterExerciseItemType menuElementType;
    }

    /**
     * Represents description of character exercise single step:[theory page/game/etc.]
     */
    public static class CharacterExerciseItemStepInfo
    {
        /* Database row identifier */
        public int id;
        /* Identifier of character exercise item to which step belongs to */
        public int characterExerciseItemId;

        /* Action which must be done */
        public CharacterExerciseActionType action;
        /* Specific value for action */
        public int value;
    }

    /**
     * Represents theory page for specific material
     */
    public static class TheoryPageInfo
    {
        public int id;

        /* Optional: image id for theory material */
        public int imageId;

        /* Optional: url to wich redirection is processed when clicked on image */
        public String imageRedirectUrl;

        /* Optional: sound id which contains oral theory description */
        public int soundId;

        /* Optional: Text information theory description */
        public String message;
    }

    /**
     * Represents a single word
     */
    public static class WordInfo
    {
        public int id;
        public AlphabetType alphabetType;
        public String word;

        /*Complexity of the words, higher is the number more complex is the word (longer, harder to pronounce, rare)*/
        public int complexity;
    }

    public enum ContainRelationship
    {
        Contain,
        Begin,
        End
    }

    public static class WordObjectInfo
    {
        public ContainRelationship containFlag;

        public WordInfo word;

        public String imageFilePath;
        public String soundFilePath;
    }

    /**
     * Describes information about sound for character exercise in specified word.
     */
    public static class SoundObjectInfo
    {
        /**
         * Sound flag values: Where the sound is positioned
         */
        public static final int Contain = 1;
        public static final int Begin = 2;
        public static final int End = 4;

        /* element identifier*/
        public int id;
        /* identifier of character exercise to which item belongs to */
        public int characterExerciseId;

        /* identifier of appropriate word */
        public WordInfo word;
        /* Specifies the relationship and sound of letter in character exercise */
        public int soundFlag;

        /* Specifies the image id of an object */
        public int imageId;
        /* Specifies the sound id of an object */
        public int soundId;
    }

    /**
     * General class constants
     */
    private final int DatabaseVersion = 0;
    private final String DatabaseName = "alphabet.db";

    /**
     * SQL-expressions from verse table
     */
    private static final String ExtractVerseTextByAlphabetIdSqlStatement =
            "SELECT verse_text " +
            "FROM verse " +
            "WHERE (alphabet_type = ?) AND (verse_text LIKE ?) AND (length(verse_text) < ?) " +
            "ORDER BY RANDOM() LIMIT 1";

    /**
     * SQL-expressions from exercise table
     */
    private static final String ExtractExerciseInfoByTypeSqlStatement =
            "SELECT _id, name, max_score FROM exercise WHERE type = ?";

    /**
     * SQL-expressions form character_exercise and relative tables
     */
    private static final String ExtractAllCharacterExercisesByAlphabetSqlStatement =
            "SELECT ce._id, ce.character, img1.file_name, img2.file_name " +
                    "FROM character_exercise ce, image img1, image img2 " +
                    "WHERE (img1._id = ce.not_passed_image_id) AND (img2._id = ce.passed_image_id) " +
                    "      AND (ce.alphabet_type = ?)";
    private static final String ExtractCharacterExerciseByIdSqlStatement =
            "SELECT ce.alphabet_type, ce.character, img1.file_name, img2.file_name " +
                    "FROM character_exercise ce, image img1, image img2 " +
                    "WHERE (img1._id = ce.not_passed_image_id) AND (img2._id = ce.passed_image_id) " +
                    "      AND (ce._id = ?)";

    private static final String ExtractCharacterItemByIdSqlStatement =
            "SELECT character_exercise_id, menu_position, name, display_name FROM character_exercise_item WHERE _id = ?";
    private static final String ExtractAllCharacterExerciseItemsByCharacterExerciseIdSqlStatement =
            "SELECT chi._id, chi.menu_element_type, ex._id, ex.type, ex.name, ex.max_score " +
                    "FROM character_exercise_item chi, exercise ex " +
                    "WHERE (chi.exercise_id = ex._id) AND (character_exercise_id = ?)";
    private static final String ExtractAllCharacterExerciseStepsByCharacterExerciseItemIdSqlStatement =
            "SELECT _id, step_number, action, value FROM character_exercise_item_step WHERE character_exercise_item_id = ?";
    private static final String ExtractCharacterItemDisplayNameByIdSqlStatement =
            "SELECT display_name FROM character_exercise_item WHERE _id = ?";
    private static final String ExtractCharacterItemDisplayNameByIdNameSqlStatement =
            "SELECT display_name FROM character_exercise_item WHERE name = ?";

    /**
     * SQL-expressions from sound_words table
     */
    private static final String ExtractRandomSoundWordsByCharacterExerciseIdAndFlagSqlStatement =
            "SELECT sw._id, sw.word_id, sw.sound_flag, w.alphabet_id, w.word, w.complexity, wid.image_id, wsd.sound_id " +
                    "FROM sound_words sw, word w, word_image_description wid, word_sound_description wsd " +
                    "WHERE (sw.character_exercise_id = ?) AND (sw.sound_flag & ? <> 0) AND " +
                    "(sw.word_id = w._id) AND (w._id = wid.word_id) AND (w._id = wsd.word_id) GROUP BY sw.word_id ORDER BY RANDOM() LIMIT ?";

    private static final String ExtractRandomSoundWordsByCharacterExerciseIdNotMatchFlagSqlStatement =
            "SELECT sw._id, sw.word_id, sw.sound_flag, w.alphabet_id, w.word, w.complexity, wid.image_id, wsd.sound_id " +
                    "FROM sound_words sw,word w, word_image_description wid, word_sound_description wsd " +
                    "WHERE (sw.word_id = w._id) AND (w._id = wid.word_id) AND (w._id = wsd.word_id) AND " +
                    "sw.word_id NOT IN(SELECT word_id FROM sound_words WHERE (character_exercise_id = ?) AND (sound_flag & ? <> 0)) " +
                    "GROUP BY sw.word_id ORDER BY RANDOM() LIMIT ?";

    //private static final String ExtractRandomSoundWordsByCharacterExerciseIdNotMatchFlagSqlStatement =
    //        "SELECT sw._id, sw.word_id, sw.sound_flag, w.alphabet_id, w.word, w.complexity, wid.image_id, wsd.sound_id " +
    //                "FROM sound_words sw, word w, word_image_description wid, word_sound_description wsd " +
    //                "WHERE (NOT ((sw.character_exercise_id = ?) AND (sw.sound_flag & ? <> 0))) AND " +
    //                "(sw.word_id = w._id) AND (w._id = wid.word_id) AND (w._id = wsd.word_id) GROUP BY sw.word_id ORDER BY RANDOM() LIMIT ?";
    /*
    private static final String ExtractRandomSoundWordsByCharacterExerciseIdAndFlagSqlStatement =
            "SELECT sw._id, sw.word_id, sw.sound_flag, w.alphabet_id, w.word, w.complexity " +
                    "FROM sound_words sw, word w " +
                    "WHERE (sw.word_id = w._id) AND (sw.character_exercise_id = ?) AND (sw.sound_flag & ? <> 0) " +
                    "GROUP BY sw.word_id ORDER BY RANDOM() LIMIT ?";
    private static final String ExtractRandomSoundWordsByCharacterExerciseIdNotMatchFlagSqlStatement =
            "SELECT sw._id, sw.word_id, sw.sound_flag, w.alphabet_id, w.word, w.complexity " +
                    "FROM sound_words sw, word w " +
                    "WHERE (sw.word_id = w._id) AND (NOT ((sw.character_exercise_id = ?) AND (sw.sound_flag & ? <> 0))) " +
                    "GROUP BY sw.word_id ORDER BY RANDOM() LIMIT ?";*/

    /**
     * SQL-expressions for word tables
     */
    private static final String ExtractWordsByLengthAndAlphabetId =
            "SELECT _id, alphabet_id, complexity, word " +
                    "FROM word " +
                    "WHERE (alphabet_id = ?) AND (length(word) >= %d) AND (length(word) <= %d)";
    private static final String ExtractRandomWordByAlphabetIdAndLength =
            "SELECT _id, alphabet_id, complexity, word " +
                    "FROM word " +
                    "WHERE (alphabet_id = ?) AND (length(word) >= %d) AND (length(word) <= %d) ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomWordCreationExerciseByAlphabetAndLength =
            "SELECT w._id, w.complexity, w.word " +
                    "FROM word_creation_exercise wce, word w " +
                    "WHERE (wce.word_id = w._id) AND (w.alphabet_id = ?) AND (length(w.word) >= %d) AND (length(w.word) <= %d) ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomWordAndImageByAlphabetAndLength =
            "SELECT w._id, w.word, w.complexity, wid.image_id " +
                    "FROM word w, word_image_description wid " +
                    "WHERE (w.alphabet_id = ?) AND (length(w.word) >= %d) AND (length(w.word) <= %d) AND (w._id = wid.word_id) ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomImageIdByWordId = "SELECT image_id FROM word_image_description wid " +
            "WHERE wid.word_id = ? ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomSoundIdByWordId = "SELECT sound_id FROM word_sound_description wsd " +
            "WHERE wsd.word_id = ? ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomWordAndImageByPatternAndAlphabetSqlStatement =
            "SELECT w._id, w.word, w.complexity, img.file_name " +
            "FROM word w, word_image_description wid, image img " +
            "WHERE (w.alphabet_id = ?) AND (w._id = wid.word_id) AND (img._id = wid.image_id) " +
            "AND (w.word LIKE ?) " +
            "ORDER BY RANDOM() LIMIT ?";
    private static final String ExtractRandomWordAndImageNotByPatternAndAlphabetSqlStatement =
            "SELECT w._id, w.word, w.complexity, img.file_name " +
            "FROM word w, word_image_description wid, image img " +
            "WHERE (w.alphabet_id = ?) AND (w._id = wid.word_id) AND (img._id = wid.image_id) " +
            "AND (NOT w.word LIKE ?) " +
            "ORDER BY RANDOM() LIMIT ?";

    /**
     * SQL-expressions for theory_page table
     */
    private static final String ExtractTheoryPageById =
            "SELECT iamge_id, image_redirect_url, sound_id, message " +
            "FROM theory_page " +
            "WHERE _id = ?";

    /**
     * SQL-expressions with image table
     */
    private static final String ExtractImageNameByIdSqlStatement = "SELECT file_name FROM image WHERE _id = ?";

    /**
     * SQL-expressions with sound table
     */
    private static final String ExtractSoundNameByIdSqlStatement = "SELECT file_name FROM sound WHERE _id = ?";

    /**
     * SQL-expressions with special_sound table
     */
    private static final String ExtractRandomSoundNameByTypeSqlStatement = "SELECT s.file_name FROM sound s, special_sound ss " +
            "WHERE (s._id = ss.sound_id) AND (ss.sound_type = ?) ORDER BY RANDOM() LIMIT 1";

    public AlphabetDatabase(Context _context, boolean _failIfNotFound) throws CommonException
    {
        ///
        /// Initialize connection to database
        ///

        // Construct path to database
        m_pathToDatabase = _context.getApplicationInfo().dataDir + File.separator + DatabaseConstant.DatabaseFolder + File.separator + DatabaseName;

        m_databaseConnection = createNewDatabaseConnection();

        boolean isSucceededOpeningDatabase = m_databaseConnection != null;
        if (!isSucceededOpeningDatabase)
        {
            // try to create database if allowed
            if (_failIfNotFound)
            {
                isSucceededOpeningDatabase = false;
            }
            else
            {
                isSucceededOpeningDatabase = createDatabase(_context);
            }
        }
        else
        {
            // upgrade database if versions do not match
            int currentDatabaseVersion = m_databaseConnection.getVersion();
            if (currentDatabaseVersion != DatabaseVersion)
            {
                isSucceededOpeningDatabase = updateDatabase(_context, currentDatabaseVersion);
            }
        }

        if (!isSucceededOpeningDatabase)
            throw new CommonException(CommonResultCode.UnknownReason);
    }

    private SQLiteDatabase createNewDatabaseConnection()
    {
        final String pathToDatabase = getDatabasePath();

        SQLiteDatabase databaseConnection = null;
        try
        {
            databaseConnection = SQLiteDatabase.openDatabase(pathToDatabase, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS);
        }
        catch (SQLiteException exp)
        {
            Log.e(AlphabetDatabase.class.getName(), String.format("Failed to open database, error=\"%s\"", exp.getMessage()));
        }

        return databaseConnection;
    }

    /**
     * Gets database from assets folder
     *
     * @return
     * True if database is successfully created, False otherwise
     */
    private boolean createDatabase(Context context)
    {
        boolean result = false;
        InputStream assetsDatabaseFileStream = null;
        FileOutputStream localFileDatabaseStream = null;

        try
        {
            assetsDatabaseFileStream = context.getAssets().open(DatabaseName);

            //Open the empty db as the output stream
            {
                File databaseDirectory = new File(context.getApplicationInfo().dataDir + File.separator + DatabaseConstant.DatabaseFolder);
                databaseDirectory.mkdir();
            }

            File localFileDatabase = new File(getDatabasePath());
            if (!localFileDatabase.createNewFile())
                throw new RuntimeException("Failed to create new file");
            localFileDatabaseStream = new FileOutputStream(localFileDatabase);

            {
                byte[] buffer = new byte[10 * 1024];
                int length = 0;
                while ((length = assetsDatabaseFileStream.read(buffer)) > 0)
                {
                    localFileDatabaseStream.write(buffer, 0, length);
                }
            }
            localFileDatabaseStream.flush();

            m_databaseConnection = createNewDatabaseConnection();
            result = m_databaseConnection != null;
        }
        catch (IOException ioExp)
        {
            result = false;
        }
        finally
        {
            try
            {
                if (assetsDatabaseFileStream != null)
                    assetsDatabaseFileStream.close();

                if (localFileDatabaseStream != null)
                {
                    localFileDatabaseStream.flush();
                    localFileDatabaseStream.close();
                }
            }
            catch (IOException ioExp)
            {
                // might never happen
            }
        }

        return result;
    }

    private boolean updateDatabase(Context context, int oldVersion)
    {
        m_databaseConnection.close();

        return createDatabase(context);
    }

    public String getDatabasePath()
    {
        return m_pathToDatabase;
    }

    public String getVerseTextByAlphabet(@NonNull AlphabetType alphabetType, char character
            , int minSearchCharCount, int maxCharactersCount)
    {
        Cursor dataReader = null;

        try
        {
            final Character characterObject = character;

            String characterCountCondition = "%";
            for (int i = 0; i < minSearchCharCount; ++i)
                characterCountCondition = characterCountCondition + characterObject + "%";

            dataReader = m_databaseConnection.rawQuery(ExtractVerseTextByAlphabetIdSqlStatement, new String[]
                    {
                            ((Integer) alphabetType.getValue()).toString(),
                            characterCountCondition,
                            ((Integer) maxCharactersCount).toString()
                    });
            if (dataReader.moveToFirst())
                return dataReader.getString(0);
        }
        catch (SQLiteException exp)
        {
            return null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }
        return null;
    }

    public ExerciseInfo[] getExerciseInfoByType(@NonNull ExerciseType exerciseType)
    {
        ExerciseInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            final Integer exerciseTypeObject = exerciseType.getValue();
            dataReader = m_databaseConnection.rawQuery(ExtractExerciseInfoByTypeSqlStatement
                    , new String[] { exerciseTypeObject.toString() });

            List<ExerciseInfo> exercises = new ArrayList<>();
            while (dataReader.moveToNext())
            {
                ExerciseInfo exerciseInfo = new ExerciseInfo();
                exerciseInfo.id = dataReader.getInt(0);
                exerciseInfo.type = exerciseType;
                exerciseInfo.name = dataReader.getString(1);
                exerciseInfo.maxScore = dataReader.getInt(2);
                exercises.add(exerciseInfo);
            }
            result = new ExerciseInfo[exercises.size()];
            exercises.toArray(result);
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }
        return result;
    }

    public CharacterExerciseInfo[] getAllCharacterExercisesByAlphabetType(@NonNull AlphabetType alphabetType)
    {
        CharacterExerciseInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            final Integer alphabetTypeIntObject = alphabetType.getValue();
            dataReader = m_databaseConnection.rawQuery(ExtractAllCharacterExercisesByAlphabetSqlStatement
                    , new String[] { alphabetTypeIntObject.toString() });

            List<CharacterExerciseInfo> resultList = new ArrayList<>();
            while (dataReader.moveToNext())
            {
                CharacterExerciseInfo characterExercise = new CharacterExerciseInfo();
                characterExercise.id = dataReader.getInt(0);
                characterExercise.alphabetType = alphabetType;
                characterExercise.character = dataReader.getString(1).charAt(0);
                characterExercise.notPassedImageName = dataReader.getString(2);
                characterExercise.passedImageName = dataReader.getString(3);

                resultList.add(characterExercise);
            }

            result = new CharacterExerciseInfo[resultList.size()];
            resultList.toArray(result);
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public CharacterExerciseInfo getCharacterExerciseById(int characterExerciseId)
    {
        CharacterExerciseInfo result = null;
        Cursor dataReader = null;

        try
        {
            final Integer characterExerciseIdObject = characterExerciseId;
            dataReader = m_databaseConnection.rawQuery(ExtractCharacterExerciseByIdSqlStatement,
                    new String[]{ characterExerciseIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                result = new CharacterExerciseInfo();
                result.id = characterExerciseId;
                result.alphabetType = AlphabetType.getTypeByValue(dataReader.getInt(0));
                result.character = dataReader.getString(1).charAt(0);
                result.notPassedImageName = dataReader.getString(2);
                result.passedImageName = dataReader.getString(3);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public CharacterExerciseItemInfo getCharacterExerciseItemById(int characterExerciseItemId)
    {
        return null;
        /*CharacterExerciseItemInfo result = null;
        Cursor dataReader = null;
        try
        {
            final Integer characterExerciseItemIdObject = characterExerciseItemId;
            dataReader = m_databaseConnection.rawQuery(ExtractCharacterItemByIdSqlStatement,
                    new String[]{characterExerciseItemIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                result = new CharacterExerciseItemInfo();
                result.id = characterExerciseItemId;
                result.characterExerciseId = dataReader.getInt(0);
                result.menuPosition = dataReader.getInt(1);
                result.name = dataReader.getString(2);
                result.displayName = dataReader.getString(3);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;*/
    }

    public CharacterExerciseItemInfo[] getAllCharacterExerciseItemsByCharacterExerciseId(int characterExerciseId)
    {
        CharacterExerciseItemInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            final Integer characterExerciseIdObj = characterExerciseId;
            dataReader = m_databaseConnection.rawQuery(ExtractAllCharacterExerciseItemsByCharacterExerciseIdSqlStatement,
                    new String[] { characterExerciseIdObj.toString() });

            List<CharacterExerciseItemInfo> resultList = new ArrayList<>();
            while (dataReader.moveToNext())
            {
                CharacterExerciseItemInfo characterItem = new CharacterExerciseItemInfo();
                characterItem.id = dataReader.getInt(0);
                characterItem.characterExerciseId = characterExerciseId;
                characterItem.menuElementType = CharacterExerciseItemType.getTypeByValue(dataReader.getInt(1));
                characterItem.exerciseInfo = new ExerciseInfo();
                characterItem.exerciseInfo.id = dataReader.getInt(2);
                characterItem.exerciseInfo.type = ExerciseType.getTypeByValue(dataReader.getInt(3));
                characterItem.exerciseInfo.name = dataReader.getString(4);
                characterItem.exerciseInfo.maxScore = dataReader.getInt(5);

                resultList.add(characterItem);
            }
            result = new CharacterExerciseItemInfo[resultList.size()];
            resultList.toArray(result);
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
        /*

        try
        {
            final Integer characterExerciseIdObject = characterExerciseId;
            dataReader = m_databaseConnection.rawQuery(ExtractAllCharacterExerciseItemsByCharacterExerciseIdSqlStatement, new String[]{characterExerciseIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                ArrayList<CharacterExerciseItemInfo> resultList = new ArrayList<>();

                do
                {
                    CharacterExerciseItemInfo item = new CharacterExerciseItemInfo();
                    item.id = dataReader.getInt(0);
                    item.characterExerciseId = characterExerciseId;
                    item.menuPosition = dataReader.getInt(1);
                    item.name = dataReader.getString(2);
                    item.displayName = dataReader.getString(3);

                    resultList.add(item);
                }
                while (dataReader.moveToNext());

                result = new CharacterExerciseItemInfo[resultList.size()];
                resultList.toArray(result);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
        */
    }

    public String getCharacterItemDisplayNameById(int characterExerciseItemId)
    {
        return getStringBySqlExpressionAndInteger(ExtractCharacterItemDisplayNameByIdSqlStatement, characterExerciseItemId);
    }

    public String getCharacterItemDisplayNameByIdName(@NonNull String characterExerciseName)
    {
        return getStringBySqlExpressionAndString(ExtractCharacterItemDisplayNameByIdNameSqlStatement, characterExerciseName);
    }

    public CharacterExerciseItemStepInfo[] getAllCharacterExerciseStepsByCharacterExerciseItemId(int characterExerciseItemId)
    {
        return null;
        /*
        CharacterExerciseItemStepInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            final Integer characterExerciseItemIdObject = characterExerciseItemId;
            dataReader = m_databaseConnection.rawQuery(ExtractAllCharacterExerciseStepsByCharacterExerciseItemIdSqlStatement,
                    new String[]{characterExerciseItemIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                List<CharacterExerciseItemStepInfo> resultsList = new ArrayList<>();
                do
                {
                    CharacterExerciseItemStepInfo item = new CharacterExerciseItemStepInfo();
                    item.id = dataReader.getInt(0);
                    item.characterExerciseItemId = characterExerciseItemId;
                    item.stepNumber = dataReader.getInt(1);
                    item.action = CharacterExerciseActionType.getTypeByValue(dataReader.getInt(2));
                    item.value = dataReader.getInt(3);

                    resultsList.add(item);
                } while (dataReader.moveToNext());

                result = new CharacterExerciseItemStepInfo[resultsList.size()];
                resultsList.toArray(result);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
        */
    }

    public TheoryPageInfo getTheoryPageById(int theoryPageId)
    {
        TheoryPageInfo result = null;
        Cursor dataReader = null;

        try
        {
            final Integer theoryPageIdObject = theoryPageId;
            dataReader = m_databaseConnection.rawQuery(ExtractTheoryPageById, new String[]{theoryPageIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                result = new TheoryPageInfo();

                result.id = theoryPageId;
                result.imageId = dataReader.getInt(0);
                result.imageRedirectUrl = dataReader.getString(1);
                result.soundId = dataReader.getInt(2);
                result.message = dataReader.getString(3);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public WordObjectInfo[] getRandomImageWords(@NonNull AlphabetType alphabetType,
                                                char testedChar,
                                                @NonNull ContainRelationship containRelationship,
                                                boolean negateRelationship,
                                                int maxCount)
    {
        WordObjectInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            String charLikePattern;
            final Character testedCharObj = testedChar;
            switch (containRelationship)
            {
                case Begin:
                    charLikePattern = testedCharObj.toString() + "%";
                    break;

                case Contain:
                    charLikePattern = "%" + testedCharObj.toString() + "%";
                    break;

                case End:
                    charLikePattern = "%" + testedCharObj.toString();
                    break;

                default:
                    throw new CommonException(CommonResultCode.InvalidArgument);
            }

            final String rawSelectionQuery = negateRelationship ? ExtractRandomWordAndImageNotByPatternAndAlphabetSqlStatement :
                    ExtractRandomWordAndImageByPatternAndAlphabetSqlStatement;

            dataReader = m_databaseConnection.rawQuery(rawSelectionQuery,
                    new String[]
                            {
                                    ((Integer) alphabetType.getValue()).toString(),
                                    charLikePattern,
                                    ((Integer) maxCount).toString()
                            });

            if (dataReader.moveToFirst())
            {
                List<WordObjectInfo> resultsList = new ArrayList<>();
                do
                {
                    WordObjectInfo item = new WordObjectInfo();
                    item.containFlag = containRelationship;
                    item.imageFilePath = dataReader.getString(3);
                    item.word = new WordInfo();
                    item.word.id = dataReader.getInt(0);
                    item.word.alphabetType = alphabetType;
                    item.word.word = dataReader.getString(1);
                    item.word.complexity = dataReader.getInt(2);

                    resultsList.add(item);
                } while (dataReader.moveToNext());

                result = new WordObjectInfo[resultsList.size()];
                resultsList.toArray(result);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public SoundObjectInfo[] getCharacterSoundObjectsByCharacterExerciseIdAndMatchFlag(int characterExerciseId, int soundFlag, int count)
    {
        return getCharacterSoundObjectsBySqlExpressionCharacterExerciseIdAndFlag(ExtractRandomSoundWordsByCharacterExerciseIdAndFlagSqlStatement,
                characterExerciseId, soundFlag, count);
    }

    public SoundObjectInfo[] getCharacterSoundObjectsByCharacterExerciseIdAndNotMatchFlag(int characterExerciseId, int soundFlag, int count)
    {
        return getCharacterSoundObjectsBySqlExpressionCharacterExerciseIdAndFlag(ExtractRandomSoundWordsByCharacterExerciseIdNotMatchFlagSqlStatement,
                characterExerciseId, soundFlag, count);
    }

    public WordInfo getRandomWordByAlphabetAndLength(final AlphabetType alphabetType, int minWordLength, int maxWordLength)
    {
        WordInfo result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(String.format(ExtractRandomWordByAlphabetIdAndLength, minWordLength, maxWordLength),
                    new String[]{((Integer) alphabetType.getValue()).toString()});

            if (dataReader.moveToFirst())
            {
                result = new WordInfo();
                result.id = dataReader.getInt(0);
                result.alphabetType = AlphabetType.getTypeByValue(dataReader.getInt(1));
                result.complexity = dataReader.getInt(2);
                result.word = dataReader.getString(3);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public WordInfo getRandomCreationWordExerciseByAlphabetAndLength(final AlphabetType alphabetType, int minWordLength, int maxWordLength)
    {
        WordInfo result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(String.format(ExtractRandomWordCreationExerciseByAlphabetAndLength, minWordLength, maxWordLength),
                    new String[]{((Integer) alphabetType.getValue()).toString()});

            if (dataReader.moveToFirst())
            {
                result = new WordInfo();
                result.id = dataReader.getInt(0);
                result.alphabetType = alphabetType;
                result.complexity = dataReader.getInt(1);
                result.word = dataReader.getString(2);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public Pair<WordInfo, Integer> getRandomWordAndImageByAlphabetAndLength(final AlphabetType alphabetId, int minWordLength, int maxWordLength)
    {
        Pair<WordInfo, Integer> result = null;
        Cursor dataReader = null;

        try
        {
            final Integer AlphabetIdObject = alphabetId.getValue();

            dataReader = m_databaseConnection.rawQuery(String.format(ExtractRandomWordAndImageByAlphabetAndLength, minWordLength, maxWordLength),
                    new String[]
                            {
                                    AlphabetIdObject.toString()
                            });

            if (dataReader.moveToFirst())
            {
                WordInfo word = new WordInfo();
                word.id = dataReader.getInt(0);
                word.alphabetType = alphabetId;
                word.word = dataReader.getString(1);
                word.complexity = dataReader.getInt(2);

                result = new Pair<>(word, dataReader.getInt(3));
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public WordInfo[] getSubWords(WordInfo wordInfo)
    {
        WordInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(String.format(ExtractWordsByLengthAndAlphabetId, 0, wordInfo.word.length()),
                    new String[]{((Integer) wordInfo.alphabetType.getValue()).toString()});

            if (dataReader.moveToFirst())
            {
                final Map<Character, Integer> MainWordStatistics = calculateStringStatistics(wordInfo.word);

                List<WordInfo> subWords = new ArrayList<>();
                do
                {
                    final String PossibleSubWord = dataReader.getString(3).toLowerCase();
                    final Map<Character, Integer> CurrentWordStatistics = calculateStringStatistics(PossibleSubWord);

                    boolean isSubWord = true;
                    {
                        final Set<Map.Entry<Character, Integer>> StatItems = CurrentWordStatistics.entrySet();
                        for (Map.Entry<Character, Integer> charStat : StatItems)
                        {
                            final Integer CharCount = MainWordStatistics.get(charStat.getKey());
                            if ((CharCount == null) || (CharCount < charStat.getValue()))
                            {
                                isSubWord = false;
                                break;
                            }
                        }
                    }

                    if (isSubWord)
                    {
                        WordInfo subWord = new WordInfo();
                        subWord.id = dataReader.getInt(0);
                        subWord.alphabetType = AlphabetType.getTypeByValue(dataReader.getInt(1));
                        subWord.complexity = dataReader.getInt(2);
                        subWord.word = PossibleSubWord;

                        subWords.add(subWord);
                    }
                }
                while (dataReader.moveToNext());

                result = new WordInfo[subWords.size()];
                subWords.toArray(result);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public int getRandomImageIdByWordId(int wordId)
    {
        return getIntegerBySqlExpressionAndId(ExtractRandomImageIdByWordId, wordId);
    }

    public int getRandomSoundIdByWordId(int wordId)
    {
        return getIntegerBySqlExpressionAndId(ExtractRandomSoundIdByWordId, wordId);
    }

    public String getRandomSoundFileNameByType(SoundType soundType)
    {
        String result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(ExtractRandomSoundNameByTypeSqlStatement,
                    new String[]{((Integer) soundType.getValue()).toString()});

            if (dataReader.moveToFirst())
            {
                result = dataReader.getString(0);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    public String getSoundFileNameById(int soundId)
    {
        return getStringBySqlExpressionAndInteger(ExtractSoundNameByIdSqlStatement, soundId);
    }

    public String getImageFileNameById(int imageId)
    {
        return getStringBySqlExpressionAndInteger(ExtractImageNameByIdSqlStatement, imageId);
    }

    private int getIntegerBySqlExpressionAndId(final String sqlExpression, int id)
    {
        int result = DatabaseConstant.InvalidDatabaseIndex;
        Cursor dataReader = null;

        try
        {
            final Integer IdObj = id;
            dataReader = m_databaseConnection.rawQuery(sqlExpression, new String[]{IdObj.toString()});

            if (dataReader.moveToFirst())
            {
                result = dataReader.getInt(0);
            }
        }
        catch (Exception e)
        {
            result = DatabaseConstant.InvalidDatabaseIndex;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    private String getStringBySqlExpressionAndInteger(@NonNull String sqlExpression, int value)
    {
        final Integer integerObj = value;
        return getStringBySqlExpressionAndString(sqlExpression, integerObj.toString());
    }

    private String getStringBySqlExpressionAndString(@NonNull String sqlExpression, @NonNull String value)
    {
        String resultName = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(sqlExpression, new String[]{value});

            if (dataReader.moveToFirst())
                resultName = dataReader.getString(0);
        }
        catch (Exception exp)
        {
            resultName = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return resultName;
    }

    private SoundObjectInfo[] getCharacterSoundObjectsBySqlExpressionCharacterExerciseIdAndFlag
            (final String sqlExpression, int characterExerciseId, int soundFlag, int count)
    {
        SoundObjectInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            final Integer CharacterExerciseIdObject = characterExerciseId;
            final Integer SoundFlagObject = soundFlag;
            final Integer CountObject = count;

            // SELECT sw.id, sw.word_id, sw.sound_flag, w.alphabet_id, w.word, w.complexity
            dataReader = m_databaseConnection.rawQuery(sqlExpression,
                    new String[] { CharacterExerciseIdObject.toString(), SoundFlagObject.toString(), CountObject.toString() });

            if (dataReader.moveToFirst())
            {
                List<SoundObjectInfo> resultList = new ArrayList<SoundObjectInfo>();

                do
                {
                    SoundObjectInfo item = new SoundObjectInfo();
                    item.id = dataReader.getInt(0);
                    item.characterExerciseId = characterExerciseId;
                    item.soundFlag = dataReader.getInt(2);

                    item.word = new WordInfo();
                    item.word.id = dataReader.getInt(1);
                    item.word.alphabetType = AlphabetType.getTypeByValue(dataReader.getInt(3));
                    item.word.word = dataReader.getString(4);
                    item.word.complexity = dataReader.getInt(5);

                    item.imageId = dataReader.getInt(6);
                    item.soundId = dataReader.getInt(7);

                    resultList.add(item);

                } while (dataReader.moveToNext());

                result = new SoundObjectInfo[resultList.size()];
                resultList.toArray(result);
            }
        }
        catch (Exception exp)
        {
            result = null;
        }
        finally
        {
            if (dataReader != null)
                dataReader.close();
        }

        return result;
    }

    private static Map<Character, Integer> calculateStringStatistics(final String str)
    {
        Map<Character, Integer> result = new HashMap<>();

        final char[] NormalizedString = str.toCharArray();
        for (char ch : NormalizedString)
        {
            final Character CurrentCharacter = ch;
            Integer currentValue = 0;
            if (result.containsKey(CurrentCharacter))
                currentValue = result.get(CurrentCharacter);

            result.put(CurrentCharacter, currentValue + 1);
        }

        return result;
    }

    private String m_pathToDatabase;
    private SQLiteDatabase m_databaseConnection;
}
