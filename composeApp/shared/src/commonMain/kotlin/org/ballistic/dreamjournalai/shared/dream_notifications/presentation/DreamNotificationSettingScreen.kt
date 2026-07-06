package org.ballistic.dreamjournalai.shared.dream_notifications.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.*
import kotlinx.datetime.LocalTime
import org.ballistic.dreamjournalai.shared.core.components.dynamicBottomNavigationPadding
import org.ballistic.dreamjournalai.shared.dream_main.domain.MainScreenEvent
import org.ballistic.dreamjournalai.shared.dream_main.presentation.rememberNotificationPermissionState
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.FreeRealityCheckReminderLimit
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.MaxRealityCheckReminders
import org.ballistic.dreamjournalai.shared.dream_notifications.domain.NotificationEvent
import org.ballistic.dreamjournalai.shared.dream_notifications.presentation.components.DreamNotificationTopBar
import org.ballistic.dreamjournalai.shared.dream_notifications.presentation.viewmodel.NotificationScreenState
import org.ballistic.dreamjournalai.shared.dream_premium.domain.PremiumEntrySource
import org.jetbrains.compose.resources.stringResource

@Composable
fun DreamNotificationSettingScreen(
    notificationScreenState: NotificationScreenState,
    bottomPaddingValue: Dp,
    isPremiumMember: Boolean,
    onMainEvent: (MainScreenEvent) -> Unit,
    onEvent: (NotificationEvent) -> Unit,
    onNavigateToPremiumFlow: (PremiumEntrySource) -> Unit,
) {
    val notificationPermissionState = rememberNotificationPermissionState()
    val notificationControlsEnabled = !notificationPermissionState.shouldShowPrompt

    LaunchedEffect(Unit) {
        onMainEvent(MainScreenEvent.SetTopBarState(false))
        onMainEvent(MainScreenEvent.SetBottomBarVisibilityState(true))
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            DreamNotificationTopBar()
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding(), bottom = bottomPaddingValue)
                .fillMaxSize()
                .dynamicBottomNavigationPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (notificationPermissionState.shouldShowPrompt) {
                NotificationPermissionPrompt(
                    onEnableClick = notificationPermissionState.requestPermission
                )
            }

            ReminderSettingCard(
                icon = Icons.Filled.Create,
                title = stringResource(Res.string.dream_journal_reminder_title),
                subtitle = if (notificationScreenState.dreamJournalReminderEnabled) {
                    stringResource(Res.string.notification_daily_at, formatReminderTime(notificationScreenState.dreamJournalReminderTime))
                } else {
                    stringResource(Res.string.off)
                },
                enabled = notificationScreenState.dreamJournalReminderEnabled,
                controlsEnabled = notificationControlsEnabled,
                onToggle = { enabled -> onEvent(NotificationEvent.ToggleDreamJournalReminder(enabled)) },
            ) {
                TimeStepper(
                    time = notificationScreenState.dreamJournalReminderTime,
                    enabled = notificationControlsEnabled && notificationScreenState.dreamJournalReminderEnabled,
                    onTimeChanged = { time -> onEvent(NotificationEvent.SetDreamJournalReminderTime(time)) }
                )
            }

            RealityCheckerCard(
                state = notificationScreenState,
                isPremiumMember = isPremiumMember,
                controlsEnabled = notificationControlsEnabled,
                onEvent = onEvent,
                onLockedPremiumClick = { onNavigateToPremiumFlow(PremiumEntrySource.RealityChecker) }
            )

            ReminderSettingCard(
                icon = Icons.Filled.Notifications,
                title = stringResource(Res.string.dream_token_alerts),
                subtitle = if (notificationScreenState.dailyTokenReminderEnabled) {
                    stringResource(Res.string.notification_daily_at, formatReminderTime(notificationScreenState.dailyTokenReminderTime))
                } else {
                    stringResource(Res.string.off)
                },
                enabled = notificationScreenState.dailyTokenReminderEnabled,
                controlsEnabled = notificationControlsEnabled,
                onToggle = { enabled -> onEvent(NotificationEvent.ToggleDailyTokenReminder(enabled)) },
            ) {
                TimeStepper(
                    time = notificationScreenState.dailyTokenReminderTime,
                    enabled = notificationControlsEnabled && notificationScreenState.dailyTokenReminderEnabled,
                    onTimeChanged = { time -> onEvent(NotificationEvent.SetDailyTokenReminderTime(time)) }
                )
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun NotificationPermissionPrompt(
    onEnableClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF48182C),
                        Color(0xFF2A1648),
                        Color(0xFF102840)
                    )
                )
            )
            .border(1.dp, Color(0xFFFF6B6B).copy(alpha = 0.42f), RoundedCornerShape(24.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935).copy(alpha = 0.22f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Notifications,
                    contentDescription = null,
                    tint = Color(0xFFFFB4AB),
                    modifier = Modifier.size(23.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = "Notifications are off",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Enable permission to receive dream reminders, reality checks, and token alerts.",
                    color = Color(0xFFFFDAD6),
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Button(
            onClick = onEnableClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFB4AB),
                contentColor = Color(0xFF321018)
            )
        ) {
            Text(
                text = "Enable notifications",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
private fun ReminderSettingCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    enabled: Boolean,
    controlsEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (controlsEnabled) 1f else 0.52f)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF211047),
                        Color(0xFF38135E),
                        Color(0xFF181033)
                    )
                )
            )
            .border(1.dp, Color(0xFFB66CFF).copy(alpha = 0.34f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFC269),
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFD8CBFF),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }

            ReminderSwitch(
                checked = enabled,
                enabled = controlsEnabled,
                onCheckedChange = onToggle
            )
        }

        content()
    }
}

