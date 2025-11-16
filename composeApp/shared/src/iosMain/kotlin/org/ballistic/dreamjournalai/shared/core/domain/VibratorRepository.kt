package org.ballistic.dreamjournalai.shared.core.domain

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

actual interface VibratorUtil {
    actual fun triggerVibration()
    actual fun triggerVibrationSuccess()
    actual fun cancelVibration()
}

class VibratorUtilImpl : VibratorUtil {
    // On iOS, you can do a haptic feedback, or just leave it no-op if you don’t want any feedback.
    override fun triggerVibration() {
        // Example: simple impact feedback
        val impactGenerator = UIImpactFeedbackGenerator(
            style = UIImpactFeedbackStyle.UIImpactFeedbackStyleMedium
        )
        impactGenerator.prepare()
        impactGenerator.impactOccurred()
    }

    override fun triggerVibrationSuccess() {
        val notificationGenerator = UINotificationFeedbackGenerator()
        notificationGenerator.prepare()
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeSuccess)
    }

    override fun cancelVibration() {
        // No-op
    }
}