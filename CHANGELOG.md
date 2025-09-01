# Changelog

All notable changes to the RainRental RFID project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.17] - 2024-12-19

### Fixed
- Fixed inventory UI layout to prevent buttons from being cut off
- Removed `Spacer(modifier = Modifier.weight(1f))` that was pushing buttons below visible area
- Changed layout to use `Arrangement.spacedBy(16.dp)` for consistent vertical spacing
- Buttons now appear immediately after barcode prompt instead of at bottom of screen
- Added debug logging to track UI flow states

### Added
- Manual barcode entry feature to inventory function
- "Inventory All" function to capture all company assets
- New use cases: `StartInventoryAllUseCase`, `StopInventoryAllUseCase`, `LogInventoryAllUseCase`
- Updated `InventoryFlow` states to support new features
- Enhanced `InventoryViewModel` to handle manual entry and inventory all events

### Changed
- Updated `LogInventoryRequestDto` to support flexible inventory logging
- Enhanced `InventoryRepository` with new UI flow states and events
- Improved inventory screen layout with better spacing and visibility

## [1.0.15] - 2024-12-19

### Fixed
- Fixed main menu layout to prevent squashing
- Changed from `Arrangement.Center` to `Arrangement.Top` for better spacing
- Added proper top spacing (32dp) to account for navigation bar
- Increased space between logo and buttons (48dp)
- Reduced button spacing from 26dp to 20dp for better fit
- Improved overall menu layout and readability

### Removed
- Deleted `PulsatingCircles.kt` file (replaced by `RfidScanningAnimation`)

## [1.0.14] - 2024-12-19

### Added
- New black app icon with RFID-themed design
- Black launch background for professional appearance
- Central RFID chip with scanning waves and antenna lines
- Consistent black theme across light and dark modes

### Changed
- Updated app theme to use black background for launch screen
- Configured status bar and navigation bar to be black
- Replaced default green robot icon with professional RFID scanning icon

## [1.0.13] - 2024-12-19

### Changed
- Enhanced continuous scanning animation - faster and larger
- Reduced scan pulse animation duration from 1500ms to 800ms (47% faster)
- Reduced tag pulse animation duration from 800ms to 400ms (50% faster)
- Increased overall animation size from 160dp to 200dp (25% larger)
- Increased scanning rings sizes: outer 140dp→180dp, middle 100dp→130dp, inner 60dp→80dp
- Increased center RFID icon from 40dp to 50dp with larger 30dp icon
- Increased text sizes and spacing for better visibility
- Increased overall container height from 220dp to 280dp

## [1.0.12] - 2024-12-19

### Fixed
- Fixed settings persistence for 'Ignore right side key' setting
- Added proper SharedPreferences persistence to `setIgnoreRightSideKey` method
- Updated `isRightSideKeyIgnored` to load from SharedPreferences on app restart
- Fixed `SettingsViewModel` to pass context to `AppConfig` methods
- Ensure setting persists across app restarts

## [1.0.11] - 2024-12-19

### Fixed
- Fixed top navigation bar layout and add back navigation
- Added proper status bar padding to prevent overlap with system UI
- Implemented back navigation for settings screen (replaces app title with back button)
- Hide settings button when on settings screen to avoid confusion
- Use AutoMirrored ArrowBack icon for proper RTL support

## [1.0.10] - 2024-12-19

### Fixed
- Fix URL construction to handle missing trailing slashes
- Added `NetworkUtils.constructUrl()` utility function for proper URL construction
- Fixed `ApiModule` to ensure all base URLs end with trailing slash for Retrofit
- Updated `UpdateRepository` to use `NetworkUtils.constructUrl()` for proper URL handling
- Ensures URLs work regardless of whether base URL has trailing slash or not

## [1.0.9] - 2024-12-19

### Fixed
- Fix update version display and add verbose logging
- Fix settings page to show actual installed app version instead of `BuildConfig.VERSION_NAME`
- Add verbose logging to `UpdateManager` and `UpdateRepository` for debugging
- Fix update check logic to properly find latest available version
- Add proper feedback when no updates are available
- Move version detection to ViewModel to avoid composable context issues

## [1.0.8] - 2024-12-19

### Added
- New "Tag Lookup" feature with menu route
- Detect trigger up from user and start scanning for a single RFID tag
- Lookup asset using the tag ID (TID)
- Display found assets using the `AssetView` component
- Show "Asset Not Found" message if tag is not associated to anything
- Implemented using solid principles with proper architecture

### Changed
- Replaced `PulsatingCircles` animation with dynamic `RfidScanningAnimation`
- New animation is more representative of RFID scanning with concentric circles
- Displays unique tag count, RSSI, and last scanned TID
- More engaging and informative scanning visualization

## [1.0.7] - 2024-12-19

### Added
- Device type and company ID to authentication requests
- Enhanced settings UI with tab-based navigation
- Integrated button test functionality into settings
- Added authentication details and revoke functionality
- Improved string resource localization

## [1.0.6] - 2024-12-19

### Changed
- Refactored settings page with tab-style sub-navigation
- Removed "Button Test" menu option and integrated into settings
- Added authentication section with current user details
- Added revoke authentication functionality with confirmation dialog
- Improved UI layout and spacing

## [1.0.5] - 2024-12-19

### Changed
- Localized login screen to use company name from string resources
- Improved UI consistency and maintainability

## [1.0.4] - 2024-12-19

### Added
- MIT License to the repository
- Improved project documentation

## [1.0.3] - 2024-12-19

### Changed
- Enhanced .gitignore to exclude build artifacts and caches
- Improved repository structure for better maintainability

## [1.0.2] - 2024-12-19

### Changed
- Patch increment for version management

## [1.0.1] - 2024-12-19

### Changed
- Improved main layout with compact status icons
- Added proper top navigation bar with settings integration
- Enhanced hardware status indicators with color coding
- Removed large text indicators for better UX
- Fixed auto-update system with proper version display
- Added authentication API improvements with device type and company ID

## [1.0.0] - 2024-12-19

### Added
- Initial release
- RFID tag reading and writing
- Barcode scanning
- Inventory management
- Real-time MQTT synchronization
- Commission management
- Tag hunting functionality
- Continuous scanning with MQTT delivery
- Authentication system
- Modern Jetpack Compose UI

---

## Version Code Encoding

The app uses a specific version code encoding system:
- **Version Code**: Numeric identifier for app stores (must increase with each release)
- **Version Name**: Human-readable version string

**Encoding Rules**:
- `1.0.7` → `versionCode = 10007`
- `1.12.7` → `versionCode = 11207`
- `13.1.1` → `versionCode = 130101`

**Format**: `MMmmpp` where:
- `MM` = Major version (2 digits)
- `mm` = Minor version (2 digits) 
- `pp` = Patch version (2 digits)

Example in `build.gradle.kts`:
```kotlin
defaultConfig {
    versionCode = 10001  // For version 1.0.1
    versionName = "1.0.1"
}
```