@Composable
private fun RealityCheckerCard(
    state: NotificationScreenState,
    isPremiumMember: Boolean,
    controlsEnabled: Boolean,
    onEvent: (NotificationEvent) -> Unit,
    onLockedPremiumClick: () -> Unit,
) {
    val allowedMax = if (isPremiumMember) MaxRealityCheckReminders else FreeRealityCheckReminderLimit

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (controlsEnabled) 1f else 0.52f)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF102840),
                        Color(0xFF17334B),
                        Color(0xFF251640)
                    )
                )
            )
            .border(1.dp, Color(0xFF7EE6D2).copy(alpha = 0.34f), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.NightsStay,
                contentDescription = null,
                tint = Color(0xFF7EE6D2),
                modifier = Modifier.size(24.dp)
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = stringResource(Res.string.reality_checkers),
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = if (state.realityCheckReminderEnabled) {
                        stringResource(Res.string.daily_checks, state.realityCheckReminderCount)
                    } else {
                        stringResource(Res.string.off)
                    },
                    color = Color(0xFFD8F8F2),
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }

            ReminderSwitch(
                checked = state.realityCheckReminderEnabled,
                enabled = controlsEnabled,
                onCheckedChange = { enabled -> onEvent(NotificationEvent.ToggleRealityCheckReminder(enabled)) }
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(MaxRealityCheckReminders) { index ->
                val slot = index + 1
                val locked = slot > allowedMax
                val selected = state.realityCheckReminderEnabled && state.realityCheckReminderCount >= slot && !locked
                RealitySlotButton(
                    slot = slot,
                    selected = selected,
                    locked = locked,
                    enabled = controlsEnabled,
                    onClick = {
                        if (locked) {
                            onLockedPremiumClick()
                        } else {
                            onEvent(NotificationEvent.SetRealityCheckReminderCount(slot, isPremiumMember))
                        }
                    }
                )
            }
        }

        repeat(MaxRealityCheckReminders) { index ->
            val slot = index + 1
            val locked = slot > allowedMax
            val visible = state.realityCheckReminderCount >= slot || locked
            if (visible) {
                RealityCheckTimeRow(
                    slot = slot,
                    time = state.realityCheckReminderTimes[index],
                    enabled = controlsEnabled && state.realityCheckReminderEnabled && !locked && state.realityCheckReminderCount >= slot,
                    locked = locked,
                    controlsEnabled = controlsEnabled,
                    onTimeChanged = { time ->
                        onEvent(NotificationEvent.SetRealityCheckReminderTime(index, time))
                    },
                    onLockedPremiumClick = onLockedPremiumClick
                )
            }
        }
    }
}

