//
// MaxFreeForm native hook: launcher freeform / multiple-window shortcut entries.
//
// HyperOS 4's MiuiHome hides both entries on phones behind two independent
// policies:
//
//   ValidSmallWindowShortcutMenu.isValid
//       has a form-factor gate ("not a Pad device") that returns false on
//       phones.
//
//   ValidMultipleSmallWindowShortcutMenu.isValid
//       rejects earlier in the "not SupportSmallWindow" branch, which asks
//       SmallWindowConfig.isSupportSmallWindow() and gets false on phones.
//
// Only the false return of those two gates is flipped to true.  Every other
// validity check in both validators is left untouched, so an ineligible item
// can never reach the menu.
//
// Both validators emit one log-and-return block per rejection reason.  The
// second block of each validator is the form-factor / support gate we need;
// that ordering is stable because the compiler emits the branches in source
// order.  A block looks like:
//
//   add  x1, x27, #<pool_base>, lsl #12
//   ldr  x1, [x1, #<slot>]        ; "why this item is rejected"
//   mov  x2, x22
//   stur x0, [x29, #-<n>]
//   bl   <string normalize>
//   ldur x1, [x29, #-<n>]
//   mov  x2, x0
//   ldr  x4, [x27, #0x70]
//   bl   <log print>
//   add  x0, x22, #0x30           ; false  <-- patch
//   b    <epilogue>
//
// The pool base, slot, stack offsets, call targets and branch offset all move
// between launcher builds, so the scan masks them.  Blocks belonging to one
// validator are emitted consecutively with the same pool base; the small
// window validator has 8 blocks and the multiple window validator has 9 or
// 10, which is how the two runs are told apart.  Verified on 8.01.02.7722,
// 7726, 7748, 7751, 8565 and 8583.
//
// Dart AOT convention: x22+0x20 is the true object, x22+0x30 is false.

#include "hooks.h"

#include "../log.h"

#include <inttypes.h>
#include <stddef.h>
#include <stdint.h>

