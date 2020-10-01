/*
 * Copyright (C) 2020 Adrian Miozga <AdrianMiozga@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package org.wentura.getflow.statistics.activitychart;

import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextDirectionHeuristic;
import android.text.TextDirectionHeuristics;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Constructor;

final class StaticLayoutWithMaxLines {

    private static final String TAG = StaticLayoutWithMaxLines.class.getSimpleName();

    private static boolean sInitialized;

    private static Constructor<StaticLayout> sConstructor;
    private static Object[] sConstructorArgs;
    private static Object sTextDirection;

    static synchronized void ensureInitialized() {
        if (sInitialized) {
            return;
        }

        try {
            final Class<?> textDirClass = TextDirectionHeuristic.class;
            sTextDirection = TextDirectionHeuristics.FIRSTSTRONG_LTR;

            final Class<?>[] signature = new Class[]{
                    CharSequence.class,
                    int.class,
                    int.class,
                    TextPaint.class,
                    int.class,
                    Layout.Alignment.class,
                    textDirClass,
                    float.class,
                    float.class,
                    boolean.class,
                    TextUtils.TruncateAt.class,
                    int.class,
                    int.class
            };

            // Make the StaticLayout constructor with max lines public
            sConstructor = StaticLayout.class.getDeclaredConstructor(signature);
            sConstructor.setAccessible(true);
            sConstructorArgs = new Object[signature.length];
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "StaticLayout constructor with max lines not found.", e);
        } finally {
            sInitialized = true;
        }
    }

    public static synchronized StaticLayout create(CharSequence source, int bufstart, int bufend,
                                                   TextPaint paint, int outerWidth, Layout.Alignment align,
                                                   float spacingMult, float spacingAdd,
                                                   boolean includePad, TextUtils.TruncateAt ellipsize,
                                                   int ellipsisWidth, int maxLines) {
        ensureInitialized();

        try {
            sConstructorArgs[0] = source;
            sConstructorArgs[1] = bufstart;
            sConstructorArgs[2] = bufend;
            sConstructorArgs[3] = paint;
            sConstructorArgs[4] = outerWidth;
            sConstructorArgs[5] = align;
            sConstructorArgs[6] = sTextDirection;
            sConstructorArgs[7] = spacingMult;
            sConstructorArgs[8] = spacingAdd;
            sConstructorArgs[9] = includePad;
            sConstructorArgs[10] = ellipsize;
            sConstructorArgs[11] = ellipsisWidth;
            sConstructorArgs[12] = maxLines;

            return sConstructor.newInstance(sConstructorArgs);
        } catch (Exception e) {
            throw new IllegalStateException("Error creating StaticLayout with max lines: " + e);
        }
    }
}
