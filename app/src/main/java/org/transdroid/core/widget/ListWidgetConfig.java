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
package org.transdroid.core.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import org.transdroid.core.gui.navigation.StatusType;
import org.transdroid.daemon.TorrentsSortBy;

/**
 * Represents a set of settings that define how the user configured a specific app widget.
 *
 * @author Eric Kok
 */
public class ListWidgetConfig {

    private final int serverId;
    private final StatusType statusType;
    private final TorrentsSortBy sortBy;
    private final boolean reserveSort;
    private final boolean showStatusView;
    private final boolean useDarkTheme;

    public ListWidgetConfig(int serverId, StatusType statusType, TorrentsSortBy sortBy, boolean reverseSort,
                            boolean showStatusView, boolean useDarkTheme) {
        this.serverId = serverId;
        this.statusType = statusType;
        this.sortBy = sortBy;
        this.reserveSort = reverseSort;
        this.showStatusView = showStatusView;
        this.useDarkTheme = useDarkTheme;
    }

    public int getServerId() {
        return serverId;
    }

    public StatusType getStatusType() {
        return statusType;
    }

    public TorrentsSortBy getSortBy() {
        return sortBy;
    }

    public boolean shouldReserveSort() {
        return reserveSort;
    }

    public boolean shouldShowStatusView() {
        return showStatusView;
    }

    public boolean shouldUseDarkTheme() {
        return useDarkTheme;
    }

    /**
     * Builds the configuration result that is handed back to the app widget host, pointing it at the details document
     * that this widget was configured against, when the host supplied one.
     *
     * @param appWidgetId   The id of the app widget that was configured
     * @param detailsSource The location of the details document the host wants to keep reading, or null for none
     * @return The result intent to hand back to the calling app widget host
     */
    public Intent buildHostResult(int appWidgetId, String detailsSource) {
        Intent result = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        Uri detailsUri = resolveDetailsSource(detailsSource);
        if (detailsUri != null) {
            result.setData(detailsUri);
            result.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        }
        return result;
    }

    private Uri resolveDetailsSource(String detailsSource) {
        if (detailsSource == null || !detailsSource.startsWith(ContentResolver.SCHEME_CONTENT)) {
            return null; // Only documents served by a content provider can be handed back to the host
        }
        return Uri.parse(detailsSource.trim());
    }

}
