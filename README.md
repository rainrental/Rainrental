# RainRental RFID

A comprehensive Android RFID application built with Kotlin, Jetpack Compose, and Hilt dependency injection. This app provides RFID tag reading, barcode scanning, inventory management, and real-time data synchronization capabilities.

## Features

- **RFID Tag Reading**: Support for Chainway C72 and similar RFID devices
- **Barcode Scanning**: Integrated barcode scanning functionality
- **Inventory Management**: Track and manage inventory items
- **Real-time Sync**: MQTT-based real-time data synchronization
- **Commission Management**: Tag commissioning and asset tracking
- **Modern UI**: Built with Jetpack Compose for a modern user experience
- **Hilt DI**: Clean architecture with dependency injection

## Quick Start

### Prerequisites

- Android Studio Arctic Fox or later
- Java JDK 11 or later
- Android SDK (API 30+)
- Chainway C72 device (or similar RFID device)

### Configuration

#### Option 1: Non-Destructive Generation (Recommended)

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/rainrental-rfid.git
   cd rainrental-rfid
   ```

2. **Generate your customized app**:
   ```bash
   ./generate_app.sh
   ```

   The script will create a `.env` file if it doesn't exist. Edit the `.env` file with your configuration:

   ```env
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
   ```

3. **Run the generation script again**:
   ```bash
   ./generate_app.sh
   ```

   This creates a completely new project folder with your customizations!

#### Option 2: In-Place Configuration

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/rainrental-rfid.git
   cd rainrental-rfid
   ```

2. **Configure your app**:
   ```bash
   ./configure_app.sh
   ```

   The script will create a `.env` file if it doesn't exist. Edit the `.env` file with your configuration:

   ```env
   # App Configuration
   PACKAGE_NAME=com.yourcompany.yourapp
   APP_NAME=Your RFID App
   COMPANY_ID=your_company_id

   # Optional: Device deployment
   DEPLOY_TO_DEVICE=false
   BUILD_DEBUG=true
   BUILD_RELEASE=true
   ```

3. **Run the configuration script again**:
   ```bash
   ./configure_app.sh
   ```

### What the Scripts Do

#### `generate_app.sh` (Recommended - Non-Destructive)
The `generate_app.sh` script automatically:

- ✅ **Creates a new project folder** with your customizations
- ✅ **Copies all source files** to the new location
- ✅ **Renames the package** from `org.rainrental.rainrentalrfid` to your custom package
- ✅ **Updates the app name** in strings and manifest
- ✅ **Sets your company ID** in string resources
- ✅ **Renames all folders** and updates import statements
- ✅ **Updates class names** and theme references
- ✅ **Modifies build configuration** (namespace, applicationId)
- ✅ **Updates Google Services** configuration
- ✅ **Builds debug and release APKs**
- ✅ **Deploys to connected devices** (optional)
- ✅ **Creates project summary** with next steps
- ✅ **Generates build scripts** for the new project

#### `configure_app.sh` (In-Place)
The `configure_app.sh` script automatically:

- ✅ **Renames the package** from `org.rainrental.rainrentalrfid` to your custom package
- ✅ **Updates the app name** in strings and manifest
- ✅ **Sets your company ID** in string resources
- ✅ **Renames all folders** and updates import statements
- ✅ **Updates class names** and theme references
- ✅ **Modifies build configuration** (namespace, applicationId)
- ✅ **Updates Google Services** configuration
- ✅ **Builds debug and release APKs**
- ✅ **Deploys to connected devices** (optional)
- ✅ **Creates backups** of original files

### Manual Build

