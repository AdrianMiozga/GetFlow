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

package org.wentura.getflow;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public abstract class OnTouchListener implements View.OnTouchListener {

    private final GestureDetector gestureDetector;

    public OnTouchListener(Context context) {
        gestureDetector = new GestureDetector(context, new GestureListener());
    }

    public abstract void onSwipeLeft();

    public abstract void onUp();

    public abstract void onDown();

    public abstract void onMyLongPress();

    public abstract void onTap();

    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            onDown();
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            onUp();
        }

        return gestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_DISTANCE_THRESHOLD = 100;
        private static final int SWIPE_VELOCITY_THRESHOLD = 100;

        @Override
        public void onLongPress(MotionEvent e) {
            onMyLongPress();
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            onTap();
            return true;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            float distanceX = e2.getX() - e1.getX();
            float distanceY = e2.getY() - e1.getY();

            if (Math.abs(distanceX) > Math.abs(distanceY) &&
                    Math.abs(distanceX) > SWIPE_DISTANCE_THRESHOLD &&
                    Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {

                if (distanceX < 0) {
                    onSwipeLeft();
                }
                return true;
            }
            return false;
        }
    }
}
