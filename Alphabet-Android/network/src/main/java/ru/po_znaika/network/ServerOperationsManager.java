package ru.po_znaika.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.ExerciseScore;

/**
 * Created by Rihter on 10.02.2015.
 * Provides implementation for all server operations
 */
public class ServerOperationsManager implements IServerOperations
{
    private static final String LogTag = ServerOperationsManager.class.getName();

    private static final String ExerciseScoreUrl = NetworkConstant.ServiceUrl + "mark";
    private static final String DiaryUrl = NetworkConstant.ServiceUrl + "diary";

    private static final String DateHeader = "Date";
    private static final String ExerciseNameHeader = "Exercise";
    private static final String ScoreHeader = "Score";

    public ServerOperationsManager(@NonNull IAuthenticationProvider _authenticationProvider)
    {
        m_authenticationProvider = _authenticationProvider;

        try
        {
            m_exerciseScoreUrl = new URL(ExerciseScoreUrl);
            m_diaryUrl = new URL(DiaryUrl);
        }
        catch (MalformedURLException exp)
        {
            Log.e(LogTag, String.format("Url creating exception: \"%s\"", exp.getMessage()));
            throw new Error();
        }
    }

    /**
     * Sends student score on server
     * @param date date of the operation in GMT
     * @param exerciseName unique exercise id
     * @param score exercise score
     * @throws ru.po_znaika.common.CommonException
     * @throws NetworkException
     */
    @Override
    public void reportExerciseScore(@NonNull Date date, @NonNull String exerciseName, int score)
            throws CommonException, NetworkException
    {
        final LoginPasswordCredentials credentials = m_authenticationProvider.getLoginPasswordCredentials();
        if ((credentials == null) || (!credentials.isValid()))
            throw new NetworkException(NetworkResultCode.AuthenticationError);

        try
        {
            HttpURLConnection request = (HttpURLConnection) m_exerciseScoreUrl.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty(NetworkConstant.LoginHeader, credentials.login);
            if (!TextUtils.isEmpty(credentials.password))
                request.setRequestProperty(NetworkConstant.PasswordHeader, credentials.password);
            //request.setRequestProperty();
            request.setRequestProperty(ExerciseNameHeader, exerciseName);
            request.setRequestProperty(ScoreHeader, ((Integer)score).toString());

            int statusCode = 0;
            try
            {
                statusCode = request.getResponseCode();
            }
            catch (IOException exp)
            {
                Log.i(LogTag, "report exercise score exception: " + exp.getMessage());
            }

            if (statusCode != 200)
            {

            }


        }
        catch (IOException exp)
        {
            Log.e(LogTag, "reportExerciseScore exception: " + exp.getMessage());
        }
        //throw new UnsupportedOperationException();
    }

    @Override
    public void reportExerciseScore(@NonNull Collection<ExerciseScore> marks) throws CommonException, NetworkException
    {
        // TODO: temporary realization due to inconsistent protocol
        throw new UnsupportedOperationException();
    }

    /**
     * Returns obtained scores for specified time period
     * @param exerciseGroupId id of the single exercise or logical group of exercises, if 0 - all alphabet exercises
     * @param startDate start date of exercise completion in GMT. If null - no start date
     * @param endDate end date of exercise completion n GMT. If null - no end date
     * @return
     * @throws CommonException
     * @throws NetworkException
     */
    @Override
    public List<ExerciseScore> getExercisesScores(int exerciseGroupId, Date startDate, Date endDate)
            throws CommonException, NetworkException
    {
        return null;
        //throw new UnsupportedOperationException();
    }

    private IAuthenticationProvider m_authenticationProvider;

    private URL m_exerciseScoreUrl;
    private URL m_diaryUrl;
}
