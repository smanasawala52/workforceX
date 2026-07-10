#!/bin/bash
# ============================================================
# WorkforceX - Worker App: Build iOS Framework
# Run this from: android/worker/ on a macOS machine
# Pre-requisite: JDK 17+ installed
# ============================================================

set -e # Exit immediately if a command exits with a non-zero status.

echo ""
echo "========================================"
echo "  Building iOS Framework for Worker App  "
echo "========================================"
echo ""

# ── Step 1: Check for JDK ─────────────────────────────────────────────────────

echo "Checking for Java..."
if ! command -v java &> /dev/null
then
    echo "ERROR: Java not found. Please install JDK 17 or higher."
    exit 1
fi
java -version

# ── Step 2: Run Gradle Task ───────────────────────────────────────────────────

echo ""
echo "Building the shared XCFramework..."
./gradlew :shared:assembleXCFramework

# ── Step 3: Locate the Framework ──────────────────────────────────────────────

FRAMEWORK_PATH="shared/build/XCFrameworks/release/shared.xcframework"

if [ -d "$FRAMEWORK_PATH" ]; then
    echo ""
    echo "✅ SUCCESS!"
    echo "Framework created at: $FRAMEWORK_PATH"
    echo ""
    echo "Next Steps:"
    echo "1. Create a new Xcode project for your iOS app."
    echo "2. Drag and drop the 'shared.xcframework' bundle into your Xcode project's 'Frameworks, Libraries, and Embedded Content' section."
    echo "3. Import the framework in your Swift code with 'import shared' and start using your shared networking logic!"
else
    echo ""
    echo "❌ BUILD FAILED."
    echo "Could not find the framework at the expected path."
fi
