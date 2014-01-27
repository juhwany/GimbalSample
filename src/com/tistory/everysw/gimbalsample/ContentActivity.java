/**
 * Copyright (C) 2011 Qualcomm Retail Solutions, Inc. All rights reserved.
 *
 * This software is the confidential and proprietary information of Qualcomm Retail Solutions, Inc.
 *
 * The following sample code illustrates various aspects of the Gimbal SDK.
 *
 * The sample code herein is provided for your convenience, and has not been
 * tested or designed to work on any particular system configuration. It is
 * provided AS IS and your use of this sample code, whether as provided or with
 * any modification, is at your own risk. Neither Qualcomm Retail Solutions, Inc. nor any
 * affiliate takes any liability nor responsibility with respect to the sample
 * code, and disclaims all warranties, express and implied, including without
 * limitation warranties on merchantability, fitness for a specified purpose,
 * and against infringement.
 */
package com.tistory.everysw.gimbalsample;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class ContentActivity extends Activity {

    private static final String TAG = ContentActivity.class.getSimpleName();
    public static final String CONTENT_KEY = "com.company.CONTENT_URL";

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content);
        textView = (TextView) findViewById(R.id.textView);
        Button saveButton = (Button) findViewById(R.id.save_button);
        saveButton.setOnClickListener(onClickGoBack());
        Button notNowButton = (Button) findViewById(R.id.not_now_button);
        notNowButton.setOnClickListener(onClickGoBack());
    }

    private OnClickListener onClickGoBack() {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String content = intent.getStringExtra(CONTENT_KEY);
        textView.setText(content);
    }

}
