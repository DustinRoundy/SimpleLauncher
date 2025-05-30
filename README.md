# SimpleLauncher
This project is a custom built Android Launcher built for Flip Phones and takes a D-Pad first approach. This Launcher features 3 main pages accessable via the left and right keys on the D-Pad and is geared towards the Vortex V3.

![This is an image caption](https://github.com/user-attachments/assets/393990af-64cb-4321-b526-51625c655e53)
![image](https://github.com/user-attachments/assets/d9ad1ce7-d022-4fb4-892e-73d4251ba686)
![image](https://github.com/user-attachments/assets/7adb65e7-cd0d-431d-bf67-07e003beff25)

## Current Features:
- Display and open current notifications
- Add and remove apps from the home screen
- Displays currently installed applications on the apps screen
- Trigger app uninstall from apps screen
- Open app details page from apps screen
- Display currently playing music on the home screen
- Launch dialer from keypad

## Future Features:
- Save apps added to the home screen
- Add additional themes and colorways
- Update design for notifications page
- Fix the App Context Menu

## Known Bugs:
- Pressing the back button too many times will restart the launcher
- Some apps listed in the menu will launch the previous launcher (Vortex V3 specific issue)
- Some notifications do not display correctly

## Tested Devices:
| Device | Buttons | Notifications | Other issues |
| :----- | :-----: | :-----------: | :----------- |
| Vortex V3 | ✅ | ✅ | Known Bugs |
| Olitech EasyFlipSmart | ❌ | ❌ | Buttons do not correctly identify |
| Doro 731x | ? | ? | Launcher needs to be replaced, unable to test further |

## Notification Access by ADB:
Some devices (mainly devices running Android Go with the Low Memory flag) seem to have removed the ability to grant notification access on the device itself. This usually presents with a screen that reads "This function is not available on this device". If you run into this issue, please run the following ADB command to grant notification access.

``
adb shell cmd notification allow_listener com.example.simplelauncher/com.example.simplelauncher.MyNotificationListener
``

