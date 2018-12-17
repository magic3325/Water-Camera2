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

package com.water.camera.feature.setting.microphone;

import com.water.camera.common.ICameraContext;
import com.water.camera.common.app.IApp;
import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil;
import com.water.camera.common.setting.ISettingManager;
import com.water.camera.common.setting.SettingBase;

/**
 * This class is for MicroPhone feature interacted with others.
 */

public class MicroPhone extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(MicroPhone.class.getSimpleName());
    private static final String KEY_MICRO = "key_microphone";
    private static final String MICROPHONE_OFF = "off";
    private static final String MICROPHONE_ON = "on";
    private ISettingChangeRequester mSettingChangeRequester;
    private MicroPhoneSettingView mSettingView;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     ISettingManager.SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mSettingView = new MicroPhoneSettingView();
        mSettingView.setMicroViewListener(mMicroViewListener);
    }

    @Override
    public void unInit() {

    }

    @Override
    public void addViewEntry() {
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            mSettingView.setChecked(MICROPHONE_ON.equals(getValue()));
            mSettingView.setEnabled(getEntryValues().size() > 1);
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_MICRO;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        MicroPhoneParametersConfig parameterConfig;
        if (mSettingChangeRequester == null) {
            parameterConfig = new MicroPhoneParametersConfig(this, mSettingDeviceRequester);
            mSettingChangeRequester = parameterConfig;
        }
        return (MicroPhoneParametersConfig) mSettingChangeRequester;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        MicroPhoneCaptureRequestConfig requestConfig;
        if (mSettingChangeRequester == null) {
            requestConfig = new MicroPhoneCaptureRequestConfig(this, mSettingDevice2Requester);
            mSettingChangeRequester = requestConfig;
        }
        return (MicroPhoneCaptureRequestConfig) mSettingChangeRequester;
    }

    private MicroPhoneSettingView.OnMicroViewListener mMicroViewListener
            = new MicroPhoneSettingView.OnMicroViewListener() {
        @Override
        public void onItemViewClick(boolean isOn) {
            LogHelper.i(TAG, "[onItemViewClick], isOn:" + isOn);
            String value = isOn ? MICROPHONE_ON : MICROPHONE_OFF;
            setValue(value);
            mDataStore.setValue(getKey(), value, getStoreScope(), true);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mSettingChangeRequester.sendSettingChangeRequest();
                }
            });
        }

        @Override
        public boolean onCachedValue() {
            return MICROPHONE_ON.equals(
                    mDataStore.getValue(getKey(), MICROPHONE_ON, getStoreScope()));
        }
    };

    /**
     * update set value.
     * @param value the default value
     */
    public void updateValue(String value) {
        setValue(mDataStore.getValue(getKey(), value, getStoreScope()));
    }

}