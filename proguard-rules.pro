# Ochrona klas i interfejsów Compose DrawScope oraz grafiki przed agresywną optymalizacją R8
-keep class androidx.compose.ui.graphics.** { *; }
-keep interface androidx.compose.ui.graphics.** { *; }

# Ogólna ochrona Jetpack Compose przed zmianą sygnatur metod
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod