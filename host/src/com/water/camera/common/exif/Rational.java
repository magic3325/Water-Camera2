/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.water.camera.common.exif;

/**
 * The rational data type of EXIF tag. Contains a pair of longs representing the
 * numerator and denominator of a Rational number.
 */
public class Rational {

    private final long mNumerator;
    private final long mDenominator;

    /**
     * Create a Rational with a given numerator and denominator.
     *
     * @param numerator the numerator
     * @param denominator the denominator
     */
    public Rational(long numerator, long denominator) {
        mNumerator = numerator;
        mDenominator = denominator;
    }

    /**
     * Create a copy of a Rational.
     * @param r the original rational.
     */
    public Rational(Rational r) {
        mNumerator = r.mNumerator;
        mDenominator = r.mDenominator;
    }

    /**
     * Gets the numerator of the rational.
     * @return the numerator of the rational.
     */
    public long getNumerator() {
        return mNumerator;
    }

    /**
     * Gets the denominator of the rational.
     * @return the denominator of the rational.
     */
    public long getDenominator() {
        return mDenominator;
    }

    /**
     * Gets the rational value as type double. Will cause a divide-by-zero error
     * if the denominator is 0.
     * @return the rational value as type double.
     */
    public double toDouble() {
        return mNumerator / (double) mDenominator;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        if (obj instanceof Rational) {
            Rational data = (Rational) obj;
            return mNumerator == data.mNumerator && mDenominator == data.mDenominator;
        }
        return false;
    }

    @Override
    public String toString() {
        return mNumerator + "/" + mDenominator;
    }
}
