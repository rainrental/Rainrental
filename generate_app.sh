#!/bin/bash

# RainRental RFID App Generator
# This script creates a new customized app folder without modifying the original

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

# Output Configuration
OUTPUT_DIR=../generated-app
PROJECT_NAME=MyRfidApp

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
    
    # Set default output directory if not specified
    if [ -z "$OUTPUT_DIR" ]; then
        OUTPUT_DIR="../generated-app"
    fi
    
    # Set default project name if not specified
    if [ -z "$PROJECT_NAME" ]; then
        PROJECT_NAME=$(echo "$APP_NAME" | sed 's/[^a-zA-Z0-9]/_/g')
    fi
    
    print_success "Environment variables loaded"
    print_status "Package: $PACKAGE_NAME"
    print_status "App Name: $APP_NAME"
    print_status "Company ID: $COMPANY_ID"
    print_status "Output Directory: $OUTPUT_DIR"
    print_status "Project Name: $PROJECT_NAME"
}

# Convert package name to directory structure
package_to_path() {
    echo "$1" | sed 's/\./\//g'
}

# Convert app name to safe filename
app_name_to_safe() {
    echo "$1" | sed 's/[^a-zA-Z0-9]/_/g'
}

# Create new project directory
create_project_directory() {
    print_status "Creating new project directory..."
    
    # Create output directory
    mkdir -p "$OUTPUT_DIR"
    
    # Create project directory
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    if [ -d "$PROJECT_PATH" ]; then
        print_warning "Project directory already exists. Removing..."
        rm -rf "$PROJECT_PATH"
    fi
    
    mkdir -p "$PROJECT_PATH"
    
    print_success "Project directory created: $PROJECT_PATH"
}

# Copy project files
copy_project_files() {
    print_status "Copying project files..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    # Copy all project files except build artifacts and IDE files
    rsync -av --exclude='.gradle' \
              --exclude='app/build' \
              --exclude='.idea' \
              --exclude='backup' \
              --exclude='*.iml' \
              --exclude='.env' \
              --exclude='env.example' \
              --exclude='configure_app.sh' \
              --exclude='generate_app.sh' \
              --exclude='test_configuration.sh' \
              --exclude='Makefile' \
              --exclude='QUICKSTART.md' \
              --exclude='.git' \
              ./ "$PROJECT_PATH/"
    
    print_success "Project files copied"
}

