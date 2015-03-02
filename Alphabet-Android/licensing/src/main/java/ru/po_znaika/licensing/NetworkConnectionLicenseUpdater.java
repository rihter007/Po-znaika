package ru.po_znaika.licensing;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ru.po_znaika.network.IAuthenticationProvider;
import ru.po_znaika.network.CacheAuthenticationProvider;

/**
 * Created by Rihter on 25.01.2015.
 * Used for license check even while the other main program logic is running
 */
public class NetworkConnectionLicenseUpdater extends BroadcastReceiver
{
    public NetworkConnectionLicenseUpdater()
    {
        m_authProvider = null; //new CacheAuthenticationProvider();
    }

    public void onReceive(Context context, Intent intent)
    {
       /* ConnectivityManager connectivityManager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo network = connectivityManager.getActiveNetworkInfo();
        if ((network == null) || (network.getState() != NetworkInfo.State.CONNECTED))
            return;

        Licensing licensing = new Licensing(context, m_authProvider);

        try
        {
            licensing.getCurrentLicenseInfo();
        }
        catch (Exception exp)
        {
            // just ignore
        }
        */
    }

    private IAuthenticationProvider m_authProvider;
}
