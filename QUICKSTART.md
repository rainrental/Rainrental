# Quick Start Guide

Get your RFID app up and running in minutes!

## 🚀 5-Minute Setup

### 1. Clone and Navigate
```bash
git clone https://github.com/yourusername/rainrental-rfid.git
cd rainrental-rfid
```

### 2. Generate Your Customized App
```bash
# Copy the example configuration
cp env.example .env

# Edit the configuration (use your favorite editor)
nano .env
# or
code .env
```

### 3. Update Your Settings
Edit `.env` with your information:
```env
PACKAGE_NAME=com.mycompany.rfidapp
APP_NAME=My RFID Scanner
COMPANY_ID=my_company_123
OUTPUT_DIR=../my-rfid-app
PROJECT_NAME=MyRfidApp
DEPLOY_TO_DEVICE=true
```

### 4. Generate the App
```bash
./generate_app.sh
```

### 5. Navigate to Your New App
```bash
cd ../my-rfid-app
```

### 6. Deploy to Device (Optional)
If you have a Chainway C72 connected:
```bash
./deploy.sh
```

## 🎯 What Just Happened?

The generation script automatically:
- ✅ Created a completely new project folder
- ✅ Renamed your app package
- ✅ Updated the app name
- ✅ Set your company ID
- ✅ Built debug and release APKs
- ✅ Deployed to your device (if enabled)
- ✅ Generated build scripts for your new project

## 📱 Your App is Ready!

Your APK files are in:
- **Debug**: `../my-rfid-app/app/build/outputs/apk/debug/`
- **Release**: `../my-rfid-app/app/build/outputs/apk/release/`

## 🔧 Alternative Commands

### Using Make
```bash
make configure    # Configure and build
make deploy       # Deploy to device
make clean        # Clean build files
make help         # Show all commands
```

### Manual Build
```bash
./gradlew assembleDebug    # Build debug APK
./gradlew assembleRelease  # Build release APK
./gradlew installDebug     # Install to device
```

## 🐛 Troubleshooting

### Build Fails?
```bash
./gradlew clean
./configure_app.sh
```

### Device Not Found?
```bash
adb devices
# Make sure your device is listed
```

### Package Name Error?
- Use only lowercase letters, numbers, and dots
- Example: `com.mycompany.app`

## 📚 Next Steps

- **Customize UI**: Edit colors in `app/src/main/java/your/package/ui/theme/`
- **Add APIs**: Configure your endpoints in `app/src/main/res/values/strings.xml`
- **Change Logo**: Replace files in `app/src/main/res/mipmap-*/`
- **Read Full Docs**: Check the main [README.md](README.md)

## 🆘 Need Help?

- Check the [README.md](README.md) for detailed documentation
- Run `./test_configuration.sh` to verify your setup
- Create an issue on GitHub for bugs or questions

---

**Happy RFID Scanning! 🏷️** 