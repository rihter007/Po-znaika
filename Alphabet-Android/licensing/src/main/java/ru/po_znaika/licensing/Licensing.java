package ru.po_znaika.licensing;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import java.io.InputStreamReader;
import java.util.Date;

import java.io.InputStream;
import java.io.IOException;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.HttpURLConnection;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;

import ru.po_znaika.network.IAuthenticationProvider;
import ru.po_znaika.network.LoginPasswordCredentials;
import ru.po_znaika.network.NetworkConstant;
import com.arz_x.NetworkException;
import com.arz_x.NetworkResultCode;

/**
 * Created by Rihter on 19.01.2015.
 * Realization of ILicense interface
 */
class LicensingCache
{
    private static final String CacheFileName = "LicensingCache";
    private static final String AccountName = "account_name";
    private static final String LicenseKey = "license";
    private static final String CheckDateKey = "check_date";

    private static final int UndefinedValue = -1;

    public LicensingCache(@NonNull Context context)
    {
        m_cacheFile = context.getSharedPreferences(CacheFileName, Context.MODE_PRIVATE);
    }

    public String getAccountName()
    {
        return m_cacheFile.getString(AccountName, "");
    }

    public LicenseType getLicense()
    {
        final int licenseValue = m_cacheFile.getInt(LicenseKey, UndefinedValue);
        if (licenseValue == UndefinedValue)
            return LicenseType.NotChecked;

        return LicenseType.getType(licenseValue);
    }

    public Date getCheckDate()
    {
        final long checkDateUTC = m_cacheFile.getLong(CheckDateKey, UndefinedValue);
        if (checkDateUTC == UndefinedValue)
            return null;

        return new Date(checkDateUTC);
    }

    public void clear()
    {
        SharedPreferences.Editor editor = m_cacheFile.edit();
        editor.remove(AccountName);
        editor.remove(LicenseKey);
        editor.remove(CheckDateKey);
        editor.commit();
    }

    public void updateLicense(@NonNull String accountName, @NonNull LicenseType licenseType)
    {
        final Date currentDate = new Date();
        SharedPreferences.Editor editor = m_cacheFile.edit();
        editor.putString(AccountName, accountName);
        editor.putInt(LicenseKey, licenseType.getValue());
        editor.putLong(CheckDateKey, currentDate.getTime());
        editor.commit();
    }

    private SharedPreferences m_cacheFile;
}

public class Licensing implements ILicensing
{
    private static final String LogTag = "Licensing";
    private static final String LicensingCheckUrl = NetworkConstant.ServiceUrl + "license";
    private static final int MilliSecondsInDay = 24 * 60 * 60 * 1000;
    private static final int MaxLicenseTimeDeltaDays = 7;

    public Licensing(@NonNull Context _context, @NonNull IAuthenticationProvider _authProvider)
    {
        try
        {
            m_url = new URL(LicensingCheckUrl);
        }
        catch (MalformedURLException exp)
        {
            Log.e(LogTag, String.format("Url creating exception: \"%s\"", exp.getMessage()));
            throw new Error();
        }

        m_licensingCache = new LicensingCache(_context);
        m_authProvider = _authProvider;
    }

    @Override
    public LicenseType getCurrentLicenseInfo(@NonNull LoginPasswordCredentials credentials)
            throws CommonException, NetworkException
    {
        if (!credentials.isValid())
            throw new CommonException(CommonResultCode.InvalidArgument);

        LicenseType licenseType = getLicenseFromServer(credentials);
        m_licensingCache.updateLicense(credentials.login, licenseType);
        return licenseType;
    }

    @Override
    public LicenseType getCurrentLicenseInfo() throws CommonException, NetworkException
    {
        LoginPasswordCredentials credentials = m_authProvider.getLoginPasswordCredentials();
        if (credentials == null)
            throw new CommonException(CommonResultCode.InvalidArgument);

        return getLicenseByCredentials(credentials);
    }

