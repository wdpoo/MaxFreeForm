#pragma once

#include <nhk/config.h>

namespace maxfreeform::config {

// Feature bits, mirrored by NativeConfigEndpoint.kt.
// The native shortcut patch opens two narrow validator gates (form factor on
// the small-window entry, SupportSmallWindow on the multiple-window entry) and
// is not limited by device form factor.
constexpr uint32_t kAddFreeformShortcut = 1u << 0u;
constexpr uint32_t kKnownFeatures = kAddFreeformShortcut;

inline constexpr nhk::config::Descriptor kDescriptor{
    0x004D4646u, 0x4D464631, kKnownFeatures};

inline void Configure() { nhk::config::Configure(&kDescriptor); }
inline bool Refresh() { return nhk::config::Refresh(); }
inline bool EnsureReady() { return nhk::config::EnsureReady(); }
inline bool Enabled(uint32_t feature) {
    return nhk::config::Enabled(feature);
}
inline bool Ready() { return nhk::config::Ready(); }
inline uint32_t Flags() { return nhk::config::Flags(); }

}  // namespace maxfreeform::config
