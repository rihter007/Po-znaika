package ru.po_znaika.licensing;

import java.util.HashMap;

/**
 * Created by Rihter on 26.01.2015.
 */
public enum LicenseType
{
    NotChecked(0),            // no previous license status was ever checked
    NoLicense(1),             // no active license
    Expired(2),               // previous license was active commercial, but now it is expired
    Commercial(3);            // current license is active commercial license

    private static HashMap<Integer, LicenseType> m_typesMap = new HashMap<Integer, LicenseType>()
    {
        { put(NotChecked.m_value, NotChecked); }
        { put(NoLicense.m_value, NoLicense); }
        { put(Expired.m_value, Expired); }
        { put(Commercial.m_value, Commercial); }
    };

    private LicenseType(int value)
    {
        m_value = value;
    }

    public int getValue()
    {
        return m_value;
    }

    public static LicenseType getType(int value)
    {
        return m_typesMap.get(value);
    }

    public static boolean isActive(LicenseType licenseType)
    {
        return licenseType == LicenseType.Commercial;
    }

    private int m_value;
}
