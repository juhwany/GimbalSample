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

import java.util.Iterator;
import java.util.Map;

import android.content.Intent;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcommlabs.usercontext.protocol.ContentAttributes;
import com.qualcommlabs.usercontext.protocol.ContentDescriptor;
import com.qualcommlabs.usercontext.protocol.ContentEvent;

public class MallMartPresenter {

    private static final String TAG = "MallMart";

    private final MallMartActivity activity;
    private View settingsLayout;
    private ImageButton userContextSettingsButton;
    private TextView placeEventTextView;
    private TextView contentTextView;
    private LinearLayout enableUserContextAreaLayout;
    private Button enableUserContextButton;
    private Button acceptTermsButton;
    private LinearLayout locationAndContentAreoLayout;

    public MallMartPresenter(MallMartActivity mallMartActivity) {
        this.activity = mallMartActivity;
    }

    public void initializeView() {
        activity.setContentView(R.layout.main);
        placeEventTextView = (TextView) activity.findViewById(R.id.place_event_text);
        contentTextView = (TextView) activity.findViewById(R.id.content_text);
        settingsLayout = activity.findViewById(R.id.settings_layout);
        enableUserContextAreaLayout = (LinearLayout) activity.findViewById(R.id.enable_relasphere_area);
        locationAndContentAreoLayout = (LinearLayout) activity.findViewById(R.id.location_content_area);
        enableUserContextButton = (Button) activity.findViewById(R.id.download_relasphere_button);
        acceptTermsButton = (Button) activity.findViewById(R.id.accept_terms_button);

        initializeSettingsButton();
        initializeHiddenMap();
    }

    public void onContextConnectorEnableFailure(String message) {
        showEnableContextConnectorButton();
        if (message != null) {
            toastAndLogError(message);
        }
    }

    public void showEnableContextConnectorButton() {
        setPlaceEventAndContentAreaVisibility(View.GONE);
        enableUserContextAreaLayout.setVisibility(View.VISIBLE);
        initializeEnableUserContextButton();
    }

    public void hideEnableContextConnectorButton() {
        setPlaceEventAndContentAreaVisibility(View.VISIBLE);
        enableUserContextAreaLayout.setVisibility(View.GONE);
    }

    public void updateContentText(String title) {
        contentTextView.setText(title);
    }

    public void onPermissionsDisabled() {
        updatePlaceEventText(activity.getString(R.string.disabled));
    }

    public void onLocationPermissionDisabled() {
        updatePlaceEventText(activity.getString(R.string.location_disabled));
    }

    public void updatePlaceEventText(String placeText) {
        placeEventTextView.setText(placeText);
    }

    public void showSettings() {
        settingsLayout.setVisibility(View.VISIBLE);
    }

    public void hideSettings() {
        settingsLayout.setVisibility(View.GONE);
    }

    public void updateContentTextAndSetOnClick(ContentEvent contentEvent) {
        if (!contentEvent.getContent().isEmpty()) {
            final ContentDescriptor contentDescriptor = contentEvent.getContent().get(0);
            updateContextTextAndSetOnClick(contentDescriptor);
        }
    }

    public void updateContextTextAndSetOnClick(final ContentDescriptor contentDescriptor) {
        String title = contentDescriptor.getTitle();
        updateContentText(title);
        locationAndContentAreoLayout.setOnClickListener(onClickTransitionToContent(contentDescriptor));
    }

    public void transitionToContentActivity(ContentDescriptor contentDescriptor) {
        Intent intent = new Intent();
        intent.setClass(activity, ContentActivity.class);
        intent.putExtra(ContentActivity.CONTENT_KEY, generateContentString(contentDescriptor));
        activity.startActivity(intent);
    }

    private String generateContentString(ContentDescriptor contentDescriptor) {
        return "Content:\nTitle = " + contentDescriptor.getTitle() + "\nDescription = "
                + contentDescriptor.getContentDescription() + "\nIcon URL = " + contentDescriptor.getIconUrl()
                + "\nContent URL = " + contentDescriptor.getContentUrl() + "\nCampaign ID = "
                + contentDescriptor.getCampaignId() + "\nExpires = " + contentDescriptor.getExpires()
                + "\nContent Attributes:\n" + getContentAttributeString(contentDescriptor.getContentAttributes());
    }

    private String getContentAttributeString(ContentAttributes contentAttributes) {
        if (contentAttributes == null || contentAttributes.getAttributes() == null) {
            return null;
        }
        String attributesString = new String();
        Map<String, Object> attributes = contentAttributes.getAttributes();
        Iterator attributeIterator = attributes.entrySet().iterator();
        while (attributeIterator.hasNext()) {
            Map.Entry pairs = (Map.Entry) attributeIterator.next();
            attributesString += "Key = " + pairs.getKey() + ", Value = " + pairs.getValue() + "\n";
        }
        return attributesString;
    }

    public void toastAndLogError(String errorMessage) {
        if (errorMessage == null) {
            errorMessage = "Received an unexpected error with a null message";
        }
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, errorMessage);
    }

    private void setPlaceEventAndContentAreaVisibility(int visibility) {
        locationAndContentAreoLayout.setVisibility(visibility);
    }

    private OnClickListener onClickTransitionToContent(final ContentDescriptor contentDescriptor) {
        return new OnClickListener() {
            @Override
            public void onClick(View v) {
                transitionToContentActivity(contentDescriptor);
            }
        };
    }

    private void initializeSettingsButton() {
        userContextSettingsButton = (ImageButton) activity.findViewById(R.id.settings_button);
        userContextSettingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.showUpdatePermissionsUI();
            }
        });
    }
    
    private void initializeHiddenMap() {
    	ImageView imageView = (ImageView)activity.findViewById(R.id.hiddenMap);
    	imageView.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//Intent companyServiceIntent = new Intent(this, CompanyService.class);
				Intent intent = new Intent(activity, GoogleMapActivity.class);
				activity.startActivity(intent);
				
			}
		});
    }

    private void initializeEnableUserContextButton() {
        acceptTermsButton.setVisibility(View.GONE);
        enableUserContextButton.setVisibility(View.VISIBLE);
        enableUserContextButton.setText(R.string.download_usercontext);
        enableUserContextButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.enableContextConnector();
            }
        });

        enableUserContextButton.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    enableUserContextButton.setBackgroundDrawable(activity.getResources().getDrawable(
                            R.drawable.button_download_relasphere_pressed));
                }
                else {
                    enableUserContextButton.setBackgroundDrawable(activity.getResources().getDrawable(
                            R.drawable.button_download_relasphere));
                }
                return false;
            }
        });

    }

}
