package org.odk.collect.android.preferences;

import com.google.android.gms.maps.GoogleMap;
import com.mapbox.mapboxsdk.maps.Style;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.QuestionFontSizeUtils;

import java.util.HashMap;

public final class GeneralKeys {
    // server_preferences.xml
    public static final String KEY_PROTOCOL                 = "protocol";

    // odk_server_preferences.xmll
    public static final String KEY_SERVER_URL               = "server_url";
    public static final String KEY_USERNAME                 = "username";
    public static final String KEY_PASSWORD                 = "password";

    // custom_server_paths_preferences.xmlreferences.xml
    public static final String KEY_FORMLIST_URL             = "formlist_url";
    public static final String KEY_SUBMISSION_URL           = "submission_url";

    // google_preferences.xml
    public static final String KEY_SELECTED_GOOGLE_ACCOUNT  = "selected_google_account";
    public static final String KEY_GOOGLE_SHEETS_URL        = "google_sheets_url";

    // user_interface_preferences.xml
    public static final String KEY_APP_THEME                = "appTheme";
    public static final String KEY_APP_LANGUAGE             = "app_language";
    public static final String KEY_FONT_SIZE                = "font_size";
    public static final String KEY_NAVIGATION               = "navigation";
    public static final String KEY_SHOW_SPLASH              = "showSplash";
    public static final String KEY_SPLASH_PATH              = "splashPath";

    // map_preferences.xml
    public static final String KEY_BASEMAP_SOURCE           = "basemap_source";

    // basemap styles
    public static final String KEY_GOOGLE_MAP_STYLE         = "google_map_style";
    public static final String KEY_MAPBOX_MAP_STYLE         = "mapbox_map_style";
    public static final String KEY_USGS_MAP_STYLE           = "usgs_map_style";
    public static final String KEY_CARTO_MAP_STYLE          = "carto_map_style";

    public static final String KEY_REFERENCE_LAYER          = "reference_layer";

    // form_management_preferences.xml
    public static final String KEY_PERIODIC_FORM_UPDATES_CHECK = "periodic_form_updates_check";
    public static final String KEY_AUTOMATIC_DOWNLOAD = "automatic_update";
    public static final String KEY_HIDE_OLD_FORM_VERSIONS   = "hide_old_form_versions";
    public static final String KEY_AUTOSEND                 = "autosend";
    public static final String KEY_DELETE_AFTER_SEND        = "delete_send";
    public static final String KEY_COMPLETED_DEFAULT        = "default_completed";
    public static final String KEY_CONSTRAINT_BEHAVIOR      = "constraint_behavior";
    public static final String KEY_HIGH_RESOLUTION          = "high_resolution";
    public static final String KEY_IMAGE_SIZE               = "image_size";
    public static final String KEY_GUIDANCE_HINT            = "guidance_hint";
    public static final String KEY_INSTANCE_SYNC            = "instance_sync";
    public static final String KEY_FORM_UPDATE_MODE         = "form_update_mode";

    // identity_preferences.xml
    public static final String KEY_ANALYTICS                = "analytics";

    // form_metadata_preferences.xml
    public static final String KEY_METADATA_USERNAME        = "metadata_username";
    public static final String KEY_METADATA_PHONENUMBER     = "metadata_phonenumber";
    public static final String KEY_METADATA_EMAIL           = "metadata_email";

    static final String KEY_FORM_METADATA                   = "form_metadata";

    public static final String KEY_BACKGROUND_LOCATION      = "background_location";

    // values
    public static final String NAVIGATION_SWIPE             = "swipe";
    public static final String CONSTRAINT_BEHAVIOR_ON_SWIPE = "on_swipe";
    public static final String NAVIGATION_BUTTONS           = "buttons";
    private static final String AUTOSEND_OFF                = "off";
    private static final String GUIDANCE_HINT_OFF           = "no";
    static final String KEY_AUTOSEND_WIFI                   = "autosend_wifi";
    static final String KEY_AUTOSEND_NETWORK                = "autosend_network";

    // basemap section
    public static final String CATEGORY_BASEMAP             = "category_basemap";

