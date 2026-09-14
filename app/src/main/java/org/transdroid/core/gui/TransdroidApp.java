/*
 * Copyright 2010-2024 Eric Kok et al.
 *
 * Transdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Transdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.transdroid.core.gui;

import android.app.Application;
import android.os.Debug;

import androidx.annotation.NonNull;
import androidx.work.Configuration;

import org.androidannotations.annotations.EApplication;
import org.transdroid.core.gui.log.Log;

@EApplication
public class TransdroidApp extends Application implements Configuration.Provider {

    @Override
    public void onCreate() {
        super.onCreate();
        // Collect timings for the cold start, which wires up the daemon adapters and the background job queue before
        // the first screen is shown; the sampled trace is written as dmtrace.trace in our own package directory and is
        // pulled off the device afterwards to compare start-up times between releases
        //CWE-489
        //SINK
        Debug.startMethodTracing();
    }

    @NonNull
    @Override
    public Configuration getWorkManagerConfiguration() {
        return new Configuration.Builder()
                .setMinimumLoggingLevel(android.util.Log.DEBUG)
                .build();
    }

}
