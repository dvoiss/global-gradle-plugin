[![build status][travis-image]][travis-url]
[travis-image]: https://img.shields.io/travis/dvoiss/global-gradle-plugin.svg?style=flat-square
[travis-url]: https://travis-ci.org/dvoiss/global-gradle-plugin

Global Gradle Plugin Applier
============================

This plugin applies specified plugins to *all* gradle projects run on your machine. This is useful for using plugins without needing to add them to every project and without adding them to version control. See examples below for more information.

**Important Note**: Be careful with the plugins you add. Builds should be portable and consistent and certain plugins could have side effects. In the examples below the plugins used add reporting tasks and extra information to the output.

Usage
-----

Open the `~/.gradle/init.gradle` file and add the code below.

```groovy
initscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "gradle.plugin.com.dvoiss.globalplugins:global-gradle-plugin:1.0"
    }
}

apply plugin: com.dvoiss.globalplugins.GlobalDependencyPlugin
```

Then use the plugin's `globalDependencies` closure inside the `allprojects` block to apply the dependencies to all your projects (see the examples below).

The `globalDependencies` closure has the following options available:

  * `repos`: A closure specifying the repos for your plugins (`jcenter()`, `mavenLocal()`, `maven { url ... }`).
  * `add`: A method that takes the `classpath` for your plugin as the first parameter and the plugin ID as the second. There is an optional third parameter that takes a predicate (see example below).

If you don't want to use `init.gradle` a custom file under `~/.gradle/init.d/` (such as `~/.gradle/init.d/global-plugins.gradle`) can be used instead.

Examples
--------

In the example below I add the [godot plugin](https://github.com/hannesstruss/godot) so that *all* my gradle projects *anywhere* on my machine will log build times in the background and also have a task specified by godot to generate a report that tells me how long I spend per week, month, etc. waiting for the given project to build.

This is an example of a plugin you may want to use, but don't want to individually add to each project and also don't always want to commit to version control when your team may not use it or may have their own plugins that they prefer.

```groovy
initscript {
    repositories {
        maven {
            url "https://plugins.gradle.org/m2/"
        }
    }
    dependencies {
        classpath "gradle.plugin.com.dvoiss.globalplugins:global-gradle-plugin:1.0"
    }
}

apply plugin: com.dvoiss.globalplugins.GlobalDependencyPlugin

allprojects {
    globalDependencies {
        // Use the repos block to add the repos needed for the plugins: jcenter, maven, mavenLocal, etc.
        repos {
            jcenter()
        }

        // Pass the plugin's classpath and plugin ID to the add method and it will now be used in every project.
        add "de.hannesstruss:godot:+", 'de.hannesstruss.godot'
    }
}
```

In this extended Example a predicate is passed to the `add` method to check for the plugin IDs of the Android plugins.

This is necessary so the dexcount plugin is only applied after the Android plugins are configured. The [dexcount plugin](https://github.com/KeepSafe/dexcount-gradle-plugin) will be applied to *all* Android projects and will log the dex-count of each project automatically. Trying to apply an Android plugin before Android is configured would result in an error.

```groovy
allprojects {
    globalDependencies {
        // The Android dexcount plugin needs to be applied after the plugins with these IDs are configured, 
        // we will pass this predicate into the add method below.
        def afterAndroidPluginPredicate = { plugin -> 
            androidPlugins.contains([
                'com.android.build.gradle.AppPlugin',
                'com.android.build.gradle.LibraryPlugin',
                'com.android.build.gradle.TestPlugin'
            ])
        }

        // The following "dexcount" plugin is for Android projects only and so will need to be applied after 
        // Android configuration has taken place. The predicate above is passed as an optional third parameter here. 
        add 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.5.6', 'com.getkeepsafe.dexcount', afterAndroidPluginPredicate
    }
}
```

The above example has a convenient built in method for Android that does the same as the code above:

```groovy
allprojects {
    globalDependencies {
        addAndroid 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.5.6', 'com.getkeepsafe.dexcount'
    }
}
```

Notes
-----

Gradle 2.1 introduced a `plugins` block that can be used to add plugins. This will not work with this plugin because the `plugins` block needs to be in an individual project's `buildscript` block whereas this plugin works from the `allprojects` block. 

"Why not just use `apply from` in the `allprojects`?" This will not work for `allprojects` as expected. Some solutions suggest creating a common script to use with `apply from` and to add that to a `buildscript` in each project's gradle `buildscript`. This would mean committing to version control and adding to every project which isn't desirable. Read the links below for more information.

1. [How do I apply a plugin to a project from a (shared) applied .gradle file?](https://discuss.gradle.org/t/how-do-i-apply-a-plugin-to-a-project-from-a-shared-applied-gradle-file/7508). To apply a plugin from an external buildscript, our `init.gradle` in this case, we have to use the fully qualified class name (`com.dvoiss.globalplugins.GlobalDependencyPlugin`) instead of the ID (`com.dvoiss.globalplugins`).
2. [Applying plugin dependencies from external gradle file](https://discuss.gradle.org/t/applying-plugin-dependencies-from-external-gradle-file/4720). Some options include creating a custom Gradle distribution.
3. [Cannot load custom plugin (from outside distribution) to project from an init script](https://issues.gradle.org/browse/GRADLE-2407). There is an open ticket on gradle.org that may obsolete this plugin once resolved.