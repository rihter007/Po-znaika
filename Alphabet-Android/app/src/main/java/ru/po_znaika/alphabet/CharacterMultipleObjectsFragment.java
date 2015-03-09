package ru.po_znaika.alphabet;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;

import android.app.Activity;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Represents fragment with multiple objects represented by name, sound and image
 */
public class CharacterMultipleObjectsFragment extends Fragment
{
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
     * Restores all internal objects
     * @param savedInstanceState activity saved state
     */
    private void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        AlphabetDatabase alphabetDatabase = new AlphabetDatabase(getActivity(), false);

        m_mediaPlayer = null;
        m_isResumed = false;

        if (savedInstanceState == null)
        {
            m_state = new CharacterMultipleObjectsState();

            Bundle fragmentArguments = getArguments();
            m_state.characterExerciseId = fragmentArguments.getInt(Constant.CharacterExerciseIdTag);
            final AlphabetDatabase.CharacterExerciseInfo exerciseInfo = alphabetDatabase.getCharacterExerciseById(m_state.characterExerciseId);
            if (exerciseInfo == null)
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            m_state.exerciseCharacter = exerciseInfo.character;

            AlphabetDatabase.SoundObjectInfo[] rawSoundObjects =
                    alphabetDatabase.getCharacterSoundObjectsByCharacterExerciseIdAndMatchFlag(m_state.characterExerciseId, AlphabetDatabase.SoundObjectInfo.Contain, DisplayedObjectsCount);

            List<SoundObject> resultDisplayObjects = new ArrayList<>();
            for (int soundObjectIndex = 0; soundObjectIndex < rawSoundObjects.length; ++soundObjectIndex)
            {
                final AlphabetDatabase.SoundObjectInfo rawSoundObject = rawSoundObjects[soundObjectIndex];
                final int ImageId = rawSoundObject.imageId; // alphabetDatabase.getRandomImageIdByWordId(RawSoundObject.word.id);
                final int SoundId = rawSoundObject.soundId; // alphabetDatabase.getRandomSoundIdByWordId(RawSoundObject.word.id);

                // Object must contain both image and sound
                if ((ImageId == DatabaseConstant.InvalidDatabaseIndex) || (SoundId == DatabaseConstant.InvalidDatabaseIndex))
                    continue;

                Resources resources = getResources();
                final String PackageName = getActivity().getPackageName();

                final int ImageResourceId = DatabaseHelpers.getDrawableIdByName(resources, alphabetDatabase.getImageFileNameById(ImageId));
                final int SoundResourceId = resources.getIdentifier(alphabetDatabase.getSoundFileNameById(SoundId),
                        Constant.RawResourcesTag, PackageName);

                if ((ImageResourceId != 0) && (SoundResourceId != 0))
                    resultDisplayObjects.add(new SoundObject(rawSoundObject.word.word, ImageResourceId,SoundResourceId));
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
        if (m_mediaPlayer != null)
            m_mediaPlayer.release();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (m_mediaPlayer == null)
            return;

        if (m_mediaPlayer.isPlaying())
        {
            m_mediaPlayer.pause();
            m_isResumed = true;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (m_mediaPlayer == null)
            return;

        if (m_isResumed)
        {
            m_isResumed = false;
            m_mediaPlayer.start();
        }
    }

    private void onListViewItemSelected(int elementId)
    {
        if (m_mediaPlayer != null)
            m_mediaPlayer.stop();
        m_isResumed = false;

        m_mediaPlayer = MediaPlayer.create(getActivity(), m_state.soundObjects[elementId].wordSoundResourceId);
        m_mediaPlayer.start();
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

    private MediaPlayer m_mediaPlayer;
    private boolean m_isResumed;
}
