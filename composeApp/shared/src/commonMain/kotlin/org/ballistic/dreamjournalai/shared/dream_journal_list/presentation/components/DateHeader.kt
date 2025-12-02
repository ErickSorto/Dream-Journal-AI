package org.ballistic.dreamjournalai.shared.dream_journal_list.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.today
import dreamjournalai.composeapp.shared.generated.resources.yesterday
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.ballistic.dreamjournalai.shared.core.util.getDayOfWeekDisplayName
import org.ballistic.dreamjournalai.shared.core.util.getStartOfWeek
import org.ballistic.dreamjournalai.shared.core.util.parseCustomDate
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors
import org.jetbrains.compose.resources.stringResource
import kotlin.time.ExperimentalTime


@OptIn(ExperimentalTime::class)
@Composable
fun DateHeader(dateString: String,
               paddingStart: Int = 12
) {
    // Resolve string resources outside the remember block
    val todayString = stringResource(Res.string.today)
    val yesterdayString = stringResource(Res.string.yesterday)

    val displayString = remember(dateString, todayString, yesterdayString) {
        // Define the system's default time zone
        val timeZone = TimeZone.currentSystemDefault()

        // Get today's date
        val todayDate = kotlin.time.Clock.System.todayIn(timeZone)

        // Get yesterday's date
        val yesterdayDate = todayDate.minus(DatePeriod(days = 1))

        // Calculate the start of the current week (assuming week starts on Sunday)
        val thisWeekStartDate = getStartOfWeek(todayDate, DayOfWeek.SUNDAY)

        // Custom parser for "MMM d, yyyy" format
        val fixedDateString = dateString.replaceFirstChar {
            if (it.isLowerCase()) it.uppercaseChar() else it
        }

        val date = try {
            parseCustomDate(fixedDateString)
        } catch (_: IllegalArgumentException) {
            null
        }

        when {
            date == todayDate -> todayString
            date == yesterdayDate -> yesterdayString
            date != null && date > thisWeekStartDate -> {
                // Get the full display name of the day of the week
                getDayOfWeekDisplayName(date.dayOfWeek)
            }
            else -> dateString
        }
    }

    Box(
        modifier = Modifier
            .padding(start = paddingStart.dp, top = 8.dp, end = 8.dp, bottom = 8.dp)
            .background(
                color = OriginalXmlColors.LightBlack.copy(alpha = 0.8f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
            )
    ) {
        Text(
            text = displayString,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .padding(6.dp)
                .padding(horizontal = 8.dp),
            color = OriginalXmlColors.White,
            fontWeight = MaterialTheme.typography.bodyLarge.fontWeight
        )
    }
}