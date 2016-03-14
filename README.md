Global Gradle Plugin Applier
============================

This plugin will apply specified gradle plugins to all gradle projects run on your machine. To do this it must be added in an `initscript` at `~/.gradle/init.gradle` or in a custom file under `~/.gradle/init.d/` (such as `~/.gradle/init.d/global-plugins.gradle`).

Usage
-----

### Gradle 2.1 and above
In your init file add the following:

```groovy
plugins {
    id "com.dvoiss.globalplugins" version "1.0-SNAPSHOT"
}
```

### Gradle 1.x/2.0
In your init file add the following:

```groovy
initscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath "com.dvoiss.globalplugins:global-gradle-plugin:1.0-SNAPSHOT"
    }
}

apply plugin: com.dvoiss.globalplugins.GlobalDependencyPlugin
```

Then use the `globalDependencies` closure in an `allprojects` closure so the dependencies are available in all your projects. See the example below. The `globalDependencies` closure has the following options available:

  * `repos`: A closure specifying the repos for your plugins (jcenter(), mavenLocal(), maven { url ... }).
  * `add`: A method that takes the `classpath` for your plugin as the first parameter and the plugin ID as the second.
    * Another `add` variant accepts a predicate as a third argument. This predicate takes the name of another plugin when that plugin is applied as a condition for when to apply the plugin being passed in. See the example below.
  * `addAndroid`: Add the `classpath` and plugin ID as the first and second arguments. This is a short-hand method that uses a predicate to dictate that the plugin being passed in should only be applied after Android configuration is complete.

Example:

```groovy
apply plugin: com.dvoiss.globalplugins.GlobalDependencyPlugin

// add dependencies to all projects
allprojects {
    globalDependencies {

        // repos for plugins added below, add other repos including mavenLocal() for local plugins

        repos {
            jcenter()
        }

        // add <PLUGIN CLASSPATH>, <PLUGIN ID>

        add "de.hannesstruss:godot:+", 'de.hannesstruss.godot'

        // android dependencies, these should only be applied after
        // the android plugins or else an error will occur

        def androidPlugins = [
            'com.android.build.gradle.AppPlugin',
            'com.android.build.gradle.LibraryPlugin',
            'com.android.build.gradle.TestPlugin'
        ]

        // Is the plugin that is passed into the predicate
        // one of the android plugins in the list above?

        def afterAndroidPluginPredicate = { plugin -> androidPlugins.contains(plugin) }

        // Pass this predicate as the third argument so that this android plugin
        // is only ran after android configuration has taken place

        add 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.4.2', 'com.getkeepsafe.dexcount', afterAndroidPluginPredicate

        // special short-hand for Android, this does the same thing as above

        addAndroid 'com.getkeepsafe.dexcount:dexcount-gradle-plugin:0.4.2', 'com.getkeepsafe.dexcount'
    }
}

```