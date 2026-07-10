#!/bin/bash
# ============================================================
# WorkforceX - Employer App: Build iOS Framework
# Run this from: android/employer/ on a macOS machine
# Pre-requisite: JDK 17+ installed, Xcode command line tools installed
# ============================================================

set -e # Exit immediately if a command exits with a non-zero status.

echo ""
echo "========================================"
echo "  Building iOS Framework for Employer App  "
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
echo "Building the shared_employer XCFramework..."
./gradlew :shared_employer:assembleXCFramework

# ── Step 3: Locate the Framework ──────────────────────────────────────────────

FRAMEWORK_PATH="shared_employer/build/XCFrameworks/release/shared_employer.xcframework"

if [ -d "$FRAMEWORK_PATH" ]; then
    echo ""
    echo "✅ SUCCESS!"
    echo "Framework created at: $FRAMEWORK_PATH"
    echo ""
    echo "Next Steps:"
    echo "1. Create a new Xcode project for your iOS app (or open the existing one)."
    echo "2. Drag and drop the 'shared_employer.xcframework' bundle into your Xcode project's 'Frameworks, Libraries, and Embedded Content' section."
    echo "3. Import the framework in your Swift code with 'import shared_employer' and start using your shared networking logic!"
else
    echo ""
    echo "❌ BUILD FAILED."
    echo "Could not find the framework at the expected path."
fi
