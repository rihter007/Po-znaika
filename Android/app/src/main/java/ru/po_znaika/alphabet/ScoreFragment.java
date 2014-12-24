package ru.po_znaika.alphabet;



import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;


/**
 * Shows final score and finishes the exercise
 */
public class ScoreFragment extends Fragment
{
    public static final String ScoreTag = "score";

    public ScoreFragment()
    {
        // Required empty public constructor
    }

    private  void restoreInternalState()
    {
        m_score = getArguments().getInt(ScoreTag);
    }

    private void constructUserInterface(View fragmentView)
    {
        // restore caption
        try
        {
            final String resultCaption = String.format(getResources().getString(R.string.caption_score), m_score);

            TextView scoreTextView = (TextView)fragmentView.findViewById(R.id.scoreTextView);
            scoreTextView.setText(resultCaption);
        }
        catch (Exception exp)
        {
            MediaPlayer med = new MediaPlayer();
        }

        // Set button onclick listener
        {
            Button finishButton = (Button)fragmentView.findViewById(R.id.finishButton);
            finishButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    getActivity().finish();
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View fragmentView = inflater.inflate(R.layout.fragment_score, container, false);

        restoreInternalState();
        constructUserInterface(fragmentView);

        return fragmentView;
    }

    private int m_score;
}
