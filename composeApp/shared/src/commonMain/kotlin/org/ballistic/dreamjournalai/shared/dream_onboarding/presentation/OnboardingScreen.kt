package org.ballistic.dreamjournalai.shared.dream_onboarding.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColor
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import dreamjournalai.composeapp.shared.generated.resources.Res
import dreamjournalai.composeapp.shared.generated.resources.dream_onboarding_design
import dreamjournalai.composeapp.shared.generated.resources.onboarding_long
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.ballistic.dreamjournalai.shared.core.components.TypewriterText
import org.ballistic.dreamjournalai.shared.dream_account.MyGoogleSignInButton
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.AnonymousButton
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.LoginLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.ObserveLoginState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignupLoginLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.components.SignupLoginTabLayout
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.LoginEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.events.SignupEvent
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.LoginViewModelState
import org.ballistic.dreamjournalai.shared.dream_authentication.presentation.signup_screen.viewmodel.SignupViewModelState
import org.ballistic.dreamjournalai.shared.theme.OriginalXmlColors.LightBlack
import org.jetbrains.compose.resources.painterResource
import kotlin.uuid.ExperimentalUuidApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.random.Random

// Animation timing constants (file-level)
private const val STAGGER_MS: Long = 200
private const val ENTER_DURATION_MS: Int = 300

// --- Shooting star overlay (defined early so it's always in scope) ---
@Composable
fun ShootingStarLayer(
    trigger: Int,
    durationMs: Int = 1100,
    headRadiusDp: Float = 2.8f,
    trailCount: Int = 14,
    trailStep: Float = 0.04f,
    trailMaxAlpha: Float = 0.9f,
    starColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }
    val opacity = remember { Animatable(0f) }
    val active = remember { mutableStateOf(false) }

    LaunchedEffect(trigger) {
        if (trigger > 0) {
            active.value = true
            progress.snapTo(0f)
            opacity.snapTo(1f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = durationMs,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1f)
                )
            )
            // Post travel fade-out (slightly slower)
            opacity.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = CubicBezierEasing(0.17f, 0.67f, 0.36f, 1f)
                )
            )
            active.value = false
        }
    }

    if (!active.value && opacity.value <= 0.001f) return

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val oa = opacity.value

            // Extended curved path using a quadratic Bezier
            val start = Offset(-0.30f * w, 0.10f * h)
            val end = Offset(1.30f * w, 0.70f * h)
            val control = Offset(0.50f * w, 0.02f * h)

            fun posAt(t: Float): Offset {
                val tt = t.coerceIn(0f, 1f)
                val u = 1f - tt
                val x = u * u * start.x + 2f * u * tt * control.x + tt * tt * end.x
                val y = u * u * start.y + 2f * u * tt * control.y + tt * tt * end.y
                return Offset(x, y)
            }

            val p = progress.value
            val head = posAt(p)

            fun timeFade(tp: Float): Float {
                val t = tp.coerceIn(0f, 1f)
                return t * t * t
            }
            fun spaceFade(falloff: Float): Float {
                val f = falloff.coerceIn(0f, 1f)
                return f * f
            }

            // Glowing line trail (more apparent)
            val lineSegments = maxOf(6, (trailCount * 3) / 4)
            var prev = posAt((p - trailStep).coerceAtLeast(0f))
            for (i in 0 until lineSegments) {
                val tp = p - i * trailStep
                if (tp <= 0f) break
                val curr = posAt(tp)
                val falloff = 1f - i / lineSegments.toFloat()
                val alpha = (trailMaxAlpha * 0.85f) * timeFade(tp) * spaceFade(falloff) * oa
                if (alpha <= 0.01f) continue
                val stroke = (headRadiusDp * (1.15f + 0.65f * falloff)).dp.toPx()
                drawLine(
                    color = starColor.copy(alpha = alpha),
                    start = curr,
                    end = prev,
                    strokeWidth = stroke
                )
                prev = curr
            }

            // Sparkle dot trail (less pronounced)
            for (i in 0 until trailCount) {
                val tp = p - i * trailStep
                if (tp <= 0f) continue
                val center = posAt(tp)
                val falloff = 1f - i / trailCount.toFloat()
                val alpha = (trailMaxAlpha * 0.30f) * timeFade(tp) * spaceFade(falloff) * oa
                if (alpha <= 0.01f) continue
                val radius = headRadiusDp * (0.55f + 0.25f * falloff)
                drawCircle(
                    color = starColor.copy(alpha = alpha),
                    radius = radius.dp.toPx(),
                    center = center
                )
            }

            // Head glow (brighter)
            val headRadiusPx = headRadiusDp.dp.toPx()
            val glowAlpha = 0.45f * oa
            if (glowAlpha > 0.01f) {
                drawCircle(
                    color = starColor.copy(alpha = glowAlpha),
                    radius = headRadiusPx * 3.0f,
                    center = head
                )
            }
            // Head core (slightly brighter)
            val headAlpha = 1.0f * oa
            if (headAlpha > 0.01f) {
                drawCircle(
                    color = starColor.copy(alpha = headAlpha),
                    radius = headRadiusPx,
                    center = head
                )
                val crossLen = headRadiusPx * 2.2f
                val crossAlpha = 0.50f * oa
                if (crossAlpha > 0.01f) {
                    drawLine(
                        color = starColor.copy(alpha = crossAlpha),
                        start = Offset(head.x - crossLen, head.y),
                        end = Offset(head.x + crossLen, head.y),
                        strokeWidth = headRadiusPx * 0.35f
                    )
                    drawLine(
                        color = starColor.copy(alpha = crossAlpha),
                        start = Offset(head.x, head.y - crossLen),
                        end = Offset(head.x, head.y + crossLen),
                        strokeWidth = headRadiusPx * 0.35f
                    )
                }
            }
        }
    }
}

