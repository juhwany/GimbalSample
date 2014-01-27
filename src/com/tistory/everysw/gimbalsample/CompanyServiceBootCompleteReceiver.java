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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CompanyServiceBootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent companyServiceIntent = new Intent(context, CompanyService.class);
        String packageName = context.getPackageName();
        companyServiceIntent.setAction(packageName + ".COMPANY_SERVICE");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            context.startService(companyServiceIntent);
        }
        else if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            context.stopService(companyServiceIntent);
        }
    }

}
