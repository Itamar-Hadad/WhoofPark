// Top-level build file where you can add configuration options common to all sub-projects/modules.
// Using explicit versions here so the root script does not depend on the version catalog (fixes sync when libs is unresolved).
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
