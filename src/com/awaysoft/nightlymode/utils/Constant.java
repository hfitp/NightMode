
package com.awaysoft.nightlymode.utils;

import android.graphics.Color;

/**
 * Configuration for constants
 *
 * @author kang
 */
public class Constant {
    /** Animation duration */
    public static final int ANIMATION_DURATION = 450; // unit:（ms）

    /** Nightly mode Constant */
    public static final int MODE_MASK = 0X0001;
    public static final int MODE_AUTO = 0X0001; // Auto mode
    public static final int MODE_NIGHT = 0X0002; // Manual:opened
    public static final int MODE_NORMAL = 0X0004; // Manual:closed

    /** Handle message */
    public static final int MSG_STATUS_CHANGED = 0XFF01;
    public static final int MSG_UPDATE_APP_INFO = 0XFF02;
    public static final int MSG_PREFERENCE_CHANGED = 0XFF03;

    /** Broadcast message */
    public static final String BDC_PREFERENCE_CHENGED = "bdc_preference_changed";
    public static final String BDC_SWITCH_MODE = "bdc_switch_mode";

    /** Preference save file */
    public static final String KEY_PREFERENCE_FILE = "nightly_settings_preference";

    /** Default preference */
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final float DEFAULT_ALPHA = 0.6F;
    public static final String DEFAULT_TIMEBUCKETS = "20:00|06:30";
    public static final String DEFAULT_WHITELIST = "me.imid.fuubo|com.google.android.apps.currents|com.tencent.mobileqq";

    /** Preference settings key */
    public static final String KEY_SERVICES_RUNNING = "service_is_running";
    public static final String KEY_SERVICES_NIGHTLY_MODE = "service_current_status";
    public static final String KEY_SERVICES_AUTOSTART = "service_autostart_on_startup";
    public static final String KEY_SERVICES_SHOW_NOTIFICATON = "service_show_notification";
    public static final String KEY_SERVICES_SHOW_FLOAT_WIDGET = "service_show_float_widget";
    public static final String KEY_MATTE_LAYER_ALPHA = "matte_layer_alpha";
    public static final String KEY_MATTE_LAYER_COLOR = "matte_layer_color";
    public static final String KEY_NIGHLTY_FOR_ALLAPP = "nighlty_apply_for_all";
    public static final String KEY_NIGHTLY_WHITE_LIST = "nightly_auto_white_list";
    public static final String KEY_NIGHTLY_TIMEBUCKETS = "nightly_auto_time_buckets";
    public static final String KEY_FLOAT_WIDGET_LOCATION = "float_widget_location";

    /** Preference target id */
    public static final int TAG_ID_ATUO_START = 0XFFF1;
    public static final int TAG_ID_NOTIFICATION = 0XFFF2;
    public static final int TAG_ID_FLOATWIDGET = 0XFFF3;
    public static final int TAG_ID_ALPHA = 0XFFF4;
    public static final int TAG_ID_COLOR = 0XFFF5;
    public static final int TAG_ID_AUTO_TIME = 0XFFF6;
    public static final int TAG_ID_GLOBAL = 0XFFF7;
    public static final int TAG_ID_WHITE_LIST = 0XFFF8;
    public static final int TAG_ID_FEEDBACK = 0XFFF9;
    public static final int TAG_ID_ABOUT = 0XFFFA;
    public static final int TAG_ID_MODE = 0XFFFB;

    /** Preference target key for intent */
    public static final String PREFERENCE_TARGET_KEY = "preference_target_key";
}
