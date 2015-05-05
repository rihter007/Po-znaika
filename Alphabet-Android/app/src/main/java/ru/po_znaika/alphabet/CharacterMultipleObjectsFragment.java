package ru.po_znaika.alphabet;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import android.app.Activity;
import android.app.Fragment;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Represents fragment with multiple selectionVariants represented by name, sound and image
 */
public class CharacterMultipleObjectsFragment extends Fragment
{
    private static class CharacterMultipleObjectsState implements Parcelable
    {
        public char exerciseCharacter;
        public SoundObject[] soundObjects;

        public CharacterMultipleObjectsState() {}

        public CharacterMultipleObjectsState(@NonNull Parcel _in)
        {
            this.exerciseCharacter = _in.readString().charAt(0);

            // get array of sound selectionVariants
            {
                final int SoundObjectsNumber = _in.readInt();
                if (SoundObjectsNumber > 0)
                {
                    this.soundObjects = new SoundObject[SoundObjectsNumber];
                    for (int soundObjectIndex = 0; soundObjectIndex < SoundObjectsNumber; ++soundObjectIndex)
                        this.soundObjects[soundObjectIndex] = _in.readParcelable(SoundObject.class.getClassLoader());
                }
            }
        }

        @Override
        public int describeContents()
        {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel container, int flags)
        {
            container.writeString(((Character)this.exerciseCharacter).toString());

            container.writeInt(soundObjects.length);
            for (int soundObjectIndex = 0; soundObjectIndex < soundObjects.length; ++soundObjectIndex)
                container.writeParcelable(soundObjects[soundObjectIndex], 0);
        }

        public static final Creator CREATOR = new Creator()
        {
            public CharacterMultipleObjectsState createFromParcel(Parcel in)
            {
                return new CharacterMultipleObjectsState(in);
            }

            public CharacterMultipleObjectsState[] newArray(int size)
            {
                return new CharacterMultipleObjectsState[size];
            }
        };
    }

    private static final String LogTag = CharacterMultipleObjectsFragment.class.getName();
    private static final int DisplayedObjectsCount = 8;

    private static final String InternalStateTag = "internal_state";
    private static final String CharacterExerciseIdTag = "character_exercise_id";

    public static CharacterMultipleObjectsFragment CreateFragment(int characterExerciseId)
    {
        CharacterMultipleObjectsFragment multipleObjectsFragment = new CharacterMultipleObjectsFragment();

        // Put character exercise id as state
        {
            Bundle fragmentState = new Bundle();
            fragmentState.putInt(CharacterExerciseIdTag, characterExerciseId);
            multipleObjectsFragment.setArguments(fragmentState);
        }

        return multipleObjectsFragment;
    }

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     */
    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        m_serviceLocator = new CoreServiceLocator(getActivity());

        AlphabetDatabase alphabetDatabase = m_serviceLocator.getAlphabetDatabase();
        m_mediaPlayerManager = m_serviceLocator.getMediaPlayerManager();

