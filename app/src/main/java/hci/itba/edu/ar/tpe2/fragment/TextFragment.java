package hci.itba.edu.ar.tpe2.fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hci.itba.edu.ar.tpe2.R;

/**
 * A simple {@link Fragment} to display text. The text can be changed, but the fragment does not
 * react to interaction.
 */
public class TextFragment extends Fragment {
    private String text;
    private TextView textView;

    public static TextFragment newInstance(String startingText) {
        TextFragment result = new TextFragment();
        result.text = startingText;
        return result;
    }

    public TextFragment() {
        // Required empty public constructor
    }

    public void setText(String text) {
        textView.setText(text);
    }

    public TextView getTextView() {
        return textView;
    }

    public void appendText(String text) {
        textView.append(text);
    }

    public void clear() {
        textView.setText("");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_text, container, false);
        textView = (TextView) v.findViewById(R.id.fragment_text);
        if (text != null) {
            setText(text);
        }
        return v;
    }
}
