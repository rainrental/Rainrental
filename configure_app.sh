#!/bin/bash

# RainRental RFID App Configuration Script
# This script renames the app package, app name, and configures the app based on .env variables

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check required tools
check_requirements() {
    print_status "Checking requirements..."
    
    if ! command_exists java; then
        print_error "Java is not installed. Please install Java JDK."
        exit 1
    fi
    
    if ! command_exists adb; then
        print_warning "ADB is not installed. Device deployment will be disabled."
        ADB_AVAILABLE=false
    else
        ADB_AVAILABLE=true
    fi
    
    print_success "Requirements check completed"
}

# Load environment variables
load_env() {
    print_status "Loading environment variables..."
    
    if [ ! -f ".env" ]; then
        print_error ".env file not found. Creating template..."
        cat > .env << EOF
# App Configuration
PACKAGE_NAME=com.yourcompany.yourapp
APP_NAME=Your RFID App
COMPANY_ID=your_company_id

# Optional: Device deployment
DEPLOY_TO_DEVICE=false
BUILD_DEBUG=true
BUILD_RELEASE=true
EOF
        print_warning "Please edit .env file with your configuration and run the script again."
        exit 1
    fi
    
    # Load .env file
    export $(cat .env | grep -v '^#' | xargs)
    
    # Validate required variables
    if [ -z "$PACKAGE_NAME" ]; then
        print_error "PACKAGE_NAME is required in .env file"
        exit 1
    fi
    
    if [ -z "$APP_NAME" ]; then
        print_error "APP_NAME is required in .env file"
        exit 1
    fi
    
    if [ -z "$COMPANY_ID" ]; then
        print_error "COMPANY_ID is required in .env file"
        exit 1
    fi
    
    print_success "Environment variables loaded"
    print_status "Package: $PACKAGE_NAME"
    print_status "App Name: $APP_NAME"
    print_status "Company ID: $COMPANY_ID"
}

# Convert package name to directory structure
package_to_path() {
    echo "$1" | sed 's/\./\//g'
}

# Convert package name to namespace (for Kotlin)
package_to_namespace() {
    echo "$1"
}

# Convert app name to safe filename
app_name_to_safe() {
    echo "$1" | sed 's/[^a-zA-Z0-9]/_/g'
}

# Backup original files
backup_original() {
    print_status "Creating backup of original files..."
    
    if [ ! -d "backup" ]; then
        mkdir -p backup
    fi
    
    # Backup key files
    cp app/build.gradle.kts backup/
    cp app/src/main/AndroidManifest.xml backup/
    cp app/src/main/res/values/strings.xml backup/
    cp app/src/main/res/values/themes.xml backup/
    cp app/google-services.json backup/
    cp settings.gradle.kts backup/
    cp README.md backup/
    
    print_success "Backup created in backup/ directory"
}

