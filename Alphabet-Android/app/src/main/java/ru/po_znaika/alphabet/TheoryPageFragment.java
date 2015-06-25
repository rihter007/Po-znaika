package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.os.Bundle;
import android.app.Fragment;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.common.IExerciseStepCallback;
import ru.po_znaika.alphabet.database.DatabaseConstant;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;
import ru.po_znaika.common.ru.po_znaika.common.helpers.ProcessUrl;
import ru.po_znaika.common.ru.po_znaika.common.helpers.TextFormatBlock;
import ru.po_znaika.common.ru.po_znaika.common.helpers.TextFormatter;

/**
 * Describes theory page
 */
public class TheoryPageFragment extends Fragment
{
    private static final String LogTag = TheoryPageFragment.class.getName();

    private static final String TheoryTableIndexTag = "theory_table_index";
    private static final String TheoryImageIndexTag = "theory_image_index";
    private static final String TheoryImageRedirectUrlTag = "theory_image_url_redirect";
    private static final String TheorySoundIndexTag = "theory_sound_index";
    private static final String TheoryMessageTag = "theory_message";

    public static TheoryPageFragment createFragment(int theoryPageDatabaseId)
    {
        TheoryPageFragment pageFragment = new TheoryPageFragment();
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putInt(TheoryTableIndexTag, theoryPageDatabaseId);
        pageFragment.setArguments(fragmentArguments);

        return pageFragment;
    }

    /**
     * Restores all internal selectionVariants
     * @param savedInstanceState activity saved state
     * @throws com.arz_x.CommonException
    */
    void restoreInternalState(Bundle savedInstanceState) throws CommonException
    {
        m_alphabetDatabase = new AlphabetDatabase(getActivity(), false);

        Bundle fragmentArguments = getArguments();
        final Integer TheoryTableIndex = fragmentArguments.getInt(TheoryTableIndexTag);

        // Restore essential internal members
        if (savedInstanceState == null)
        {
            AlphabetDatabase.TheoryPageInfo theoryPageInfo = m_alphabetDatabase.getTheoryPageById(TheoryTableIndex);
            m_theoryImageId = theoryPageInfo.imageId;
            m_theoryImageRedirectUrl = theoryPageInfo.imageRedirectUrl;
            m_theorySoundId = theoryPageInfo.soundId;
            m_theoryMessage = theoryPageInfo.message;
        }
        else
        {
            m_theoryImageId = savedInstanceState.getInt(TheoryImageIndexTag);
            m_theoryImageRedirectUrl = savedInstanceState.getString(TheoryImageRedirectUrlTag);
            m_theorySoundId = savedInstanceState.getInt(TheorySoundIndexTag);
            m_theoryMessage = savedInstanceState.getString(TheoryMessageTag);
        }
    }

