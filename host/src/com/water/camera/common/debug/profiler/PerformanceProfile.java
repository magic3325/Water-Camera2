/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein is
 * confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 * the prior written permission of MediaTek inc. and/or its licensors, any
 * reproduction, modification, use or disclosure of MediaTek Software, and
 * information contained herein, in whole or in part, shall be strictly
 * prohibited.
 *
 * MediaTek Inc. (C) 2016. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 * ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 * WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 * RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 * INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 * TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 * RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 * OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 * SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 * RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 * ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 * RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 * MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 * CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek
 * Software") have been modified by MediaTek Inc. All revisions are subject to
 * any receiver's applicable license agreements with MediaTek Inc.
 */


package com.water.camera.common.debug.profiler;

import com.water.camera.common.debug.LogUtil.Tag;

/**
 * A logging profile writes out all events to the provided
 * writer and uses the standard message formatting.
 */
public class PerformanceProfile extends ProfileBase {
    private ILogWriter mWriter;
    private final Tag mTag;

    /** Create a new PerformanceProfile.
     * @param writer the log writer.
     * @param tag the class tag.
     * @param name the function name.
     */
    public PerformanceProfile(ILogWriter writer, Tag tag, String name) {
        super(name);
        mTag = tag;
        mWriter = writer;
    }

    @Override
    protected void onStart() {
        mWriter.write(mTag, mFormatter.format(0.0, "[BEGIN]"));
    }

    @Override
    protected void onMark(double totalMillis, double lastMillis) {
        mWriter.write(mTag, mFormatter.format(totalMillis, "[MARK]"));
    }

    @Override
    protected void onMark(double totalMillis, double lastMillis, String reason) {
        mWriter.write(mTag, mFormatter.format(totalMillis, "[MARK]", lastMillis, reason));
    }

    @Override
    protected void onStop(double totalMillis, double lastMillis) {
        mWriter.write(mTag, mFormatter.format(totalMillis, "[END]"));
    }

    @Override
    protected void onStop(double totalMillis, double lastMillis, String reason) {
        mWriter.write(mTag, mFormatter.format(totalMillis, "[END]", lastMillis, reason));
    }
}
