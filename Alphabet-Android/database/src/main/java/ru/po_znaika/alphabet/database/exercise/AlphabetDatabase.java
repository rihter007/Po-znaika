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
import android.util.Log;
import android.util.Pair;

import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;

/**
 * Created by Rihter on 07.08.2014.
 * Help tutorial = http://www.reigndesign.com/blog/using-your-own-sqlite-database-in-android-applications/
 */
public final class AlphabetDatabase
{
    /**
     *  Types declarations
     */
    public static enum SoundType
    {
        Correct(78467623),                     // crc32 of 'Correct'
        Praise(-1022835248),                   // crc32 of 'Praise'
        TryAgain(2010528955);                  // crc32 of 'TryAgain'

        private int m_value;

        private SoundType(int _value) { this.m_value = _value; }
        public int getValue() { return m_value; }

        private static final Map<Integer, SoundType> ValuesMap = new HashMap<Integer,SoundType>()
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
     *  Represents hardcoded type of an exercise in Alphabet studies
     */
    public static enum ExerciseType
    {
        Character(294335127),                   // crc32 of 'Character'
        WordGather(402850721),                  // crc32 of 'WordGather'
        CreateWordsFromSpecified(-858355490);   // crc32 of 'CreateWordsFromSpecified'

        private int m_value;

        private ExerciseType(int _value)
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

    public static enum CharacterExerciseItemType
    {
        General(26480598),      // a crc32 of 'General'
        Sound(961539200),       // a crc32 of 'SoundPronunciation'
        Letter(-1985025220);    // a ccr32 of 'Letter'

        private int m_value;

        private CharacterExerciseItemType(int _value) { this.m_value = _value; }
        public int getValue() { return m_value; }

        private static final Map<Integer, CharacterExerciseItemType> ValuesMap = new HashMap<Integer, CharacterExerciseItemType>()
        {{
                put(-1081850258, General);
                put(1538820439, Sound);
                put(-1985025220, Letter);
        }};

        public static CharacterExerciseItemType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }
    }

    public static enum CharacterExerciseActionType
    {
        TheoryPage(1986991965),             // a crc32 of 'TheoryPage'
        CustomAction(291784361);            // a crc32 of 'CustomAction'

        private int m_value;

        private CharacterExerciseActionType(int _value) { m_value = _value; }
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
    }

    public static enum AlphabetType
    {
        Russian(-345575051);               // crc32 of 'russian'

        private int m_value;

        private AlphabetType(int _value)
        {
            this.m_value = _value;
        }
        public int getValue()
        {
            return m_value;
        }

        private static final Map<Integer, AlphabetType> ValuesMap = new HashMap<Integer, AlphabetType>()
        {{
                put(2092928056, Russian);
        }};

        public static AlphabetType getTypeByValue(int value)
        {
            return ValuesMap.get(value);
        }
    }

    public static class ExerciseShortInfo
    {
        public int id;
        public ExerciseType type;
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

        /* exercise user-friendly name */
        public String displayName;
        /* exercise display image */
        public int imageId;
    }

    public static class CharacterExerciseInfo
    {
        public int id;
        public int exerciseId;
        public char character;
        public AlphabetType alphabetId;
    }

    /**
     * Represents single group of sub-exercises for selected character
     */
    public static class CharacterExerciseItemInfo
    {
        public int id;
        public int characterExerciseId;
        public CharacterExerciseItemType type;
        public int menuPosition;
        public String name;
        public String displayName;
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

        /* Position of action in exercise */
        public int stepNumber;
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
     * SQL-expressions from exercise table
     */
    private static final String ExtractAllExercisesShortInfoSqlStatement =
            "SELECT _id, type FROM exercise";
    private static final String ExtractAllExercisesShortInfoByTypeSqlStatement =
            "SELECT _id FROM exercise WHERE type = ?";
    private static final String ExtractAllExercisesShortInfoNotByTypeSqlStatement =
            "SELECT _id, type FROM exercise WHERE type <> ?";
    private static final String ExtractExerciseInfoByIdSqlStatement =
            "SELECT type, name, display_name, image_id FROM exercise WHERE _id = ?";
    private static final String ExtractExerciseDisplayNameByIdSqlStatement =
            "SELECT display_name FROM exercise WHERE _id = ?";

