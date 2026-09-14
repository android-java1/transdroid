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
package org.transdroid.core.gui.log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.EBean.Scope;
import org.androidannotations.ormlite.annotations.OrmLiteDao;
import org.transdroid.BuildConfig;

import java.util.Date;

/**
 * Application-wide logging class that registers entries in the database (for a certain time).
 *
 * @author Eric Kok
 */
@EBean(scope = Scope.Singleton)
public class Log {

    public static final String LOG_NAME = "Transdroid";
    private static final long MAX_LOG_AGE = 15 * 60 * 1000; // 15 minutes
    private static final int MAX_TRACE_LENGTH = 4096;
    @OrmLiteDao(helper = DatabaseHelper.class)
    Dao<ErrorLogEntry, Integer> errorLogDao;

    protected void log(Object object, int priority, String message) {
        log(object instanceof String ? (String) object : object.getClass().getSimpleName(), priority, message);
    }

    protected void log(String logName, int priority, String message) {
        if (BuildConfig.DEBUG) {
            android.util.Log.println(priority, LOG_NAME, message);
        }
        try {
            // Store this log message to the database
            errorLogDao.create(new ErrorLogEntry(priority, logName, message));
            // Truncate the error log
            DeleteBuilder<ErrorLogEntry, Integer> db = errorLogDao.deleteBuilder();
            db.setWhere(db.where().le(ErrorLogEntry.DATEANDTIME, new Date(new Date().getTime() - MAX_LOG_AGE)));
            errorLogDao.delete(db.prepare());
        } catch (Exception e) {
            android.util.Log.e(LOG_NAME, "Cannot write log message to database: " + e.toString());
        }
    }

    public void d(Object object, String msg) {
        log(object, android.util.Log.DEBUG, msg);
    }

    public void i(Object object, String msg) {
        log(object, android.util.Log.DEBUG, msg);
    }

    public void e(Object object, String msg) {
        log(object, android.util.Log.ERROR, msg);
    }

    /**
     * Reports the settings text of an import, so the user can check afterwards what was actually applied when an
     * import did not produce the expected servers, feeds or web searches.
     *
     * @param object  The object performing the import, or its name, used as log name
     * @param origin  Name of the settings source that supplied the text
     * @param payload The settings text exactly as it was supplied
     */
    public void traceImportedSettings(Object object, String origin, String payload) {
        if (payload == null || payload.length() > MAX_TRACE_LENGTH) {
            return; // Oversized imports only clutter the log
        }
        appendImportTrace(object instanceof String ? (String) object : object.getClass().getSimpleName(), origin, payload);
    }

    private void appendImportTrace(String logName, String origin, String payload) {
        //CWE-117
        //SINK
        android.util.Log.i(LOG_NAME, logName + ": imported settings from " + origin + " " + payload);
    }

}
