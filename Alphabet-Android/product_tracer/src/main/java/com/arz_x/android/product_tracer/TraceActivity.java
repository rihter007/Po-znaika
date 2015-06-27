package com.arz_x.android.product_tracer;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.arz_x.android.AlertDialogHelper;

public class TraceActivity extends ActionBarActivity implements ITraceFilesMenuCallback, ITraceFileContentCallback
{
    enum ActivityStage
    {
        Initial,
        TraceFileMenu,
        TraceFileContent;
    }

    private static final String TracesDropFolderTag = "traces_drop_folder";
    private static final String FragmentTag = "fragment_tag";

    public static void startActivity(@NonNull Context context, @NonNull String tracesDropFolder)
    {
        Intent intent = new Intent(context, TraceActivity.class);
        intent.putExtra(TracesDropFolderTag, tracesDropFolder);

        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(com.arz_x.android.product_tracer.R.layout.activity_trace);

        try
        {
            FragmentManager fragmentManager = getFragmentManager();

            final Fragment resultFragment = (savedInstanceState != null) ?
                    fragmentManager.getFragment(savedInstanceState, FragmentTag) :
                    TraceFilesMenuFragment.createFragment(getIntent().getExtras().getString(TracesDropFolderTag));
            drawFragment(resultFragment);
        }
        catch (Throwable exp)
        {
            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(this,
                    resources.getString(R.string.alert_title),
                    String.format(resources.getString(R.string.error_assert_with_description), exp.getMessage()));
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);

        final FragmentManager fragmentManager = getFragmentManager();
        final Fragment currentFragment = fragmentManager.findFragmentByTag(FragmentTag);
        fragmentManager.putFragment(savedInstanceState, FragmentTag, currentFragment);
    }

    @Override
    public void openTraceFile(@NonNull String fileName)
    {
        drawFragment(TraceFileContentFragment.createFragment(fileName, 0));
    }

    @Override
    public void close()
    {
        switch (getCurrentStage())
        {
            case TraceFileMenu:
                finish();
                break;
            case TraceFileContent:
                final Fragment traceMenuFragment = TraceFilesMenuFragment.createFragment(getIntent().getExtras().getString(TracesDropFolderTag));
                drawFragment(traceMenuFragment);
                break;
        }
    }

    private void drawFragment(@NonNull Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(com.arz_x.android.product_tracer.R.id.container, fragment, FragmentTag);
        fragmentTransaction.commit();
    }

    private ActivityStage getCurrentStage()
    {
        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.container);
        if (currentFragment == null)
            return ActivityStage.Initial;

        if (currentFragment instanceof TraceFilesMenuFragment)
            return ActivityStage.TraceFileMenu;
        if (currentFragment instanceof TraceFileContentFragment)
            return ActivityStage.TraceFileContent;

        throw new UnknownError();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.arz_x.android.product_tracer.R.menu.menu_trace, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.arz_x.android.product_tracer.R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