    private LicenseType getLicenseByCredentials(@NonNull LoginPasswordCredentials credentials) throws CommonException, NetworkException
    {
        if (!credentials.isValid())
            throw new CommonException(CommonResultCode.InvalidArgument);

        LicenseType licenseType = LicenseType.NoLicense;
        try
        {
            licenseType = getLicenseFromServer(credentials);
            m_licensingCache.updateLicense(credentials.login, licenseType);
            return licenseType;
        }
        catch (NetworkException exp)
        {
            Log.i(LogTag, "Exception when verifying license occurred: " + exp.getMessage());
            if (exp.getResultCode() == NetworkResultCode.AuthenticationError)
                throw exp;
        }

        //
        // Take information from cache
        //
        final String accountName = m_licensingCache.getAccountName();
        if (!accountName.equalsIgnoreCase(credentials.login))
        {
            m_licensingCache.clear();
            return null;
        }

        final LicenseType cachedLicense = m_licensingCache.getLicense();
        if (!LicenseType.isActive(cachedLicense))
            return cachedLicense;

        final Date cachedDate = m_licensingCache.getCheckDate();
        if (cachedDate == null)
        {
            // assert condition!!
            Log.e(LogTag, "License cachedDate is null, while cached license is: " + cachedLicense.name());
            return LicenseType.Expired;
        }

        final Date currentDate = new Date();
        if (currentDate.compareTo(cachedDate) == -1)
        {
            // warning condition: Current time is less than cached license check time!!
            // User got his time back
            return cachedLicense;
        }

        final long TimeDiffDays = (currentDate.getTime() - cachedDate.getTime()) / MilliSecondsInDay;
        return MaxLicenseTimeDeltaDays >= TimeDiffDays ? cachedLicense : LicenseType.Expired;
    }

    private LicenseType getLicenseFromServer(@NonNull LoginPasswordCredentials credentials) throws NetworkException
    {
        Log.i(LogTag, "Login/Password credentials are provided. Try to renew license");
        Log.i(LogTag, "Login: " + credentials.login);
        Log.i(LogTag, "password: " + credentials.password);

        try
        {
            HttpURLConnection request = (HttpURLConnection) m_url.openConnection();
            request.setRequestMethod("GET");
            request.setRequestProperty(NetworkConstant.LoginHeader, credentials.login);
            if (!TextUtils.isEmpty(credentials.password))
                request.setRequestProperty(NetworkConstant.PasswordHeader, credentials.password);

            int responseCode = 0;
            try
            {
                responseCode = request.getResponseCode();
            }
            catch (IOException exp)
            {
                if (exp instanceof java.net.UnknownHostException)
                    throw new NetworkException(NetworkResultCode.NoConnection);
            }

            if (responseCode != 200)
            {
                Log.e(LogTag, "Response error code= " + responseCode);
                switch (responseCode)
                {
                    case 401:
                    case 404:
                        throw new NetworkException(NetworkResultCode.AuthenticationError);
                    default:
                        throw new NetworkException(NetworkResultCode.Unknown);
                }
            }

            InputStream responseBodyStream = request.getInputStream();

            String responseEncoding = request.getContentEncoding();
            responseEncoding = TextUtils.isEmpty(responseEncoding) ? "UTF-8" : responseEncoding;

            // http://developer.android.com/reference/android/util/JsonReader.html
            JsonReader jsonReader = new JsonReader(new InputStreamReader(responseBodyStream, responseEncoding));
            try
            {
                jsonReader.beginObject();

                while (jsonReader.hasNext())
                {
                    final String nodeName = jsonReader.nextName();
                    if (nodeName.equalsIgnoreCase("Type"))
                    {
                        return ParseLicenseType(jsonReader.nextString());
                    }
                }
                Log.e(LogTag, "No type node is found");

                jsonReader.endObject();
            }
            finally
            {
                Log.e(LogTag, "Failed to parse response json");
                jsonReader.close();
            }
        }
        catch (IOException exp)
        {
            Log.e(LogTag, exp.getMessage());
            throw new NetworkException(NetworkResultCode.Unknown);
        }

        return LicenseType.NoLicense;
    }

    private static LicenseType ParseLicenseType(@NonNull String license)
    {
        if (license.equalsIgnoreCase("Commercial"))
            return LicenseType.Commercial;
        else if (license.equalsIgnoreCase("Expired"))
            return LicenseType.Expired;
        else if (license.equalsIgnoreCase("NoLicense"))
            return LicenseType.NoLicense;

        Log.e(LogTag, "Could not parse license type: " + license);
        return LicenseType.NoLicense;
    }

    private URL m_url;
    private LicensingCache m_licensingCache;
    private IAuthenticationProvider m_authProvider;
}
