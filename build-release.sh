#!/bin/sh

ant clean release

APK=bin/.-unsigned.apk
jarsigner -verbose -keystore ~/android-release-key.keystore $APK android_release_key
jarsigner -verify $APK
zipalign -v 4 $APK valtech-droid-aligned-release.apk
