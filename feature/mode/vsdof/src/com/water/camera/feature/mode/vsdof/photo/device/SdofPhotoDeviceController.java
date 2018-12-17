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

package com.water.camera.feature.mode.vsdof.photo.device;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCaptureSession.CaptureCallback;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Build;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.water.camera.common.ICameraContext;
import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil.Tag;
import com.water.camera.common.device.CameraDeviceManager;
import com.water.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.water.camera.common.device.CameraOpenException;
import com.water.camera.common.device.v2.Camera2CaptureSessionProxy;
import com.water.camera.common.device.v2.Camera2Proxy;
import com.water.camera.common.device.v2.Camera2Proxy.StateCallback;
import com.water.camera.common.mode.Device2Controller;
import com.water.camera.common.relation.Relation;
import com.water.camera.common.setting.ISettingManager;
import com.water.camera.common.setting.ISettingManager.SettingController;
import com.water.camera.common.setting.ISettingManager.SettingDevice2Configurator;
import com.water.camera.common.sound.ISoundPlayback;
import com.water.camera.common.utils.CameraUtil;
import com.water.camera.common.utils.Size;
import com.water.camera.feature.mode.vsdof.photo.DeviceInfo;
import com.water.camera.feature.mode.vsdof.photo.SdofPhotoRestriction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

