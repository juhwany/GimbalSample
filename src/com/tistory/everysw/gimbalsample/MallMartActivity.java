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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.qualcommlabs.usercontext.Callback;
import com.qualcommlabs.usercontext.ConnectorPermissionChangeListener;
import com.qualcommlabs.usercontext.ContentListener;
import com.qualcommlabs.usercontext.ContextCoreConnector;
import com.qualcommlabs.usercontext.ContextCoreConnectorFactory;
import com.qualcommlabs.usercontext.ContextCoreStatus;
import com.qualcommlabs.usercontext.ContextInterestsConnector;
import com.qualcommlabs.usercontext.ContextInterestsConnectorFactory;
import com.qualcommlabs.usercontext.ContextPlaceConnector;
import com.qualcommlabs.usercontext.ContextPlaceConnectorFactory;
import com.qualcommlabs.usercontext.PlaceEventListener;
import com.qualcommlabs.usercontext.protocol.ContentDescriptor;
import com.qualcommlabs.usercontext.protocol.ContentDescriptorHistory;
import com.qualcommlabs.usercontext.protocol.ContentEvent;
import com.qualcommlabs.usercontext.protocol.ContextConnectorError;
import com.qualcommlabs.usercontext.protocol.PlaceEvent;
import com.qualcommlabs.usercontext.protocol.profile.Profile;

public class MallMartActivity extends Activity {

    private static final Logger LOG = LoggerFactory.getLogger(MallMartActivity.class);

    private ContextCoreConnector contextCoreConnector;
    private ContextInterestsConnector contextInterestsConnector;
    private ContextPlaceConnector contextPlaceConnector;

    private MallMartPresenter mallMartPresenter;

    PlaceEventListener placeEventListener = new PlaceEventListener() {
        @Override
        public void placeEvent(PlaceEvent placeEvent) {
            String placeEventDescription = String.format("%s %s", placeEvent.getEventType(), placeEvent.getPlace()
                    .getPlaceName());
            mallMartPresenter.updatePlaceEventText(placeEventDescription);
        }
    };

    ContentListener contentListener = new ContentListener() {
        @Override
        public void contentEvent(ContentEvent contentEvent) {
            mallMartPresenter.updateContentTextAndSetOnClick(contentEvent);
        }
    };

    ConnectorPermissionChangeListener corePrivacyControlListener = new ConnectorPermissionChangeListener() {
        @Override
        public void permissionChanged(Boolean enabled) {
            LOG.info("Core permission change");
            LOG.info("The current contextPlaceConnector enabled state is: "
                    + contextPlaceConnector.isPermissionEnabled());
            LOG.info("The current contextInterestsConnector enabled state is: "
                    + contextInterestsConnector.isPermissionEnabled());
            if (enabled) {
                updateUIText();
            }
            else {
                mallMartPresenter.showEnableContextConnectorButton();
            }
        }
    };

    ConnectorPermissionChangeListener geoFencePrivacyControlListener = new ConnectorPermissionChangeListener() {
        @Override
        public void permissionChanged(Boolean enabled) {
            LOG.info("Geofence permission change");
            LOG.info("The current contextPlaceConnector enabled state is: "
                    + contextPlaceConnector.isPermissionEnabled());
            updateUIText();
        }
    };