    /**
     * SQL-expressions form character_exercise and relative tables
     */
    private static final String ExtractCharacterExerciseByExerciseIdSqlStatement =
            "SELECT _id, character, alphabet_id FROM character_exercise WHERE exercise_id = ?";
    private static final String ExtractAllCharacterExerciseItemsByCharacterExerciseIdSqlStatement =
            "SELECT _id, type, menu_position, name, display_name FROM character_exercise_item WHERE character_exercise_id = ?";
    private static final String ExtractAllCharacterExerciseStepsByCharacterExerciseItemIdSqlStatement =
            "SELECT _id, step_number, action, value FROM character_exercise_item_step WHERE character_exercise_item_id = ?";
    private static final String ExtractCharacterItemDisplayNameByIdSqlStatement =
            "SELECT display_name FROM character_exercise_item WHERE _id = ?";

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
            "WHERE (wce.word_id = w._id) AND (w.alphabet_id = ?) AND (length(w.word) >= %d) AND (length(w.word) <= %d) ORDER BY RANDOM() LIMIT 1"
            ;
    private static final String ExtractRandomWordAndImageByAlphabetAndLength =
            "SELECT w._id, w.word, w.complexity, wid.image_id " +
            "FROM word w, word_image_description wid " +
            "WHERE (w.alphabet_id = ?) AND (length(w.word) >= %d) AND (length(w.word) <= %d) AND (w._id = wid.word_id) ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomImageIdByWordId = "SELECT image_id FROM word_image_description wid " +
            "WHERE wid.word_id = ? ORDER BY RANDOM() LIMIT 1";
    private static final String ExtractRandomSoundIdByWordId = "SELECT sound_id FROM word_sound_description wsd " +
            "WHERE wsd.word_id = ? ORDER BY RANDOM() LIMIT 1";

    /**
     * SQL-expressions for theory_page table
     */
    private static final String ExtractTheoryPageById = "SELECT image_id, sound_id, message FROM theory_page WHERE _id = ?";

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
           databaseConnection = SQLiteDatabase.openDatabase(pathToDatabase, null, SQLiteDatabase.NO_LOCALIZED_COLLATORS | SQLiteDatabase.OPEN_READWRITE);
        }
        catch (SQLiteException exp)
        {
            Log.e(AlphabetDatabase.class.getName(), String.format("Failed to open database, error=\"%s\"", exp.getMessage()));
        }