/**
 * An implementation of {@link ISdofPhotoDeviceController} with Camera2Proxy.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class SdofPhotoDeviceController extends Device2Controller implements
        ISdofPhotoDeviceController,
        CaptureSurface.ImageCallback,
        ISettingManager.SettingDevice2Requester {
    private static final Tag TAG = new Tag(SdofPhotoDeviceController.class.getSimpleName());
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final int CAPTURE_FORMAT = ImageFormat.JPEG;
    private static final int CAPTURE_MAX_NUMBER = 2;
    private static final int WAIT_TIME = 5;
    private static final String VSDOF_KEY = "com.mediatek.multicamfeature.multiCamFeatureMode";
    private static final String STEREO_WARNING_KEY = "com.mediatek.stereofeature.stereowarning";
    private static final String DOF_LEVEL_KEY = "com.mediatek.stereofeature.doflevel";
    private static final String PREVIEW_SIZE_KEY =
            "com.mediatek.vsdoffeature.vsdofFeaturePreviewSize";
    private static final String MTK_VSDOF_FEATURE_WARNING =
            "com.mediatek.vsdoffeature.vsdofFeatureWarning";
    private static final String MTK_VSDOF_FEATURE_CAPTURE_WARNING_MSG =
            "com.mediatek.vsdoffeature.vsdofFeatureCaptureWarningMsg";
    private String mZsdStatus = "on";
    private static final int[] VSDOF_KEY_VALUE = new int[]{1};
    private static final int[] PREVIEW_SIZE_KEY_VALUE = new int[]{1080,1920};
    private static final int LEVEL_DEFAULT = 7;
    private int mCurrentLevel = LEVEL_DEFAULT;
    private static int[] CURRENT_DOFLEVEL_VALUE = new int[]{LEVEL_DEFAULT};
    private static final int DUAL_CAMERA_TOO_FAR = 1 << 31;
    private static int mVsdofWarningValue = 0;
    private static int[] DUAL_CAMERA_TOO_FAR_VALUE = new int[]{mVsdofWarningValue};


    private final Activity mActivity;
    private final CameraManager mCameraManager;
    private CameraCharacteristics mCameraCharacteristics;
    private CaptureRequest.Key<int[]> mVsdofKey = null;
    private CaptureRequest.Key<int[]> mWarningKey = null;
    private CaptureResult.Key<int[]> mStereoWarningKey = null;
    private CaptureResult.Key<int[]> mVsdofWarningKey = null;
    private CaptureRequest.Key<int[]> mDofLevelKey = null;
    private CaptureRequest.Key<int[]> mPreviewSizeKey = null;
    private final CaptureSurface mCaptureSurface;
    private final ICameraContext mICameraContext;
    private final Object mSurfaceHolderSync = new Object();
    private final StateCallback mDeviceCallback = new DeviceStateCallback();
    private StereoWarningCallback mStereoWarningCallback = null;

    private int mJpegRotation;
    private volatile int mPreviewWidth;
    private volatile int mPreviewHeight;
    private volatile Camera2Proxy mCamera2Proxy;
    private volatile Camera2CaptureSessionProxy mSession;

    private boolean mFirstFrameArrived = false;
    private boolean mIsPictureSizeChanged = false;

    private Lock mLockState = new ReentrantLock();
    private Lock mDeviceLock = new ReentrantLock();
    private CameraState mCameraState = CameraState.CAMERA_UNKNOWN;

    private String mCurrentCameraId;
    private Surface mPreviewSurface;
    private JpegCallback mJpegCallback;
    private Object mSurfaceObject;
    private ISettingManager mSettingManager;
    private DeviceCallback mModeDeviceCallback;
    private SettingController mSettingController;
    private PreviewSizeCallback mPreviewSizeCallback;
    private CameraDeviceManager mCameraDeviceManager;
    private SettingDevice2Configurator mSettingDevice2Configurator;
    private static Relation sRelation = null;

    /**
     * this enum is used for tag native camera open state.
     */
    private enum CameraState {
        CAMERA_UNKNOWN,
        CAMERA_OPENING,
        CAMERA_OPENED,
        CAMERA_CLOSING,
    }

    /**
     * PhotoDeviceController may use activity to get display rotation.
     * @param activity the camera activity.
     */
    SdofPhotoDeviceController(@Nonnull Activity activity, @Nonnull ICameraContext context) {
        LogHelper.d(TAG, "[SdofPhotoDeviceController]");
        mActivity = activity;
        mCameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        mICameraContext = context;
        mCaptureSurface = new CaptureSurface();
        mCaptureSurface.setCaptureCallback(this);
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public void queryCameraDeviceManager() {
        mCameraDeviceManager = mICameraContext.getDeviceManager(CameraApi.API2);
    }

    @Override
    public void openCamera(DeviceInfo info) {
        String cameraId = info.getCameraId();
        boolean sync = info.getNeedOpenCameraSync();
        LogHelper.i(TAG, "[openCamera] cameraId : " + cameraId + ",sync = " + sync);
        initSettingManager(info.getSettingManager());
        if (canOpenCamera(cameraId)) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                mCurrentCameraId = cameraId;
                mCameraCharacteristics = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
                mVsdofKey = CameraUtil.getAvailableSessionKeys(
                        mCameraCharacteristics, VSDOF_KEY);
                mWarningKey = CameraUtil.getRequestKey(
                        mCameraCharacteristics, MTK_VSDOF_FEATURE_CAPTURE_WARNING_MSG);
                mDofLevelKey = CameraUtil.getRequestKey(
                        mCameraCharacteristics, DOF_LEVEL_KEY);
                mPreviewSizeKey = CameraUtil.getAvailableSessionKeys(
                        mCameraCharacteristics, PREVIEW_SIZE_KEY);
                initSettings();
                updateCameraState(CameraState.CAMERA_OPENING);
                doOpenCamera(sync);
            } catch (CameraOpenException e) {
                if (CameraOpenException.ExceptionType.SECURITY_EXCEPTION == e.getExceptionType()) {
                    CameraUtil.showErrorInfoAndFinish(mActivity,
                            CameraUtil.CAMERA_HARDWARE_EXCEPTION);
                }
            } catch (CameraAccessException e) {
                CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_HARDWARE_EXCEPTION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                mDeviceLock.unlock();
            }
        }
    }

    @Override
    public void updatePreviewSurface(Object surfaceObject) {
        LogHelper.d(TAG, "[updatePreviewSurface] surfaceHolder = " + surfaceObject + " state = "
                + mCameraState);
        synchronized (mSurfaceHolderSync) {
            mSurfaceObject = surfaceObject;
            if (surfaceObject instanceof SurfaceHolder) {
                mPreviewSurface = surfaceObject == null ? null :
                        ((SurfaceHolder) surfaceObject).getSurface();
            } else if (surfaceObject instanceof SurfaceTexture) {
                mPreviewSurface = surfaceObject == null ? null :
                        new Surface((SurfaceTexture) surfaceObject);
            }
            boolean isStateReady = CameraState.CAMERA_OPENED == mCameraState;
            if (isStateReady && mCamera2Proxy != null) {
                if (surfaceObject != null) {
                    configureSession();
                } else {
                    stopPreview();
                }
            }
        }
    }

    @Override
    public void setDeviceCallback(DeviceCallback callback) {
        mModeDeviceCallback = callback;
    }

    @Override
    public void setPreviewSizeReadyCallback(PreviewSizeCallback callback) {
        mPreviewSizeCallback = callback;
    }

    /**
     * Set the new picture size.
     *
     * @param size current picture size.
     */
    @Override
    public void setPictureSize(Size size) {
        mIsPictureSizeChanged = mCaptureSurface.updatePictureInfo(size.getWidth(),
                size.getHeight(), CAPTURE_FORMAT, CAPTURE_MAX_NUMBER);
    }

    /**
     * Check whether can take picture or not.
     *
     * @return true means can take picture; otherwise can not take picture.
     */
    @Override
    public boolean isReadyForCapture() {
        boolean canCapture = mSession != null
                        && mCamera2Proxy != null && getCameraState() == CameraState.CAMERA_OPENED;
        LogHelper.i(TAG, "[isReadyForCapture] canCapture = " + canCapture);
        return canCapture;
    }

    @Override
    public void destroyDeviceController() {
        if (mCaptureSurface != null) {
            mCaptureSurface.release();

        }
    }

    @Override
    public void startPreview() {
        LogHelper.i(TAG, "[startPreview]");
        configureSession();
    }

    @Override
    public void stopPreview() {
        LogHelper.i(TAG, "[stopPreview]");
        abortOldSession();
    }

    @Override
    public void takePicture(@Nonnull JpegCallback callback) {
        LogHelper.i(TAG, "[takePicture] mSession= " + mSession);
        if (mSession != null && mCamera2Proxy != null) {
            mJpegCallback = callback;
            try {
                Builder builder = doCreateAndConfigStillCaptureRequest();
                mSession.capture(builder.build(), mCaptureCallback, mModeHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
                LogHelper.e(TAG, "[takePicture] error because create build fail.");
            }
        }
    }

    @Override
    public void setZSDStatus(String value) {
        mZsdStatus = value;
    }

    @Override
    public void updateGSensorOrientation(int orientation) {
        mJpegRotation = orientation;
    }

    @Override
    public void closeCamera(boolean sync) {
        LogHelper.i(TAG, "[closeCamera] + sync = " + sync + " current state : " + mCameraState);
        if (CameraState.CAMERA_UNKNOWN != mCameraState) {
            try {
                mDeviceLock.tryLock(WAIT_TIME, TimeUnit.SECONDS);
                super.doCameraClosed(mCamera2Proxy);
                updateCameraState(CameraState.CAMERA_CLOSING);
                abortOldSession();
                if (mModeDeviceCallback != null) {
                    mModeDeviceCallback.beforeCloseCamera();
                }
                doCloseCamera(sync);
                updateCameraState(CameraState.CAMERA_UNKNOWN);
                recycleVariables();
                mCaptureSurface.releaseCaptureSurface();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                super.doCameraClosed(mCamera2Proxy);
                mDeviceLock.unlock();
            }
            recycleVariables();
        }
        LogHelper.i(TAG, "[closeCamera] -");
    }

    @Override
    public Size getPreviewSize(double targetRatio) {
        int oldPreviewWidth = mPreviewWidth;
        int oldPreviewHeight = mPreviewHeight;
        getTargetPreviewSize(targetRatio);
        boolean isSameSize = oldPreviewHeight == mPreviewHeight && oldPreviewWidth == mPreviewWidth;
        LogHelper.i(TAG, "[getPreviewSize] old size : " + oldPreviewWidth + " X " +
                oldPreviewHeight + " new  size :" + mPreviewWidth + " X " + mPreviewHeight);
        //if preview size don't change, but picture size changed,need do configure the surface.
        //if preview size changed,do't care the picture size changed,because surface will be
        //changed.
        if (isSameSize && mIsPictureSizeChanged) {
            configureSession();
        }
        return new Size(mPreviewWidth, mPreviewHeight);
    }

    @Override
    public void onPictureCallback(byte[] data) {
        LogHelper.i(TAG, "[onPictureCallback]");
        //need notify preview is started, such as API2 take picture done,
        //will do start preview, the ui can enable by :onCaptureCompleted ->
        //mModeDeviceCallback.onPreviewCallback();
        mFirstFrameArrived = false;
        if (mJpegCallback != null) {
            DataCallbackInfo info = new DataCallbackInfo();
            info.data = data;
            info.needUpdateThumbnail = true;
            info.needRestartPreview = false;
            mJpegCallback.onDataReceived(info);
        }

    }

    @Override
    public void createAndChangeRepeatingRequest() {
        if (mCamera2Proxy == null || mCameraState != CameraState.CAMERA_OPENED) {
            LogHelper.e(TAG, "camera is closed or in opening state can't request ");
            return;
        }
        repeatingPreview();
    }

    @Override
    public CaptureRequest.Builder createAndConfigRequest(int templateType) {
        CaptureRequest.Builder builder = null;
        try {
            builder = doCreateAndConfigRequest(templateType);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return builder;
    }

    @Override
    public com.water.camera.common.mode.photo.device.CaptureSurface
            getModeSharedCaptureSurface()
            throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Surface getModeSharedPreviewSurface() throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Surface getModeSharedThumbnailSurface() throws IllegalStateException {
        //not support now
        throw new IllegalStateException("get invalid capture surface!");
    }

    @Override
    public Camera2CaptureSessionProxy getCurrentCaptureSession() {
        return mSession;
    }

    @Override
    public void requestRestartSession() {
        configureSession();
    }

    @Override
    public int getRepeatingTemplateType() {
        return Camera2Proxy.TEMPLATE_PREVIEW;
    }

    @Override
    public void setStereoWarningCallback(StereoWarningCallback callback) {
        mStereoWarningCallback = callback;
    }

    @Override
    public void setVsDofLevelParameter(int level) {
        if (mCurrentLevel != level) {
            mCurrentLevel = level;
            createAndChangeRepeatingRequest();
        }
    }

    private Builder doCreateAndConfigStillCaptureRequest()
            throws CameraAccessException {
        LogHelper.i(TAG, "[doCreateAndConfigStillCaptureRequest]" +
                "mCamera2Proxy =" + mCamera2Proxy);
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(Camera2Proxy.TEMPLATE_STILL_CAPTURE);
            mSettingDevice2Configurator.configCaptureRequest(builder);
            builder.addTarget(mCaptureSurface.getSurface());
            setSpecialVendorTag(builder);
            if ("off".equalsIgnoreCase(mZsdStatus)) {
                LogHelper.d(TAG, "[takePicture] take picture with preview image.");
                builder.addTarget(mPreviewSurface);
            } else {
                LogHelper.d(TAG, "[takePicture] take picture not with preview image.");
            }
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(
                    Integer.parseInt(mCurrentCameraId), mJpegRotation, mActivity);
            builder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
        }
        return builder;
    }

    private void setSpecialVendorTag(CaptureRequest.Builder builder) {
        if (mVsdofKey != null) {
            builder.set(mVsdofKey, VSDOF_KEY_VALUE);
            LogHelper.d(TAG, "[setSpecialVendorTag] set vsdof key.");
        }
        if (mDofLevelKey != null) {
            CURRENT_DOFLEVEL_VALUE[0] = mCurrentLevel;
            builder.set(mDofLevelKey, CURRENT_DOFLEVEL_VALUE);
            LogHelper.d(TAG, "[setSpecialVendorTag] sdoflevel " + mCurrentLevel);
        }
        if (mPreviewSizeKey != null) {
            PREVIEW_SIZE_KEY_VALUE[0] = mPreviewWidth;
            PREVIEW_SIZE_KEY_VALUE[1] = mPreviewHeight;
            builder.set(mPreviewSizeKey, PREVIEW_SIZE_KEY_VALUE);
            LogHelper.d(TAG, "[setSpecialVendorTag] set preview size " +
                    "width " + mPreviewWidth + ", height " + mPreviewHeight);
        }
        if (mWarningKey != null) {
            DUAL_CAMERA_TOO_FAR_VALUE = new int[]{mVsdofWarningValue};
            builder.set(mWarningKey, DUAL_CAMERA_TOO_FAR_VALUE);
            LogHelper.d(TAG, "[setSpecialVendorTag] set warning key to capture " +
                    DUAL_CAMERA_TOO_FAR_VALUE[0]);
        } else {
            LogHelper.d(TAG, "[setSpecialVendorTag] mWarningKey is null");
        }
    }

    private void initSettingManager(ISettingManager settingManager) {
        mSettingManager = settingManager;
        settingManager.updateModeDevice2Requester(this);
        mSettingDevice2Configurator = settingManager.getSettingDevice2Configurator();
        mSettingController = settingManager.getSettingController();
    }

    private void initSettings() throws CameraAccessException {
        mSettingManager.createAllSettings();
        mSettingDevice2Configurator.setCameraCharacteristics(mCameraCharacteristics);
        SdofPhotoRestriction.setCameraCharacteristics(mCameraCharacteristics,
                mICameraContext.getDataStore());
        sRelation = SdofPhotoRestriction.getRestriction().getRelation("on", false);
        if (sRelation != null) {
            mSettingController.postRestriction(sRelation);
        }
        mSettingController.addViewEntry();
        mSettingController.refreshViewEntry();
    }

    private void doOpenCamera(boolean sync) throws CameraOpenException {
        if (sync) {
            mCameraDeviceManager.openCameraSync(mCurrentCameraId, mDeviceCallback, null);
        } else {
            mCameraDeviceManager.openCamera(mCurrentCameraId, mDeviceCallback, null);
        }
    }

    private void updateCameraState(CameraState state) {
        LogHelper.d(TAG, "[updateCameraState] new state = " + state + " old =" + mCameraState);
        mLockState.lock();
        try {
            mCameraState = state;
        } finally {
            mLockState.unlock();
        }
    }

    private CameraState getCameraState() {
        mLockState.lock();
        try {
            return mCameraState;
        } finally {
            mLockState.unlock();
        }
    }

    private void doCloseCamera(boolean sync) {
        if (sync) {
            mCameraDeviceManager.closeSync(mCurrentCameraId);
        } else {
            mCameraDeviceManager.close(mCurrentCameraId);
        }
        mCamera2Proxy = null;
        synchronized (mSurfaceHolderSync) {
            mSurfaceObject = null;
            mPreviewSurface = null;
        }
    }

    private void recycleVariables() {
        mCurrentCameraId = null;
        updatePreviewSurface(null);
        mCamera2Proxy = null;
        mIsPictureSizeChanged = false;
    }

    private boolean canOpenCamera(String newCameraId) {
        boolean isSameCamera = newCameraId.equalsIgnoreCase(mCurrentCameraId);
        boolean isStateReady = mCameraState == CameraState.CAMERA_UNKNOWN;
        boolean value = !isSameCamera && isStateReady;
        LogHelper.i(TAG, "[canOpenCamera] new id: " + newCameraId + " current camera :" +
                mCurrentCameraId + " isSameCamera = " + isSameCamera + " current state : " +
                mCameraState + " isStateReady = " + isStateReady + " can open : " + value);
        return value;
    }

    private void configureSession() {
        LogHelper.i(TAG, "[configureSession]");
        mDeviceLock.lock();
        try {
            if (mCamera2Proxy != null) {
                abortOldSession();
                List<Surface> surfaces = new LinkedList<>();
                surfaces.add(mPreviewSurface);
                surfaces.add(mCaptureSurface.getSurface());
                mSettingDevice2Configurator.configSessionSurface(surfaces);
                Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                mCamera2Proxy.createCaptureSession(surfaces, mSessionCallback,
                        mModeHandler, builder);
                mIsPictureSizeChanged = false;
            }
        } catch (CameraAccessException e) {
            LogHelper.e(TAG, "[configureSession] error");
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            mDeviceLock.unlock();
        }
    }

    private void abortOldSession() {
        if (mSession != null) {
            try {
                mSession.abortCaptures();
                mSession = null;
            } catch (CameraAccessException e) {
                LogHelper.e(TAG, "[abortOldSession] exception", e);
            }
        }
    }

    private void repeatingPreview() {
        LogHelper.i(TAG, "[repeatingPreview] mSession =" + mSession + " mCamera =" + mCamera2Proxy);
        if (mSession != null && mCamera2Proxy != null) {
            try {
                mFirstFrameArrived = false;
                Builder builder = doCreateAndConfigRequest(Camera2Proxy.TEMPLATE_PREVIEW);
                mCaptureSurface.setCaptureCallback(this);
                mSession.setRepeatingRequest(builder.build(), mPreviewCallback, mModeHandler);
            } catch (CameraAccessException | RuntimeException e) {
                LogHelper.e(TAG, "[repeatingPreview] error");
            }
        }
    }

    private Builder doCreateAndConfigRequest(int templateType) throws CameraAccessException {
        LogHelper.i(TAG, "[doCreateAndConfigRequest] mCamera2Proxy =" + mCamera2Proxy);
        CaptureRequest.Builder builder = null;
        if (mCamera2Proxy != null) {
            builder = mCamera2Proxy.createCaptureRequest(templateType);
            setSpecialVendorTag(builder);
            mSettingDevice2Configurator.configCaptureRequest(builder);
            builder.addTarget(mPreviewSurface);
        }
        return builder;
    }

    private Size getTargetPreviewSize(double ratio) {
        Size values = null;
        try {
            CameraCharacteristics cs = mCameraManager.getCameraCharacteristics(mCurrentCameraId);
            StreamConfigurationMap streamConfigurationMap =
                    cs.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            android.util.Size previewSizes[] =
                    streamConfigurationMap.getOutputSizes(SurfaceHolder.class);
            int length = previewSizes.length;
            List<Size> sizes = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                sizes.add(i, new Size(previewSizes[i].getWidth(), previewSizes[i].getHeight()));
            }
            values = CameraUtil.getOptimalPreviewSize(mActivity, sizes, ratio, true);
            mPreviewWidth = values.getWidth();
            mPreviewHeight = values.getHeight();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        LogHelper.d(TAG, "[getTargetPreviewSize] " + mPreviewWidth + " X " + mPreviewHeight);
        return values;
    }

    private void updatePreviewSize() {
        ISettingManager.SettingController controller = mSettingManager.getSettingController();
        String pictureSize = controller.queryValue(KEY_PICTURE_SIZE);
        LogHelper.i(TAG, "[updatePreviewSize] :" + pictureSize);
        if (pictureSize != null) {
            String[] pictureSizes = pictureSize.split("x");
            int width = Integer.parseInt(pictureSizes[0]);
            int height = Integer.parseInt(pictureSizes[1]);
            double ratio = (double) width / height;
            getTargetPreviewSize(ratio);
        }
    }

    @Override
    public void doCameraOpened(@Nonnull Camera2Proxy camera2proxy) {
            LogHelper.i(TAG, "[onOpened]  camera2proxy = " + camera2proxy + " preview surface = "
                    + mPreviewSurface + "  mCameraState = " + mCameraState + "camera2Proxy id = "
                    + camera2proxy.getId() + " mCameraId = " + mCurrentCameraId);
            try {
                if (CameraState.CAMERA_OPENING == getCameraState()
                        && camera2proxy != null && camera2proxy.getId().equals(mCurrentCameraId)) {
                    mCamera2Proxy = camera2proxy;
                    if (mModeDeviceCallback != null) {
                        mModeDeviceCallback.onCameraOpened(mCurrentCameraId);
                    }
                    updateCameraState(CameraState.CAMERA_OPENED);
                    updatePreviewSize();
                    if (mPreviewSizeCallback != null) {
                        mPreviewSizeCallback.onPreviewSizeReady(new Size(mPreviewWidth,
                                mPreviewHeight));
                    }
                }
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        }

    @Override
    public void doCameraDisconnected(@Nonnull Camera2Proxy camera2proxy) {
            LogHelper.i(TAG, "[onDisconnected] camera2proxy = " + camera2proxy);
            if (mCamera2Proxy != null && mCamera2Proxy == camera2proxy) {
                CameraUtil.showErrorInfoAndFinish(mActivity, CameraUtil.CAMERA_ERROR_SERVER_DIED);
            }
        }

        @Override
    public void doCameraError(@Nonnull Camera2Proxy camera2Proxy, int error) {
            LogHelper.i(TAG, "[onError] camera2proxy = " + camera2Proxy + " error = " + error);
            if ((mCamera2Proxy != null && mCamera2Proxy == camera2Proxy)
                    || error == CameraUtil.CAMERA_OPEN_FAIL
                    || error == CameraUtil.CAMERA_ERROR_EVICTED) {
                    updateCameraState(CameraState.CAMERA_UNKNOWN);
                CameraUtil.showErrorInfoAndFinish(mActivity, error);
            }
        }

    /**
     * Camera session callback.
     */
    private final Camera2CaptureSessionProxy.StateCallback mSessionCallback = new
            Camera2CaptureSessionProxy.StateCallback() {

                @Override
                public void onConfigured(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigured],session = " + session);
                    mDeviceLock.lock();
                    mSession = session;
                    try {
                        if (CameraState.CAMERA_OPENED == getCameraState()) {
                            synchronized (mSurfaceHolderSync) {
                                if (mPreviewSurface != null) {
                                    repeatingPreview();
                                }
                            }
                        }
                    } finally {
                        mDeviceLock.unlock();
                    }
                }

                @Override
                public void onConfigureFailed(@Nonnull Camera2CaptureSessionProxy session) {
                    LogHelper.i(TAG, "[onConfigureFailed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }

                @Override
                public void onClosed(@Nonnull Camera2CaptureSessionProxy session) {
                    super.onClosed(session);
                    LogHelper.i(TAG, "[onClosed],session = " + session);
                    if (mSession == session) {
                        mSession = null;
                    }
                }
            };

    /**
     * Capture callback.
     */
    private final CaptureCallback mCaptureCallback = new CaptureCallback() {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            if (CameraUtil.isStillCaptureTemplate(request)) {
                LogHelper.d(TAG, "[onCaptureStarted] will play capture sound");
                mICameraContext.getSoundPlayback().play(ISoundPlayback.SHUTTER_CLICK);
            }
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            LogHelper.d(TAG, "[onCaptureCompleted] mModeDeviceCallback = " + mModeDeviceCallback
                + ", mFirstFrameArrived = " + mFirstFrameArrived);
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
            if (mModeDeviceCallback != null && !mFirstFrameArrived) {
                mFirstFrameArrived = true;
                mModeDeviceCallback.onPreviewCallback(null, 0);
            }
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogHelper.d(TAG, "[onCaptureFailed]");
            mSettingDevice2Configurator.getRepeatingCaptureCallback()
                    .onCaptureFailed(session, request, failure);
        }
    };


    /**
     * Preview callback.
     */
    private final CaptureCallback mPreviewCallback = new CaptureCallback() {

        @Override
        public void onCaptureStarted(CameraCaptureSession session, CaptureRequest request, long
                timestamp, long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
        }

        @Override
        public void onCaptureCompleted(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            LogHelper.d(TAG, "[onCaptureCompleted] mModeDeviceCallback = " + mModeDeviceCallback
                + ", mFirstFrameArrived = " + mFirstFrameArrived);
            mSettingDevice2Configurator.getRepeatingCaptureCallback().onCaptureCompleted(
                    session, request, result);
            if (mModeDeviceCallback != null && !mFirstFrameArrived) {
                mFirstFrameArrived = true;
                mModeDeviceCallback.onPreviewCallback(null, 0);
            }
            notifyWarningKey(result);
        }

        @Override
        public void onCaptureFailed(@Nonnull CameraCaptureSession session,
                @Nonnull CaptureRequest request, @Nonnull CaptureFailure failure) {
            super.onCaptureFailed(session, request, failure);
            LogHelper.d(TAG, "[onCaptureFailed]");
            mSettingDevice2Configurator.getRepeatingCaptureCallback()
                    .onCaptureFailed(session, request, failure);
        }
    };

    private void notifyWarningKey(TotalCaptureResult result) {
        if (mStereoWarningCallback == null) {
            return;
        }
        // Normal warning value(normal,too close,lens cover,too light)
        if (mStereoWarningKey == null) {
            mStereoWarningKey =
                    CameraUtil.getResultKey(mCameraCharacteristics, STEREO_WARNING_KEY);
        }
        if (mStereoWarningKey != null) {
            LogHelper.d(TAG, "[notifyWarningKey] mStereoWarningKey is not null");
            int[] warningVlue = result.get(mStereoWarningKey);
            LogHelper.d(TAG, "[notifyWarningKey] mStereoWarningKey value is " + warningVlue);
            if (warningVlue != null && warningVlue.length > 0) {
                LogHelper.d(TAG, "[notifyWarningKey] onWarning");
                mStereoWarningCallback.onWarning(warningVlue[0]);
            }
        }
        // from p1 node return(too far)
        if (mVsdofWarningKey == null) {
            mVsdofWarningKey =
                    CameraUtil.getResultKey(mCameraCharacteristics, MTK_VSDOF_FEATURE_WARNING);
        }
        LogHelper.d(TAG, "[notifyWarningKey] mVsdofWarningKey is " + mVsdofWarningKey);
        if (mVsdofWarningKey != null) {
            LogHelper.d(TAG, "[notifyWarningKey] mVsdofWarningKey is not null");
            int[] warningVlue = result.get(mVsdofWarningKey);
            LogHelper.d(TAG, "[notifyWarningKey] mVsdofWarningKey value is " + warningVlue);
            if (warningVlue != null && warningVlue.length > 0) {
                LogHelper.d(TAG, "[notifyWarningKey] onWarning too far");
                mVsdofWarningValue = warningVlue[0];
                mStereoWarningCallback.onWarning(warningVlue[0]);
                return;
            }
        }
        mVsdofWarningValue = 0;
    }
}
