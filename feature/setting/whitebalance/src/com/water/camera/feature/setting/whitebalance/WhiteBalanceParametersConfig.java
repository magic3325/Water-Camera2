/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.water.camera.feature.setting.whitebalance;

import android.hardware.Camera;

import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil;
import com.water.camera.common.device.v1.CameraProxy;
import com.water.camera.common.setting.ICameraSetting;
import com.water.camera.common.setting.ISettingManager.SettingDeviceRequester;

import java.util.List;

/**
 * White balance parameters configure.
 */

public class WhiteBalanceParametersConfig implements
        ICameraSetting.IParametersConfigure {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            WhiteBalanceParametersConfig.class.getSimpleName());

    private WhiteBalance mWhiteBalance;
    private SettingDeviceRequester mDeviceRequester;
    private String mValue;

    /**
     * White balance parameters config constructor.
     *
     * @param whiteBalance The instance of {@link WhiteBalance}.
     * @param deviceRequester The implementer of {@link SettingDeviceRequester}.
     */
    public WhiteBalanceParametersConfig(WhiteBalance whiteBalance,
                                     SettingDeviceRequester deviceRequester) {
        mWhiteBalance = whiteBalance;
        mDeviceRequester = deviceRequester;
    }

    @Override
    public void setOriginalParameters(Camera.Parameters originalParameters) {
        List<String> supportedWhiteBalances = originalParameters.getSupportedWhiteBalance();
        mWhiteBalance.initializeValue(supportedWhiteBalances,
                originalParameters.getWhiteBalance());
    }

    @Override
    public boolean configParameters(Camera.Parameters parameters) {
        String value = mWhiteBalance.getValue();
        LogHelper.d(TAG, "[configParameters], value:" + value +
                ", isNeedLock = " + mWhiteBalance.isNeedLock());
        if (value != null) {
            parameters.setWhiteBalance(value);
            parameters.setAutoWhiteBalanceLock(mWhiteBalance.isNeedLock());
        }
        return false;
    }

    @Override
    public void configCommand(CameraProxy cameraProxy) {
    }

    @Override
    public void sendSettingChangeRequest() {
        mDeviceRequester.requestChangeSettingValue(mWhiteBalance.getKey());
    }
}
