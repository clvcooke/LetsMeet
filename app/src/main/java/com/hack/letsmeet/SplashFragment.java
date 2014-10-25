/**
 * Copyright 2010-present Facebook.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hack.letsmeet;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.facebook.LoginActivity;
import com.facebook.widget.LoginButton;
import com.parse.LogInCallback;
import com.parse.Parse;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;

import java.text.ParseException;
import java.util.Arrays;
import java.util.List;

public class SplashFragment extends Fragment{

    private TextView skipLoginButton;
    private SkipLoginCallback skipLoginCallback;

    public interface SkipLoginCallback {
        void onSkipLoginPressed();
    }


    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.splash,
                container, false);

        LoginButton authButton = (LoginButton) view.findViewById(R.id.login_button);
        authButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                onLoginButtonClicked();
            }
        });
        getActivity().getActionBar().hide();
        return view;
    }

    private  void onLoginButtonClicked() {
        //TODO add progress message
        List<String> permissions = Arrays.asList("public_profile", "user_friends", "user_about_me",
                "user_relationships", "user_birthday", "user_location");
        ParseFacebookUtils.logIn(permissions, this.getActivity(), new LogInCallback() {

            @Override
            public void done(ParseUser user, com.parse.ParseException e) {
                if (user == null) {
                    Log.d("NULL",
                            "Uh oh. The user cancelled the Facebook login.");
                } else if (user.isNew()) {
                    Log.d("NEW",
                            "User signed up and logged in through Facebook!");
                  /*  showUserDetailsActivity();*/
                } else {
                    Log.d("LOGGED IN",
                            "User logged in through Facebook!");
                    /*showUserDetailsActivity();*/
                }
            }
        });



    }
}

