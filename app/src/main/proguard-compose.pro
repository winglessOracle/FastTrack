# Jetpack Compose ProGuard Rules

# Keep `Companion` object fields of serializable classes.
# This avoids serializer lookup through `getDeclaredClasses` as done for named companion objects.
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

# Keep `serializer()` on companion objects (both default and named) of serializable classes.
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Compose lambdas and anonymous functions
-keep class kotlin.jvm.functions.Function* { *; }
-keep class kotlin.coroutines.Continuation { *; }

# Keep Compose animation
-keep class androidx.compose.animation.** { *; }

# Keep Compose foundation
-keep class androidx.compose.foundation.** { *; }

# Keep Compose layout
-keep class androidx.compose.foundation.layout.** { *; }

# Keep Compose material
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }

# Keep Compose runtime
-keep class androidx.compose.runtime.** { *; }

# Keep Compose UI
-keep class androidx.compose.ui.** { *; }

# Keep Compose graphics
-keep class androidx.compose.ui.graphics.** { *; }

# Keep Compose text
-keep class androidx.compose.ui.text.** { *; }

# Keep Compose unit
-keep class androidx.compose.ui.unit.** { *; }

# Keep Compose geometry
-keep class androidx.compose.ui.geometry.** { *; }

# Keep Compose state
-keep class androidx.compose.runtime.snapshots.** { *; }
-keep class androidx.compose.runtime.internal.** { *; }

# Keep Compose saveable
-keep class androidx.compose.runtime.saveable.** { *; }

# Keep Compose tooling
-keep class androidx.compose.ui.tooling.** { *; }
-keep class androidx.compose.ui.tooling.preview.** { *; }

# Keep Compose animation core
-keep class androidx.compose.animation.core.** { *; }

# Keep Compose foundation gestures
-keep class androidx.compose.foundation.gestures.** { *; }

# Keep Compose foundation interaction
-keep class androidx.compose.foundation.interaction.** { *; }

# Keep Compose foundation lazy
-keep class androidx.compose.foundation.lazy.** { *; }

# Keep Compose foundation pager
-keep class androidx.compose.foundation.pager.** { *; }

# Keep Compose foundation text
-keep class androidx.compose.foundation.text.** { *; }

# Keep Compose material icons
-keep class androidx.compose.material.icons.** { *; }

# Keep Compose material ripple
-keep class androidx.compose.material.ripple.** { *; }

# Keep Compose material snackbar
-keep class androidx.compose.material.snackbar.** { *; }

# Keep Compose material swipeable
-keep class androidx.compose.material.swipeable.** { *; }

# Keep Compose UI util
-keep class androidx.compose.ui.util.** { *; }

# Keep Compose UI viewbinding
-keep class androidx.compose.ui.viewbinding.** { *; }

# Keep Compose UI window
-keep class androidx.compose.ui.window.** { *; }

# Keep Compose UI platform
-keep class androidx.compose.ui.platform.** { *; }

# Keep Compose UI node
-keep class androidx.compose.ui.node.** { *; }

# Keep Compose UI layout
-keep class androidx.compose.ui.layout.** { *; }

# Keep Compose UI input
-keep class androidx.compose.ui.input.** { *; }

# Keep Compose UI focus
-keep class androidx.compose.ui.focus.** { *; }

# Keep Compose UI draw
-keep class androidx.compose.ui.draw.** { *; }

# Keep Compose UI semantics
-keep class androidx.compose.ui.semantics.** { *; }

# Keep Compose UI state
-keep class androidx.compose.ui.state.** { *; }

# Keep Compose UI text selection
-keep class androidx.compose.ui.text.selection.** { *; }

# Keep Compose UI text input
-keep class androidx.compose.ui.text.input.** { *; }

# Keep Compose UI text font
-keep class androidx.compose.ui.text.font.** { *; }

# Keep Compose UI text style
-keep class androidx.compose.ui.text.style.** { *; } 