package com.dvoiss.globalplugins

import org.gradle.api.Plugin
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

class GlobalDependencyPlugin implements Plugin<Gradle> {
    final Logger log = Logging.getLogger(GlobalDependencyPlugin)

    void apply(Gradle gradle) {
        // TODO:
        // I want to be able to load a file, from local or remote URL,
        // that has my config which contains:
        //
        boolean configured = false

        gradle.allprojects { project ->
            extensions.create('globalDependencies', GlobalDependencyExtension)
            extensions.create('androidDependencies', GlobalDependencyExtension)
        }

        gradle.allprojects { project ->
            plugins.whenPluginAdded { plugin ->
                if (!configured) {
                    println "Configuring classpaths"

                    // TODO:
                    // move this configured block out of whenPluginAdded

                    configured = true

                    // TODO: is this line needed?
                    project.getBuildScriptSource()
                    // get the buildscript
                    def bs = project.getBuildscript()
                    // TODO: is this line needed?
                    bs.getDependencies()
                    // get classpath config
                    def ccf = bs.class.getDeclaredField("classpathConfiguration")
                    // set accessible
                    ccf.setAccessible(true)
                    // get accessible build-script
                    def cc = ccf.get(bs)

                    if (!project.globalDependencies.dependencies) {
                        println "Global dependencies not found!"
                        return
                    }

                    def classpaths = globalDependencies.dependencies.keySet()
                    classpaths.each { c ->
                        cc.dependencies.add(project.dependencies.create(c))
                    }

                    classpaths = androidDependencies.dependencies.keySet()
                    classpaths.each { c ->
                        cc.dependencies.add(project.dependencies.create(c))
                    }
                }

                if (configured) {
                    // If the plugin is an Android plugin, then apply the Android dependency plugins.
                    if (!project.plugins.findPlugin(plugin.class.name)) {
                        println ""
                        println "PLUGIN ADDED: $plugin"
                        println "WILL ADD? ${!project.plugins.findPlugin(plugin.class.name)}"
                        println ""

                        def pluginIds = globalDependencies.dependencies.values()
                        pluginIds.each { pluginId ->
                            if (globalDependencies.shouldApply(plugin.class.name, pluginId)) {
                                project.apply plugin: pluginId
                            }
                        }
                    } else {
                        println "Plugin: ${plugin.class.name}"
                    }
                }
            }
        }
    }
}