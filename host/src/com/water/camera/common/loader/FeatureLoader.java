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

package com.water.camera.common.loader;

import android.content.Context;

import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil.Tag;
import com.water.camera.common.debug.profiler.IPerformanceProfile;
import com.water.camera.common.debug.profiler.PerformanceTracker;
import com.water.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.water.camera.common.mode.CameraApiHelper;
import com.water.camera.common.mode.photo.intent.IntentPhotoModeEntry;
import com.water.camera.common.mode.photo.PhotoModeEntry;
import com.water.camera.common.mode.video.intentvideo.IntentVideoModeEntry;
import com.water.camera.common.mode.video.VideoModeEntry;
import com.water.camera.common.setting.ICameraSetting;
import com.water.camera.feature.mode.vsdof.photo.SdofPhotoEntry;
import com.water.camera.feature.mode.slowmotion.SlowMotionEntry;
import com.water.camera.feature.mode.panorama.PanoramaEntry;
import com.water.camera.feature.mode.longexposure.LongExposureModeEntry;
import com.water.camera.feature.setting.antiflicker.AntiFlickerEntry;
import com.water.camera.feature.setting.cameraswitcher.CameraSwitcherEntry;
import com.water.camera.feature.setting.continuous.ContinuousShotEntry;
import com.water.camera.feature.setting.aaaroidebug.AaaRoiDebugEntry;
import com.water.camera.feature.setting.ais.AISEntry;
import com.water.camera.feature.setting.dng.DngEntry;
import com.water.camera.feature.setting.eis.EISEntry;
import com.water.camera.feature.setting.exposure.ExposureEntry;
import com.water.camera.feature.setting.facedetection.FaceDetectionEntry;
import com.water.camera.feature.setting.flash.FlashEntry;
import com.water.camera.feature.setting.focus.FocusEntry;
import com.water.camera.feature.setting.format.FormatEntry;
import com.water.camera.feature.setting.hdr.HdrEntry;
import com.water.camera.feature.setting.iso.ISOEntry;
import com.water.camera.feature.setting.microphone.MicroPhoneEntry;
import com.water.camera.feature.setting.noisereduction.NoiseReductionEntry;
import com.water.camera.feature.setting.picturesize.PictureSizeEntry;
import com.water.camera.feature.setting.previewmode.PreviewModeEntry;
import com.water.camera.feature.setting.scenemode.SceneModeEntry;
import com.water.camera.feature.setting.selftimer.SelfTimerEntry;
import com.water.camera.feature.setting.shutterspeed.ShutterSpeedEntry;
import com.water.camera.feature.setting.videoquality.VideoQualityEntry;
import com.water.camera.feature.setting.whitebalance.WhiteBalanceEntry;
import com.water.camera.feature.setting.zoom.ZoomEntry;
import com.water.camera.feature.setting.zsd.ZSDEntry;
import com.water.camera.feature.setting.postview.PostViewEntry;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nonnull;

/**
 * Used for load the features.
 */
