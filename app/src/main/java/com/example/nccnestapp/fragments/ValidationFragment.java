/*
 * Copyright 2018 AFeas1987
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.nccnestapp.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.example.nccnestapp.R;
import com.example.nccnestapp.activities.AbstractActivity;
import com.example.nccnestapp.activities.QuestionActivity;
import com.example.nccnestapp.utilities.PantryGuest;

import io.realm.Case;
import io.realm.RealmResults;

public class ValidationFragment extends Fragment {

    EditText emailView;
    AbstractActivity myActivity;
    RealmResults<PantryGuest> mResults;
    ProgressBar mProgress;
    ImageView mImage;
    Button launchButton;
    TextWatcher eListener;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_validation, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        myActivity = (AbstractActivity) getActivity();
        emailView = myActivity.findViewById(R.id.edit_question_response);
        mProgress = myActivity.findViewById(R.id.progress_valid_email);
        mImage = myActivity.findViewById(R.id.img_valid_email);
        launchButton = myActivity.findViewById(R.id.btn_valid_launch);
        launchButton.setOnClickListener(launchView -> {
            ((QuestionActivity)myActivity).showPinDialog(emailView.getText().toString());
//            onDestroyView();
        });

        emailView.addTextChangedListener(eListener = new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailView = myActivity.findViewById(R.id.edit_question_response);
                mImage.setVisibility(View.GONE);
                mProgress.setVisibility(View.VISIBLE);
                if (myActivity.realm != null) {
                    mResults = myActivity.realm.where(PantryGuest.class)
                            .equalTo("email", emailView.getText().toString(), Case.INSENSITIVE).findAll();
                    mProgress.setVisibility(View.GONE);
                    if (!isValidEmail(emailView.getText()) || mResults.size() != 0) {
                        mImage.setImageResource(android.R.drawable.ic_delete);
                        mImage.setVisibility(View.VISIBLE);
                        launchButton.setEnabled(false);
                    } else {
                        mImage.setImageResource(android.R.drawable.presence_online);
                        mImage.setVisibility(View.VISIBLE);
                        launchButton.setEnabled(true);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    @Override
    public void onDestroyView() {
        emailView.removeTextChangedListener(eListener);
        super.onDestroyView();
    }


    private boolean isValidEmail(CharSequence target) {
        return target != null && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}