package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;

import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.android.product_tracer.TraceActivity;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

public class StartActivity extends Activity
{
    private final static String LogTag = StartActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try
        {
            m_tracer = TracerHelper.createFileTraceIfRestorationFailed(this, savedInstanceState);
            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "onCreate");

            constructUserInterface();
        }
        catch (Throwable exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);

            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this
                    , resources.getString(R.string.alert_title)
                    , resources.getString(R.string.alert_unknown_error));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "SaveInstanceState");

        m_tracer.saveInstance(savedInstanceState);
    }

    private void constructUserInterface()
    {
        {
            ImageView playImageView = (ImageView)findViewById(R.id.playImageView);
            playImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "Play is selected");
                    MainMenuActivity.startActivity(StartActivity.this);
                }
            });
        }

        {
            ImageView settingsImageView = (ImageView)findViewById(R.id.settingsImageView);
            settingsImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    TraceActivity.startActivity(StartActivity.this
                            , TracerHelper.getTracesDirectory(StartActivity.this));
                }
            });
        }

        {
            ImageView exitImageView = (ImageView)findViewById(R.id.exitImageView);
            exitImageView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    StartActivity.this.finish();
                }
            });
        }
    }

    private FileTracerInstance m_tracer;
}
