# Msitu - RTK Tree Planting App

Msitu is a React Native tree planting app designed to simplify the pegging and rope-tying process during tree planting operations. The app integrates with RTK (Real-Time Kinematic) Rover and Base Station equipment to provide centimeter-level accuracy for precise tree placement.

## 🚀 Features

- **RTK Integration**: Centimeter-level GPS accuracy (1cm error margin)
- **Grid Generation**: Automatic planting line generation with customizable mesh patterns
- **Distance Calculations**: Precise distance measurements between coordinates
- **Bluetooth/USB Connectivity**: Connect to RTK equipment via Bluetooth or USB
- **Project Management**: Create and manage multiple planting projects
- **Real-time Positioning**: Live rover position tracking with visual feedback
- **Offline Capability**: Work without internet connectivity

## 🏗️ Architecture

### React Native Turbo Modules

This project uses **React Native Turbo Modules** (the new architecture) to interface with native code for high-performance geometric calculations. The main native module is `RTNMsitu`, which provides:

#### RTNMsitu Module Capabilities

```typescript
// Core functions available through RTNMsitu
RTNMsitu.toPoint(coord)                    // Convert lat/lng to UTM coordinates
RTNMsitu.lineToCoords(points, center)      // Convert UTM points to lat/lng coordinates
RTNMsitu.linesToCoords(lines, center)      // Convert multiple UTM lines to lat/lng
RTNMsitu.generateMesh(first, second, direction, type, gapSize, lineLength)  // Generate planting grid
RTNMsitu.closetPointRelativeToRoverPosition(roverLocation, points)  // Find closest point
RTNMsitu.distanceBtnCoords(pt1, pt2)       // Calculate distance between coordinates
```

#### Native Implementation

The RTNMsitu module is implemented in:
- **Android**: Kotlin with S2 Geometry library for spatial calculations
- **iOS**: Swift (planned)
- **TypeScript**: Type-safe interface definitions

Key native libraries used:
- **S2 Geometry**: For spatial indexing and nearest point calculations
- **UTM Coordinate System**: For precise distance and area calculations
- **Kotlin Coroutines**: For asynchronous mesh generation

## 🛠️ Development Setup

### Prerequisites

- **Node.js**: >= 18.0.0
- **React Native CLI**: Latest version
- **Android Studio**: Dolphin or later
- **Java Development Kit (JDK)**: 17 or later
- **Android SDK**: API level 24+ (minimum), 35 (target)
- **Git**: For version control

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/dsmagicug/msitu.git
   cd kibira
   ```

2. **Install dependencies**
   ```bash
   npm install
   # or
   yarn install
   ```

3. **Start Metro bundler**
   ```bash
   # From project root
   npm start
   # or
   yarn start
   ```

4. **Run the app**
   ```bash
   # For Android
   npm run android
   # or
   yarn android
   
   # For iOS (requires macOS)
   npm run ios
   # or
   yarn ios
   ```

### Development Environment Configuration

#### Android Studio Setup

1. Open Android Studio
2. Open the `android` folder from the project root
3. Wait for Gradle sync to complete
4. Ensure the following are configured:
   - **Minimum SDK**: 24
   - **Target SDK**: 35
   - **View Binding**: Enabled
   - **Kotlin Support**: Enabled


### Building for Production

#### Android Release Build

```bash
# Generate release APK
cd android
./gradlew assembleRelease

# The APK will be located at:
# android/app/build/outputs/apk/release/app-release.apk
```

#### Bundle Generation

```bash
# Generate Android bundle
npm run bundle-android
```

## 📱 Usage Guide

### Equipment Setup

1. **Base Station**: Ensure clear sky view for accurate satellite readings
2. **Rover**: Connect to base station and establish RTK communication
3. **App Connection**: Connect app to rover via Bluetooth or USB
4. **RTK Fix**: Wait for RTK fix indication (takes a few minutes)

### Creating a Planting Project

1. **Establish RTK Fix**: Ensure you have centimeter-level accuracy
2. **Place Base Points**: 
   - Place two pegs on the plot outskirts (~50m apart)
   - Mimic desired tree planting orientation
   - Tie rope between pegs for straightness
3. **Capture Coordinates**: 
   - Copy two coordinate values from the rope
   - Keep them as far apart as possible
4. **Create Project**: 
   - Fill in project details
   - Paste coordinates into "Base Points" field
   - The app will generate planting lines automatically

### Working Without Internet

If no internet connection is available:
1. Move around the plot with the rover to establish bearing
2. Use offline maps or visual references
3. The app will work without visual map representation
4. That being said, for reference, please at least load the map.

### Syncing Multiple Equipment Sets

When using different equipment sets:

1. **Create Project A** using Equipment Set One
2. **Draw all possible lines** for Project A
3. **Stand on last line** of Project A
4. **Copy coordinates** using Equipment Set Two
5. **Create Project B** with new base points
6. **Project B** will be an extension of Project A with minimal offset (1-2 ft)

## 🔧 Technical Details

### RTNMsitu Module Structure

```
RTNMsitu/
├── android/                    # Android native implementation
│   ├── src/main/java/
│   │   └── com/rtnmsitu/
│   │       ├── MsituModule.kt  # Main Turbo module implementation
│   │       ├── geometry/       # Geometric calculations
│   │       ├── s2/            # S2 geometry utilities
│   │       └── utils/         # Helper utilities
├── spec/                      # TypeScript interface definitions
│   ├── NativeRTNMsitu.ts     # Turbo module spec
│   └── types/                # Type definitions
└── index.ts                  # Module exports
```

### Key Dependencies

- **React Native**: 0.76.1
- **React**: 18.3.1
- **React Native Maps**: For map visualization
- **React Native Reanimated**: For smooth animations
- **Redux Toolkit**: State management
- **NativeWind**: Tailwind CSS for React Native
- **RTNMsitu**: Custom Turbo module for geometric calculations

### Performance Optimizations

- **Font Optimization**: Only includes used vector icon fonts (5/20 fonts)
- **ProGuard Rules**: Optimized for release builds
- **Coordinate Validation**: Robust validation to prevent crashes
- **Async Operations**: Heavy calculations run off main thread

## 🐛 Troubleshooting

### Common Build Issues

1. **Gradle Sync Fails**
   - Clean project: `./gradlew clean`
   - Invalidate caches in Android Studio
   - Check Java/JDK version compatibility

2. **Metro Bundler Issues**
   - Clear Metro cache: `npx react-native start --reset-cache`
   - Check Node.js version (>= 18)

3. **RTNMsitu Module Issues**
   - Ensure Turbo modules are properly configured
   - Check native code compilation
   - Verify TypeScript interface definitions

### Debug Mode

```bash
# Enable debug logging
adb logcat | grep -E "(RTNMsitu|Msitu)"

# View React Native logs
npx react-native log-android
```

## 📄 License

This project is licensed under the GPL License. See the LICENSE file for details.

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests if applicable
5. Submit a pull request

## 📚 Resources

- [What is RTK?](https://www.youtube.com/watch?v=257WX_agvtg)
- [How Rover and Base Station Work](https://www.youtube.com/watch?v=Rk09oMD_I24&t=4s)
- [React Native Turbo Modules](https://reactnative.dev/docs/the-new-architecture/pillars-turbomodules)
- [S2 Geometry Library](https://s2geometry.io/)

## 📞 Support

For issues and questions:
- Create an issue on GitHub
- Contact: support@ds.co.ug
- Repository: https://github.com/dsmagicug/msitu
