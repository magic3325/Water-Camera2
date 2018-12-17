package com.water.camera.feature.setting.cameraswitcher;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.view.KeyEvent;
import android.view.View;

import com.water.camera.R;
import com.water.camera.common.ICameraContext;
import com.water.camera.common.app.IApp;
import com.water.camera.common.debug.LogHelper;
import com.water.camera.common.debug.LogUtil;
import com.water.camera.common.setting.ISettingManager.SettingController;
import com.water.camera.common.setting.SettingBase;
import com.water.camera.common.utils.CameraUtil;
import com.mediatek.camera.portability.SystemProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Switch Camera setting item.
 *
 */
public class CameraSwitcher extends SettingBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(CameraSwitcher.class.getSimpleName());

    private static final String CAMERA_FACING_BACK = "back";
    private static final String CAMERA_FACING_FRONT = "front";
    private static final String CAMERA_DEFAULT_FACING = CAMERA_FACING_BACK;

    private static final String KEY_CAMERA_SWITCHER = "key_camera_switcher";
    private String mFacing;
    private View mSwitcherView;
    private static final String SWITCH_CAMERA_DEBUG_PROPERTY = "mtk.camera.switch.camera.debug";
    private static final String DEBUG_CAMERA_ID_PROPERTY = "mtk.camera.switch.id.debug";
    private String mLastRequestCameraId = "0";

    // [Add for CCT tool] Receive keycode and switch camera @{
    private KeyEventListenerImpl mKeyEventListener;
    // @}

    //for Main2 debug.
    private static final String DEBUG_MAIN2 = "vendor.debug.camera.single_main2";
    private static final String KEY_DEBUG_STEREO_MAIN2 = "key_stereo_main2";
    private static final String BACK_MAIN2_CAMERA_ID = "2";
    private static final String FRONT_CAMERA_ID = "1";
    private static final String BACK_CAMERA_ID = "0";
    private String mPreBackCamera = BACK_CAMERA_ID;
    private String[] mIdList = null;

    @Override
    public void init(IApp app,
                     ICameraContext cameraContext,
                     SettingController settingController) {
        super.init(app, cameraContext, settingController);
        mFacing = mDataStore.getValue(KEY_CAMERA_SWITCHER, CAMERA_DEFAULT_FACING, getStoreScope());

        int numOfCameras = Camera.getNumberOfCameras();
        if (numOfCameras > 1) {
            List<String> camerasFacing = getCamerasFacing(numOfCameras);
            if (camerasFacing.size() == 0) {
                return;
            }
            if (camerasFacing.size() == 1) {
                mFacing = camerasFacing.get(0);
                setValue(mFacing);
                return;
            }

            setSupportedPlatformValues(camerasFacing);
            setSupportedEntryValues(camerasFacing);
            setEntryValues(camerasFacing);

            mSwitcherView = initView();
            mAppUi.addToQuickSwitcher(mSwitcherView, 0);
        } else if (numOfCameras == 1) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(0, info);
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                mFacing = CAMERA_FACING_BACK;
            } else if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                mFacing = CAMERA_FACING_FRONT;
            }
        }
        setValue(mFacing);

        // get all cameraId for debug main2
        if (SystemProperties.getInt(DEBUG_MAIN2, 0) == 1) {
            try {
                Context context = mActivity.getApplicationContext();
                CameraManager cameraManager =
                        (CameraManager) context.getSystemService(context.CAMERA_SERVICE);
                mIdList = cameraManager.getCameraIdList();
                if (mIdList != null) {
                    for (String id : mIdList) {
                        LogHelper.d(TAG, "<getCameraIdList> id is " + id);
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        // [Add for CCT tool] Receive keycode and switch camera @{
        mKeyEventListener = new KeyEventListenerImpl();
        mApp.registerKeyEventListener(mKeyEventListener, IApp.DEFAULT_PRIORITY);
        // @}
    }

    @Override
    public void unInit() {
        if (mSwitcherView != null) {
            mAppUi.removeFromQuickSwitcher(mSwitcherView);
        }
        // [Add for CCT tool] Receive keycode and switch camera @{
        mApp.unRegisterKeyEventListener(mKeyEventListener);
        // @}
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public void refreshViewEntry() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
        if (mSwitcherView != null) {
            if (getEntryValues().size() <= 1) {
                mSwitcherView.setVisibility(View.GONE);
            } else {
                mSwitcherView.setVisibility(View.VISIBLE);
            }
        }
    }
        });
    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_CAMERA_SWITCHER;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public String getStoreScope() {
        return mDataStore.getGlobalScope();
    }

    private List<String> getCamerasFacing(int numOfCameras) {
        List<String> camerasFacing = new ArrayList<>();
        for (int i = 0; i < numOfCameras; i++) {
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(i, info);

            String facing = null;
            if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
                facing = CAMERA_FACING_BACK;
            } else if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                facing = CAMERA_FACING_FRONT;
            }

            if (!camerasFacing.contains(facing)) {
                camerasFacing.add(facing);
            }
        }
        return camerasFacing;
    }

    private View initView() {
        Activity activity = mApp.getActivity();
        View switcher = activity.getLayoutInflater().inflate(R.layout.camera_switcher, null);
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemProperties.getInt(SWITCH_CAMERA_DEBUG_PROPERTY, 0) == 1) {
                    LogHelper.d(TAG, "[onClick], enter debug mode.");
                    switchCameraInDebugMode();
                } else if (SystemProperties.getInt(DEBUG_MAIN2, 0) == 1) {
                    LogHelper.d(TAG, "[onClick], enter main2 debug mode.");
                    switchCameraInDebugMain2();
                } else {
                    LogHelper.d(TAG, "[onClick], enter camera normal mode.");
                    switchCameraInNormal();
                }
            }
        });
        switcher.setContentDescription(mFacing);
        return switcher;
    }

    private void switchCameraInDebugMain2() {
        if (mIdList == null || mIdList.length == 0) {
            return;
        }
        String nextFacing = mFacing.equals(CAMERA_FACING_BACK) ? CAMERA_FACING_FRONT
                : CAMERA_FACING_BACK;
        String newCameraId = mDataStore.getValue(KEY_DEBUG_STEREO_MAIN2, "0", getStoreScope());
        LogHelper.d(TAG, "[switchCameraInDebugMain2] last cameraId = " + newCameraId);
        int index = 0;
        for (int i = 0; i < mIdList.length; i++) {
            if (mIdList[i].equals(newCameraId)) {
                index = i;
                index++;
                break;
            }
        }
        if (index > mIdList.length - 1 || index < 0) {
            index = 0;
        }
        newCameraId = mIdList[index];
        LogHelper.d(TAG, "[switchCameraInDebugMain2] current cameraId = " + newCameraId);
        boolean success = mApp.notifyCameraSelected(newCameraId);
        if (success) {
            LogHelper.d(TAG, "[switchCameraInDebugMain2] switch to " + newCameraId + " success");
            mDataStore.setValue(KEY_DEBUG_STEREO_MAIN2, newCameraId, getStoreScope(), true);
            mFacing = nextFacing;
            mDataStore.setValue(KEY_CAMERA_SWITCHER, mFacing, getStoreScope(), true);
        }
        mSwitcherView.setContentDescription(mFacing);
    }

    private void switchCameraInNormal() {
        String nextFacing = mFacing.equals(CAMERA_FACING_BACK) ? CAMERA_FACING_FRONT
                : CAMERA_FACING_BACK;
        LogHelper.d(TAG, "[switchCameraInNormal], switch camera to " + nextFacing);
        String newCameraId = mFacing.equals(CAMERA_FACING_BACK) ? CameraUtil
                .getCamIdsByFacing(false, mApp.getActivity()).get(0) :
                CameraUtil.getCamIdsByFacing(true, mApp.getActivity()).get(0);
        boolean success = mApp.notifyCameraSelected(newCameraId);
        if (success) {
            LogHelper.d(TAG, "[switchCameraInNormal], switch camera success.");
            mFacing = nextFacing;
            mDataStore.setValue(KEY_CAMERA_SWITCHER, mFacing,
                    getStoreScope(), true);
        }
        mSwitcherView.setContentDescription(mFacing);
    }

    private void switchCameraInDebugMode() {
        LogHelper.d(TAG, "[switchCameraInDebugMode]");
        String requestCamera = SystemProperties.getString(DEBUG_CAMERA_ID_PROPERTY, "back-0");
        int cameraIndex = 0;
        String resultCameraId = "0";
        List<String> backIds = CameraUtil.getCamIdsByFacing(true, mApp.getActivity());
        List<String> frontIds = CameraUtil.getCamIdsByFacing(false, mApp.getActivity());
        cameraIndex = Integer.parseInt(requestCamera.substring(requestCamera.indexOf("-") + 1));
        if (requestCamera.contains(CAMERA_FACING_BACK) && backIds == null) {
            LogHelper.e(TAG, "[switchCameraInDebugMode] backIds is null");
            return;
        }
        if (requestCamera.contains(CAMERA_FACING_FRONT) && frontIds == null) {
            LogHelper.e(TAG, "[switchCameraInDebugMode] frontIds is null");
            return;
        }
        if (requestCamera.contains(CAMERA_FACING_BACK)) {
            if (cameraIndex < backIds.size()) {
                resultCameraId = backIds.get(cameraIndex);
            } else {
                LogHelper.e(TAG, "[switchCameraInDebugMode] invalid back camera index "
                        + cameraIndex);
                return;
            }
        } else if (requestCamera.contains(CAMERA_FACING_FRONT)) {
            if (cameraIndex < frontIds.size()) {
                resultCameraId = frontIds.get(cameraIndex);
            } else {
                LogHelper.e(TAG, "[switchCameraInDebugMode] invalid front camera index "
                        + cameraIndex);
                return;
            }
        }
        LogHelper.i(TAG, "[switchCameraInDebugMode] requestCamera " + requestCamera
                + ",resultCameraId " + resultCameraId + ",mLastRequestCameraId " +
                mLastRequestCameraId);
        if (resultCameraId.equals(mLastRequestCameraId)) {
            return;
        }
        mLastRequestCameraId = resultCameraId;
        mApp.notifyCameraSelected(resultCameraId);
        mSwitcherView.setContentDescription(requestCamera);
        mFacing = requestCamera.substring(0, requestCamera.indexOf("-"));;
        mDataStore.setValue(KEY_CAMERA_SWITCHER, mFacing, getStoreScope(), true);
    }

    // [Add for CCT tool] Receive keycode and switch camera @{
    // KEYCODE_C: click switch camera icon
    private class KeyEventListenerImpl implements IApp.KeyEventListener {
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (keyCode != CameraUtil.KEYCODE_SWITCH_CAMERA
                    || !CameraUtil.isSpecialKeyCodeEnabled()) {
                return false;
            }
            return true;
        }

        @Override
        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (keyCode != CameraUtil.KEYCODE_SWITCH_CAMERA
                    || !CameraUtil.isSpecialKeyCodeEnabled()) {
                return false;
            }
            if (mSwitcherView != null && mSwitcherView.getVisibility() == View.VISIBLE
                    && mSwitcherView.isEnabled()) {
                mSwitcherView.performClick();
            }
            return true;
        }
    }
    // @}
}
