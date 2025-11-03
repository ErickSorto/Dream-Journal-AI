To ensure `dream_dictionary.csv` is bundled into the iOS app:

1. Preferred (Xcode):
   - Open `iosApp.xcodeproj` or `iosApp.xcworkspace` in Xcode.
   - In Project navigator, locate `iosApp/Resources/dream_dictionary.csv` (or `iosApp/iosApp/Resources/dream_dictionary.csv`).
   - Select the file and in the File inspector (right pane) ensure the app target is checked under "Target Membership".
   - Build the app; the file will be included in the app bundle and available at runtime via `NSBundle.mainBundle.pathForResource("dream_dictionary", "csv")`.

2. If Xcode doesn't include it automatically, add a Run Script Build Phase (before Compile Sources) to copy it into the app bundle:

   mkdir -p "$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/Resources"
   cp "${PROJECT_DIR}/iosApp/Resources/dream_dictionary.csv" "$BUILT_PRODUCTS_DIR/$PRODUCT_NAME.app/Resources/"

   (Adjust the source path if the CSV is in `iosApp/iosApp/Resources`.)

3. Confirm at runtime by printing the bundle path (in Swift):
   if let path = Bundle.main.path(forResource: "dream_dictionary", ofType: "csv") {
     print("Found: \(path)")
   } else {
     print("Missing dream_dictionary.csv in bundle")
   }

If you want, I can add the Run Script phase automatically to your Xcode project via a script, or adjust the Kotlin reader to search multiple likely resource paths. Let me know which approach you prefer.
