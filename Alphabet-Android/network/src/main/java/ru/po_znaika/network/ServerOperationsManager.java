package ru.po_znaika.network;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.common.CommonResultCode;
import ru.po_znaika.common.ExerciseScore;
import ru.po_znaika.common.ru.po_znaika.common.helpers.CommonHelpers;

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
    private static final String StartDateHeader = "StartDate";
    private static final String EndDateHeader = "EndDate";

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
            request.setRequestProperty(DateHeader, NetworkHelpers.getHttpDateRepresentation(date));
            request.setRequestProperty(ExerciseNameHeader, exerciseName);
            request.setRequestProperty(ScoreHeader, ((Integer)score).toString());

            int statusCode = 0;
            try
            {
                statusCode = request.getResponseCode();
            }
            catch (IOException exp)
            {
                if (exp instanceof java.net.UnknownHostException)
                    throw new NetworkException(NetworkResultCode.NoConnection);
            }

            if (statusCode != 200)
            {
                switch (statusCode)
                {
                    case 401:
                    case 404:
                        throw new NetworkException(NetworkResultCode.AuthenticationError);
                }

                throw new NetworkException(NetworkResultCode.Unknown);
            }
        }
        catch (IOException exp)
        {
            Log.e(LogTag, "reportExerciseScore exception: " + exp.getMessage());
        }
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
    public ExerciseScore[] getExercisesScores(@NonNull String exerciseGroupId, Date startDate, Date endDate)
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
            request.setRequestProperty(ExerciseNameHeader, exerciseGroupId);

            if (startDate != null)
                request.setRequestProperty(StartDateHeader, NetworkHelpers.getHttpDateRepresentation(startDate));
            if (endDate != null)
                request.setRequestProperty(EndDateHeader, NetworkHelpers.getHttpDateRepresentation(endDate));

            int statusCode = 0;
            try
            {
                statusCode = request.getResponseCode();
            }
            catch (IOException exp)
            {
                if (exp instanceof java.net.UnknownHostException)
                    throw new NetworkException(NetworkResultCode.NoConnection);
            }

            if (statusCode != 200)
            {
                switch (statusCode)
                {
                    case 401:
                    case 404:
                        throw new NetworkException(NetworkResultCode.AuthenticationError);
                }

                throw new NetworkException(NetworkResultCode.Unknown);
            }

            InputStream responseBodyStream = request.getInputStream();

            String responseEncoding = request.getContentEncoding();
            responseEncoding = TextUtils.isEmpty(responseEncoding) ? "UTF-8" : responseEncoding;

            List<ExerciseScore> returnedExercises = new ArrayList<>();

            JsonReader jsonReader = new JsonReader(new InputStreamReader(responseBodyStream, responseEncoding));
            try
            {
                jsonReader.beginArray();
                while (jsonReader.hasNext())
                {
                    ExerciseScore exerciseScore = new ExerciseScore();
                    boolean nameIsSet = false;
                    boolean dateIsSet = false;
                    boolean scoreIsSet = false;

                    jsonReader.beginObject();
                    while (jsonReader.hasNext())
                    {
                        final String propertyName = jsonReader.nextName();
                        if (propertyName.equalsIgnoreCase("Exercise"))
                        {
                            exerciseScore.exerciseName = jsonReader.nextString();
                            nameIsSet = true;
                        }
                        else if (propertyName.equalsIgnoreCase("Date"))
                        {
                            exerciseScore.date = CommonHelpers.gmtToLocal(new Date(jsonReader.nextLong()));
                            dateIsSet = true;
                        }
                        else if (propertyName.equalsIgnoreCase("Score"))
                        {
                            exerciseScore.score = jsonReader.nextInt();
                            scoreIsSet = true;
                        }
                    }
                    jsonReader.endObject();

                    if (!(nameIsSet & dateIsSet & scoreIsSet) || (TextUtils.isEmpty(exerciseScore.exerciseName)))
                        throw new CommonException(CommonResultCode.UnknownReason);

                    returnedExercises.add(exerciseScore);
                }
                jsonReader.endArray();
            }
            finally
            {
                jsonReader.close();
            }

            ExerciseScore[] result = new ExerciseScore[returnedExercises.size()];
            returnedExercises.toArray(result);
            return result;
        }
        catch (IOException exp)
        {
            Log.e(LogTag, "reportExerciseScore exception: " + exp.getMessage());
        }

        return null;
    }

    private IAuthenticationProvider m_authenticationProvider;

    private URL m_exerciseScoreUrl;
    private URL m_diaryUrl;
}