    /**
     * Constructs parts of user interface
     */
    void constructUserInterface(View fragmentView) throws CommonException
    {
        if ((m_theoryImageId == DatabaseConstant.InvalidDatabaseIndex) && (m_theorySoundId == DatabaseConstant.InvalidDatabaseIndex) &&
                (TextUtils.isEmpty(m_theoryMessage)))
        {
            Log.e(LogTag, "Invalid data");
            throw new CommonException(CommonResultCode.InvalidInternalState);
        }

        // process image
        if (m_theoryImageId != DatabaseConstant.InvalidDatabaseIndex)
        {
            final Resources resources = getResources();

            final String resourceFileName = m_alphabetDatabase.getImageFileNameById(m_theoryImageId);
            final int imageResourceId = DatabaseHelpers.getDrawableIdByName(resources, resourceFileName);
            if (imageResourceId == 0)
            {
                Log.e(LogTag, String.format("Failed to obtain sound by id:\"%d\"", imageResourceId));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            ImageView theoryImageView = (ImageView) fragmentView.findViewById(R.id.theoryImageView);
            theoryImageView.setImageDrawable(resources.getDrawable(imageResourceId));

            if (m_theoryImageRedirectUrl != null)
            {
                theoryImageView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        try
                        {
                            ProcessUrl.openUrl(getActivity(), m_theoryImageRedirectUrl);
                        }
                        catch (Exception exp)
                        {
                            AlertDialogHelper.showMessageBox(getActivity(),
                                    resources.getString(R.string.alert_title),
                                    resources.getString(R.string.error_unknown_error));
                        }
                    }
                });
            }
        }
        else
        {
            ImageView theoryImageView = (ImageView) fragmentView.findViewById(R.id.theoryImageView);
            theoryImageView.setVisibility(View.INVISIBLE);

            ViewGroup.LayoutParams layoutParams = theoryImageView.getLayoutParams();
            layoutParams.height = layoutParams.width = 0;
            theoryImageView.setLayoutParams(layoutParams);
        }

        // process sound
        if (m_theorySoundId != DatabaseConstant.InvalidDatabaseIndex)
        {
            final String resourceFileName = m_alphabetDatabase.getSoundFileNameById(m_theorySoundId);
            final int soundResourceId = DatabaseHelpers.getSoundIdByName(getResources(), resourceFileName);
            if (soundResourceId == 0)
            {
                Log.e(LogTag, String.format("Failed to obtain sound by id:\"%d\"", soundResourceId));
                throw new CommonException(CommonResultCode.InvalidExternalSource);
            }

            m_theorySoundPlayer = MediaPlayer.create(getActivity(), soundResourceId);
            m_theorySoundPlayer.start();
            m_isResumed = false;
        } else
        {
            m_theorySoundPlayer = null;

            ImageButton playSoundButton = (ImageButton) fragmentView.findViewById(R.id.playSoundButton);
            playSoundButton.setVisibility(View.INVISIBLE);

            TextView playSoundTextView = (TextView) fragmentView.findViewById(R.id.playSoundTextView);
            playSoundTextView.setVisibility(View.INVISIBLE);
        }

        // process text

        if (!TextUtils.isEmpty(m_theoryMessage))
        {
            TextView theoryTextView = (TextView) fragmentView.findViewById(R.id.theoryTextView);
            theoryTextView.setText(null);
            final List<TextFormatBlock> formatBlocks = TextFormatter.processText(m_theoryMessage);

            for (TextFormatBlock formatBlock : formatBlocks)
            {
                Spannable spanText = new SpannableString(formatBlock.getText());
                if (formatBlock.isColorSet())
                {
                    spanText.setSpan(new ForegroundColorSpan(formatBlock.getARGBColor()),
                            0,
                            formatBlock.getText().length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                theoryTextView.append(spanText);
            }
        }
        else
        {
            TextView theoryTextView = (TextView) fragmentView.findViewById(R.id.theoryTextView);
            theoryTextView.setVisibility(View.INVISIBLE);
        }

        // set buttons callbacks
        {
            {
                final ImageButton playSoundButton = (ImageButton) fragmentView.findViewById(R.id.playSoundButton);
                playSoundButton.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        onPlaySoundButtonClick();
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

    private void onPlaySoundButtonClick()
    {
        if (m_theorySoundPlayer == null)
            return;

        //m_theorySoundPlayer.stop();
        m_isResumed = false;

        m_theorySoundPlayer.start();
    }

    private void onForwardButtonClick()
    {
        m_stepsCallback.processNextStep();
    }

    private void onBackButtonClick()
    {
        m_stepsCallback.processPreviousStep();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_theory_page, container, false);

        try
        {
            restoreInternalState(savedInstanceState);
            constructUserInterface(fragmentView);
        }
        catch (Exception exp)
        {
            Resources resources = getResources();
            AlertDialogHelper.showMessageBox(getActivity(),
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.failed_action),
                    false,
                    new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            getActivity().finish();
                        }
                    });
        }
        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);

        m_stepsCallback = (IExerciseStepCallback) activity;
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_stepsCallback = null;
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (m_theorySoundPlayer == null)
            return;

        if (m_theorySoundPlayer.isPlaying())
        {
            m_theorySoundPlayer.pause();
            m_isResumed = true;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (m_theorySoundPlayer == null)
            return;

        if (m_isResumed)
        {
            m_isResumed = false;
            m_theorySoundPlayer.start();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(TheoryImageIndexTag, m_theoryImageId);
        savedInstanceState.putString(TheoryImageRedirectUrlTag, m_theoryImageRedirectUrl);
        savedInstanceState.putInt(TheorySoundIndexTag, m_theorySoundId);
        savedInstanceState.putString(TheoryMessageTag, m_theoryMessage);
    }

    /* Performs switching between exercise steps */
    private IExerciseStepCallback m_stepsCallback;

    private AlphabetDatabase m_alphabetDatabase;

    private int m_theoryImageId;
    private String m_theoryImageRedirectUrl;
    private int m_theorySoundId;
    private String m_theoryMessage;

    private MediaPlayer m_theorySoundPlayer;
    private boolean m_isResumed;
}
