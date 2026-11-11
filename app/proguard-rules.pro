# Project-specific R8 rules for Seriee.
#
# Compose, AndroidX, Kotlin, and Coil ship their own consumer rules, so no
# additional keep rules are required for them. Keep the metadata below so
# release crash reports remain readable.

# Keep source file names and line numbers, but do not expose local file paths.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# The app serializes its data manually with org.json; no reflection is used,
# so model classes may be shrunk and renamed freely.
