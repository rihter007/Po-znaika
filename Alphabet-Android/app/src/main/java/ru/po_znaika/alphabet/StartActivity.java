package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.arz_x.CommonException;
import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.ImageHelper;
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
                    , resources.getString(R.string.error_unknown_error));
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        releaseUserInterface();
        m_tracer.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        drawUserInterface();
        try
        {
            m_tracer.resume();
        }
        catch (CommonException exp)
        {
            // this should never happen
            throw new AssertionError();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        try
        {
            ProductTracer.traceMessage(m_tracer, TraceLevel.Info, LogTag, "SaveInstanceState");
            FileTracerInstance.saveInstance(m_tracer, savedInstanceState);
        }
        catch (Throwable exp)
        {
            ProductTracer.traceException(m_tracer, TraceLevel.Error, LogTag, exp);
            throw exp;
        }
    }

    private void constructUserInterface()
    {
        //drawUserInterface();
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

    private void drawUserInterface()
    {
        final DisplayMetricsHelper displayMetricsHelper = new DisplayMetricsHelper(this);

        {
            RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.mainLayout);
            final Bitmap backgroundImage = ImageHelper.getImageForSpecifiedDimensions(getResources()
                    , R.drawable.start_screen_background
                    , displayMetricsHelper.getDisplayWidth()
                    , displayMetricsHelper.getDisplayHeight());
            relativeLayout.setBackground(new BitmapDrawable(getResources(), backgroundImage));
        }

        Pair<Integer, Integer>[] viewImagePairs = new Pair[]
                {
                        new Pair(R.id.sunImageView, R.drawable.start_screen_sun),
                        new Pair(R.id.helloPupilImageView, R.drawable.start_screen_hello_pupil),
                        new Pair(R.id.playImageView, R.drawable.start_screen_play),
                        new Pair(R.id.parentsImageView, R.drawable.start_screen_parents),
                        new Pair(R.id.exitImageView, R.drawable.start_screen_exit),
                        new Pair(R.id.settingsImageView, R.drawable.start_screen_settings)
                };

        for (Pair<Integer, Integer> viewImage : viewImagePairs)
        {
            View processedView = findViewById(viewImage.first);
            final ViewGroup.LayoutParams viewParams = processedView.getLayoutParams();
            final Bitmap backgroundImage = ImageHelper.getImageForSpecifiedDimensions(getResources()
                    , viewImage.second
                    , viewParams.width
                    , viewParams.height);
            findViewById(viewImage.first).setBackground(new BitmapDrawable(getResources(), backgroundImage));
        }
    }

    private void releaseUserInterface()
    {
        final int backgroundColor = getResources().getColor(R.color.standard_background);

        final int[] viewIds = new int[]
                {
                        R.id.mainLayout,
                        R.id.sunImageView,
                        R.id.helloPupilImageView,
                        R.id.playImageView,
                        R.id.parentsImageView,
                        R.id.exitImageView,
                        R.id.settingsImageView
                };

        for (int viewId : viewIds)
            findViewById(viewId).setBackgroundColor(backgroundColor);
    }

    private FileTracerInstance m_tracer;
}
