//
// MAXFREEFORM native hook: shared logging helpers.
//
// HyperOS logd filters INFO/DEBUG messages from the launcher process, so all
// diagnostics must go through WARN or ERROR.

#pragma once

#include <android/log.h>

namespace maxfreeform {

constexpr char kLogTag[] = "MaxFreeFormNative";
constexpr int kLogLevel = ANDROID_LOG_WARN;

}  // namespace maxfreeform

#define MAXFREEFORM_LOGW(...) \
    __android_log_print(::maxfreeform::kLogLevel, ::maxfreeform::kLogTag, __VA_ARGS__)
#define MAXFREEFORM_LOGE(...) \
    __android_log_print(ANDROID_LOG_ERROR, ::maxfreeform::kLogTag, __VA_ARGS__)
