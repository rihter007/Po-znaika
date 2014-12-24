using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;

using ru.po_znaika.database.Alphabet;

namespace ru.po_znaika.alphabet
{
    /// <summary>
    /// Plays sound of any type
    /// </summary>
    class GeneralSoundPlayer
    {
        private const int GeneralWavAudioFileSize = 100 * 1024;

        public GeneralSoundPlayer(AlphabetDatabase database)
        {
            if (database == null)
                throw new ArgumentNullException();

            m_database = database;
        }

        public bool PlaySoundByType(SoundType soundType)
        {
            return PlaySoundById(m_database.GetRandomSoundByType(soundType));
        }

        public bool PlaySoundById(int soundId)
        {
            bool result = false;

            if (soundId != ru.po_znaika.database.Constant.InvalidDatabaseIndex)
            {
                try
                {
                    MemoryStream audioStream = m_database.GetSoundById(soundId);
                    if (audioStream != null)
                    {
                        using (audioStream)
                        {
                            PlaySoundByStream(audioStream);
                        }
                    }
                }
                catch
                {
                    result = false;
                }
            }

            return result;
        }

        private void PlaySoundByStream(MemoryStream audioStream)
        {
            NAudio.Wave.IWaveProvider soundProvider = null;

            ///
            /// Small heruistic based on file length to decide wich
            /// sound type do we have
            ///
            if (audioStream.Length >= GeneralWavAudioFileSize)
            {
                try
                {
                    soundProvider = new NAudio.Wave.WaveFileReader(audioStream);
                }
                catch
                {
                    audioStream.Seek(0, SeekOrigin.Begin);
                    soundProvider = new NAudio.Wave.Mp3FileReader(audioStream);
                }
            }
            else
            {
                try
                {
                    soundProvider = new NAudio.Wave.Mp3FileReader(audioStream);
                }
                catch
                {
                    audioStream.Seek(0, SeekOrigin.Begin);
                    soundProvider = new NAudio.Wave.Mp3FileReader(audioStream);
                }
            }

            NAudio.Wave.IWavePlayer wavePlayer = new NAudio.Wave.WaveOut();
            wavePlayer.Init(soundProvider);

            wavePlayer.PlaybackStopped += new EventHandler<NAudio.Wave.StoppedEventArgs>(wavePlayer_PlaybackStopped);
            wavePlayer.Play();
        }

        private static void wavePlayer_PlaybackStopped(object sender, NAudio.Wave.StoppedEventArgs e)
        {
            NAudio.Wave.IWavePlayer wavePlayer = sender as NAudio.Wave.IWavePlayer;
            wavePlayer.Dispose();
        }

        private AlphabetDatabase m_database;
    }
}