        return databaseConnection;
    }
     /**
     * Gets database from assets folder
     * @return
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
            localFileDatabase.createNewFile();
            localFileDatabaseStream = new FileOutputStream(localFileDatabase);

            {
                byte[] buffer = new byte[10*1024];
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

    public ExerciseShortInfo[] getAllExercisesShortInfo()
    {
        ExerciseShortInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(ExtractAllExercisesShortInfoSqlStatement, null);
            List<ExerciseShortInfo> resultList = new ArrayList<ExerciseShortInfo>();

            if (dataReader.moveToFirst())
            {
                do
                {
                    ExerciseShortInfo item = new ExerciseShortInfo();
                    item.id = dataReader.getInt(0);
                    item.type = ExerciseType.getTypeByValue(dataReader.getInt(1));

                    resultList.add(item);
                } while (dataReader.moveToNext());
            }

            result = new ExerciseShortInfo[resultList.size()];
            resultList.toArray(result);
        }
        catch (Exception e)
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

    public ExerciseShortInfo[] getAllExercisesShortInfoByType(ExerciseType exerciseType)
    {
        ExerciseShortInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(ExtractAllExercisesShortInfoByTypeSqlStatement,
                    new String[] { ((Integer)exerciseType.getValue()).toString() });

            if (dataReader.moveToFirst())
            {
                List<ExerciseShortInfo> exercises = new ArrayList<ExerciseShortInfo>();

                do
                {
                    ExerciseShortInfo exerciseShortInfo = new ExerciseShortInfo();
                    exerciseShortInfo.id = dataReader.getInt(0);
                    exerciseShortInfo.type = exerciseType;

                    exercises.add(exerciseShortInfo);
                } while (dataReader.moveToNext());

                result = new ExerciseShortInfo[exercises.size()];
                exercises.toArray(result);
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

    public ExerciseShortInfo[] getAllExercisesShortInfoExceptType(ExerciseType exerciseType)
    {
        ExerciseShortInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            dataReader = m_databaseConnection.rawQuery(ExtractAllExercisesShortInfoNotByTypeSqlStatement,
                    new String[] { ((Integer)exerciseType.getValue()).toString() });

            if (dataReader.moveToFirst())
            {
                List<ExerciseShortInfo> exercises = new ArrayList<ExerciseShortInfo>();

                do
                {
                    ExerciseShortInfo exerciseShortInfo = new ExerciseShortInfo();
                    exerciseShortInfo.id = dataReader.getInt(0);
                    exerciseShortInfo.type = ExerciseType.getTypeByValue(dataReader.getInt(1));

                    exercises.add(exerciseShortInfo);
                } while (dataReader.moveToNext());

                result = new ExerciseShortInfo[exercises.size()];
                exercises.toArray(result);
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

    public ExerciseInfo getExerciseInfoById(int exerciseId)
    {
        ExerciseInfo result = null;
        Cursor dataReader = null;

        try
        {
            Integer exerciseIdObject = exerciseId;
            dataReader = m_databaseConnection.rawQuery(ExtractExerciseInfoByIdSqlStatement, new String[]{exerciseIdObject.toString()});
            if (dataReader.moveToFirst())
            {
                result = new ExerciseInfo();
                result.id = exerciseId;
                result.type = ExerciseType.getTypeByValue(dataReader.getInt(0));
                result.name = dataReader.getString(1);
                result.displayName = dataReader.getString(2);
                result.imageId = dataReader.getInt(3);
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

    public String getExerciseDisplayNameById(int exerciseId)
    {
        return getStringBySqlExpressionAndId(ExtractExerciseDisplayNameByIdSqlStatement, exerciseId);
    }

    public CharacterExerciseInfo getCharacterExerciseByExerciseId(int exerciseId)
    {
        CharacterExerciseInfo result = null;
        Cursor dataReader = null;

        try
        {
            final Integer exerciseIdObject = exerciseId;
            dataReader = m_databaseConnection.rawQuery(ExtractCharacterExerciseByExerciseIdSqlStatement,
                    new String[]{ exerciseIdObject.toString() });

            if (dataReader.moveToFirst())
            {
                result = new CharacterExerciseInfo();
                result.id = dataReader.getInt(0);
                result.exerciseId = exerciseId;
                result.character = dataReader.getString(1).charAt(0);
                result.alphabetId = AlphabetType.getTypeByValue(dataReader.getInt(2));
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

        return  result;
    }

    public CharacterExerciseItemInfo[] getAllCharacterExerciseItemsByCharacterExerciseId(int characterExerciseId)
    {
        CharacterExerciseItemInfo[] result = null;
        Cursor dataReader = null;
        try
        {
            final Integer characterExerciseIdObject = characterExerciseId;
            dataReader = m_databaseConnection.rawQuery(ExtractAllCharacterExerciseItemsByCharacterExerciseIdSqlStatement, new String[] { characterExerciseIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                ArrayList<CharacterExerciseItemInfo> resultList = new ArrayList<CharacterExerciseItemInfo>();

                do
                {
                    CharacterExerciseItemInfo item = new CharacterExerciseItemInfo();
                    item.id = dataReader.getInt(0);
                    item.characterExerciseId = characterExerciseId;
                    item.type = CharacterExerciseItemType.getTypeByValue(dataReader.getInt(1));
                    item.menuPosition = dataReader.getInt(2);
                    item.name = dataReader.getString(3);
                    item.displayName = dataReader.getString(4);

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
    }

    public String getCharacterItemDisplayNameById(int characterExerciseItemId)
    {
        return getStringBySqlExpressionAndId(ExtractCharacterItemDisplayNameByIdSqlStatement, characterExerciseItemId);
    }

    public CharacterExerciseItemStepInfo[] getAllCharacterExerciseStepsByCharacterExerciseItemId(int characterExerciseItemId)
    {
        CharacterExerciseItemStepInfo[] result = null;
        Cursor dataReader = null;

        try
        {
            final Integer characterExerciseItemIdObject = characterExerciseItemId;
            dataReader = m_databaseConnection.rawQuery(ExtractAllCharacterExerciseStepsByCharacterExerciseItemIdSqlStatement,
                    new String[] {characterExerciseItemIdObject.toString()});

            if (dataReader.moveToFirst())
            {
                List<CharacterExerciseItemStepInfo> resultsList = new ArrayList<CharacterExerciseItemStepInfo>();
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
    }

    public TheoryPageInfo getTheoryPageById(int theoryPageId)
    {
        TheoryPageInfo result = null;
        Cursor dataReader = null;

        try
        {
            final Integer theoryPageIdObject = theoryPageId;
            dataReader = m_databaseConnection.rawQuery(ExtractTheoryPageById, new String[] { theoryPageIdObject.toString() });

            if (dataReader.moveToFirst())
            {
                result = new TheoryPageInfo();

                result.id = theoryPageId;
                result.imageId = dataReader.getInt(0);
                result.soundId = dataReader.getInt(1);
                result.message = dataReader.getString(2);
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
                    new String[] { ((Integer)alphabetType.getValue()).toString() });

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
                    new String[] { ((Integer)alphabetType.getValue()).toString() });

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

                result = new Pair<WordInfo, Integer>(word, dataReader.getInt(3));
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
                    new String[] { ((Integer)wordInfo.alphabetType.getValue()).toString() });

            if (dataReader.moveToFirst())
            {
                final Map<Character, Integer> MainWordStatistics = calculateStringStatistics(wordInfo.word);

                List<WordInfo> subWords = new ArrayList<WordInfo>();
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
                    new String[] { ((Integer)soundType.getValue()).toString() });

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
        return getStringBySqlExpressionAndId(ExtractSoundNameByIdSqlStatement, soundId);
    }

    public String getImageFileNameById(int imageId)
    {
        return getStringBySqlExpressionAndId(ExtractImageNameByIdSqlStatement, imageId);
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

    private String getStringBySqlExpressionAndId(final String sqlExpression, int id)
    {
        String resultName = null;
        Cursor dataReader = null;

        try
        {
            final Integer IdObj = id;
            dataReader = m_databaseConnection.rawQuery(sqlExpression, new String[]{IdObj.toString()});

            if (dataReader.moveToFirst())
                resultName = dataReader.getString(0);
        }
        catch (Exception e)
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
        Map<Character, Integer> result = new HashMap<Character, Integer>();

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
