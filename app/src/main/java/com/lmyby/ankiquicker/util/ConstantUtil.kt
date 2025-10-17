package com.lmyby.ankiquicker.util

/**
 * Converted to Kotlin as part of Phase 1 utility migration
 */
object ConstantUtil {

    const val CONTENT = "content://"
    const val AUTHORITY = "com.forfun.bigbang"
    const val SEPARATOR = "/"
    const val CONTENT_URI = CONTENT + AUTHORITY

    const val TYPE_STRING = "string"
    const val TYPE_INT = "int"
    const val TYPE_LONG = "long"
    const val TYPE_FLOAT = "float"
    const val TYPE_BOOLEAN = "boolean"
    const val TYPE_CONTAIN = "contain"
    const val DEFAULT_CURSOR_NAME = "default"
    const val VALUE = "value"
    const val NULL_STRING = "null"

    const val BROADCAST_RELOAD_SETTING = "broadcast_reload_setting"
    const val BROADCAST_BIGBANG_MONITOR_SERVICE_MODIFIED = "broadcast_bigbang_monitor_service_modified"

    const val BROADCAST_CLIPBOARD_LISTEN_SERVICE_MODIFIED = "broadcast_clipboard_listen_service_modified"

    const val BROADCAST_SET_TO_CLIPBOARD = "broadcast_set_to_clipboard"
    const val BROADCAST_SET_TO_CLIPBOARD_MSG = "broadcast_set_to_clipboard_msg"

    //shareCard
    const val HAD_SHARED = "had_shared"
    const val SETTING_OPEN_TIMES = "setting_open_times"

    //FunctionSettingCard
    const val MONITOR_CLIP_BOARD = "monitor_clip_board"
    const val MONITOR_CLICK = "monitor_click"
    const val TOTAL_SWITCH = "total_switch"
    const val SHOW_FLOAT_VIEW = "show_float_view"
    const val REMAIN_SYMBOL = "remain_symbol"
    const val REMAIN_SECTION = "remain_section"
    const val DEFAULT_LOCAL = "default_local"

    const val AUTO_OPEN_SETTING = "auto_open_setting"

    //floatview
    const val FLOAT_SWITCH_STATE = "float_switch_state"
    const val FLOAT_VIEW_LAND_X = "float_view_land_x"
    const val FLOAT_VIEW_LAND_Y = "float_view_land_Y"
    const val FLOAT_VIEW_PORT_X = "float_view_port_x"
    const val FLOAT_VIEW_PORT_Y = "float_view_port_y"

    const val IS_SHOW_NOTIFY = "is_show_notify"
    const val NOTIFY_DISABLED_IGNORE = "notify_disabled_ignore"

    //FeedBackAndUpdateCard

    //MonitorSettingCard
    const val TEXT_ONLY = "text_only"
    const val QQ_SELECTION = "qq_selection"
    const val WEIXIN_SELECTION = "weixin_selection"
    const val OTHER_SELECTION = "other_selection"

    const val BROWSER_SELECTION = "browser_selection"

    const val Setting_content_Changes = "tencent_contents_change"
    const val SHOW_TENCENT_SETTINGS = "tencent_settings"

    const val ONLINE_CONFIG_OPEN_UPDATE = "online_config_open_update"
    const val DOUBLE_CLICK_INTERVAL = "double_click_interval"
    const val DEFAULT_DOUBLE_CLICK_INTERVAL = 1000

    //SettingBigBangActivity
    const val TEXT_SIZE = "text_size"
    const val LINE_MARGIN = "line_margin"
    const val ITEM_MARGIN = "item_margin"
    const val ITEM_PADDING = "item_padding"
    const val BIGBANG_ALPHA = "bigbang_alpha"
    const val USE_LOCAL_WEBVIEW = "use_local_webview"
    const val USE_FLOAT_VIEW_TRIGGER = "use_float_view_trigger"
    const val BIGBANG_DIY_BG_COLOR = "bigbang_diy_bg_color"
    const val IS_FULL_SCREEN = "is_full_screen"
    const val IS_STICK_HEADER = "is_stick_header"
    const val IS_STICK_SHAREBAR = "is_stick_sharebar"
    const val AUTO_ADD_BLANKS = "auto_add_blanks"
    const val TREAT_BLANKS_AS_SYMBOL = "treat_blanks_as_symbol"

    const val FLOATVIEW_SIZE = "floatview_size_"
    const val FLOATVIEW_ALPHA = "floatview_alpha"
    const val FLOATVIEW_DIY_BG_COLOR = "floatview_diy_bg_color"
    const val FLOATVIEW_IS_STICK = "floatview_is_stick"

    const val DEFAULT_TEXT_SIZE = 14
    const val DEFAULT_LINE_MARGIN = 8
    const val DEFAULT_ITEM_MARGIN = 0
    const val DEFAULT_ITEM_PADDING = 10

    //whiteListActivity
    const val WHITE_LIST_COUNT = "white_list_count"
    const val WHITE_LIST = "white_list"
    const val REFRESH_WHITE_LIST_BROADCAST = "refresh_white_list_broadcast"

    //FloatwhiteListActivity
    const val FLOAT_WHITE_LIST_COUNT = "float_white_list_count"
    const val FLOAT_WHITE_LIST = "float_white_list_"
    const val FLOAT_REFRESH_WHITE_LIST_BROADCAST = "float_refresh_white_list_broadcast"

    const val HAS_ADDED_LAUNCHER_AS_WHITE_LIST = "has_added_launcher_as_white_list"

    const val UNIVERSAL_COPY_BROADCAST = "universal_copy_broadcast"
    const val UNIVERSAL_COPY_BROADCAST_DELAY = "universal_copy_broadcast_delay"
    const val SCREEN_CAPTURE_OVER_BROADCAST = "screen_capture_over_broadcast"

    const val TOTAL_SWITCH_BROADCAST = "total_switch_broadcast"
    const val MONITOR_CLICK_BROADCAST = "monitor_click_broadcast"
    const val MONITOR_CLIPBOARD_BROADCAST = "monitor_clipboard_broadcast"

    const val NOTIFY_UNIVERSAL_COPY_BROADCAST = "notify_universal_copy_broadcast"
    const val NOTIFY_SCREEN_CAPTURE_OVER_BROADCAST = "notify_screen_capture_over_broadcast"

    const val OCR_TIME = "ocr_time"
    const val OCR_TIME_TO_ALERT = 5
    const val SHOULD_SHOW_DIY_OCR = "should_show_diy_ocr"

    const val DIY_OCR_KEY = "diy_ocr_key"

    const val EFFECT_AFTER_REBOOT_BROADCAST = "effect_after_reboot_broadcast"

    const val LONG_PRESS_KEY_INDEX = "long_press_key_index"

    const val SHARE_APPS_DIS = "share_app_dis"
    const val HAD_ENTER_INTRO = "had_enter_intro"
    const val SHARE_APP_INDEX = "share_app_index"

    const val HAD_SHOW_LONG_PRESS_TOAST = "had_show_long_press_toast"

    //copyActivity
    const val IS_FULL_SCREEN_COPY = "is_full_screen_copy"

    //feedback
    //    const val ALI_APP_KEY= BigBangApp.getInstance().getString(R.string.ali_feedback_key)
    //xp全局复制
    const val UNIVERSAL_COPY_BROADCAST_XP = "universal_copy_broadcast_xp"
}
