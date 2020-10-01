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

package org.wentura.getflow.applicationlock;

import android.graphics.drawable.Drawable;

final class Application implements Comparable<Application> {

    private final String packageName;
    private final String applicationName;
    private final Drawable applicationIcon;

    public Application(String packageName, String applicationName, Drawable applicationIcon) {
        this.packageName = packageName;
        this.applicationName = applicationName;
        this.applicationIcon = applicationIcon;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public Drawable getApplicationIcon() {
        return applicationIcon;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public int compareTo(Application application) {
        return this.applicationName.toLowerCase().compareTo(application.applicationName.toLowerCase());
    }
}
