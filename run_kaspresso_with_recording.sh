#!/usr/bin/env bash
#
# run_kaspresso_with_recording.sh
# Professional orchestration script for running Kaspresso tests on an Android device/emulator
# with screen recording (adb screenrecord), screenshot pulls, and Allure results collection.
#
# Usage:
#   ./run_kaspresso_with_recording.sh [smoke | all | single-class-name]
#
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$SCRIPT_DIR"
RECORDINGS_DIR="$PROJECT_ROOT/kaspresso_screenrecords"
ALLURE_DIR="$PROJECT_ROOT/kaspresso_allure_results"
BUILD_LOG="$PROJECT_ROOT/kaspresso_build.log"

cd "$PROJECT_ROOT"

# ═══════════════════════ 1. Device selection  ═══════════════════════
if [[ "$(adb devices | grep -c 'device$')" -eq 0 ]]; then
    echo "[FATAL] No connected Android devices found."
    exit 1
fi

# Prefer emulator over physical device (screenrecord works reliably on emulators)
EMULATOR=$(adb devices -l | grep 'emulator' | awk '{print $1}' | head -1)
DEVICE="${EMULATOR:-$(adb devices -l | grep 'device ' | awk '{print $1}' | head -1)}"
echo "[INFO] Target device: $DEVICE"

# ═══════════════════════ 2. Keep screen on  ═══════════════════════
echo "[INFO] Waking device and disabling screen lock..."
adb -s "$DEVICE" shell "svc power stayon usb" || true
adb -s "$DEVICE" shell "settings put system screen_off_timeout 600000" || true
adb -s "$DEVICE" shell "input keyevent KEYCODE_WAKEUP" || true
adb -s "$DEVICE" shell "wm dismiss-keyguard" || true

# ═══════════════════════ 3. Compile tests  ═══════════════════════
echo "[INFO] Compiling connectedDebugAndroidTest..."
./gradlew :application:app:compileDebugAndroidTestKotlin \
    --quiet > "$BUILD_LOG" 2>&1 || {
        echo "[FATAL] Build failed — see $BUILD_LOG"
        tail -n 40 "$BUILD_LOG"
        exit 1
    }
echo "[INFO] Build OK"

# ═══════════════════════ 4. Start screen recording  ═══════════════════════
mkdir -p "$RECORDINGS_DIR"
RECORDING_FILE="/sdcard/kaspresso_screenrecord_$(date +%s).mp4"

echo "[INFO] Starting screen recording on device..."
adb -s "$DEVICE" shell screenrecord --time-limit 300 "$RECORDING_FILE" &
RECORD_PID=$!
sleep 2  # Give screenrecord time to start

# ═══════════════════════ 5. Run tests  ═══════════════════════
TEST_FILTER="${1:-smoke}"
case "$TEST_FILTER" in
    smoke)
        echo "[INFO] Running SMOKE tests only..."
        TEST_ARG="-Pandroid.testInstrumentationRunnerArguments.package=org.videolan.vlc.kaspresso.tests"
        TEST_ARG="$TEST_ARG -Pandroid.testInstrumentationRunnerArguments.class=SmokeTest"
        ;;
    all)
        echo "[INFO] Running ALL Kaspresso tests..."
        TEST_ARG="-Pandroid.testInstrumentationRunnerArguments.package=org.videolan.vlc.kaspresso"
        ;;
    *)
        echo "[INFO] Running single class: ${TEST_FILTER}"
        TEST_ARG="-Pandroid.testInstrumentationRunnerArguments.class=org.videolan.vlc.kaspresso.tests.${TEST_FILTER}"
        ;;
esac

echo "[INFO] Executing: ./gradlew :application:app:connectedDebugAndroidTest $TEST_ARG"
set +e
./gradlew :application:app:connectedDebugAndroidTest \
    $TEST_ARG \
    --info 2>&1 | tee "$PROJECT_ROOT/kaspresso_run.log"
EXIT_CODE=${PIPESTATUS[0]}
set -e

# ═══════════════════════ 6. Pull recording  ═══════════════════════
echo "[INFO] Stopping screen recording..."
kill $RECORD_PID 2>/dev/null || true
sleep 1

LOCAL_RECORDING="$RECORDINGS_DIR/screenrecord_$(date +%Y%m%d_%H%M%S).mp4"
adb -s "$DEVICE" pull "$RECORDING_FILE" "$LOCAL_RECORDING" 2>/dev/null || {
    echo "[WARN] Could not pull recording from device"
}
adb -s "$DEVICE" shell rm -f "$RECORDING_FILE" || true

echo "[INFO] Recording saved to: $LOCAL_RECORDING"

# ═══════════════════════ 7. Pull Allure results  ═══════════════════════
mkdir -p "$ALLURE_DIR"
adb -s "$DEVICE" shell "find /sdcard -path '*/allure-results' -type d 2>/dev/null" | \
    while read -r allure_path; do
        [[ -n "$allure_path" ]] || continue
        echo "[INFO] Pulling Allure results from $allure_path ..."
        adb -s "$DEVICE" pull "$allure_path" "$ALLURE_DIR/" 2>/dev/null || true
    done

# ═══════════════════════ 8. Report  ═══════════════════════
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Kaspresso Test Run Complete"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Device        : $DEVICE"
echo "  Test filter   : $TEST_FILTER"
echo "  Exit code     : $EXIT_CODE"
echo "  Recording     : $LOCAL_RECORDING"
echo "  Allure results: $ALLURE_DIR"
echo "  Build log     : $BUILD_LOG"
echo "  Run log       : $PROJECT_ROOT/kaspresso_run.log"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

exit $EXIT_CODE