    // basemap source values
    public static final String BASEMAP_SOURCE_GOOGLE        = "google";
    public static final String BASEMAP_SOURCE_MAPBOX        = "mapbox";
    public static final String BASEMAP_SOURCE_OSM           = "osm";
    public static final String BASEMAP_SOURCE_USGS          = "usgs";
    public static final String BASEMAP_SOURCE_STAMEN        = "stamen";
    public static final String BASEMAP_SOURCE_CARTO         = "carto";

    private static HashMap<String, Object> getHashMap() {
        HashMap<String, Object> hashMap = new HashMap<>();
        // odk_server_preferences.xmll
        hashMap.put(KEY_SERVER_URL,                 Collect.getInstance().getString(R.string.default_server_url));
        hashMap.put(KEY_USERNAME,                   "");
        hashMap.put(KEY_PASSWORD,                   "");
        // form_management_preferences.xml
        hashMap.put(KEY_AUTOSEND,                   AUTOSEND_OFF);
        hashMap.put(KEY_GUIDANCE_HINT,              GUIDANCE_HINT_OFF);
        hashMap.put(KEY_DELETE_AFTER_SEND,          false);
        hashMap.put(KEY_COMPLETED_DEFAULT,          true);
        hashMap.put(KEY_CONSTRAINT_BEHAVIOR,        CONSTRAINT_BEHAVIOR_ON_SWIPE);
        hashMap.put(KEY_HIGH_RESOLUTION,            true);
        hashMap.put(KEY_IMAGE_SIZE,                 "original_image_size");
        hashMap.put(KEY_INSTANCE_SYNC,              true);
        hashMap.put(KEY_PERIODIC_FORM_UPDATES_CHECK, "every_fifteen_minutes");
        hashMap.put(KEY_AUTOMATIC_DOWNLOAD,           false);
        hashMap.put(KEY_HIDE_OLD_FORM_VERSIONS,     true);
        hashMap.put(KEY_BACKGROUND_LOCATION,        true);
        hashMap.put(KEY_FORM_UPDATE_MODE,           "manual");
        // form_metadata_preferences.xml
        hashMap.put(KEY_METADATA_USERNAME,          "");
        hashMap.put(KEY_METADATA_PHONENUMBER,       "");
        hashMap.put(KEY_METADATA_EMAIL,             "");
        // google_preferences.xml
        hashMap.put(KEY_SELECTED_GOOGLE_ACCOUNT,    "");
        hashMap.put(KEY_GOOGLE_SHEETS_URL,          "");
        // identity_preferences.xml
        hashMap.put(KEY_ANALYTICS,                  true);
        // custom_server_paths_preferenceshs_preferences.xml
        hashMap.put(KEY_FORMLIST_URL,               Collect.getInstance().getString(R.string.default_odk_formlist));
        hashMap.put(KEY_SUBMISSION_URL,             Collect.getInstance().getString(R.string.default_odk_submission));
        // server_preferences.xml
        hashMap.put(KEY_PROTOCOL,                   Collect.getInstance().getString(R.string.protocol_odk_default));
        // user_interface_preferences.xml
        hashMap.put(KEY_APP_THEME,                  Collect.getInstance().getString(R.string.app_theme_light));
        hashMap.put(KEY_APP_LANGUAGE,               "");
        hashMap.put(KEY_FONT_SIZE,                  String.valueOf(QuestionFontSizeUtils.DEFAULT_FONT_SIZE));
        hashMap.put(KEY_NAVIGATION,                 NAVIGATION_SWIPE);
        hashMap.put(KEY_SHOW_SPLASH,                false);
        hashMap.put(KEY_SPLASH_PATH,                Collect.getInstance().getString(R.string.default_splash_path));
        // map_preferences.xml
        hashMap.put(KEY_BASEMAP_SOURCE,             BASEMAP_SOURCE_GOOGLE);
        hashMap.put(KEY_CARTO_MAP_STYLE,            "positron");
        hashMap.put(KEY_USGS_MAP_STYLE,             "topographic");
        hashMap.put(KEY_GOOGLE_MAP_STYLE,           String.valueOf(GoogleMap.MAP_TYPE_NORMAL));
        hashMap.put(KEY_MAPBOX_MAP_STYLE,           Style.MAPBOX_STREETS);
        return hashMap;
    }

    public static final HashMap<String, Object> DEFAULTS = getHashMap();

    private GeneralKeys() {

    }

}