# Rename package structure in new project
rename_package_structure() {
    print_status "Renaming package structure..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    OLD_PACKAGE="org.rainrental.rainrentalrfid"
    NEW_PACKAGE="$PACKAGE_NAME"
    OLD_PATH="$PROJECT_PATH/app/src/main/java/org/rainrental/rainrentalrfid"
    NEW_PATH="$PROJECT_PATH/app/src/main/java/$(package_to_path $NEW_PACKAGE)"
    
    # Create new package directory
    mkdir -p "$NEW_PATH"
    
    # Copy files to new package structure
    cp -r "$OLD_PATH"/* "$NEW_PATH/"
    
    # Remove old package directory
    rm -rf "$OLD_PATH"
    
    # Handle test directories
    OLD_TEST_PATH="$PROJECT_PATH/app/src/test/java/org/rainrental/rainrentalrfid"
    NEW_TEST_PATH="$PROJECT_PATH/app/src/test/java/$(package_to_path $NEW_PACKAGE)"
    
    if [ -d "$OLD_TEST_PATH" ]; then
        mkdir -p "$NEW_TEST_PATH"
        cp -r "$OLD_TEST_PATH"/* "$NEW_TEST_PATH/"
        rm -rf "$OLD_TEST_PATH"
    fi
    
    # Handle androidTest directories
    OLD_ANDROID_TEST_PATH="$PROJECT_PATH/app/src/androidTest/java/org/rainrental/rainrentalrfid"
    NEW_ANDROID_TEST_PATH="$PROJECT_PATH/app/src/androidTest/java/$(package_to_path $NEW_PACKAGE)"
    
    if [ -d "$OLD_ANDROID_TEST_PATH" ]; then
        mkdir -p "$NEW_ANDROID_TEST_PATH"
        cp -r "$OLD_ANDROID_TEST_PATH"/* "$NEW_ANDROID_TEST_PATH/"
        rm -rf "$OLD_ANDROID_TEST_PATH"
    fi
    
    print_success "Package structure renamed"
}

# Update package declarations and imports in new project
update_package_references() {
    print_status "Updating package references..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    OLD_PACKAGE="org.rainrental.rainrentalrfid"
    NEW_PACKAGE="$PACKAGE_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update package declarations in all Kotlin files
    find "$PROJECT_PATH/app/src" -name "*.kt" -exec sed -i '' "s/package $OLD_PACKAGE/package $NEW_PACKAGE/g" {} \;
    
    # Update import statements
    find "$PROJECT_PATH/app/src" -name "*.kt" -exec sed -i '' "s/import $OLD_PACKAGE/import $NEW_PACKAGE/g" {} \;
    
    # Update R class references
    find "$PROJECT_PATH/app/src" -name "*.kt" -exec sed -i '' "s/import $OLD_PACKAGE.R/import $NEW_PACKAGE.R/g" {} \;
    
    # Update test assertions
    find "$PROJECT_PATH/app/src" -name "*.kt" -exec sed -i '' "s/assertEquals(\"$OLD_PACKAGE\"/assertEquals(\"$NEW_PACKAGE\"/g" {} \;
    
    print_success "Package references updated"
}

# Update build configuration in new project
update_build_config() {
    print_status "Updating build configuration..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    NEW_PACKAGE="$PACKAGE_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update build.gradle.kts
    sed -i '' "s/namespace = \"org.rainrental.rainrentalrfid\"/namespace = \"$NEW_PACKAGE\"/g" "$PROJECT_PATH/app/build.gradle.kts"
    sed -i '' "s/applicationId = \"org.rainrental.rainrentalrfid\"/applicationId = \"$NEW_PACKAGE\"/g" "$PROJECT_PATH/app/build.gradle.kts"
    
    # Update settings.gradle.kts
    sed -i '' "s/rootProject.name = \"RainRentalRfid\"/rootProject.name = \"$SAFE_APP_NAME\"/g" "$PROJECT_PATH/settings.gradle.kts"
    
    print_success "Build configuration updated"
}

# Update AndroidManifest.xml in new project
update_manifest() {
    print_status "Updating AndroidManifest.xml..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    NEW_PACKAGE="$PACKAGE_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update application name
    sed -i '' "s/android:name=\".app.RainRentalRfidApp\"/android:name=\".app.${SAFE_APP_NAME}App\"/g" "$PROJECT_PATH/app/src/main/AndroidManifest.xml"
    
    # Update theme references
    sed -i '' "s/@style\/Theme.RainRentalRfid/@style\/Theme.$SAFE_APP_NAME/g" "$PROJECT_PATH/app/src/main/AndroidManifest.xml"
    
    print_success "AndroidManifest.xml updated"
}

# Update string resources in new project
update_strings() {
    print_status "Updating string resources..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    # Update app name
    sed -i '' "s/<string name=\"app_name\">.*<\/string>/<string name=\"app_name\">$APP_NAME<\/string>/g" "$PROJECT_PATH/app/src/main/res/values/strings.xml"
    
    # Add company_id if it doesn't exist
    if ! grep -q "company_id" "$PROJECT_PATH/app/src/main/res/values/strings.xml"; then
        # Add after the last string tag
        sed -i '' "s/<\/resources>/    <string name=\"company_id\">$COMPANY_ID<\/string>\n<\/resources>/g" "$PROJECT_PATH/app/src/main/res/values/strings.xml"
    else
        # Update existing company_id
        sed -i '' "s/<string name=\"company_id\">.*<\/string>/<string name=\"company_id\">$COMPANY_ID<\/string>/g" "$PROJECT_PATH/app/src/main/res/values/strings.xml"
    fi
    
    print_success "String resources updated"
}

# Update theme in new project
update_theme() {
    print_status "Updating theme..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Update theme name in themes.xml
    sed -i '' "s/<style name=\"Theme.RainRentalRfid\"/<style name=\"Theme.$SAFE_APP_NAME\"/g" "$PROJECT_PATH/app/src/main/res/values/themes.xml"
    
    # Update theme function name in Theme.kt
    sed -i '' "s/fun RainRentalRfidTheme(/fun ${SAFE_APP_NAME}Theme(/g" "$PROJECT_PATH/app/src/main/java/$(package_to_path $PACKAGE_NAME)/ui/theme/Theme.kt"
    
    # Update theme references in all Kotlin files
    find "$PROJECT_PATH/app/src" -name "*.kt" -exec sed -i '' "s/RainRentalRfidTheme/${SAFE_APP_NAME}Theme/g" {} \;
    
    print_success "Theme updated"
}

# Update app class name in new project
update_app_class() {
    print_status "Updating app class name..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    SAFE_APP_NAME=$(app_name_to_safe "$APP_NAME")
    
    # Rename the app class file
    mv "$PROJECT_PATH/app/src/main/java/$(package_to_path $PACKAGE_NAME)/app/RainRentalRfidApp.kt" "$PROJECT_PATH/app/src/main/java/$(package_to_path $PACKAGE_NAME)/app/${SAFE_APP_NAME}App.kt"
    
    # Update class name in the file
    sed -i '' "s/class RainRentalRfidApp/class ${SAFE_APP_NAME}App/g" "$PROJECT_PATH/app/src/main/java/$(package_to_path $PACKAGE_NAME)/app/${SAFE_APP_NAME}App.kt"
    
    print_success "App class name updated"
}

# Update Google Services configuration in new project
update_google_services() {
    print_status "Updating Google Services configuration..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    NEW_PACKAGE="$PACKAGE_NAME"
    
    # Update package name in google-services.json
    sed -i '' "s/\"package_name\": \"org.rainrental.rainrentalrfid\"/\"package_name\": \"$NEW_PACKAGE\"/g" "$PROJECT_PATH/app/google-services.json"
    
    print_success "Google Services configuration updated"
}

# Update README in new project
update_readme() {
    print_status "Updating README..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    # Update README title
    sed -i '' "s/# RainRental RFID/# $APP_NAME/g" "$PROJECT_PATH/README.md"
    
    print_success "README updated"
}

# Create new configuration files for the generated project
create_config_files() {
    print_status "Creating configuration files for new project..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    # Create .env file for the new project
    cat > "$PROJECT_PATH/.env" << EOF
# Generated App Configuration
PACKAGE_NAME=$PACKAGE_NAME
APP_NAME=$APP_NAME
COMPANY_ID=$COMPANY_ID

# Build Configuration
BUILD_DEBUG=true
BUILD_RELEASE=true

# Device Deployment
DEPLOY_TO_DEVICE=false
EOF
    
    # Create a simple build script for the new project
    cat > "$PROJECT_PATH/build.sh" << 'EOF'
#!/bin/bash

# Simple build script for generated app

echo "Building $APP_NAME..."

# Clean and build
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease

echo "Build complete! APKs are in app/build/outputs/apk/"
EOF
    
    chmod +x "$PROJECT_PATH/build.sh"
    
    # Create a deployment script
    cat > "$PROJECT_PATH/deploy.sh" << 'EOF'
#!/bin/bash

# Deploy to connected device

echo "Deploying to device..."

# Check for connected devices
DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)

if [ "$DEVICES" -eq 0 ]; then
    echo "No devices connected. Please connect a device and try again."
    exit 1
fi

# Install debug APK
./gradlew installDebug

echo "Deployment complete!"
EOF
    
    chmod +x "$PROJECT_PATH/deploy.sh"
    
    print_success "Configuration files created"
}

# Build the app in the new project
build_app() {
    print_status "Building application in new project..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    # Change to project directory
    cd "$PROJECT_PATH"
    
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
    
    # Return to original directory
    cd - > /dev/null
    
    print_success "Build completed"
}

# Deploy to device from new project
deploy_to_device() {
    if [ "$DEPLOY_TO_DEVICE" = "true" ] && [ "$ADB_AVAILABLE" = "true" ]; then
        print_status "Deploying to connected device..."
        
        PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
        
        # Change to project directory
        cd "$PROJECT_PATH"
        
        # Check for connected devices
        DEVICES=$(adb devices | grep -v "List of devices" | grep "device$" | wc -l)
        
        if [ "$DEVICES" -eq 0 ]; then
            print_warning "No devices connected. Skipping deployment."
            cd - > /dev/null
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
        
        # Return to original directory
        cd - > /dev/null
        
        print_success "Deployment completed"
    else
        print_status "Skipping deployment (DEPLOY_TO_DEVICE=false or ADB not available)"
    fi
}

# Create project summary
create_summary() {
    print_status "Creating project summary..."
    
    PROJECT_PATH="$OUTPUT_DIR/$PROJECT_NAME"
    
    # Create a summary file
    cat > "$PROJECT_PATH/PROJECT_SUMMARY.md" << EOF
# Generated Project Summary

## Project Details
- **Project Name**: $PROJECT_NAME
- **Package Name**: $PACKAGE_NAME
- **App Name**: $APP_NAME
- **Company ID**: $COMPANY_ID
- **Generated**: $(date)

## Project Structure
\`\`\`
$PROJECT_NAME/
├── app/
│   ├── src/main/java/$(package_to_path $PACKAGE_NAME)/
│   ├── build.gradle.kts
│   └── google-services.json
├── gradle/
├── .env
├── build.sh
├── deploy.sh
└── README.md
\`\`\`

## Build Commands
\`\`\`bash
cd $PROJECT_NAME
./build.sh      # Build debug and release APKs
./deploy.sh     # Deploy to connected device
./gradlew clean # Clean build artifacts
\`\`\`

## APK Locations
- **Debug APK**: app/build/outputs/apk/debug/
- **Release APK**: app/build/outputs/apk/release/

## Next Steps
1. Open the project in Android Studio
2. Configure your Firebase project (update google-services.json)
3. Update API endpoints in app/src/main/res/values/strings.xml
4. Customize the UI theme in app/src/main/java/$(package_to_path $PACKAGE_NAME)/ui/theme/
5. Test on your RFID device

## Configuration
The project is pre-configured with your settings:
- Package: $PACKAGE_NAME
- App Name: $APP_NAME
- Company ID: $COMPANY_ID

All package references, imports, and class names have been updated automatically.
EOF
    
    print_success "Project summary created"
}

# Main execution
main() {
    print_status "Starting app generation..."
    
    check_requirements
    load_env
    create_project_directory
    copy_project_files
    rename_package_structure
    update_package_references
    update_build_config
    update_manifest
    update_strings
    update_theme
    update_app_class
    update_google_services
    update_readme
    create_config_files
    build_app
    deploy_to_device
    create_summary
    
    print_success "App generation completed successfully!"
    print_status "Generated project: $OUTPUT_DIR/$PROJECT_NAME"
    print_status "APK files are in: $OUTPUT_DIR/$PROJECT_NAME/app/build/outputs/apk/"
    print_status "Project summary: $OUTPUT_DIR/$PROJECT_NAME/PROJECT_SUMMARY.md"
    print_status ""
    print_status "Next steps:"
    print_status "1. cd $OUTPUT_DIR/$PROJECT_NAME"
    print_status "2. Open in Android Studio"
    print_status "3. Configure your Firebase project"
    print_status "4. Test on your RFID device"
}

# Run main function
main "$@" 