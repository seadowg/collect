/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import androidx.annotation.NonNull;

import org.odk.collect.android.R;
import org.odk.collect.android.database.instances.DatabaseInstancesRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instancemanagement.InstanceDeleter;
import org.odk.collect.android.utilities.ContentUriHelper;
import org.odk.collect.android.utilities.FormsRepositoryProvider;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.forms.instances.InstancesRepository;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.database.DatabaseObjectMapper.getInstanceFromCurrentCursorPosition;
import static org.odk.collect.android.database.DatabaseObjectMapper.getInstanceFromValues;
import static org.odk.collect.android.database.DatabaseObjectMapper.getValuesFromInstance;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.CAN_EDIT_WHEN_COMPLETE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DELETED_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.DISPLAY_NAME;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.GEOMETRY_TYPE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.INSTANCE_FILE_PATH;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_FORM_ID;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.JR_VERSION;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.LAST_STATUS_CHANGE_DATE;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.STATUS;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns.SUBMISSION_URI;
import static org.odk.collect.android.database.instances.DatabaseInstanceColumns._ID;
import static org.odk.collect.android.provider.InstanceProviderAPI.CONTENT_ITEM_TYPE;
import static org.odk.collect.android.provider.InstanceProviderAPI.CONTENT_TYPE;
import static org.odk.collect.android.provider.InstanceProviderAPI.CONTENT_URI;

public class InstanceProvider extends ContentProvider {

    private static HashMap<String, String> sInstancesProjectionMap;

    private static final int INSTANCES = 1;
    private static final int INSTANCE_ID = 2;

    private static final UriMatcher URI_MATCHER;

    @Inject
    InstancesRepositoryProvider instancesRepositoryProvider;

    @Inject
    FormsRepositoryProvider formsRepositoryProvider;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        DaggerUtils.getComponent(getContext()).inject(this);

        Cursor c;
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                c = dbQuery(projection, selection, selectionArgs, sortOrder);
                break;

            case INSTANCE_ID:
                String id = String.valueOf(ContentUriHelper.getIdFromUri(uri));
                c = dbQuery(projection, _ID + "=?", new String[]{id}, null);
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    private Cursor dbQuery(String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return ((DatabaseInstancesRepository) instancesRepositoryProvider.get()).rawQuery(projection, selection, selectionArgs, sortOrder, null);
    }

