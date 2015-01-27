package ru.po_znaika.licensing;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Date;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.network.IAuthenticationProvider;
import ru.po_znaika.network.LoginPasswordCredentials;
import ru.po_znaika.network.NetworkConstant;
import ru.po_znaika.network.NetworkException;

/**
 * Created by Rihter on 19.01.2015.
 * Realization of ILicense interface
 */
class LicensingCache
{
    public LicensingCache(Context context)
    {

    }

    public LicenseType getCurrentLicense()
    {
        return LicenseType.NoLicense;
    }

    public Date getCheckDate()
    {
        throw new UnsupportedOperationException();
    }

    public void UpdateLicenseInfo(LicenseType licenseType)
    {

    }

    private Context m_context;
}

public class Licensing implements ILicensing
{
    private static final String LicensingCheckUrl = NetworkConstant.ServiceUrl + "license";
    private static final int TrustDeltaInDays = 7;

    public Licensing( Context context, @NonNull IAuthenticationProvider _authProvider)
    {
        m_licensingCache = new LicensingCache(context);

        m_authProvider = _authProvider;
        if (m_authProvider == null)
            throw new NullPointerException();
    }

    public LicenseType getCurrentLicenseInfo() throws CommonException, NetworkException
    {
        LoginPasswordCredentials credentials = m_authProvider.getLogicPasswordCredentials();
        if ((credentials == null) || (!credentials.isValid()))
            return null;

        //
        // Try to refresh license info by internet
        //

        //
        // Take information from cache
        //

        throw new UnsupportedOperationException();
    }

    private LicensingCache m_licensingCache;
    private IAuthenticationProvider m_authProvider;
}
