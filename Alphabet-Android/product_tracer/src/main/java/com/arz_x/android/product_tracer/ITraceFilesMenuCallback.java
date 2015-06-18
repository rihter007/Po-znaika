package com.arz_x.android.product_tracer;

import android.support.annotation.NonNull;

/**
 * Created by Rihter on 15.05.2015.
 * For link between ACtivity and menu fragment
 */
public interface ITraceFilesMenuCallback
{
    void openTraceFile(@NonNull String fileName);
    void close();
}
