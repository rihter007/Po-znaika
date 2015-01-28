package ru.po_znaika.licensing;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.net.MalformedURLException;
import java.util.Date;

import java.net.URL;
import java.net.HttpURLConnection;

import java.io.IOException;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.network.IAuthenticationProvider;
import ru.po_znaika.network.LoginPasswordCredentials;
import ru.po_znaika.network.NetworkConstant;

/**
 * Created by Rihter on 19.01.2015.
 * Realization of ILicense interface
 */
class LicensingCache
{
    private static final String CacheFileName = "LicensingCache";
    private static final String LicenseKey = "license";
    private static final String CheckDateKey = "check_date";

    private static final int UndefinedValue = -1;

    public LicensingCache(@NonNull Context context)
    {
        m_cacheFile = context.getSharedPreferences(CacheFileName, Context.MODE_PRIVATE);
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
        final long checkDateUTC = m_cacheFile.getInt(CheckDateKey, UndefinedValue);
        if (checkDateUTC == UndefinedValue)
            return null;

        return new Date(checkDateUTC);
    }

    public void updateLicense(@NonNull LicenseType licenseType)
    {
        final Date currentDate = new Date();
        SharedPreferences.Editor editor = m_cacheFile.edit();
        editor.putInt(LicenseKey, licenseType.getValue());
        editor.putLong(CheckDateKey, currentDate.getTime());
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

    public LicenseType getCurrentLicenseInfo() throws CommonException
    {
        //
        // Try to refresh license info by internet
        //

        LoginPasswordCredentials credentials = m_authProvider.getLoginPasswordCredentials();
        if ((credentials == null) || (!credentials.isValid()))
        {
            Log.i(LogTag, "Login/Password credentials are provided. Try to renew license");
            try
            {
                HttpURLConnection request = (HttpURLConnection) m_url.openConnection();
                request.setRequestMethod("GET");
                request.setRequestProperty(NetworkConstant.LoginHeader, credentials.login);
                if (!TextUtils.isEmpty(credentials.password))
                    request.setRequestProperty(NetworkConstant.PasswordHeader, credentials.password);

                final String currentLicenseLiteral = request.getHeaderField("License");
                if (currentLicenseLiteral != null)
                {
                    final LicenseType currentLicense = ParseLicenseType(currentLicenseLiteral);
                    m_licensingCache.updateLicense(currentLicense);
                    return currentLicense;
                }
            }
            catch (IOException exp)
            {
                Log.i(LogTag, String.format("Failed to open connection, exception message: \"%s\"", exp.getMessage()));
            }
        }

        //
        // Take information from cache
        //
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
