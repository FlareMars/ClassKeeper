package com.flaremars.classmanagers.uis;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.List;

public abstract class BaseFragment extends Fragment {

    private ContainerActivity parentContainer;

    @Override
    public void onAttach(Activity parent) {
        super.onAttach(parent);
        try {
            parentContainer = (ContainerActivity) parent;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public ContainerActivity getContainerActivity() {
        return parentContainer;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        //设置返回动作
        parentContainer.setOnBackListener(new ContainerActivity.OnBackListener() {
            @Override
            public void onBack() {
                backAction();
            }
        });

        //设置标题
        parentContainer.setTitle(fragmentTitle());

        //设置actionView
        parentContainer.showActionViews(setActionViews());
    }

    abstract void backAction();

    abstract String fragmentTitle();

    abstract List<Integer> setActionViews();
}
