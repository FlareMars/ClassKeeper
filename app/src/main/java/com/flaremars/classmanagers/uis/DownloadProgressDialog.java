package com.flaremars.classmanagers.uis;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.flaremars.classmanagers.R;

public class DownloadProgressDialog extends DialogFragment {

    private static DownloadProgressDialog instance;

    private NumberProgressBar progressBar;

    public DownloadProgressDialog() {

    }

    public static DownloadProgressDialog getDownloadDialog() {
        if (instance == null) {
            instance = new DownloadProgressDialog();
            instance.setStyle(DialogFragment.STYLE_NO_TITLE,0);
        }
        return instance;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.layout_about, null);
        progressBar = (NumberProgressBar) view.findViewById(R.id.number_progress_bar);
        return view;
    }

    public void setProgress(Integer progress) {
        instance.progressBar.setProgress(progress);
        if (progress >= 100) {
            instance.progressBar.setProgress(0);
            instance.dismiss();
        }
    }
}