private data class VerticalBiasAlignment(
    val verticalBias: Float
) : Alignment {
    override fun align(size: IntSize, space: IntSize, layoutDirection: LayoutDirection): IntOffset {
        val x = (space.width - size.width) / 2
        val biasFraction = (verticalBias + 1f) / 2f // -1..1 -> 0..1
        val y = ((space.height - size.height) * biasFraction).toInt()
        return IntOffset(x, y)
    }
}

@Composable
fun TwinklesLayer(
    twinkleCount: Int = 20,
    seed: Int = 81321,
    modifier: Modifier = Modifier,
    minAlpha: Float = 0.05f,
    maxAlpha: Float = 0.22f,
    // Upward drift: panProgress (0..1) mapped to -(height * driftUpFraction + margin) px
    panProgress: Float = 0f,
    driftUpFraction: Float = 1.2f,
) {
    data class Twinkle(
        val xFrac: Float,
        val yFrac: Float,
        val baseDp: Float,
        val color: Color,
        val phase: Float,
        val speed: Float,
        val glowScale: Float,
        val tiltDeg: Float,
    )

    val palette = remember {
        listOf(
            Color(0xFFB49CFF), // purple
            Color(0xFFFF8FD0), // brighter pink
            Color(0xFFFFD04A)  // more yellow
        )
    }
    val rng = remember(seed) { Random(seed) }
    val twinkles = remember {
        val result = mutableListOf<Twinkle>()
        val minDist = 0.075f // fractional distance for spacing (slightly denser)
        val minDist2 = minDist * minDist
        var attempts = 0
        while (result.size < twinkleCount && attempts < twinkleCount * 30) {
            attempts++
            val x = rng.nextFloat().coerceIn(0.03f, 0.97f)
            val y = rng.nextFloat().coerceIn(0.04f, 0.92f)
            var ok = true
            for (e in result) {
                val dx = x - e.xFrac
                val dy = y - e.yFrac
                if (dx * dx + dy * dy < minDist2) { ok = false; break }
            }
            if (!ok) continue
            result += Twinkle(
                xFrac = x,
                yFrac = y,
                baseDp = (0.9f + rng.nextFloat() * 1.3f),
                color = palette[rng.nextInt(palette.size)],
                phase = rng.nextFloat(), // 0..1
                speed = 0.6f + rng.nextFloat() * 1.0f, // 0.6..1.6x
                glowScale = 1.6f + rng.nextFloat() * 0.9f, // 1.6..2.5 (softer/bigger)
                tiltDeg = -12f + rng.nextFloat() * 24f // -12..+12 deg slight tilt
            )
        }
        if (result.isEmpty()) {
            // fallback if spacing rejects everything for some reason
            List(twinkleCount) {
                Twinkle(
                    xFrac = rng.nextFloat().coerceIn(0.03f, 0.97f),
                    yFrac = rng.nextFloat().coerceIn(0.04f, 0.92f),
                    baseDp = (0.9f + rng.nextFloat() * 1.3f),
                    color = palette[rng.nextInt(palette.size)],
                    phase = rng.nextFloat(),
                    speed = 0.6f + rng.nextFloat() * 1.0f,
                    glowScale = 1.6f + rng.nextFloat() * 0.9f,
                    tiltDeg = -12f + rng.nextFloat() * 24f
                )
            }
        } else result
    }

    fun easeInOutCubic(t: Float): Float = if (t < 0.5f) 4f * t * t * t else 1f - (-2f * t + 2f).let { it * it * it } / 2f
    fun easeOutCubic(t: Float): Float {
        val u = 1f - t
        return 1f - u * u * u
    }

    val time by rememberInfiniteTransition(label = "twinkles_time").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "twinkle_time_anim"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Linear mapping so all move at the same rate; add margin to ensure fully off-screen
        val marginPx = 24.dp.toPx()
        val yDrift = -panProgress.coerceIn(0f, 1f) * (h * driftUpFraction + marginPx)

        fun curvedDiamondPath(cx: Float, cy: Float, halfW: Float, halfH: Float, curveIn: Float): Path {
            // curveIn: 0..1, how much sides curve inward towards center
            val path = Path()
            val top = Offset(cx, cy - halfH)
            val right = Offset(cx + halfW, cy)
            val bottom = Offset(cx, cy + halfH)
            val left = Offset(cx - halfW, cy)
            val c = Offset(cx, cy)
            // Control points slightly towards center and along edges
            fun lerp(a: Offset, b: Offset, t: Float) = Offset(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)
            val tMid = 0.55f // where along the edge the control point sits
            val inward = curveIn.coerceIn(0f, 1f)

            val trCtrl = lerp(lerp(top, right, tMid), c, inward)
            val rbCtrl = lerp(lerp(right, bottom, tMid), c, inward)
            val blCtrl = lerp(lerp(bottom, left, tMid), c, inward)
            val ltCtrl = lerp(lerp(left, top, tMid), c, inward)

            path.moveTo(top.x, top.y)
            path.quadraticTo(trCtrl.x, trCtrl.y, right.x, right.y)
            path.quadraticTo(rbCtrl.x, rbCtrl.y, bottom.x, bottom.y)
            path.quadraticTo(blCtrl.x, blCtrl.y, left.x, left.y)
            path.quadraticTo(ltCtrl.x, ltCtrl.y, top.x, top.y)
            path.close()
            return path
        }

        for (t in twinkles) {
            val localT = ((time * t.speed) + t.phase) % 1f
            val eased = easeInOutCubic(localT)
            val pulse = (0.5f * (1f - cos(2f * PI.toFloat() * eased))).coerceIn(0f, 1f)
            val alpha = (minAlpha + (maxAlpha - minAlpha) * pulse).coerceIn(0f, 1f)
            val cx = t.xFrac * w
            val cy = t.yFrac * h + yDrift
            val base = t.baseDp.dp.toPx()

            // Make it less thick: height > width
            val halfHCore = base * 2.6f
            val halfWCore = halfHCore * 0.42f // slightly thinner
            val curveIn = 0.50f // a bit deeper inward curve

            val glowScale = t.glowScale
            val halfHGlow = halfHCore * glowScale
            val halfWGlow = halfWCore * glowScale

            rotate(degrees = t.tiltDeg, pivot = Offset(cx, cy)) {
                val glowPath = curvedDiamondPath(cx, cy, halfWGlow, halfHGlow, curveIn)
                drawPath(
                    path = glowPath,
                    color = t.color.copy(alpha = alpha * 0.26f)
                )
                val corePath = curvedDiamondPath(cx, cy, halfWCore, halfHCore, curveIn)
                drawPath(
                    path = corePath,
                    color = t.color.copy(alpha = alpha)
                )
            }
        }
    }
}

