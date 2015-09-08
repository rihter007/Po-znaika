package ru.po_znaika.alphabet;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import java.util.Arrays;

import ru.po_znaika.alphabet.database.DatabaseHelpers;
import ru.po_znaika.alphabet.database.exercise.AlphabetDatabase;

/**
 * Created by Rihter on 09.03.2015.
 * Helper class for processing sound
 */
interface IMediaPlayerManager
{
    void play(int soundId) throws CommonException;
    void play(@NonNull AlphabetDatabase.SoundType soundType) throws CommonException;
    void playSequentially(int[] soundsResourceId) throws CommonException;
    //void playSequentially()
    void pause();
    void resume();
    void stop();
}

class MediaPlayerManager implements IMediaPlayerManager
{
    public MediaPlayerManager(@NonNull Context _context, @NonNull AlphabetDatabase _alphabetDatabase)
    {
        m_context = _context;
        m_alphabetDatabase = _alphabetDatabase;
    }

    @Override
    public void play(int soundResourceId) throws CommonException
    {
        resetInternalState();

        m_currentMediaPlayer = MediaPlayer.create(m_context, soundResourceId);
        if (m_currentMediaPlayer == null)
            throw new CommonException(CommonResultCode.InvalidArgument);

        m_currentMediaPlayer.start();
    }

    @Override
    public void play(@NonNull AlphabetDatabase.SoundType soundType) throws CommonException
    {
        resetInternalState();

        final String soundFileName = m_alphabetDatabase.getRandomSoundFileNameByType(soundType);
        if (TextUtils.isEmpty(soundFileName)) // TODO: exception comes here: No soundFileName
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        final int soundResourceId = DatabaseHelpers.getSoundIdByName(m_context.getResources(), soundFileName);
        if (soundResourceId == 0)
            throw new CommonException(CommonResultCode.InvalidExternalSource);

        m_currentMediaPlayer = MediaPlayer.create(m_context, soundResourceId);
        if (m_currentMediaPlayer == null)
            throw new CommonException(CommonResultCode.InvalidArgument);
        m_currentMediaPlayer.start();
    }

    @Override
    public void playSequentially(int[] soundsResourceId) throws CommonException
    {
        resetInternalState();

        m_soundsSequence = Arrays.copyOf(soundsResourceId, soundsResourceId.length);
        m_currentMediaPlayer = MediaPlayer.create(m_context, soundsResourceId[0]);
        if (m_currentMediaPlayer == null)
            throw new CommonException(CommonResultCode.InvalidArgument);
        m_currentMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
        {
            @Override
            public void onCompletion(MediaPlayer mp)
            {
                internalPlaySound();
            }
        });
        m_currentMediaPlayer.start();
    }

    @Override
    public void pause()
    {
        if (m_currentMediaPlayer != null)
            m_currentMediaPlayer.pause();
    }

    @Override
    public void resume()
    {
        if (m_currentMediaPlayer != null)
            m_currentMediaPlayer.start();
    }

    @Override
    public void stop()
    {
        if (m_currentMediaPlayer != null)
            m_currentMediaPlayer.stop();
    }

    private void resetInternalState()
    {
        if (m_currentMediaPlayer != null)
        {
            m_currentMediaPlayer.stop();
            m_currentMediaPlayer = null;
        }

        m_soundsSequence = null;
        m_currentSoundIndex = 0;
    }

    private void internalPlaySound()
    {
        ++m_currentSoundIndex;
        if (m_currentSoundIndex < m_soundsSequence.length)
        {
            m_currentMediaPlayer = MediaPlayer.create(m_context, m_soundsSequence[m_currentSoundIndex]);
            if (m_currentMediaPlayer != null) // Fail silently
            {
                m_currentMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        internalPlaySound();
                    }
                });
                m_currentMediaPlayer.start();
            }
        }
    }

    private AlphabetDatabase m_alphabetDatabase;
    private Context m_context;

    private MediaPlayer m_currentMediaPlayer;
    private int[] m_soundsSequence;
    private int m_currentSoundIndex;
}
