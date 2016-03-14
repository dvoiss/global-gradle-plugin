package com.dvoiss.globalplugins

import java.util.function.Predicate

class GlobalDependencyUtils {

    public static List<String> androidPlugins = [
            'com.android.build.gradle.AppPlugin',
            'com.android.build.gradle.LibraryPlugin',
            'com.android.build.gradle.TestPlugin'
    ]

    public static Predicate isAndroid = { plugin -> androidPlugins.contains(plugin) }

}