# Rename package structure
rename_package_structure() {
    print_status "Renaming package structure..."
    
    OLD_PACKAGE="org.rainrental.rainrentalrfid"
    NEW_PACKAGE="$PACKAGE_NAME"
    OLD_PATH="app/src/main/java/org/rainrental/rainrentalrfid"
    NEW_PATH="app/src/main/java/$(package_to_path $NEW_PACKAGE)"
    
    # Create new package directory
    mkdir -p "$NEW_PATH"
    
    # Copy files to new package structure
    cp -r "$OLD_PATH"/* "$NEW_PATH/"
    
    # Remove old package directory
    rm -rf "$OLD_PATH"
    
    # Handle test directories
    OLD_TEST_PATH="app/src/test/java/org/rainrental/rainrentalrfid"
    NEW_TEST_PATH="app/src/test/java/$(package_to_path $NEW_PACKAGE)"
    
    if [ -d "$OLD_TEST_PATH" ]; then
        mkdir -p "$NEW_TEST_PATH"
        cp -r "$OLD_TEST_PATH"/* "$NEW_TEST_PATH/"
        rm -rf "$OLD_TEST_PATH"
    fi
    
    # Handle androidTest directories
    OLD_ANDROID_TEST_PATH="app/src/androidTest/java/org/rainrental/rainrentalrfid"
    NEW_ANDROID_TEST_PATH="app/src/androidTest/java/$(package_to_path $NEW_PACKAGE)"
    
    if [ -d "$OLD_ANDROID_TEST_PATH" ]; then
        mkdir -p "$NEW_ANDROID_TEST_PATH"
        cp -r "$OLD_ANDROID_TEST_PATH"/* "$NEW_ANDROID_TEST_PATH/"
        rm -rf "$OLD_ANDROID_TEST_PATH"
    fi
    
    print_success "Package structure renamed"
}

# Update package declarations and imports
update_package_references() {
    print_status "Updating package references..."
    
    OLD_PACKAGE="org.rainrental.rainrentalrfid"
    NEW_PACKAGE="$PACKAGE_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update package declarations in all Kotlin files
    find app/src -name "*.kt" -exec sed -i '' "s/package $OLD_PACKAGE/package $NEW_PACKAGE/g" {} \;
    
    # Update import statements
    find app/src -name "*.kt" -exec sed -i '' "s/import $OLD_PACKAGE/import $NEW_PACKAGE/g" {} \;
    
    # Update R class references
    find app/src -name "*.kt" -exec sed -i '' "s/import $OLD_PACKAGE.R/import $NEW_PACKAGE.R/g" {} \;
    
    # Update test assertions
    find app/src -name "*.kt" -exec sed -i '' "s/assertEquals(\"$OLD_PACKAGE\"/assertEquals(\"$NEW_PACKAGE\"/g" {} \;
    
    print_success "Package references updated"
}

# Update build configuration
update_build_config() {
    print_status "Updating build configuration..."
    
    NEW_PACKAGE="$PACKAGE_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update build.gradle.kts
    sed -i '' "s/namespace = \"org.rainrental.rainrentalrfid\"/namespace = \"$NEW_PACKAGE\"/g" app/build.gradle.kts
    sed -i '' "s/applicationId = \"org.rainrental.rainrentalrfid\"/applicationId = \"$NEW_PACKAGE\"/g" app/build.gradle.kts
    
    # Update settings.gradle.kts
    sed -i '' "s/rootProject.name = \"RainRentalRfid\"/rootProject.name = \"$SAFE_APP_NAME\"/g" settings.gradle.kts
    
    print_success "Build configuration updated"
}

# Update AndroidManifest.xml
update_manifest() {
    print_status "Updating AndroidManifest.xml..."
    
    NEW_PACKAGE="$PACKAGE_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update application name
    sed -i '' "s/android:name=\".app.RainRentalRfidApp\"/android:name=\".app.${SAFE_APP_NAME}App\"/g" app/src/main/AndroidManifest.xml
    
    # Update theme references
    sed -i '' "s/@style\/Theme.RainRentalRfid/@style\/Theme.$SAFE_APP_NAME/g" app/src/main/AndroidManifest.xml
    
    print_success "AndroidManifest.xml updated"
}

# Update string resources
update_strings() {
    print_status "Updating string resources..."
    
    # Update app name
    sed -i '' "s/<string name=\"app_name\">.*<\/string>/<string name=\"app_name\">$APP_NAME<\/string>/g" app/src/main/res/values/strings.xml
    
    # Add company_id if it doesn't exist
    if ! grep -q "company_id" app/src/main/res/values/strings.xml; then
        # Add after the last string tag
        sed -i '' "s/<\/resources>/    <string name=\"company_id\">$COMPANY_ID<\/string>\n<\/resources>/g" app/src/main/res/values/strings.xml
    else
        # Update existing company_id
        sed -i '' "s/<string name=\"company_id\">.*<\/string>/<string name=\"company_id\">$COMPANY_ID<\/string>/g" app/src/main/res/values/strings.xml
    fi
    
    print_success "String resources updated"
}

# Update theme
update_theme() {
    print_status "Updating theme..."
    
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update theme name in themes.xml
    sed -i '' "s/<style name=\"Theme.RainRentalRfid\"/<style name=\"Theme.$SAFE_APP_NAME\"/g" app/src/main/res/values/themes.xml
    
    # Update theme function name in Theme.kt
    sed -i '' "s/fun RainRentalRfidTheme(/fun ${SAFE_APP_NAME}Theme(/g" app/src/main/java/$(package_to_path $PACKAGE_NAME)/ui/theme/Theme.kt
    
    # Update theme references in all Kotlin files
    find app/src -name "*.kt" -exec sed -i '' "s/RainRentalRfidTheme/${SAFE_APP_NAME}Theme/g" {} \;
    
    print_success "Theme updated"
}

# Update app class name
update_app_class() {
    print_status "Updating app class name..."
    
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Rename the app class file
    mv "app/src/main/java/$(package_to_path $PACKAGE_NAME)/app/RainRentalRfidApp.kt" "app/src/main/java/$(package_to_path $PACKAGE_NAME)/app/${SAFE_APP_NAME}App.kt"
    
    # Update class name in the file
    sed -i '' "s/class RainRentalRfidApp/class ${SAFE_APP_NAME}App/g" "app/src/main/java/$(package_to_path $PACKAGE_NAME)/app/${SAFE_APP_NAME}App.kt"
    
    print_success "App class name updated"
}

# Update Google Services configuration
update_google_services() {
    print_status "Updating Google Services configuration..."
    
    NEW_PACKAGE="$PACKAGE_NAME"
    
    # Update package name in google-services.json
    sed -i '' "s/\"package_name\": \"org.rainrental.rainrentalrfid\"/\"package_name\": \"$NEW_PACKAGE\"/g" app/google-services.json
    
    print_success "Google Services configuration updated"
}

# Update README
update_readme() {
    print_status "Updating README..."
    
    # Update README title
    sed -i '' "s/# RainRental RFID/# $APP_NAME/g" README.md
    
    print_success "README updated"
}

# Build the app
build_app() {
    print_status "Building application..."
    
    # Clean build
    ./gradlew clean
    
    # Build debug APK
    if [ "$BUILD_DEBUG" = "true" ]; then
        print_status "Building debug APK..."
        ./gradlew assembleDebug
        if [ $? -eq 0 ]; then
            print_success "Debug APK built successfully"
        else
            print_error "Debug build failed"
            exit 1
        fi
    fi
    
    # Build release APK
    if [ "$BUILD_RELEASE" = "true" ]; then
        print_status "Building release APK..."
        ./gradlew assembleRelease
        if [ $? -eq 0 ]; then
            print_success "Release APK built successfully"
        else
            print_error "Release build failed"
            exit 1
        fi
    fi
    
    print_success "Build completed"
}

# Deploy to device
deploy_to_device() {
    if [ "$DEPLOY_TO_DEVICE" = "true" ] && [ "$ADB_AVAILABLE" = "true" ]; then
        print_status "Deploying to connected device..."
        
        # Check for connected devices
        DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
        
        if [ "$DEVICES" -eq 0 ]; then
            print_warning "No devices connected. Skipping deployment."
            return
        fi
        
        # Install debug APK
        if [ "$BUILD_DEBUG" = "true" ]; then
            print_status "Installing debug APK..."
            ./gradlew installDebug
            if [ $? -eq 0 ]; then
                print_success "Debug APK installed successfully"
            else
                print_error "Debug APK installation failed"
            fi
        fi
        
        print_success "Deployment completed"
    else
        print_status "Skipping deployment (DEPLOY_TO_DEVICE=false or ADB not available)"
    fi
}

# Main execution
main() {
    print_status "Starting app configuration..."
    
    check_requirements
    load_env
    backup_original
    rename_package_structure
    update_package_references
    update_build_config
    update_manifest
    update_strings
    update_theme
    update_app_class
    update_google_services
    update_readme
    build_app
    deploy_to_device
    
    print_success "App configuration completed successfully!"
    print_status "APK files are in app/build/outputs/apk/"
    print_status "Backup files are in backup/"
}

# Run main function
main "$@" 