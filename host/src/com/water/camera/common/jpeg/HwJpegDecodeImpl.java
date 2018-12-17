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
package com.water.camera.common.jpeg;

import android.graphics.SurfaceTexture;
import android.util.Log;

/**
 * Jpeg decoder implementer.
 */

public class HwJpegDecodeImpl extends JpegDecoder {
    private static final String TAG = HwJpegDecodeImpl.class.getSimpleName();

    private long mNativeContext;

    /**
     * Jpeg decoder implementer constructor.
     *
     * @param surfaceTexture The surface texture object.
     */
    HwJpegDecodeImpl(SurfaceTexture surfaceTexture) {
        nativeSetup(surfaceTexture);
    }

    /**
     * Jpeg decoder implementer constructor.
     *
     * @param width The width used to create surface.
     * @param height The height used to create surface.
     * @param format The image format used to create surface.
     * @param outData The output data of jpeg decoder.
     */
    HwJpegDecodeImpl(int width, int height, int format, byte[] outData) {
        nativeSetup(width, height, format, outData);
    }

    @Override
    public void decode(byte[] jpegData) {
        Log.i(TAG, "[decode], jpegData:" + jpegData);
        nativeDecode(jpegData);
    }

    @Override
    public void release() {
        Log.i(TAG, "[release]");
        nativeRelease();
    }

    static {
        System.loadLibrary("jni_jpegdecoder");
        nativeClassInit();
    }

    private static native void nativeClassInit();
    private native void nativeSetup(SurfaceTexture surfaceTexture);
    private native void nativeSetup(int width, int height, int format, byte[] outData);
    private native void nativeDecode(byte[] jpegData);
    private native void nativeRelease();
}
