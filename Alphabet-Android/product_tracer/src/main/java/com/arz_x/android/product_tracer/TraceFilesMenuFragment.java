package com.arz_x.android.product_tracer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TextView;

import com.arz_x.android.AlertDialogHelper;
import com.arz_x.tracer.ProductTracer;

/**
 * Shows main menu of managing trace files: open/create/delete
 */
public class TraceFilesMenuFragment extends Fragment
{
    private static final String DropFolderTag = "com.arz_x.android.product_tracer.drop_folder";

    public static Fragment createFragment(@NonNull String dropFolder)
    {
        TraceFilesMenuFragment fragment = new TraceFilesMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DropFolderTag, dropFolder);
        fragment.setArguments(bundle);

        return fragment;
    }

    public TraceFilesMenuFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        Bundle arguments = getArguments();
        m_dropTraceFolder = arguments.getString(DropFolderTag);

        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(com.arz_x.android.product_tracer.R.layout.fragment_trace_files_menu, container, false);
        constructUserInterface(inflater, fragmentView);
        return fragmentView;
    }

    @Override
    public void onAttach(@NonNull Activity activity)
    {
        super.onAttach(activity);
        try
        {
            m_activityCallback = (ITraceFilesMenuCallback) activity;
        }
        catch (ClassCastException e)
        {
            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(activity,
                    resources.getString(R.string.alert_title),
                    resources.getString(R.string.error_assert));
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        m_activityCallback = null;
    }

    /**
     * Note: inflater and fragment view are marked final in order to use them in anonymous classes
     */
    private void refreshTraceFilesMenu(final @NonNull LayoutInflater inflater, final @NonNull View fragmentView)
    {
        TableLayout mainMenuLayout = (TableLayout) fragmentView.findViewById(R.id.menuTableLayout);
        mainMenuLayout.removeAllViews();

        List<String> sortedTraceFiles = new ArrayList<>();

        // add all processing files
        {
            String[] processingTraceFiles = ProductTracer.getAllProcessingTraceFiles(m_dropTraceFolder);
            if (processingTraceFiles == null)
                throw new AssertionError();
            Arrays.sort(processingTraceFiles, Collections.reverseOrder());
            Collections.addAll(sortedTraceFiles, processingTraceFiles);
        }

        // add other files
        {
            String[] finishedTraceFiles = ProductTracer.getAllFinishedTraceFiles(m_dropTraceFolder);
            if (finishedTraceFiles == null)
                throw new AssertionError();
            Arrays.sort(finishedTraceFiles, Collections.reverseOrder());
            Collections.addAll(sortedTraceFiles, finishedTraceFiles);
        }


        for (final String traceFile : sortedTraceFiles)
        {
            final View rowView = inflater.inflate(R.layout.trace_menu_item, null, false);

            TextView textView = (TextView) rowView.findViewById(R.id.fileNameTextView);
            textView.setText(new File(traceFile).getName());

            ImageButton viewButton = (ImageButton) rowView.findViewById(R.id.viewImageButton);
            viewButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    m_activityCallback.openTraceFile(traceFile);
                }
            });

            final ImageButton removeButton = (ImageButton) rowView.findViewById(R.id.removeImageButton);
            removeButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    File deleteFile = new File(traceFile);

                    final Resources resources = getResources();
                    AlertDialogHelper.showAlertDialog(getActivity()
                            , resources.getString(R.string.alert_title)
                            , String.format(resources.getString(R.string.alert_remove_file), deleteFile.getName())
                            , resources.getString(R.string.caption_ok)
                            , resources.getString(R.string.caption_cancel)
                            , new AlertDialogHelper.IDialogResultListener()
                    {
                        @Override
                        public void onDialogProcessed(@NonNull AlertDialogHelper.DialogResult dialogResult)
                        {
                            if (dialogResult == AlertDialogHelper.DialogResult.PositiveSelected)
                            {
                                File deleteFile = new File(traceFile);

                                if (!deleteFile.delete())
                                {
                                    final Resources resources = getResources();
                                    AlertDialogHelper.showMessageBox(getActivity(),
                                            resources.getString(R.string.alert_title),
                                            resources.getString(R.string.error_could_not_remove_file));
                                    return;
                                }
                                refreshTraceFilesMenu(inflater, fragmentView);
                            }
                        }
                    });
                }
            });

            mainMenuLayout.addView(rowView);
        }
    }

    private void constructUserInterface(@NonNull LayoutInflater inflater, @NonNull View fragmentView)
    {
        //
        // Deal with common controls
        //

        {
            ImageButton backButton = (ImageButton)fragmentView.findViewById(com.arz_x.android.product_tracer.R.id.backImageButton);
            backButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    m_activityCallback.close();
                }
            });
        }

        //
        // Deal with tracer files
        //

        refreshTraceFilesMenu(inflater, fragmentView);
    }

    private String m_dropTraceFolder;
    private ITraceFilesMenuCallback m_activityCallback;
}
