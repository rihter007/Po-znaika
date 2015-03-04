package ru.po_znaika.common.ru.po_znaika.common.helpers;

import android.support.annotation.NonNull;

import java.io.ByteArrayInputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Rihter on 05.03.2015.
 * Contains common helpers for usage everywhere
 */
public class CommonHelpers
{
    public static Date localToGmt(@NonNull Date date)
    {
        final TimeZone localTimeZone = TimeZone.getDefault();
        Date gmtTime = new Date(date.getTime() - localTimeZone.getRawOffset());

        // if we are now in DST, back off by the delta.  Note that we are checking the GMT date, this is the KEY.
        if (localTimeZone.inDaylightTime(date))
        {
            Date dstDate = new Date(gmtTime.getTime() - localTimeZone.getDSTSavings() );

            // check to make sure we have not crossed back into standard time
            // this happens when we are on the cusp of DST (7pm the day before the change for PDT)
            if (localTimeZone.inDaylightTime(dstDate))
            {
                gmtTime = dstDate;
            }
        }
        return gmtTime;
    }

    public static Date beginOfTheDay(@NonNull Date date)
    {
        Calendar inputDate = Calendar.getInstance();
        inputDate.setTime(date);

        Calendar resultDate = Calendar.getInstance();
        resultDate.set(inputDate.get(Calendar.YEAR), inputDate.get(Calendar.MONTH), inputDate.get(Calendar.DAY_OF_YEAR));
        return resultDate.getTime();
    }
}
