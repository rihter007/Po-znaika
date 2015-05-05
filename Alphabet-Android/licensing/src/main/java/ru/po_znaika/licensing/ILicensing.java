package ru.po_znaika.licensing;

import android.support.annotation.NonNull;

import com.arz_x.CommonException;
import ru.po_znaika.network.LoginPasswordCredentials;
import com.arz_x.NetworkException;

/**
 * Created by Rihter on 19.01.2015.
 * Represents access to license.
 */
public interface ILicensing
{
    /** Gets license for account by specified credentials
     * @param credentials Credentials used to get license
     * @return License obtained for account by specified credentials
     * @throws CommonException
     */
    LicenseType getCurrentLicenseInfo(@NonNull LoginPasswordCredentials credentials) throws CommonException, NetworkException;

    /**
     *
     * @return
     * @throws CommonException
     */
    LicenseType getCurrentLicenseInfo() throws CommonException, NetworkException;
}