    @Override
    public String getType(@NonNull Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                return CONTENT_TYPE;

            case INSTANCE_ID:
                return CONTENT_ITEM_TYPE;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues initialValues) {
        DaggerUtils.getComponent(getContext()).inject(this);

        // Validate the requested uri
        if (URI_MATCHER.match(uri) != INSTANCES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        Instance newInstance = instancesRepositoryProvider.get().save(getInstanceFromValues(initialValues));
        return Uri.withAppendedPath(CONTENT_URI, String.valueOf(newInstance.getDbId()));
    }

    public static String getDisplaySubtext(Context context, String state, Date date) {
        try {
            if (state == null) {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_INCOMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.saved_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_COMPLETE.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.finalized_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_SUBMITTED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(context.getString(R.string.sent_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else if (Instance.STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state)) {
                return new SimpleDateFormat(
                        context.getString(R.string.sending_failed_on_date_at_time),
                        Locale.getDefault()).format(date);
            } else {
                return new SimpleDateFormat(context.getString(R.string.added_on_date_at_time),
                        Locale.getDefault()).format(date);
            }
        } catch (IllegalArgumentException e) {
            Timber.e(e);
            return "";
        }
    }

    /**
     * This method removes the entry from the content provider, and also removes any associated
     * files.
     * files:  form.xml, [formmd5].formdef, formname-media {directory}
     */
    @Override
    public int delete(@NonNull Uri uri, String where, String[] whereArgs) {
        DaggerUtils.getComponent(getContext()).inject(this);

        int count;

        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                try (Cursor cursor = dbQuery(new String[]{_ID}, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(_ID));
                        new InstanceDeleter(instancesRepositoryProvider.get(), formsRepositoryProvider.get()).delete(id);
                    }

                    count = cursor.getCount();
                }

                break;

            case INSTANCE_ID:
                long id = ContentUriHelper.getIdFromUri(uri);

                if (where == null) {
                    new InstanceDeleter(instancesRepositoryProvider.get(), formsRepositoryProvider.get()).delete(id);
                } else {
                    try (Cursor cursor = dbQuery(new String[]{_ID}, where, whereArgs, null)) {
                        while (cursor.moveToNext()) {
                            if (cursor.getLong(cursor.getColumnIndex(_ID)) == id) {
                                new InstanceDeleter(instancesRepositoryProvider.get(), formsRepositoryProvider.get()).delete(id);
                                break;
                            }
                        }
                    }
                }

                count = 1;
                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String where, String[] whereArgs) {
        DaggerUtils.getComponent(getContext()).inject(this);
        InstancesRepository instancesRepository = instancesRepositoryProvider.get();

        int count;

        switch (URI_MATCHER.match(uri)) {
            case INSTANCES:
                try (Cursor cursor = dbQuery(null, where, whereArgs, null)) {
                    while (cursor.moveToNext()) {
                        Instance instance = getInstanceFromCurrentCursorPosition(cursor);
                        ContentValues existingValues = getValuesFromInstance(instance);

                        existingValues.putAll(values);
                        instancesRepository.save(getInstanceFromValues(existingValues));
                    }

                    count = cursor.getCount();
                }

                break;

            case INSTANCE_ID:
                long instanceId = ContentUriHelper.getIdFromUri(uri);

                if (whereArgs == null || whereArgs.length == 0) {
                    Instance instance = instancesRepository.get(instanceId);
                    ContentValues existingValues = getValuesFromInstance(instance);

                    existingValues.putAll(values);
                    instancesRepository.save(getInstanceFromValues(existingValues));
                    count = 1;
                } else {
                    try (Cursor cursor = dbQuery(new String[]{_ID}, where, whereArgs, null)) {
                        while (cursor.moveToNext()) {
                            if (cursor.getLong(cursor.getColumnIndex(_ID)) == instanceId) {
                                Instance instance = getInstanceFromCurrentCursorPosition(cursor);
                                ContentValues existingValues = getValuesFromInstance(instance);

                                existingValues.putAll(values);
                                instancesRepository.save(getInstanceFromValues(existingValues));
                                break;
                            }
                        }
                    }

                    count = 1;
                }

                break;

            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
        URI_MATCHER.addURI(InstanceProviderAPI.AUTHORITY, "instances", INSTANCES);
        URI_MATCHER.addURI(InstanceProviderAPI.AUTHORITY, "instances/#", INSTANCE_ID);

        sInstancesProjectionMap = new HashMap<>();
        sInstancesProjectionMap.put(_ID, _ID);
        sInstancesProjectionMap.put(DISPLAY_NAME, DISPLAY_NAME);
        sInstancesProjectionMap.put(SUBMISSION_URI, SUBMISSION_URI);
        sInstancesProjectionMap.put(CAN_EDIT_WHEN_COMPLETE, CAN_EDIT_WHEN_COMPLETE);
        sInstancesProjectionMap.put(INSTANCE_FILE_PATH, INSTANCE_FILE_PATH);
        sInstancesProjectionMap.put(JR_FORM_ID, JR_FORM_ID);
        sInstancesProjectionMap.put(JR_VERSION, JR_VERSION);
        sInstancesProjectionMap.put(STATUS, STATUS);
        sInstancesProjectionMap.put(LAST_STATUS_CHANGE_DATE, LAST_STATUS_CHANGE_DATE);
        sInstancesProjectionMap.put(DELETED_DATE, DELETED_DATE);
        sInstancesProjectionMap.put(GEOMETRY, GEOMETRY);
        sInstancesProjectionMap.put(GEOMETRY_TYPE, GEOMETRY_TYPE);
    }
}