namespace maxfreeform::hooks {
namespace {

// Dart AOT tagged booleans: add x0, x22, #0x20 -> true, #0x30 -> false.
constexpr uint32_t kReturnTrueWord = 0x910082c0u;   // add x0, x22, #0x20
constexpr uint32_t kReturnFalseWord = 0x9100c2c0u;  // add x0, x22, #0x30

// Index of the gate block within its validator (0-based).  The second block
// is the form-factor / support gate.
constexpr size_t kGateIndexInGroup = 1u;

// The small-window validator emits 8 log-and-return blocks; the multiple
// window validator emits 9 or 10.  A run ends when the pool base changes or
// the gap to the next block grows past any intra-validator distance.
constexpr size_t kSmallWindowBlockCount = 8u;
constexpr size_t kMinValidatorBlockCount = 8u;
constexpr uintptr_t kMaxBlockGap = 0x400u;

// Masked pattern for the shared log-and-return block.  The `add` immediate
// (pool base) and the `ldr` immediate (slot) move between builds, as do the
// stack offsets and both call targets.
constexpr size_t kGatePatternCount = 11u;
constexpr size_t kGateFalseReturnIndex = 9u;
constexpr dart::PatternWord kGatePattern[kGatePatternCount] = {
    {0x91400361u, 0xffc003ffu},  // add  x1, x27, #<base>, lsl #12
    {0xf9400021u, 0xffc003ffu},  // ldr  x1, [x1, #<slot>]
    {0xaa1603e2u, 0xffffffffu},  // mov  x2, x22
    {0xf81d83a0u, 0xffe003ffu},  // stur x0, [x29, #-<n>]
    {0x94000000u, 0xfc000000u},  // bl   <string normalize>
    {0xf85d83a1u, 0xffe003ffu},  // ldur x1, [x29, #-<n>]
    {0xaa0003e2u, 0xffffffffu},  // mov  x2, x0
    {0xf9403b64u, 0xffffffffu},  // ldr  x4, [x27, #0x70]
    {0x94000000u, 0xfc000000u},  // bl   <log print>
    {0x9100c2c0u, 0xffffffffu},  // add  x0, x22, #0x30  <-- patch target
    {0x14000000u, 0xfc000000u},  // b    <epilogue>
};

// One gate block found by the pattern scan.
struct GateMatch {
    uintptr_t address;
    uint32_t pool_base;
};

// A consecutive run of blocks that share one pool base.  `begin` indexes the
// match array; the blocks inside a run are not evenly spaced.
struct GateGroup {
    size_t begin;
    size_t count;
    uintptr_t last_address;
};

uint32_t g_installed = 0u;

// Scans the image and collects every log-and-return block.
size_t ScanGates(const dart::Image& image, GateMatch* matches, size_t capacity) {
    size_t total = 0u;
    for (size_t seg = 0u; seg < image.segment_count; ++seg) {
        const dart::Segment& s = image.segments[seg];
        if ((s.flags & 0x5u) != 0x5u) continue;  // need PF_R|PF_X
        for (uintptr_t p = s.start;
             p + kGatePatternCount * 4u <= s.start + s.size; p += 4u) {
            bool match = true;
            for (size_t k = 0u; k < kGatePatternCount; ++k) {
                const uint32_t word =
                    *reinterpret_cast<const uint32_t*>(p + k * 4u);
                const dart::PatternWord& pw = kGatePattern[k];
                if ((word & pw.mask) != (pw.value & pw.mask)) {
                    match = false;
                    break;
                }
            }
            if (!match) continue;
            if (total >= capacity) return total;
            const uint32_t add = *reinterpret_cast<const uint32_t*>(p);
            matches[total].address = p;
            matches[total].pool_base = (add >> 10) & 0xfffu;
            ++total;
        }
    }
    return total;
}

// Splits the ascending block list into runs that share a pool base.
size_t GroupGates(const GateMatch* matches, size_t total, GateGroup* groups,
                  size_t capacity) {
    size_t count = 0u;
    for (size_t i = 0u; i < total; ++i) {
        const bool starts_new =
            count == 0u ||
            matches[groups[count - 1u].begin].pool_base != matches[i].pool_base ||
            matches[i].address - groups[count - 1u].last_address > kMaxBlockGap;
        if (starts_new) {
            if (count >= capacity) return count;
            groups[count].begin = i;
            groups[count].count = 1u;
            groups[count].last_address = matches[i].address;
            ++count;
        } else {
            ++groups[count - 1u].count;
            groups[count - 1u].last_address = matches[i].address;
        }
    }
    return count;
}

bool PatchGate(const dart::Image& image, const GateMatch& gate,
               const char* name) {
    const uintptr_t site =
        gate.address + kGateFalseReturnIndex * sizeof(uint32_t);
    if (!dart::RangeInExecutableSegment(image, site, sizeof(uint32_t))) {
        MAXFREEFORM_LOGE("add_freeform_shortcut: %s gate site outside "
                         "executable image",
                         name);
        return false;
    }
    const uint32_t current = *reinterpret_cast<const uint32_t*>(site);
    if (current == kReturnTrueWord) {
        MAXFREEFORM_LOGW("add_freeform_shortcut: %s gate already open", name);
        return true;
    }
    if (current != kReturnFalseWord) {
        MAXFREEFORM_LOGE("add_freeform_shortcut: %s gate unexpected word "
                         "0x%08" PRIx32,
                         name, current);
        return false;
    }
    if (!dart::PatchCode(site, &kReturnTrueWord, 1u)) {
        MAXFREEFORM_LOGE("add_freeform_shortcut: failed to open %s gate",
                         name);
        return false;
    }
    MAXFREEFORM_LOGW("add_freeform_shortcut: %s gate opened at %p (pool "
                     "base 0x%" PRIx32 ")",
                     name, reinterpret_cast<void*>(site), gate.pool_base);
    return true;
}

bool OpenGate(const dart::Image& image, const GateMatch* matches,
              const GateGroup& group, const char* name) {
    if (group.count <= kGateIndexInGroup) {
        MAXFREEFORM_LOGE("add_freeform_shortcut: %s validator has only %zu "
                         "blocks, need at least %zu",
                         name, group.count, kGateIndexInGroup + 1u);
        return false;
    }
    return PatchGate(image, matches[group.begin + kGateIndexInGroup], name);
}

}  // namespace

bool InstallAddFreeformShortcutHook(const dart::Image& image) {
    if (__atomic_exchange_n(&g_installed, 1u, __ATOMIC_ACQ_REL) != 0u) {
        return true;
    }

    constexpr size_t kMaxMatches = 64u;
    GateMatch matches[kMaxMatches];
    const size_t total = ScanGates(image, matches, kMaxMatches);
    if (total == 0u) {
        MAXFREEFORM_LOGE("add_freeform_shortcut: no gate blocks found");
        return false;
    }

    constexpr size_t kMaxGroups = 16u;
    GateGroup groups[kMaxGroups];
    const size_t group_count = GroupGates(matches, total, groups, kMaxGroups);

    const GateGroup* small = nullptr;
    const GateGroup* multi = nullptr;
    for (size_t i = 0u; i < group_count; ++i) {
        if (groups[i].count < kMinValidatorBlockCount) continue;
        if (groups[i].count == kSmallWindowBlockCount) {
            small = &groups[i];
        } else if (multi == nullptr || groups[i].count > multi->count) {
            multi = &groups[i];
        }
    }

    if (small == nullptr || multi == nullptr) {
        MAXFREEFORM_LOGE("add_freeform_shortcut: validator groups not found "
                         "(%zu blocks in %zu runs)",
                         total, group_count);
        return false;
    }

    MAXFREEFORM_LOGW("add_freeform_shortcut: %zu blocks in %zu runs; small=%zu "
                     "multi=%zu",
                     total, group_count, small->count, multi->count);
    const bool small_ok = OpenGate(image, matches, *small, "small_window");
    const bool multi_ok = OpenGate(image, matches, *multi, "multiple_window");
    return small_ok && multi_ok;
}

}  // namespace maxfreeform::hooks
