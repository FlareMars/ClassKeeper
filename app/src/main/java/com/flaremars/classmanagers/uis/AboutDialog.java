package com.flaremars.classmanagers.uis;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.flaremars.classmanagers.R;

public class AboutDialog extends DialogFragment {

    private static AboutDialog instance;

    public AboutDialog() {

    }

    public static AboutDialog getAboutDialog() {
        if (instance == null) {
            instance = new AboutDialog();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        return instance;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return View.inflate(getActivity(), R.layout.layout_about2, null);
    }
}