@Composable
private fun RowScope.RealitySlotButton(
    slot: Int,
    selected: Boolean,
    locked: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                locked -> Color(0xFF5E457C)
                selected -> Color(0xFF6EE7D8)
                else -> Color.White.copy(alpha = 0.12f)
            },
            contentColor = if (selected) Color(0xFF0E2436) else Color.White,
            disabledContainerColor = Color.White.copy(alpha = 0.06f),
            disabledContentColor = Color.White.copy(alpha = 0.38f)
        ),
        modifier = Modifier.weight(1f)
    ) {
        if (locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(text = slot.toString(), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RealityCheckTimeRow(
    slot: Int,
    time: LocalTime,
    enabled: Boolean,
    locked: Boolean,
    controlsEnabled: Boolean,
    onTimeChanged: (LocalTime) -> Unit,
    onLockedPremiumClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = controlsEnabled && locked, onClick = onLockedPremiumClick)
            .background(if (locked) Color(0xFF201733) else Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.dp,
                color = if (locked) Color(0xFFFFC269).copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(Res.string.checker_number, slot),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (locked) {
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = null,
                        tint = Color(0xFFFFC269),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = stringResource(Res.string.premium_label),
                        color = Color(0xFFFFC269),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = if (locked) {
                    stringResource(Res.string.unlock_third_reality_check)
                } else {
                    formatReminderTime(time)
                },
                color = if (locked) Color(0xFFFFDDA3) else Color(0xFFD8F8F2),
                fontSize = 13.sp
            )
        }

        if (locked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = Color(0xFFFFC269),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
                    .padding(8.dp)
                    .size(18.dp)
            )
        } else {
            TimeStepper(
                time = time,
                enabled = enabled,
                onTimeChanged = onTimeChanged
            )
        }
    }
}

@Composable
private fun TimeStepper(
    time: LocalTime,
    enabled: Boolean,
    onTimeChanged: (LocalTime) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconButton(
            enabled = enabled,
            onClick = { onTimeChanged(time.plusMinutes(-15)) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (enabled) 0.12f else 0.05f))
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = stringResource(Res.string.earlier),
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.35f)
            )
        }

        Text(
            text = formatReminderTime(time),
            color = Color.White.copy(alpha = if (enabled) 1f else 0.5f),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        IconButton(
            enabled = enabled,
            onClick = { onTimeChanged(time.plusMinutes(15)) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = if (enabled) 0.12f else 0.05f))
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(Res.string.later),
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.35f)
            )
        }
    }
}

@Composable
private fun ReminderSwitch(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        enabled = enabled,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = Color(0xFFFF6D8E),
            uncheckedThumbColor = Color(0xFFCFC6E8),
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            disabledCheckedThumbColor = Color.White.copy(alpha = 0.62f),
            disabledCheckedTrackColor = Color(0xFFFF6D8E).copy(alpha = 0.28f),
            disabledUncheckedThumbColor = Color(0xFFCFC6E8).copy(alpha = 0.45f),
            disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        )
    )
}

private fun LocalTime.plusMinutes(deltaMinutes: Int): LocalTime {
    val total = ((hour * 60 + minute + deltaMinutes) % MINUTES_PER_DAY + MINUTES_PER_DAY) % MINUTES_PER_DAY
    return LocalTime(hour = total / 60, minute = total % 60)
}

private fun formatReminderTime(time: LocalTime): String {
    val hour12 = when (val hour = time.hour % 12) {
        0 -> 12
        else -> hour
    }
    val minute = time.minute.toString().padStart(2, '0')
    val suffix = if (time.hour < 12) "AM" else "PM"
    return "$hour12:$minute $suffix"
}

private const val MINUTES_PER_DAY = 24 * 60
