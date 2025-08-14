package com.zakafir.presentation.util

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import kotlin.jvm.JvmSuppressWildcards
import kotlin.reflect.KType

fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() = slideIntoContainer(
    AnimatedContentTransitionScope.SlideDirection.Start,
    tween(NAV_TRANSITION_TWEEN_IN_MS, delayMillis = NAV_TRANSITION_TWEEN_IN_MS),
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() = slideOutOfContainer(
    AnimatedContentTransitionScope.SlideDirection.Start,
    tween(NAV_TRANSITION_TWEEN_IN_MS),
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition() = slideIntoContainer(
    AnimatedContentTransitionScope.SlideDirection.End,
    tween(NAV_TRANSITION_TWEEN_IN_MS),
)

fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition() = slideOutOfContainer(
    AnimatedContentTransitionScope.SlideDirection.End,
    tween(NAV_TRANSITION_TWEEN_IN_MS),
)

const val NAV_TRANSITION_TWEEN_IN_MS = 300

inline fun <reified T : Any> NavGraphBuilder.composableWithTransitions(
    typeMap: Map<KType, @JvmSuppressWildcards NavType<*>> = emptyMap(),
    noinline enterTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() ->
        @JvmSuppressWildcards EnterTransition?
    )? = { enterTransition() },
    noinline popEnterTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() ->
        @JvmSuppressWildcards EnterTransition?
    )? = null,
    noinline exitTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() ->
        @JvmSuppressWildcards ExitTransition?
    )? = null,
    noinline popExitTransition: (
        AnimatedContentTransitionScope<NavBackStackEntry>.() ->
        @JvmSuppressWildcards ExitTransition?
    )? = { popExitTransition() },
    noinline content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = T::class,
        typeMap = typeMap,
        deepLinks = emptyList(),
        enterTransition = enterTransition,
        popEnterTransition = popEnterTransition,
        exitTransition = exitTransition,
        popExitTransition = popExitTransition,
        sizeTransform = null,
        content = content,
    )
}
