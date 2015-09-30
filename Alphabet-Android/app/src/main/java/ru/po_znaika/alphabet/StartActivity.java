package ru.po_znaika.alphabet;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.support.annotation.NonNull;

import com.arz_x.android.AlertDialogHelper;
import com.arz_x.android.DisplayMetricsHelper;
import com.arz_x.android.DrawOptions;
import com.arz_x.android.DynamicUIManager;
import com.arz_x.android.UIElementInfo;
import com.arz_x.android.UIElementInfoBuilder;
import com.arz_x.android.UIElementScaleResourceInfo;
import com.arz_x.android.UIElementScaleResourceInfoBuilder;
import com.arz_x.android.UIElementType;
import com.arz_x.android.product_tracer.FileTracerInstance;
import com.arz_x.android.product_tracer.TraceActivity;
import com.arz_x.tracer.ProductTracer;
import com.arz_x.tracer.TraceLevel;

public class StartActivity extends Activity
{
    private final static String LogTag = StartActivity.class.getName();

    private static final UIElementScaleResourceInfo[] DynamicUIElementsInfo = new UIElementScaleResourceInfo[]
    {
            new UIElementScaleResourceInfoBuilder().setElementId(R.id.helloPupilImageView).setElementType(UIElementType.ImageView)
                    .setLeft(R.integer.start_screen_hello_student_left_margin)
                    .setTop(R.integer.start_screen_hello_student_top_margin)
                    .setWidth(R.integer.start_screen_hello_student_width)
                    .setHeight(R.integer.start_screen_hello_student_height)
                    .setHeightWidthProportion(R.integer.start_screen_hello_student_height_width_proportion)
                    .setImageResourceId(R.drawable.start_screen_hello_pupil)
                    .build(),

            new UIElementScaleResourceInfoBuilder().setElementId(R.id.sunImageView).setElementType(UIElementType.ImageView)
                    .setRight(R.integer.start_screen_sun_right_margin)
                    .setTop(R.integer.start_screen_sun_top_margin)
                    .setWidth(R.integer.start_screen_sun_width)
                    .setHeight(R.integer.start_screen_sun_height)
                    .setHeightWidthProportion(R.integer.start_screen_sun_height_width_proportion)
                    .setImageResourceId(R.drawable.start_screen_sun)
                    .build(),

            new UIElementScaleResourceInfoBuilder().setElementId(R.id.playImageView).setElementType(UIElementType.ImageView)
                    .setLeft(R.integer.start_screen_play_left_margin)
                    .setTop(R.integer.start_screen_play_top_margin)
                    .setWidth(R.integer.start_screen_play_width)
                    .setHeight(R.integer.start_screen_play_height)
                    .setHeightWidthProportion(R.integer.start_screen_play_height_width_proportion)
                    .setImageResourceId(R.drawable.start_screen_play)
                    .build(),

            new UIElementScaleResourceInfoBuilder().setElementId(R.id.parentsImageView).setElementType(UIElementType.ImageView)
                    .setRight(R.integer.start_screen_parents_right_margin)
                    .setTop(R.integer.start_screen_parents_top_margin)
                    .setWidth(R.integer.start_screen_parents_width)
                    .setHeight(R.integer.start_screen_parents_height)
                    .setHeightWidthProportion(R.integer.start_screen_parents_height_width_proportion)
                    .setImageResourceId(R.drawable.start_screen_parents)
                    .build(),

            new UIElementScaleResourceInfoBuilder().setElementId(R.id.exitImageView).setElementType(UIElementType.ImageView)
                    .setLeft(R.integer.start_screen_exit_left_margin)
                    .setTop(R.integer.start_screen_exit_top_margin)
                    .setWidth(R.integer.start_screen_exit_width)
                    .setHeight(R.integer.start_screen_exit_height)
                    .setHeightWidthProportion(R.integer.start_screen_exit_height_width_proportion)
                    .setImageResourceId(R.drawable.start_screen_exit)
                    .build(),

            new UIElementScaleResourceInfoBuilder().setElementId(R.id.settingsImageView).setElementType(UIElementType.ImageView)
                    .setLeft(R.integer.start_screen_settings_left_margin)
                    .setBottom(R.integer.start_screen_settings_bottom_margin)
                    .setWidth(R.integer.start_screen_settings_width)
                    .setHeight(R.integer.start_screen_settings_height)
                    .setHeightWidthProportion(R.integer.start_screen_settings_height_width_proportion)
                    .setImageResourceId(R.drawable.start_screen_settings)
                    .build(),
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setRequestedOrientation(getResources().getDimension(R.dimen.orientation_flag) == 0 ?
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        try
        {
            DisplayMetricsHelper metricsHelper = new DisplayMetricsHelper(this);
            UIElementInfo[] dynamicElements = new UIElementInfo[DynamicUIElementsInfo.length];
            for (int elementIndex = 0; elementIndex < dynamicElements.length; ++elementIndex)
            {
                dynamicElements[elementIndex] = UIElementInfoBuilder.create(getResources()
                        , metricsHelper
                        , DynamicUIElementsInfo[elementIndex]).build();
            }

            m_dynamicUIManager = new DynamicUIManager(getResources()
                    , (RelativeLayout)findViewById(R.id.mainLayout)
                    , DynamicUIManager.CreateOptions.FailIfNotExist
                    , dynamicElements
                    , new DrawOptions.ImageDrawOption(R.drawable.start_screen_background)
                    , getResources().getColor(R.color.standard_background));

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

        m_dynamicUIManager.hideElements();
        m_tracer.pause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        m_dynamicUIManager.drawElements();
        m_tracer.resume();
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

    private DynamicUIManager m_dynamicUIManager;
    private FileTracerInstance m_tracer;
}
