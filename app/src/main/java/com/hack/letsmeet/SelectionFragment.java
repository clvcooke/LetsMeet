package com.hack.letsmeet;

import android.content.Intent;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Colin on 2014-09-20.
 */
public class SelectionFragment  extends Fragment{


    private static final String TAG = "SelectionFragment";

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.selection,
                container, false);


        return view;
    }

}