public class FeatureLoader {
    private static final Tag TAG = new Tag(FeatureLoader.class.getSimpleName());
    private static final String CAMERA_SWITCH = "com.water.camera.feature.setting.cameraswitcher.CameraSwitcherEntry";
    private static final String CONTINUOUSSHOT = "com.water.camera.feature.setting.continuous.ContinuousShotEntry";
    private static final String DNG = "com.water.camera.feature.setting.dng.DngEntry";
    private static final String SELFTIME = "com.water.camera.feature.setting.selftimer.SelfTimerEntry";
    private static final String FACE_DETECTION = "com.water.camera.feature.setting.facedetection.FaceDetectionEntry";
    private static final String FLASH = "com.water.camera.feature.setting.flash.FlashEntry";
    private static final String HDR = "com.water.camera.feature.setting.hdr.HdrEntry";
    private static final String PICTURE_SIZE = "com.water.camera.feature.setting.picturesize.PictureSizeEntry";
    private static final String PREVIEW_MODE = "com.water.camera.feature.setting.previewmode.PreviewModeEntry";
    private static final String VIDEO_QUALITY = "com.water.camera.feature.setting.videoquality.VideoQualityEntry";
    private static final String ZOOM = "com.water.camera.feature.setting.zoom.ZoomEntry";
    private static final String FOCUS = "com.water.camera.feature.setting.focus.FocusEntry";
    private static final String EXPOSURE = "com.water.camera.feature.setting.exposure.ExposureEntry";
    private static final String MICHROPHONE = "com.water.camera.feature.setting.microphone.MicroPhoneEntry";
    private static final String NOISE_REDUCTION = "com.water.camera.feature.setting.noisereduction.NoiseReductionEntry";
    private static final String EIS = "com.water.camera.feature.setting.eis.EISEntry";
    private static final String AIS = "com.water.camera.feature.setting.ais.AISEntry";
    private static final String SCENE_MODE = "com.water.camera.feature.setting.scenemode.SceneModeEntry";
    private static final String WHITE_BALANCE = "com.water.camera.feature.setting.whitebalance.WhiteBalanceEntry";
    private static final String ANTI_FLICKER = "com.water.camera.feature.setting.antiflicker.AntiFlickerEntry";
    private static final String ZSD = "com.water.camera.feature.setting.zsd.ZSDEntry";
    private static final String ISO = "com.water.camera.feature.setting.iso.ISOEntry";
    private static final String AE_AF_DEBUG = "com.water.camera.feature.setting.aaaroidebug.AaaRoiDebugEntry";
    private static final String SDOF_PHOTO_MODE = "com.water.camera.feature.mode.vsdof.photo.SdofPhotoEntry";
    private static final String PANORAMA_MODE = "com.water.camera.feature.mode.panorama.PanoramaEntry";
    private static final String SHUTTER_SPEED = "com.water.camera.feature.setting.shutterspeed.ShutterSpeedEntry";
    private static final String LONG_EXPUSURE_MODE = "com.water.camera.feature.mode.longexposure.LongExposureModeEntry";
    private static final String PHOTO_MODE = "com.water.camera.common.mode.photo.PhotoModeEntry";
    private static final String VIDEO_MODE = "com.water.camera.common.mode.video.VideoModeEntry";
    private static final String INTENT_PHOTO_MODE = "com.water.camera.common.mode.photo.intent.IntentPhotoModeEntry";
    private static final String INTENT_VIDEO_MODE = "com.water.camera.common.mode.video.intentvideo.IntentVideoModeEntry";
    private static final String SLOW_MOTION_MODE = "com.water.camera.feature.mode.slowmotion.SlowMotionEntry";
    private static final String FORMATS = "com.water.camera.feature.setting.format.FormatEntry";
    private static final String POST_VIEW = "com.water.camera.feature.setting.postview.PostViewEntry";
    private static ConcurrentHashMap<String, IFeatureEntry>
            sBuildInEntries = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, IFeatureEntry>
            sPluginEntries = new ConcurrentHashMap<>();

    /**
     * Update current mode key to feature entry, dual camera zoom need to set properties
     * in photo and video mode before open camera, this notify only update to setting feature.
     * @param context current application context.
     * @param currentModeKey current mode key.
     */
    public static void updateSettingCurrentModeKey(@Nonnull Context context,
                                                   @Nonnull String currentModeKey) {
        LogHelper.d(TAG, "[updateCurrentModeKey] current mode key:" + currentModeKey);
        if (sBuildInEntries.size() <= 0) {
            loadBuildInFeatures(context);
        }
    }

