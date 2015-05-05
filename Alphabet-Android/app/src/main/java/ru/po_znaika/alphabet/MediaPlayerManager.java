package ru.po_znaika.alphabet;

import android.content.Context;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

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
        if (m_mediaPlayer != null)
        {
            m_mediaPlayer.stop();
            m_mediaPlayer = null;
        }

        m_mediaPlayer = MediaPlayer.create(m_context, soundResourceId);
        if (m_mediaPlayer == null)
            throw new CommonException(CommonResultCode.InvalidArgument);

        m_mediaPlayer.start();
    }

    @Override
    public void play(@NonNull AlphabetDatabase.SoundType soundType) throws CommonException
    {
        if (m_mediaPlayer != null)
        {
            m_mediaPlayer.stop();
            m_mediaPlayer = null;
        }

        final String soundFileName = m_alphabetDatabase.getRandomSoundFileNameByType(soundType);
        if (TextUtils.isEmpty(soundFileName))
            throw new CommonException(CommonResultCode.InvalidExternalSource);
        final int soundResourceId = DatabaseHelpers.getSoundIdByName(m_context.getResources(), soundFileName);
        if (soundResourceId == 0)
            throw new CommonException(CommonResultCode.InvalidExternalSource);

        m_mediaPlayer = MediaPlayer.create(m_context, soundResourceId);
        m_mediaPlayer.start();
    }

    @Override
    public void pause()
    {
        if (m_mediaPlayer != null)
            m_mediaPlayer.pause();
    }

    @Override
    public void resume()
    {
        if (m_mediaPlayer != null)
            m_mediaPlayer.start();
    }

    @Override
    public void stop()
    {
        if (m_mediaPlayer != null)
            m_mediaPlayer.stop();
    }

    private AlphabetDatabase m_alphabetDatabase;
    private Context m_context;

    private MediaPlayer m_mediaPlayer;
}
