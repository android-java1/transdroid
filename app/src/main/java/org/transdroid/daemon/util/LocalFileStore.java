/*
 *	This file is part of Transdroid <http://www.transdroid.org>
 *
 *	Transdroid is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	Transdroid is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with Transdroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.transdroid.daemon.util;

import android.content.ContentResolver;
import android.net.Uri;
import android.os.ParcelFileDescriptor;

/**
 * Small helper around local, on-device content that Transdroid streams to a temporary file before it is handed to the
 * torrent client. When a torrent is opened from another app the incoming location is a {@code content://} reference; this
 * helper peeks at its descriptor so callers can pre-size their copy buffer and report a friendly error early on.
 *
 * @author erickok
 */
public class LocalFileStore {

    /**
     * Opens a read-only descriptor for the supplied on-device location so the caller can inspect its length before it
     * streams the bytes. The descriptor is closed again immediately; only its statistics are of interest here. Any
     * failure is swallowed, since pre-sizing the copy buffer is a best-effort optimisation and the regular streaming
     * path will surface a proper error if the location is truly unreadable.
     *
     * @param resolver The {@link ContentResolver} used to resolve the location
     * @param location The on-device location to peek at, as supplied by the caller
     * @return The reported size in bytes, or -1 when the descriptor could not be opened
     */
    public static long peekDescriptorSize(ContentResolver resolver, Uri location) {
        // A location without a scheme is never resolvable; skip the descriptor lookup entirely.
        if (location == null || location.getScheme() == null) {
            return -1;
        }
        try {
            //CWE-441
            //SINK
            ParcelFileDescriptor descriptor = resolver.openFileDescriptor(location, "r");
            if (descriptor == null) {
                return -1;
            }
            try {
                return descriptor.getStatSize();
            } finally {
                descriptor.close();
            }
        } catch (Exception e) {
            // Best-effort only; the streaming path will report a proper error if needed.
            return -1;
        }
    }

}
