package ru.po_znaika.licensing;

/**
 * Created by Rihter on 26.01.2015.
 */
public enum LicenseType
{
    NoLicense,
    NotChecked,
    Expired,
    Commercial;

    public static boolean isActive(LicenseType licenseType)
    {
        return licenseType == LicenseType.Commercial;
    }
}
