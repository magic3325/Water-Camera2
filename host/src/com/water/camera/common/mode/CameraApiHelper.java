/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensor. Without
 *     the prior written permission of MediaTek inc. and/or its licensor, any
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
 *     NON-INFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
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

package com.water.camera.common.mode;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;

import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil.Tag;
import com.water.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.water.camera.common.loader.DeviceDescription;
import com.water.camera.common.loader.DeviceSpec;
import com.water.camera.common.utils.CameraUtil;
import com.mediatek.camera.portability.SystemProperties;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

/**
 * <p>This class is used to query debug properties set by user or ProjectConfig.mk.
 * these debug properties includes:</p>
 * <li>Camera application choose api1 or api2.</li>
 * <li>//TODO add more properties</li>
 * <p>Note:After modifying the debugging property, you need to kill and restart
 * mediatek.camera process to take effect.</p>
 */
public class CameraApiHelper {
    private static final Tag TAG = new Tag(CameraApiHelper.class.getSimpleName());
    private static final int API_UNKNOWN = 0;
    private static DeviceSpec sDeviceSpec = new DeviceSpec();
    private static String mLogicalId = null;
    private static final String MTK_MULTI_CAM_FEATURE_AVAILABLE_MODE
    = "com.mediatek.multicamfeature.availableMultiCamFeatureMode";
    private static final String VSDOF_KEY = "com.mediatek.multicamfeature.multiCamFeatureMode";
    private static final int MTK_MULTI_CAM_FEATURE_MODE_VSDOF = 1;

    /**
     * Get Camera API type by mode class name.
     *
     * @param modeName modeName to get camera api type.
     * @return the camera api type.
     */
    public static CameraApi getCameraApiType(@Nullable String modeName) {
    	return CameraApi.API2;
    }

    /**
     * Get device spec.
     * @param context the context to access camera manager.
     * @return the device spec.
     */
    public static DeviceSpec getDeviceSpec(Context context) {
        createDeviceSpec(context);
        return sDeviceSpec;
    }

    /**
     * Get available logical camera id.
     * @return logical camera id.
     */
    public static String getLogicalCameraId() {
        return mLogicalId;
    }

    @SuppressWarnings("deprecation")
    private static void createDeviceSpec(Context context) {
        if (sDeviceSpec.getDefaultCameraApi() != null) {
            return;
        }
        CameraApi defaultCameraApi = getCameraApiType(null);
        int cameraNum = getCameraNum(context);
        ConcurrentHashMap<String, DeviceDescription>
                deviceDescriptionMap = new ConcurrentHashMap<>();
        if (cameraNum > 0) {
            for (int i = 0; i < cameraNum; i++) {
                DeviceDescription deviceDescription = new DeviceDescription(null);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    CameraManager cameraManager = (CameraManager)
                            context.getSystemService(context.CAMERA_SERVICE);
                    CameraCharacteristics characteristics = null;
                    try {
                        characteristics = cameraManager.getCameraCharacteristics(String.valueOf(i));
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                    deviceDescription.setCameraCharacteristics(characteristics);
                    deviceDescription.storeCameraCharacKeys(characteristics);
                }
                deviceDescriptionMap.put(String.valueOf(i), deviceDescription);
            }
            sDeviceSpec.setDefaultCameraApi(defaultCameraApi);
            sDeviceSpec.setDeviceDescriptions(deviceDescriptionMap);
        }

        LogHelper.i(TAG, "[createDeviceSpec] context: " + context + ", default api:"
                + defaultCameraApi + ", deviceDescriptionMap:"
                + deviceDescriptionMap + " cameraNum " + cameraNum);
    }

    /**
     * Get camera number from cameraCharacteristics.
     * @param context The camera context.
     * @return The number of camera.
     */
    public static int getCameraNum(Context context) {
        String[] idList = null;
        CameraCharacteristics cs = null;
        Set<String> physicalCameraIds = null;
        int length = 0;
        try {
            CameraManager cameraManager =
                    (CameraManager) context.getSystemService(context.CAMERA_SERVICE);
            idList = cameraManager.getCameraIdList();
            if (idList == null || idList.length == 0) {
                throw new RuntimeException("Camera num is 0, Sensor should double check");
            }
            length = idList.length;
            LogHelper.d(TAG, "<getCameraNum> idList length is " + idList.length);
            for (String id : idList) {
                cs = cameraManager.getCameraCharacteristics(id);
                if (CameraUtil.isSupportAvailableMode(cs,
                        MTK_MULTI_CAM_FEATURE_AVAILABLE_MODE,
                        MTK_MULTI_CAM_FEATURE_MODE_VSDOF)
                        && (CameraUtil.getAvailableSessionKeys(cs, VSDOF_KEY) != null)) {
                    mLogicalId = id;
                    LogHelper.d(TAG, "<getCameraNum> mLogicalId is " + mLogicalId);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return length;
    }
}