package ru.po_znaika.licensing;

import ru.po_znaika.common.CommonException;
import ru.po_znaika.network.NetworkException;

/**
 * Created by Rihter on 19.01.2015.
 * Represents access to license.
 */
public interface ILicensing
{
    /**
     *
     * @return
     * @throws CommonException
     */
    LicenseType getCurrentLicenseInfo() throws CommonException, NetworkException;
}