    /**
     * Notify setting feature before open camera, this event only need to notify setting feature.
     * @param context the context.
     * @param cameraId want to open which camera.
     * @param cameraApi use which api.
     */
    public static void notifySettingBeforeOpenCamera(@Nonnull Context context,
                                                     @Nonnull String cameraId,
                                                     @Nonnull CameraApi cameraApi) {
        LogHelper.d(TAG, "[notifySettingBeforeOpenCamera] id:" + cameraId + ", api:" + cameraApi);
        //don't consider plugin feature? because plugin feature need more time to load
        if (sBuildInEntries.size() <= 0) {
            loadBuildInFeatures(context);
        }
        Iterator iterator = sBuildInEntries.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry item = (Map.Entry) iterator.next();
            IFeatureEntry entry = (IFeatureEntry) item.getValue();
            if (ICameraSetting.class.equals(entry.getType())) {
                entry.notifyBeforeOpenCamera(cameraId, cameraApi);
            }
        }
    }

    /**
     * Load plugin feature entries, should be called in non-ui thread.
     * @param context the application context.
     * @return the plugin features.
     */
    public static ConcurrentHashMap<String, IFeatureEntry> loadPluginFeatures(
            final Context context) {
        return sPluginEntries;
    }

    /**
     * Load build in feature entries, should be called in non-ui thread.
     * @param context the application context.
     * @return the build-in features.
     */
    public static ConcurrentHashMap<String, IFeatureEntry> loadBuildInFeatures(Context context) {
        if (sBuildInEntries.size() > 0) {
            return sBuildInEntries;
        }
        IPerformanceProfile profile = PerformanceTracker.create(TAG,
                "Build-in Loading");
        profile.start();
        sBuildInEntries = new ConcurrentHashMap<>(loadClasses(context));
        profile.stop();
        return sBuildInEntries;
    }

    private static LinkedHashMap<String, IFeatureEntry> loadClasses(Context context) {
        LinkedHashMap<String, IFeatureEntry> entries = new LinkedHashMap<>();
        DeviceSpec deviceSpec = CameraApiHelper.getDeviceSpec(context);

        IFeatureEntry postviewEntry = new PostViewEntry(context, context.getResources());
        postviewEntry.setDeviceSpec(deviceSpec);
        entries.put(POST_VIEW, postviewEntry);

        IFeatureEntry cameraSwitchEntry = new CameraSwitcherEntry(context, context.getResources());
        cameraSwitchEntry.setDeviceSpec(deviceSpec);
        entries.put(CAMERA_SWITCH, cameraSwitchEntry);

        IFeatureEntry continuousShotEntry = new ContinuousShotEntry(context,
                context.getResources());
        continuousShotEntry.setDeviceSpec(deviceSpec);
        entries.put(CONTINUOUSSHOT, continuousShotEntry);

        IFeatureEntry dngEntry = new DngEntry(context, context.getResources());
        dngEntry.setDeviceSpec(deviceSpec);
        entries.put(DNG, dngEntry);

        IFeatureEntry selfTimeEntry = new SelfTimerEntry(context, context.getResources());
        selfTimeEntry.setDeviceSpec(deviceSpec);
        entries.put(SELFTIME, selfTimeEntry);

        IFeatureEntry faceDetectionEntry = new FaceDetectionEntry(context, context.getResources());
        faceDetectionEntry.setDeviceSpec(deviceSpec);
        entries.put(FACE_DETECTION, faceDetectionEntry);

        IFeatureEntry flashEntry = new FlashEntry(context, context.getResources());
        flashEntry.setDeviceSpec(deviceSpec);
        entries.put(FLASH, flashEntry);

        IFeatureEntry hdrEntry = new HdrEntry(context, context.getResources());
        hdrEntry.setDeviceSpec(deviceSpec);
        entries.put(HDR, hdrEntry);

        IFeatureEntry pictureSizeEntry = new PictureSizeEntry(context, context.getResources());
        pictureSizeEntry.setDeviceSpec(deviceSpec);
        entries.put(PICTURE_SIZE, pictureSizeEntry);

        IFeatureEntry previewModeEntry = new PreviewModeEntry(context, context.getResources());
        previewModeEntry.setDeviceSpec(deviceSpec);
        entries.put(PREVIEW_MODE, previewModeEntry);

        IFeatureEntry videoQualityEntry = new VideoQualityEntry(context, context.getResources());
        videoQualityEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_QUALITY, videoQualityEntry);

        IFeatureEntry zoomEntry = new ZoomEntry(context, context.getResources());
        zoomEntry.setDeviceSpec(deviceSpec);
        entries.put(ZOOM, zoomEntry);

        IFeatureEntry focusEntry = new FocusEntry(context, context.getResources());
        focusEntry.setDeviceSpec(deviceSpec);
        entries.put(FOCUS, focusEntry);

        IFeatureEntry exposureEntry = new ExposureEntry(context, context.getResources());
        exposureEntry.setDeviceSpec(deviceSpec);
        entries.put(EXPOSURE, exposureEntry);

        IFeatureEntry microPhoneEntry = new MicroPhoneEntry(context, context.getResources());
        microPhoneEntry.setDeviceSpec(deviceSpec);
        entries.put(MICHROPHONE, microPhoneEntry);

        IFeatureEntry noiseReductionEntry = new NoiseReductionEntry(context, context.getResources());
        noiseReductionEntry.setDeviceSpec(deviceSpec);
        entries.put(NOISE_REDUCTION, noiseReductionEntry);

        IFeatureEntry EisPhoneEntry = new EISEntry(context, context.getResources());
        EisPhoneEntry.setDeviceSpec(deviceSpec);
        entries.put(EIS, EisPhoneEntry);

        IFeatureEntry aisEntry = new AISEntry(context, context.getResources());
        aisEntry.setDeviceSpec(deviceSpec);
        entries.put(AIS, aisEntry);

        IFeatureEntry sceneModeEntry = new SceneModeEntry(context, context.getResources());
        sceneModeEntry.setDeviceSpec(deviceSpec);
        entries.put(SCENE_MODE, sceneModeEntry);

        IFeatureEntry whiteBalanceEntry = new WhiteBalanceEntry(context, context.getResources());
        whiteBalanceEntry.setDeviceSpec(deviceSpec);
        entries.put(WHITE_BALANCE, whiteBalanceEntry);

        IFeatureEntry antiFlickerEntry = new AntiFlickerEntry(context, context.getResources());
        antiFlickerEntry.setDeviceSpec(deviceSpec);
        entries.put(ANTI_FLICKER, antiFlickerEntry);

        IFeatureEntry zsdEntry = new ZSDEntry(context, context.getResources());
        zsdEntry.setDeviceSpec(deviceSpec);
        entries.put(ZSD, zsdEntry);

        IFeatureEntry isoEntry = new ISOEntry(context, context.getResources());
        isoEntry.setDeviceSpec(deviceSpec);
        entries.put(ISO, isoEntry);

        IFeatureEntry aeAfDebugEntry = new AaaRoiDebugEntry(context, context.getResources());
        aeAfDebugEntry.setDeviceSpec(deviceSpec);
        entries.put(AE_AF_DEBUG, aeAfDebugEntry);

        IFeatureEntry sDofPhotoEntry = new SdofPhotoEntry(context, context.getResources());
        sDofPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(SDOF_PHOTO_MODE, sDofPhotoEntry);

        IFeatureEntry panoramaEntry = new PanoramaEntry(context, context.getResources());
        panoramaEntry.setDeviceSpec(deviceSpec);
        entries.put(PANORAMA_MODE, panoramaEntry);

        IFeatureEntry shutterSpeedEntry = new ShutterSpeedEntry(context, context.getResources());
        shutterSpeedEntry.setDeviceSpec(deviceSpec);
        entries.put(SHUTTER_SPEED, shutterSpeedEntry);

        IFeatureEntry longExposureEntry = new LongExposureModeEntry(context,
                context.getResources());
        longExposureEntry.setDeviceSpec(deviceSpec);
        entries.put(LONG_EXPUSURE_MODE, longExposureEntry);

        IFeatureEntry photoEntry = new PhotoModeEntry(context, context.getResources());
        photoEntry.setDeviceSpec(deviceSpec);
        entries.put(PHOTO_MODE, photoEntry);

        IFeatureEntry videoEntry = new VideoModeEntry(context, context.getResources());
        videoEntry.setDeviceSpec(deviceSpec);
        entries.put(VIDEO_MODE, videoEntry);

        IFeatureEntry intentVideoEntry = new IntentVideoModeEntry(context, context.getResources());
        intentVideoEntry.setDeviceSpec(deviceSpec);
        entries.put(INTENT_VIDEO_MODE, intentVideoEntry);

        IFeatureEntry intentPhotoEntry = new IntentPhotoModeEntry(context, context.getResources());
        intentPhotoEntry.setDeviceSpec(deviceSpec);
        entries.put(INTENT_PHOTO_MODE, intentPhotoEntry);

        IFeatureEntry slowMotionEntry = new SlowMotionEntry(context, context.getResources());
        slowMotionEntry.setDeviceSpec(deviceSpec);
        entries.put(SLOW_MOTION_MODE, slowMotionEntry);

        IFeatureEntry formatsEntry = new FormatEntry(context, context.getResources());
        formatsEntry.setDeviceSpec(deviceSpec);
        entries.put(FORMATS, formatsEntry);
        return entries;
    }
}