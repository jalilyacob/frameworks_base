/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (c) 2011 Code Aurora Forum. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.MSimNetworkController;

import com.android.systemui.R;

// Intimately tied to the design of res/layout/msim_signal_cluster_view.xml
public class MSimSignalClusterView
        extends LinearLayout
        implements MSimNetworkController.MSimSignalCluster {

    static final boolean DEBUG = false;
    static final String TAG = "MSimSignalClusterView";

    MSimNetworkController mMSimNC;

    private boolean mWifiVisible = false;
    private int mWifiStrengthId = 0, mWifiActivityId = 0;
    private boolean mMobileVisible = false;
    private int[] mMobileStrengthId;
    private int[] mMobileActivityId;
    private int[] mMobileTypeId;
    private boolean mIsAirplaneMode = false;
    private String mWifiDescription, mMobileTypeDescription;
    private String[] mMobileDescription;

    ViewGroup mWifiGroup, mMobileGroup, mMobileGroupSub2;
    ImageView mWifi, mWifiActivity, mMobile, mMobileActivity, mMobileType;
    ImageView mMobileSub2, mMobileActivitySub2, mMobileTypeSub2;
    View mSpacer;

    public MSimSignalClusterView(Context context) {
        this(context, null);
    }

    public MSimSignalClusterView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MSimSignalClusterView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        int numPhones = TelephonyManager.getDefault().getPhoneCount();
        mMobileStrengthId = new int[numPhones];
        mMobileDescription = new String[numPhones];
        mMobileTypeId = new int[numPhones];
        mMobileActivityId = new int[numPhones];
        for(int i=0; i < numPhones; i++) {
            mMobileStrengthId[i] = 0;
            mMobileTypeId[i] = 0;
            mMobileActivityId[i] = 0;
        }
    }

    public void setNetworkController(MSimNetworkController nc) {
        if (DEBUG) Slog.d(TAG, "MSimNetworkController=" + nc);
        mMSimNC = nc;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mWifiGroup      = (ViewGroup) findViewById(R.id.wifi_combo);
        mWifi           = (ImageView) findViewById(R.id.wifi_signal);
        mWifiActivity   = (ImageView) findViewById(R.id.wifi_inout);
        mMobileGroup    = (ViewGroup) findViewById(R.id.mobile_combo);
        mMobile         = (ImageView) findViewById(R.id.mobile_signal);
        mMobileActivity = (ImageView) findViewById(R.id.mobile_inout);
        mMobileType     = (ImageView) findViewById(R.id.mobile_type);
        mMobileGroupSub2    = (ViewGroup) findViewById(R.id.mobile_combo_sub2);
        mMobileSub2     = (ImageView) findViewById(R.id.mobile_signal_sub2);
        mMobileActivitySub2 = (ImageView) findViewById(R.id.mobile_inout_sub2);
        mMobileTypeSub2     = (ImageView) findViewById(R.id.mobile_type_sub2);
        mSpacer         =             findViewById(R.id.spacer);

        apply();
    }

    @Override
    protected void onDetachedFromWindow() {
        mWifiGroup      = null;
        mWifi           = null;
        mWifiActivity   = null;
        mMobileGroup    = null;
        mMobile         = null;
        mMobileActivity = null;
        mMobileType     = null;
        mMobileGroupSub2 = null;
        mMobileSub2     = null;
        mMobileActivitySub2 = null;
        mMobileTypeSub2 = null;

        super.onDetachedFromWindow();
    }

    public void setWifiIndicators(boolean visible, int strengthIcon, int activityIcon,
            String contentDescription) {
        mWifiVisible = visible;
        mWifiStrengthId = strengthIcon;
        mWifiActivityId = activityIcon;
        mWifiDescription = contentDescription;

        apply();
    }

    public void setMobileDataIndicators(boolean visible, int strengthIcon, int activityIcon,
            int typeIcon, String contentDescription, String typeContentDescription, int subscription) {
        mMobileVisible = visible;
        mMobileStrengthId[subscription] = strengthIcon;
        mMobileActivityId[subscription] = activityIcon;
        mMobileTypeId[subscription] = typeIcon;
        mMobileDescription[subscription] = contentDescription;
        mMobileTypeDescription = typeContentDescription;

        apply();
        applySubscription(subscription);
    }

    public void setIsAirplaneMode(boolean is) {
        mIsAirplaneMode = is;
    }

    private void applySubscription(int subscription) {
        if (mWifiGroup == null) return;

        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            mWifi.setImageResource(mWifiStrengthId);
            mWifiActivity.setImageResource(mWifiActivityId);
            mWifiGroup.setContentDescription(mWifiDescription);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Slog.d(TAG,
                String.format("wifi: %s sig=%d act=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId, mWifiActivityId));

        if (mMobileVisible) {
            if (subscription == 0) {
                mMobileGroup.setVisibility(View.VISIBLE);
                mMobile.setImageResource(mMobileStrengthId[subscription]);
                mMobileGroup.setContentDescription(mMobileTypeDescription + " "
                    + mMobileDescription[subscription]);
                mMobileActivity.setImageResource(mMobileActivityId[subscription]);
                mMobileType.setImageResource(mMobileTypeId[subscription]);
                mMobileType.setVisibility(
                    !mWifiVisible ? View.VISIBLE : View.GONE);
            } else {
                mMobileGroupSub2.setVisibility(View.VISIBLE);
                mMobileSub2.setImageResource(mMobileStrengthId[subscription]);
                mMobileGroupSub2.setContentDescription(mMobileTypeDescription + " "
                    + mMobileDescription[subscription]);
                mMobileActivitySub2.setImageResource(mMobileActivityId[subscription]);
                mMobileTypeSub2.setImageResource(mMobileTypeId[subscription]);
                mMobileTypeSub2.setVisibility(
                    !mWifiVisible ? View.VISIBLE : View.GONE);
            }
        } else {
            if (subscription == 0) {
                mMobileGroup.setVisibility(View.GONE);
            } else {
                mMobileGroupSub2.setVisibility(View.GONE);
            }
        }

        if (mMobileVisible && mWifiVisible && mIsAirplaneMode) {
            mSpacer.setVisibility(View.INVISIBLE);
        } else {
            mSpacer.setVisibility(View.GONE);
        }

    }

    // Run after each indicator change.
    private void apply() {
        if (mWifiGroup == null) return;

        if (mWifiVisible) {
            mWifiGroup.setVisibility(View.VISIBLE);
            mWifi.setImageResource(mWifiStrengthId);
            mWifiActivity.setImageResource(mWifiActivityId);
            mWifiGroup.setContentDescription(mWifiDescription);
        } else {
            mWifiGroup.setVisibility(View.GONE);
        }

        if (DEBUG) Slog.d(TAG,
                String.format("wifi: %s sig=%d act=%d",
                    (mWifiVisible ? "VISIBLE" : "GONE"),
                    mWifiStrengthId, mWifiActivityId));
    }
}