@OptIn(ExperimentalUuidApi::class)
@ExperimentalAnimationApi
@Composable
fun OnboardingScreen(
    loginViewModelState: LoginViewModelState,
    signupViewModelState: SignupViewModelState,
    navigateToDreamJournalScreen: () -> Unit,
    onLoginEvent: (LoginEvent) -> Unit,
    onSignupEvent: (SignupEvent) -> Unit,
    onDataLoaded: () -> Unit,
) {
    val isUserAnonymous = loginViewModelState.isUserAnonymous
    val isUserLoggedIn = loginViewModelState.isLoggedIn
    val isEmailVerified = loginViewModelState.isEmailVerified
    val showLoginLayout = remember { mutableStateOf(false) }
    val isSplashScreenClosed = remember { mutableStateOf(false) }
    val titleText = remember { mutableStateOf("Welcome Dreamer!") }
    val visible = remember { mutableStateOf(true) }
    val transition = updateTransition(targetState = visible.value, label = "TitleVisibility")
    val titleColor by transition.animateColor(label = "titleColor") { state ->
        if (state) Color.White else Color.Transparent
    }
    val showSubheader = remember { mutableStateOf(false) }
    val isLoading = loginViewModelState.isLoading
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Centralized visibility states for a consistent stagger
    val vTabs = remember { mutableStateOf(false) }
    val vEmail = remember { mutableStateOf(false) }
    val vPassword = remember { mutableStateOf(false) }
    val vLoginBtn = remember { mutableStateOf(false) }
    val vGoogle = remember { mutableStateOf(false) }
    val vGuest = remember { mutableStateOf(false) }
    val onboardingImageEntered = remember { mutableStateOf(false) }

    // Shooting star trigger (monotonic). Increment to play once.
    val shootingStarTrigger = remember { mutableStateOf(0) }

    // Camera bias for onboarding_long panning. -1f = top, +1f = bottom
    val cameraBiasY = remember { Animatable(-1f) }
    // Subtle zoom scale for the background image
    val cameraScale = remember { Animatable(1f) }
    // Box height expansion before typewriter starts (0..1 fraction)
    val boxExpand = remember { Animatable(0f) }
    val startTypewriter = remember { mutableStateOf(false) }
    val showWelcomeBox = remember { mutableStateOf(true) }

    // Declare isPanningDown state alongside other UI states so exit animations can reference it during the pan-down transition.
    val isPanningDown = remember { mutableStateOf(false) }

    // Welcome box fade-in/out state
    val welcomeBoxAlpha = remember { Animatable(1f) }
    // Login container fade-in/out state
    val loginContainerAlpha = remember { Animatable(1f) }

    // Ensure we start at the very top and default scales
    LaunchedEffect(Unit) {
        cameraBiasY.snapTo(-1f)
        cameraScale.snapTo(1f)
        boxExpand.snapTo(0f)
        welcomeBoxAlpha.snapTo(1f)
        loginContainerAlpha.snapTo(1f)
    }

    // Trigger the welcome box expansion, then start the typewriter
    LaunchedEffect(isSplashScreenClosed.value) {
        if (isSplashScreenClosed.value) {
            boxExpand.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = 900,
                    easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1f) // slower expand from center
                )
            )
            startTypewriter.value = true
        }
    }

    // Stagger login items after typewriter completes
    LaunchedEffect(showLoginLayout.value) {
        if (showLoginLayout.value) {
            // defer showing tabs until the onboarding image finishes its bounce
        } else {
            // Reset all visibilities when leaving login layout
            vTabs.value = false
            vEmail.value = false
            vPassword.value = false
            vLoginBtn.value = false
            vGoogle.value = false
            vGuest.value = false
            onboardingImageEntered.value = false
        }
    }

    // After tabs reveal, drive the entire stagger chain: fields (3 steps), Google, Guest
    LaunchedEffect(Unit) {
        delay(1000)
        onDataLoaded()
        isSplashScreenClosed.value = true
    }

    LaunchedEffect(Unit) {
        onLoginEvent(LoginEvent.BeginAuthStateListener)
    }

    // When login state indicates success, pan camera to bottom with slight zoom-in, then navigate
    ObserveLoginState(
        isLoggedIn = isUserLoggedIn,
        isEmailVerified = isEmailVerified,
        isUserAnonymous = isUserAnonymous,
        navigateToDreamJournalScreen = {
            scope.launch {
                isPanningDown.value = true

                // Fade welcome box without unmounting to avoid vertical reflow
                val fadeWelcome = launch {
                    if (showWelcomeBox.value) {
                        welcomeBoxAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween<Float>(durationMillis = 450, easing = FastOutSlowInEasing)
                        )
                        showWelcomeBox.value = false
                        welcomeBoxAlpha.snapTo(1f) // reset for future entries
                    }
                }

                // Fade entire login container without changing child visibilities
                val fadeLogin = launch {
                    if (showLoginLayout.value) {
                        loginContainerAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween<Float>(durationMillis = 480, easing = FastOutSlowInEasing)
                        )
                        // keep it mounted during pan; it will be unmounted by navigation
                    }
                }

                // Run background pan and zoom concurrently (slower)
                val endScale = 1.20f
                val pan = launch {
                    cameraBiasY.animateTo(
                        targetValue = 1f,
                        animationSpec = tween(
                            durationMillis = 2400,
                            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f) // smooth ease-in-out
                        )
                    )
                }
                val zoom = launch {
                    cameraScale.animateTo(
                        targetValue = endScale,
                        animationSpec = tween(
                            durationMillis = 2400,
                            easing = CubicBezierEasing(0.42f, 0f, 0.58f, 1f)
                        )
                    )
                }

                // Wait for UI fades to finish; pan and zoom continue in parallel
                fadeWelcome.join()
                fadeLogin.join()
                pan.join(); zoom.join()

                navigateToDreamJournalScreen()
            }
        },
    )

    // Re-layer the scene: background image, shooting star (background), then foreground UI
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.onboarding_long),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = cameraScale.value,
                    scaleY = cameraScale.value,
                    transformOrigin = TransformOrigin(0.5f, 1f) // anchor at bottom center
                )
                .zIndex(0f),
            contentScale = ContentScale.Crop,
            alignment = VerticalBiasAlignment(cameraBiasY.value),
            contentDescription = "DreamNorth",
        )

        // Compute pan progress from camera bias (-1..1 -> 0..1)
        val panProgress = ((cameraBiasY.value + 1f) / 2f).coerceIn(0f, 1f)
        // Subtle twinkles behind UI and behind the shooting star; drift upward during pan
        TwinklesLayer(
            twinkleCount = 34,
            panProgress = panProgress,
            driftUpFraction = 1.3f,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0.05f)
        )

        // Shooting star behind UI, above twinkles and background image
        ShootingStarLayer(
            trigger = shootingStarTrigger.value,
            starColor = Color(0xFFB49CFF),
            trailMaxAlpha = 0.75f,
            trailCount = 28,
            trailStep = 0.028f,
            durationMs = 1250,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0.1f)
        )

        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f)
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Welcome box: fixed outer height, inner height expands from center
                if (showWelcomeBox.value) {
                    val welcomeTargetHeight = 70.dp
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(top = 24.dp, bottom = 16.dp, start = 32.dp, end = 32.dp)
                            .fillMaxWidth()
                            .height(welcomeTargetHeight)
                            .graphicsLayer(alpha = welcomeBoxAlpha.value)
                    ) {
                        val boxHeight = lerp(0.dp, welcomeTargetHeight, boxExpand.value)
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(boxHeight)
                                .graphicsLayer(
                                    transformOrigin = TransformOrigin(0.5f, 0.5f)
                                )
                                .background(
                                    color = LightBlack.copy(alpha = 0.7f),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            if (startTypewriter.value) {
                                Text(
                                    text = "", // hold space with padding for typewriter
                                    modifier = Modifier.padding(16.dp)
                                )
                                TypewriterText(
                                    text = if (visible.value) titleText.value else "DreamNorth",
                                    modifier = Modifier.padding(16.dp),
                                    style = TextStyle(
                                        color = titleColor,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    textAlign = TextAlign.Center,
                                    animationDuration = 4000,
                                    onAnimationComplete = {
                                        scope.launch {
                                            // Reveal login UI only after typewriter completes
                                            showLoginLayout.value = true
                                            showSubheader.value = true
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                // Reduce big gap: replace weighted spacer with a small fixed spacer
                Spacer(modifier = Modifier.height(2.dp))

                // Login section container: fixed height to avoid layout bumps
                val loginContainerHeight = 1000.dp
                AnimatedVisibility(
                    visible = showLoginLayout.value,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(200))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(loginContainerHeight)
                            .graphicsLayer(alpha = loginContainerAlpha.value),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalArrangement = Arrangement.Top,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Bouncy onboarding design above the whole layout
                            val onboardingScale = remember { Animatable(0f) }
                            LaunchedEffect(showLoginLayout.value) {
                                if (showLoginLayout.value) {
                                    onboardingScale.snapTo(0f)
                                    // slower pop-up: brief delay then softer spring
                                    delay(180)
                                    onboardingScale.animateTo(
                                        targetValue = 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessVeryLow
                                        )
                                    )
                                    // Mark image finished to kick off uniform stagger chain
                                    onboardingImageEntered.value = true
                                } else {
                                    onboardingScale.snapTo(0f)
                                    onboardingImageEntered.value = false
                                }
                            }
                            val floatTransition =
                                rememberInfiniteTransition(label = "onboarding_float")
                            val floatOffset by floatTransition.animateFloat(
                                initialValue = 0f,
                                targetValue = -8f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(
                                        durationMillis = 1600,
                                        easing = FastOutSlowInEasing
                                    ),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "onboarding_offset"
                            )

                            Image(
                                painter = painterResource(Res.drawable.dream_onboarding_design),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxWidth(0.99f)
                                    .padding(top = 0.dp, bottom = 8.dp)
                                    .graphicsLayer { }
                                    .align(Alignment.CenterHorizontally)
                                    .scale(onboardingScale.value)
                                    .offset(y = floatOffset.dp)
                            )

                            // Tabs row slides in first
                            if (loginViewModelState.isLoginLayout) {
                                AnimatedVisibility(
                                    visible = vTabs.value,
                                    enter = slideInHorizontally(animationSpec = tween(ENTER_DURATION_MS), initialOffsetX = { 1000 }),
                                    exit = if (isPanningDown.value) {
                                        fadeOut(animationSpec = tween(durationMillis = 220))
                                    } else {
                                        slideOutHorizontally(animationSpec = tween(ENTER_DURATION_MS), targetOffsetX = { -1000 })
                                    }
                                ) {
                                    SignupLoginTabLayout(
                                        loginViewModelState = loginViewModelState,
                                        onLayoutChange = onLoginEvent
                                    )
                                }
                            }

                            // Login/Signup content
                            if (loginViewModelState.isLoginLayout) {
                                LoginLayout(
                                    loginViewModelState = loginViewModelState,
                                    onLoginEvent = onLoginEvent,
                                    onAnimationComplete = { /* handled by scheduler */ },
                                    shouldAnimate = true,
                                    staggerMillis = STAGGER_MS,
                                    emailVisible = vEmail,
                                    passwordVisible = vPassword,
                                    loginButtonVisible = vLoginBtn,
                                    useExternalStagger = true,
                                    preferFadeExit = isPanningDown.value,
                                )
                            } else if (loginViewModelState.isSignUpLayout || loginViewModelState.isForgotPasswordLayout) {
                                SignupLoginLayout(
                                    loginViewModelState = loginViewModelState,
                                    signupViewModelState = signupViewModelState,
                                    onLoginEvent = onLoginEvent,
                                    onSignupEvent = onSignupEvent
                                )
                            }
                        }

                        // Bottom overlay for Google and Guest (stable position)
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .height(150.dp)
                                .padding(horizontal = 24.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // small spacer to reduce distance from login button consistently
                            Spacer(modifier = Modifier.height(4.dp))
                            AnimatedVisibility(
                                visible = vGoogle.value,
                                enter = slideInHorizontally(animationSpec = tween(ENTER_DURATION_MS), initialOffsetX = { 1000 }),
                                exit = if (isPanningDown.value) {
                                    fadeOut(animationSpec = tween(durationMillis = 220))
                            } else {
                                slideOutHorizontally(animationSpec = tween(ENTER_DURATION_MS), targetOffsetX = { -1000 })
                            }
                        ) {
                            MyGoogleSignInButton(
                                { account ->
                                    val googleCredential =
                                        dev.gitlive.firebase.auth.GoogleAuthProvider.credential(
                                            idToken = account.idToken,
                                            accessToken = account.accessTokenOrNonce
                                        )
                                    onLoginEvent(LoginEvent.SignInWithGoogle(googleCredential))
                                },
                                {
                                    onLoginEvent(LoginEvent.ToggleLoading(false))
                                    println("Google sign-in error: $it")
                                },
                                isLoading
                            )
                        }

                            if (!isUserAnonymous) {
                                AnonymousButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    isVisible = vGuest.value,
                                    onClick = { onSignupEvent(SignupEvent.AnonymousSignIn) },
                                    isEnabled = !isLoading,
                                    enterDurationMillis = ENTER_DURATION_MS,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Single uniform stagger sequence: Tabs -> Email -> Password -> Login -> Google -> Guest
    LaunchedEffect(onboardingImageEntered.value) {
        if (onboardingImageEntered.value) {
            vTabs.value = true
            delay(STAGGER_MS)
            vEmail.value = true
            delay(STAGGER_MS)
            vPassword.value = true
            delay(STAGGER_MS)
            vLoginBtn.value = true
            delay(STAGGER_MS)
            vGoogle.value = true
            delay(STAGGER_MS)
            vGuest.value = true
            // After the entire chain finishes, trigger a one-off shooting star
            delay(220)
            shootingStarTrigger.value = shootingStarTrigger.value + 1
        } else {
            vTabs.value = false
            vEmail.value = false
            vPassword.value = false
            vLoginBtn.value = false
            vGoogle.value = false
            vGuest.value = false
        }
    }

    // Overlay: quick shooting star pass once the chain is done
    // ShootingStarLayer(trigger = shootingStarTrigger.value)
}