If you prefer to build manually:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Install to connected device
./gradlew installDebug
```

## Project Structure

```
app/src/main/java/org/rainrental/rainrentalrfid/
├── apis/                    # API and network layer
├── app/                     # Main application classes
├── audio/                   # Audio service implementation
├── chainway/                # Chainway device integration
├── commission/              # Commission management
├── di/                      # Dependency injection modules
├── home/                    # Home screen
├── hunt/                    # Tag hunting functionality
├── inputmanager/            # Input handling
├── inventory/               # Inventory management
├── continuousScanning/      # MQTT real-time sync
├── rainrental/              # RainRental specific APIs
├── regex/                   # Regex utilities
├── result/                  # Result handling
├── shared/                  # Shared components
├── toast/                   # Toast notifications
├── ui/                      # UI theme and components
└── unified/                 # Unified API layer
```

## Configuration Options

### Environment Variables

| Variable | Description | Example |
|----------|-------------|---------|
| `PACKAGE_NAME` | Your app's package name | `com.mycompany.rfidapp` |
| `APP_NAME` | Display name of your app | `My RFID Scanner` |
| `COMPANY_ID` | Your company identifier | `my_company_123` |
| `DEPLOY_TO_DEVICE` | Auto-deploy to connected device | `true` or `false` |
| `BUILD_DEBUG` | Build debug APK | `true` or `false` |
| `BUILD_RELEASE` | Build release APK | `true` or `false` |

### Customization

#### Changing the Logo

To change the app logo:

1. Replace the logo files in `app/src/main/res/mipmap-*`
2. Update the launcher icons in `app/src/main/res/drawable/`
3. Update the app name in `app/src/main/res/values/strings.xml`

#### Adding Custom APIs

1. Add your API endpoints in the appropriate package under `apis/`
2. Update the DI modules in `di/` to provide your services
3. Update the repository implementations to use your APIs

#### Modifying the UI Theme

1. Update colors in `app/src/main/java/your/package/ui/theme/Color.kt`
2. Modify the theme in `app/src/main/java/your/package/ui/theme/Theme.kt`
3. Update typography in `app/src/main/java/your/package/ui/theme/Type.kt`

## Device Support

### Chainway C72

This app is specifically designed for the Chainway C72 RFID device. Key features:

- **Hardware Integration**: Direct access to RFID reader and barcode scanner
- **Button Mapping**: Customizable hardware button controls
- **Audio Feedback**: Beep sounds for successful/failed operations
- **Orientation Management**: Automatic screen orientation handling

### Other Devices

To support other RFID devices:

1. Implement the device-specific interfaces in `chainway/data/`
2. Update the DI modules to provide your device implementation
3. Modify the input handling in `inputmanager/` if needed

## Dependencies

### Core Dependencies

- **Kotlin**: 1.9+ with Kotlin Compose
- **Jetpack Compose**: Modern UI toolkit
- **Hilt**: Dependency injection
- **Retrofit**: Network requests
- **OkHttp**: HTTP client
- **HiveMQ MQTT**: Real-time messaging
- **Firebase**: Analytics and crash reporting

### Device Dependencies

- **Chainway SDK**: Device-specific libraries
- **Android Audio**: Audio feedback
- **Android Permissions**: Location and hardware access

## Building for Production

### Release Configuration

1. **Signing Configuration**: Set up your keystore in `app/build.gradle.kts`
2. **Google Services**: Update `app/google-services.json` with your Firebase project
3. **API Keys**: Configure your API endpoints and keys in `app/src/main/res/values/strings.xml`

### ProGuard Rules

The app includes ProGuard rules for release builds. Update `app/proguard-rules.pro` if needed.

## Troubleshooting

### Common Issues

1. **Build Failures**: Run `./gradlew clean` and try again
2. **Device Not Detected**: Ensure ADB is installed and device is connected
3. **Package Name Conflicts**: Make sure your package name is unique
4. **Google Services Error**: Update the package name in `google-services.json`

### Debug Mode

Enable debug logging by setting the log level in your app's configuration.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:

- Create an issue on GitHub
- Check the troubleshooting section
- Review the configuration documentation

## Changelog

### Version 1.0.0
- Initial release
- RFID tag reading and writing
- Barcode scanning
- Inventory management
- Real-time MQTT synchronization
- Commission management
- Modern Jetpack Compose UI
