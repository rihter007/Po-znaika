using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using ru.po_znaika.database.Diary;

namespace ru.po_znaika.server_feedback
{
    public class ServerCacheFeedback : IServerFeedback
    {
        public ServerCacheFeedback(string diaryDatabasePath, string serverAddressUri)
        {
            m_httpRequest = WebRequest.Create(serverAddressUri) as HttpWebRequest;
            m_diaryDatabase = new DiaryDatabase(diaryDatabasePath, false);
        }

        public void SendCachedDataToServer()
        {
        }

        public void SyncDiaryDataWithServer()
        {
        }
        
        public bool ReportExerciseResult(int exerciseId, int score)
        {
            bool isDataSended = SendDataToServer(exerciseId, score);

            return SaveInLocalCache(exerciseId, score, isDataSended);
        }

        private bool SaveInLocalCache(int exerciseId, int sccore, bool isSentToServer)
        {
            return m_diaryDatabase.InsertExerciseScore(DateTime.Now, exerciseId, sccore, isSentToServer) != ru.po_znaika.database.Constant.InvalidDatabaseIndex;
        }

        private bool SendDataToServer(int exerciseId, int score)
        {
            return false;
        }

        private bool SendDataToServer(IEnumerable<ExerciseDiaryInfo> records)
        {
            return false;
        }

        private HttpWebRequest m_httpRequest;
        private DiaryDatabase m_diaryDatabase;
    }
}
