#!/bin/bash

# Test script for the configuration system
# This script tests the configuration functionality

set -e

echo "Testing RainRental RFID Configuration System"
echo "============================================="

# Test 1: Check if configure script exists
echo "Test 1: Checking configure script..."
if [ -f "configure_app.sh" ]; then
    echo "✅ configure_app.sh exists"
else
    echo "❌ configure_app.sh not found"
    exit 1
fi

# Test 2: Check if Makefile exists
echo "Test 2: Checking Makefile..."
if [ -f "Makefile" ]; then
    echo "✅ Makefile exists"
else
    echo "❌ Makefile not found"
    exit 1
fi

# Test 3: Check if env.example exists
echo "Test 3: Checking env.example..."
if [ -f "env.example" ]; then
    echo "✅ env.example exists"
else
    echo "❌ env.example not found"
    exit 1
fi

# Test 4: Check if gradlew exists
echo "Test 4: Checking gradlew..."
if [ -f "gradlew" ]; then
    echo "✅ gradlew exists"
else
    echo "❌ gradlew not found"
    exit 1
fi

# Test 5: Check if gradlew is executable
echo "Test 5: Checking gradlew permissions..."
if [ -x "gradlew" ]; then
    echo "✅ gradlew is executable"
else
    echo "❌ gradlew is not executable"
    exit 1
fi

# Test 6: Check if configure script is executable
echo "Test 6: Checking configure script permissions..."
if [ -x "configure_app.sh" ]; then
    echo "✅ configure_app.sh is executable"
else
    echo "❌ configure_app.sh is not executable"
    exit 1
fi

# Test 7: Check current app configuration
echo "Test 7: Checking current app configuration..."
CURRENT_PACKAGE=$(grep 'applicationId' app/build.gradle.kts | sed 's/.*= "\(.*\)"/\1/')
CURRENT_APP_NAME=$(grep 'app_name' app/src/main/res/values/strings.xml | sed 's/.*>\(.*\)<.*/\1/')
echo "Current package: $CURRENT_PACKAGE"
echo "Current app name: $CURRENT_APP_NAME"

# Test 8: Check if backup directory exists
echo "Test 8: Checking backup functionality..."
if [ -d "backup" ]; then
    echo "✅ backup directory exists"
else
    echo "ℹ️  backup directory will be created when needed"
fi

# Test 9: Check if build works
echo "Test 9: Testing build system..."
if ./gradlew clean > /dev/null 2>&1; then
    echo "✅ Gradle clean works"
else
    echo "❌ Gradle clean failed"
    exit 1
fi

echo ""
echo "All tests passed! ✅"
echo ""
echo "Configuration system is ready for use."
echo ""
echo "Next steps:"
echo "1. Copy env.example to .env"
echo "2. Edit .env with your configuration"
echo "3. Run: ./configure_app.sh"
echo "4. Or use: make configure" 