package com.arz_x.android.product_tracer;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.arz_x.CommonException;
import com.arz_x.CommonResultCode;
import com.arz_x.android.AlertDialogHelper;

/**
 * Represents tool for viewing trace content
 */
public class TraceFileContentFragment extends Fragment
{
    private static final long DefaultMaxTraceContentSizeChars = 10 * 1024 * 1024; // 10 mb

    private static final String TraceFilePathTag = "com.arz_x.android.product_tracer.TraceFilePathTag";
    private static final String MaxReadContentTag = "com.arz_x.android.product_tracer.MaxReadContentTag";

    public static Fragment createFragment(@NonNull String traceFilePath, long maxReadContentInChars)
    {
        TraceFileContentFragment resultFragment = new TraceFileContentFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TraceFilePathTag, traceFilePath);
        bundle.putLong(MaxReadContentTag,
                maxReadContentInChars == 0 ? DefaultMaxTraceContentSizeChars : maxReadContentInChars);
        resultFragment.setArguments(bundle);

        return resultFragment;
    }

    public TraceFileContentFragment()
    {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        final View fragmentView = inflater.inflate(com.arz_x.android.product_tracer.R.layout.fragment_trace_file_content, container, false);

        try
        {
            m_maxReadContentLengthChars = getArguments().getLong(MaxReadContentTag);
            constructUserInterface(getArguments().getString(TraceFilePathTag), fragmentView);
        }
        catch (CommonException exp)
        {
            final Resources resources = getResources();
            final String message = exp.getResultCode() == CommonResultCode.NotFound ?
                    exp.getMessage() : resources.getString(R.string.error_file_not_found);

            AlertDialogHelper.showMessageBox(getActivity(),
                    resources.getString(R.string.alert_title),
                    message);
        }
        catch (Exception exp)
        {
            final Resources resources = getResources();
            AlertDialogHelper.showMessageBox(getActivity(),
                    resources.getString(R.string.alert_title),
                    String.format(resources.getString(R.string.error_assert_with_description), exp.getMessage()));
        }

        return fragmentView;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            m_fileContentCallback = (ITraceFileContentCallback) activity;
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
        m_fileContentCallback = null;
    }

    private void constructUserInterface(@NonNull String traceFilePath, @NonNull View fragmentView)
            throws IOException, CommonException
    {
        {
            ImageButton backButton = (ImageButton)fragmentView.findViewById(R.id.backImageButton);
            backButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    m_fileContentCallback.close();
                }
            });
        }

        FileInputStream fileContentStream = null;
        InputStreamReader fileContentReader = null;
        BufferedReader bufferedContentReader = null;
        try
        {
            fileContentStream = new FileInputStream(traceFilePath);
            fileContentReader = new InputStreamReader(fileContentStream, "UTF-8");
            bufferedContentReader = new BufferedReader(fileContentReader);

            String fileContent = "";
            while (fileContent.length() < m_maxReadContentLengthChars)
            {
                final String traceLine = bufferedContentReader.readLine();
                if (traceLine == null)
                    break;
                fileContent += traceLine + '\n';
            }

            TextView contentView = (TextView)fragmentView.findViewById(R.id.contentTextView);
            contentView.setText(fileContent);
        }
        finally
        {
            if (bufferedContentReader != null)
                bufferedContentReader.close();
            if (fileContentReader != null)
                fileContentReader.close();
            if (fileContentStream != null)
                fileContentStream.close();
        }
    }

    private long m_maxReadContentLengthChars;
    private ITraceFileContentCallback m_fileContentCallback;
}