    ConnectorPermissionChangeListener interestPrivacyControlListener = new ConnectorPermissionChangeListener() {
        @Override
        public void permissionChanged(Boolean enabled) {
            LOG.info("Interest permission change");
            LOG.info("The current contextInterestsConnector enabled state is: "
                    + contextInterestsConnector.isPermissionEnabled());

            if (contextInterestsConnector.isPermissionEnabled()) {
                getProfileAndSendToLog();
            }

        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mallMartPresenter = new MallMartPresenter(this);
        mallMartPresenter.initializeView();
        contextCoreConnector = ContextCoreConnectorFactory.get(this);
        contextInterestsConnector = ContextInterestsConnectorFactory.get(this);
        contextPlaceConnector = ContextPlaceConnectorFactory.get(this);
        checkContextConnectorStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        contextCoreConnector.setCurrentActivity(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        contextCoreConnector.setCurrentActivity(null);
    }

    @Override
    protected void onDestroy() {
        stopListeningForPlaceEvents();
        stopListeningForContentEvents();
        stopListeningForPermissionChanges();
        super.onDestroy();
    }

    public void checkContextConnectorStatus() {
        if (contextCoreConnector.isPermissionEnabled()) {
            startCompanyService();
            setupListenersAndUpdateUIText();
        }
        else {
            ContextCoreStatus status = contextCoreConnector.getStatus();
            toastAndLogError(status.getStatusMessage());
            mallMartPresenter.showEnableContextConnectorButton();
        }
    }

    protected void enableContextConnector() {
        contextCoreConnector.enable(this, new Callback<Void>() {
            @Override
            public void success(Void responseObject) {
                contextConnectorEnabled();
            }

            @Override
            public void failure(int statusCode, String errorMessage) {
                mallMartPresenter.onContextConnectorEnableFailure(errorMessage);
            }
        });
    }

    private void contextConnectorEnabled() {
        mallMartPresenter.hideEnableContextConnectorButton();
        setupListenersAndUpdateUIText();
        mallMartPresenter.showSettings();
        startCompanyService();
    }

    private void setupListenersAndUpdateUIText() {
        displayLatestPlaceEventAndStartListeningForPlaceEvents();
        getLatestContentEventAndStartListeningForContentEvents();
        startListeningForPermissionChanges();
    }

    private void updateUIText() {
        displayLatestKnownPlaceEvent();
        getContentHistoryAndDisplayLatestContent();
    }

    private void displayLatestPlaceEventAndStartListeningForPlaceEvents() {
        displayLatestKnownPlaceEvent();
        startListeningForPlaceEvents();
    }

    private void getLatestContentEventAndStartListeningForContentEvents() {
        getContentHistoryAndDisplayLatestContent();
        startListeningForContentEvents();
    }

    private void displayLatestKnownPlaceEvent() {
        final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final String defaultString = this.getString(R.string.unknown);

        ContextCoreStatus status = contextCoreConnector.getStatus();
        if (status.getStatusCode() == ContextConnectorError.ENABLED.getErrorCode()) {
            if (contextCoreConnector.isPermissionEnabled() && contextPlaceConnector.isPermissionEnabled()) {
                String placeEventDescription = preferences.getString(CompanyService.PLACE_EVENT_DESCRIPTION_KEY,
                        defaultString);
                mallMartPresenter.updatePlaceEventText(placeEventDescription);
            }
            else if (contextCoreConnector.isPermissionEnabled() == false) {
                mallMartPresenter.onPermissionsDisabled();
            }
            else if (contextPlaceConnector.isPermissionEnabled() == false) {
                mallMartPresenter.onLocationPermissionDisabled();
            }
        }
    }

    private void getContentHistoryAndDisplayLatestContent() {
        contextCoreConnector.requestContentHistory(new AbstractFailureLoggingCallback<List<ContentDescriptorHistory>>(
                mallMartPresenter) {
            @Override
            public void success(List<ContentDescriptorHistory> contentDescriptorHistories) {
                if (!contentDescriptorHistories.isEmpty()) {
                    ContentDescriptor contentDescriptor = contentDescriptorHistories.get(0).getContentDescriptor();
                    mallMartPresenter.updateContextTextAndSetOnClick(contentDescriptor);
                }
            }

            @Override
            public void failure(int statusCode, String errorMessage) {
                super.failure(statusCode, errorMessage);
                mallMartPresenter.updateContentText("");
            }
        });
    }

    public void showUpdatePermissionsUI() {
        contextCoreConnector.showUpdatePermissionsUI(this, new Callback<Void>() {
            @Override
            public void success(Void responseObject) {
                LOG.info("ContextConnector permissions updated.");
            }

            @Override
            public void failure(int statusCode, String errorMessage) {
                toastAndLogError(errorMessage);
            }
        });
    }

    private void getProfileAndSendToLog() {
        contextInterestsConnector.requestProfile(new AbstractFailureLoggingCallback<Profile>(mallMartPresenter) {
            @Override
            public void success(Profile profile) {
                if (profile != null) {
                    LOG.debug(profile.toString());
                }
            }
        });
    }

    private void startCompanyService() {
        Intent companyServiceIntent = new Intent(this, CompanyService.class);
        companyServiceIntent.setAction("com.company.COMPANY_SERVICE");

        startService(companyServiceIntent);
    }

    private void startListeningForContentEvents() {
        contextCoreConnector.addContentListener(contentListener);
    }

    private void stopListeningForContentEvents() {
        contextCoreConnector.removeContentListener(contentListener);
    }

    private void startListeningForPlaceEvents() {
        contextPlaceConnector.addPlaceEventListener(placeEventListener);
    }
    

    private void stopListeningForPlaceEvents() {
        contextPlaceConnector.removePlaceEventListener(placeEventListener);
    }

    private void startListeningForPermissionChanges() {
        contextCoreConnector.addConnectorPermissionChangeListener(corePrivacyControlListener);
        contextPlaceConnector.addConnectorPermissionChangeListener(geoFencePrivacyControlListener);
        contextInterestsConnector.addConnectorPermissionChangeListener(interestPrivacyControlListener);
    }

    private void stopListeningForPermissionChanges() {
        contextCoreConnector.removeConnectorPermissionChangeListener(corePrivacyControlListener);
        contextPlaceConnector.removeConnectorPermissionChangeListener(geoFencePrivacyControlListener);
        contextInterestsConnector.removeConnectorPermissionChangeListener(interestPrivacyControlListener);
    }

    public void toastAndLogError(String errorMessage) {
        mallMartPresenter.toastAndLogError(errorMessage);
    }
}