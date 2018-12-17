/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */
package com.water.camera.feature.setting.format;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.preference.PreferenceFragment;

import com.water.camera.R;
import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil;
import com.water.camera.common.preference.Preference;
import com.water.camera.common.setting.ICameraSettingView;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is for self timer feature setting view.
 */

public class FormatSettingView implements ICameraSettingView {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(FormatSettingView.class.getSimpleName());

    private String mSelectedValue;
    private List<String> mEntryValues = new ArrayList<>();
    private IFormatViewListener.OnValueChangeListener mOnValueChangeListener;
    private Preference mFormatPreference;
    private FormatSelector mFormatSelector;
    private Activity mContext;
    private boolean mEnabled;

    @Override
    public void loadView(PreferenceFragment fragment) {
        fragment.addPreferencesFromResource(R.xml.format_preference);
        mContext = fragment.getActivity();

        if (mFormatSelector == null) {
            mFormatSelector = new FormatSelector();
            mFormatSelector.setOnItemClickListener(mOnItemClickListener);
        }

        mFormatPreference = (Preference) fragment
                .findPreference(IFormatViewListener.KEY_FORMAT);
        mFormatPreference.setRootPreference(fragment.getPreferenceScreen());
        mFormatPreference.setId(R.id.format_setting);
        mFormatPreference.setContentDescription(fragment.getActivity().getResources()
                .getString(R.string.format_title));
        mFormatPreference.setSummary(getSummary());
        mFormatPreference.setOnPreferenceClickListener(
                new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(android.preference.Preference preference) {
                mFormatSelector.setValue(mSelectedValue);
                mFormatSelector.setEntryValues(mEntryValues);

                FragmentTransaction transaction = mContext.getFragmentManager()
                        .beginTransaction();
                transaction.addToBackStack(null);
                transaction.replace(R.id.setting_container,
                        mFormatSelector, "self_timer_selector").commit();
                return true;
            }
        });
        mFormatPreference.setEnabled(mEnabled);
    }

    @Override
    public void refreshView() {
        if (mFormatPreference != null) {
            LogHelper.d(TAG, "[refreshView]");
            mFormatPreference.setSummary(getSummary());
            mFormatPreference.setEnabled(mEnabled);
        }
    }

    @Override
    public void unloadView() {

    }

    @Override
    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Set listener to listen the changed self timer value.
     * @param listener The instance of {@link IFormatViewListener.OnValueChangeListener}.
     */
    public void setOnValueChangeListener(IFormatViewListener.OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    /**
     * Set the default selected value.
     * @param value The default selected value.
     */
    public void setValue(String value) {
        mSelectedValue = value;
    }

    /**
     * Set the self timer supported.
     * @param entryValues The self timer supported.
     */
    public void setEntryValues(List<String> entryValues) {
        mEntryValues = entryValues;
    }

    private IFormatViewListener.OnItemClickListener mOnItemClickListener
            = new IFormatViewListener.OnItemClickListener() {
        @Override
        public void onItemClick(String value) {
            mSelectedValue = value;
            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChanged(value);
            }
        }
    };

    private String getSummary() {
        if (Format.FORMAT_JPEG.equals(mSelectedValue)) {
            return mContext.getString(R.string.format_entry_0);
        } else if (Format.FORMAT_HEIF.equals(mSelectedValue)) {
            return mContext.getString(R.string.format_entry_1);
        } else {
            return mContext.getString(R.string.format_entry_0);
        }
    }
}
