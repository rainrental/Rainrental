# RainRental RFID App Makefile
# Alternative to configure_app.sh for users who prefer Make

.PHONY: help configure build debug release deploy clean backup restore

# Default target
help:
	@echo "RainRental RFID App Configuration"
	@echo ""
	@echo "Available targets:"
	@echo "  configure  - Configure app with .env settings"
	@echo "  build      - Build both debug and release APKs"
	@echo "  debug      - Build debug APK only"
	@echo "  release    - Build release APK only"
	@echo "  deploy     - Deploy to connected device"
	@echo "  clean      - Clean build artifacts"
	@echo "  backup     - Create backup of current state"
	@echo "  restore    - Restore from backup"
	@echo "  help       - Show this help message"
	@echo ""
	@echo "Usage:"
	@echo "  make configure  # Configure and build"
	@echo "  make deploy     # Build and deploy to device"

# Check if .env exists
check-env:
	@if [ ! -f .env ]; then \
		echo "Creating .env template..."; \
		echo "# App Configuration" > .env; \
		echo "PACKAGE_NAME=com.yourcompany.yourapp" >> .env; \
		echo "APP_NAME=Your RFID App" >> .env; \
		echo "COMPANY_ID=your_company_id" >> .env; \
		echo "" >> .env; \
		echo "# Optional: Device deployment" >> .env; \
		echo "DEPLOY_TO_DEVICE=false" >> .env; \
		echo "BUILD_DEBUG=true" >> .env; \
		echo "BUILD_RELEASE=true" >> .env; \
		echo "Please edit .env file and run 'make configure' again"; \
		exit 1; \
	fi

# Configure the app
configure: check-env
	@echo "Configuring app with .env settings..."
	@./configure_app.sh

# Build both debug and release
build: debug release

# Build debug APK
debug:
	@echo "Building debug APK..."
	@./gradlew assembleDebug

# Build release APK
release:
	@echo "Building release APK..."
	@./gradlew assembleRelease

# Deploy to connected device
deploy:
	@echo "Deploying to connected device..."
	@./gradlew installDebug

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	@./gradlew clean
	@rm -rf .gradle
	@rm -rf app/build

# Create backup
backup:
	@echo "Creating backup..."
	@mkdir -p backup/$(shell date +%Y%m%d_%H%M%S)
	@cp -r app/src backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp app/build.gradle.kts backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp app/src/main/AndroidManifest.xml backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp app/src/main/res/values/strings.xml backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp app/src/main/res/values/themes.xml backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp app/google-services.json backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp settings.gradle.kts backup/$(shell date +%Y%m%d_%H%M%S)/
	@cp README.md backup/$(shell date +%Y%m%d_%H%M%S)/
	@echo "Backup created in backup/$(shell date +%Y%m%d_%H%M%S)/"

# Restore from backup (specify BACKUP_DIR)
restore:
	@if [ -z "$(BACKUP_DIR)" ]; then \
		echo "Usage: make restore BACKUP_DIR=backup/YYYYMMDD_HHMMSS"; \
		exit 1; \
	fi
	@if [ ! -d "$(BACKUP_DIR)" ]; then \
		echo "Backup directory $(BACKUP_DIR) not found"; \
		exit 1; \
	fi
	@echo "Restoring from $(BACKUP_DIR)..."
	@cp -r $(BACKUP_DIR)/src/* app/src/
	@cp $(BACKUP_DIR)/build.gradle.kts app/
	@cp $(BACKUP_DIR)/AndroidManifest.xml app/src/main/
	@cp $(BACKUP_DIR)/strings.xml app/src/main/res/values/
	@cp $(BACKUP_DIR)/themes.xml app/src/main/res/values/
	@cp $(BACKUP_DIR)/google-services.json app/
	@cp $(BACKUP_DIR)/settings.gradle.kts ./
	@cp $(BACKUP_DIR)/README.md ./
	@echo "Restore completed"

# Check connected devices
devices:
	@echo "Connected devices:"
	@adb devices

# Show app info
info:
	@echo "App Information:"
	@echo "Package: $(shell grep 'applicationId' app/build.gradle.kts | sed 's/.*= "\(.*\)"/\1/')"
	@echo "App Name: $(shell grep 'app_name' app/src/main/res/values/strings.xml | sed 's/.*>\(.*\)<.*/\1/')"
	@echo "Company ID: $(shell grep 'company_id' app/src/main/res/values/strings.xml | sed 's/.*>\(.*\)<.*/\1/' 2>/dev/null || echo "Not set")"

# Install dependencies (for CI/CD)
install-deps:
	@echo "Installing dependencies..."
	@./gradlew --refresh-dependencies

# Run tests
test:
	@echo "Running tests..."
	@./gradlew test

# Run instrumented tests
android-test:
	@echo "Running instrumented tests..."
	@./gradlew connectedAndroidTest

# Generate APK report
report:
	@echo "Generating build report..."
	@echo "Build completed at: $(shell date)"
	@echo "Debug APK: $(shell find app/build/outputs/apk/debug -name "*.apk" 2>/dev/null || echo "Not found")"
	@echo "Release APK: $(shell find app/build/outputs/apk/release -name "*.apk" 2>/dev/null || echo "Not found")"
	@echo "APK sizes:"
	@ls -lh app/build/outputs/apk/*/ 2>/dev/null || echo "No APKs found" 