        if (savedInstanceState == null)
        {
            m_state = new CharacterMultipleObjectsState();

            Bundle fragmentArguments = getArguments();
            final int characterExerciseId = fragmentArguments.getInt(CharacterExerciseIdTag);
            final AlphabetDatabase.CharacterExerciseInfo exerciseInfo = alphabetDatabase.getCharacterExerciseById(characterExerciseId);
            if (exerciseInfo == null)
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            m_state.exerciseCharacter = exerciseInfo.character;

            AlphabetDatabase.SoundObjectInfo[] rawSoundObjects =
                    alphabetDatabase.getCharacterSoundObjectsByCharacterExerciseIdAndMatchFlag(characterExerciseId,
                            AlphabetDatabase.SoundObjectInfo.Contain,
                            DisplayedObjectsCount);
            if (rawSoundObjects == null)
                throw new CommonException(CommonResultCode.InvalidExternalSource);

            List<SoundObject> resultDisplayObjects = new ArrayList<>();
            for (int soundObjectIndex = 0; soundObjectIndex < rawSoundObjects.length; ++soundObjectIndex)
            {
                final AlphabetDatabase.SoundObjectInfo rawSoundObject = rawSoundObjects[soundObjectIndex];
                final int imageId = rawSoundObject.imageId;
                final int soundId = rawSoundObject.soundId;

                // Object must contain both image and sound
                if ((imageId == DatabaseConstant.InvalidDatabaseIndex) || (soundId == DatabaseConstant.InvalidDatabaseIndex))
                    continue;

                Resources resources = getResources();
                final int imageResourceId = DatabaseHelpers.getDrawableIdByName(resources, alphabetDatabase.getImageFileNameById(imageId));
                final int soundResourceId = DatabaseHelpers.getSoundIdByName(resources, alphabetDatabase.getSoundFileNameById(soundId));

                if ((imageResourceId != 0) && (soundResourceId != 0))
                    resultDisplayObjects.add(new SoundObject(rawSoundObject.word.word, imageResourceId, soundResourceId));
            }

            m_state.soundObjects = new SoundObject[resultDisplayObjects.size()];
            resultDisplayObjects.toArray(m_state.soundObjects);
        }
        else
        {
            m_state = savedInstanceState.getParcelable(InternalStateTag);
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface(View fragmentView)
    {
        // modify caption
        {
            final String StandardCaption = getResources().getString(R.string.sound_objects_caption);

            TextView captionTextView = (TextView)fragmentView.findViewById(R.id.captionTextView);
            captionTextView.setText(StandardCaption + '\'' + m_state.exerciseCharacter + '\'');
        }

        // http://stackoverflow.com/questions/6094315/single-textview-with-two-different-colored-text
        // show object elements in list
        {
            ListView objectsMenuList = (ListView)fragmentView.findViewById(R.id.objectsListView);
            ImageTextAdapter imageTextAdapter = new ImageTextAdapter(getActivity(), R.layout.large_image_menu_item);

            for (SoundObject soundObject : m_state.soundObjects)
            {
                imageTextAdapter.add(getResources().getDrawable(soundObject.wordImageResourceId), soundObject.word);
            }

            objectsMenuList.setAdapter(imageTextAdapter);
        }

        // set clickable elements callbacks
        {
            {
                ListView objectsMenuList = (ListView) fragmentView.findViewById(R.id.objectsListView);

                objectsMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener()
                {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long rowId)
                    {
                        onListViewItemSelected((int) rowId);
                    }
                });
            }

            {
                Button forwardButton = (Button) fragmentView.findViewById(R.id.forwardButton);
                forwardButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onForwardButtonClick();
                    }
                });
            }

            {
                Button backButton = (Button) fragmentView.findViewById(R.id.backButton);
                backButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onBackButtonClick();
                    }
                });
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_character_multiple_objects, container, false);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            AlertDialog msgBox = MessageBox.CreateDialog(getActivity(), resources.getString(R.string.failed_action),
                    resources.getString(R.string.alert_title), false, new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            getActivity().finish();
                        }
                    });
            msgBox.show();
        }

        return fragmentView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelable(InternalStateTag, m_state);
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            m_stepsCallback = (IExerciseStepCallback)activity;
        }
        catch (ClassCastException e)
        {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_stepsCallback = null;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        // release media player
        m_mediaPlayerManager.stop();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        m_mediaPlayerManager.pause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        m_mediaPlayerManager.resume();
    }

    private void onListViewItemSelected(int elementId)
    {
        m_mediaPlayerManager.stop();

        try
        {
            m_mediaPlayerManager.play(m_state.soundObjects[elementId].wordSoundResourceId);
        }
        catch (CommonException exp)
        {
            Log.e(LogTag, "Failed to play sound: " + m_state.soundObjects[elementId].wordSoundResourceId);
        }
    }

    private void onForwardButtonClick()
    {
        m_stepsCallback.processNextStep();
    }

    private void onBackButtonClick()
    {
        m_stepsCallback.processPreviousStep();
    }

    private IExerciseStepCallback m_stepsCallback;

    private CharacterMultipleObjectsState m_state;

    private CoreServiceLocator m_serviceLocator;
    private IMediaPlayerManager m_mediaPlayerManager;
}
