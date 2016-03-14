package com.dvoiss.globalplugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.initialization.dsl.ScriptHandler
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.api.plugins.BasePlugin

import java.lang.reflect.Field
import java.util.function.Predicate

class GlobalDependencyPlugin implements Plugin<Gradle> {

    private static final String EXTENSION = "globalDependencies"

    private static final Logger log = Logging.getLogger(GlobalDependencyPlugin)

    @Override
    void apply(Gradle gradle) {
        gradle.allprojects { project ->
            GlobalDependencyExtension extension = project.getExtensions().create(EXTENSION, GlobalDependencyExtension)

            if (!extension) {
                log.warn "Could not find \"globalDependencies\" extension."
                return
            }

            boolean classPathsConfigured = false
            boolean reposConfigured = false
            boolean basePluginsAdded = false

            plugins.whenPluginAdded { plugin ->
                if (plugin instanceof BasePlugin) {
                    basePluginsAdded = true
                }

                if (!reposConfigured) {
                    reposConfigured = true
                    addRepositories(extension, project)
                }

                if (!classPathsConfigured) {
                    classPathsConfigured = true
                    addClasspaths(extension, project)
                }

                if (basePluginsAdded) {
                    addDependencies(extension, project, plugin)
                }
            }
        }
    }

    /**
     * Add project dependencies if they have not been added already and their predicates return true.
     */
    private static void addDependencies(GlobalDependencyExtension extension,
                                        Project project,
                                        Plugin plugin) {
        if (!project.plugins.findPlugin(plugin.class.name)) {
            extension.getDependencies().each { dependency ->
                if (shouldApplyPlugin(dependency, plugin.class.name)) {
                    project.apply plugin: dependency.getId()
                }
            }
        } else {
            log.info "Not applying plugin: ${plugin.class.name}. Plugin may have already been applied elsewhere."
        }
    }

    /**
     * Get the build-script so that dependencies can be added to it.
     *
     * Derived from: http://stackoverflow.com/a/34818018.
     */
    private static Object getAccessibleBuildScript(Project project) throws Exception {
        project.getBuildScriptSource()

        ScriptHandler bs = project.getBuildscript()
        bs.getDependencies()

        Field ccf = bs.class.getDeclaredField("classpathConfiguration")
        ccf.setAccessible(true)

        return ccf.get(bs)
    }

    /**
     * Add classpaths specified in the extension to the project's classpaths.
     *
     * Derived from: http://stackoverflow.com/a/34818018.
     */
    private static void addClasspaths(GlobalDependencyExtension extension, Project project) {
        Object cc = null
        try {
            cc = getAccessibleBuildScript(project)
        } catch (Exception e) {
            log.error "Error: Could not get accessible build script."
            log.error e.message
            return
        }

        if (extension.getDependencies().isEmpty()) {
            log.info "No global dependencies found."
            return
        }

        extension.getClasspaths().each { c ->
            cc.dependencies.add(project.dependencies.create(c))
        }
    }

    /**
     * Add repositories specified in the extension to the repository handler.
     *
     * Derived from: http://stackoverflow.com/a/34818018.
     */
    private static void addRepositories(GlobalDependencyExtension extension, Project project) {
        if (extension?.reposClosure) {
            project.getBuildScriptSource()
            ScriptHandler bs = project.getBuildscript()
            extension.reposClosure.delegate = bs.getRepositories()
            extension.reposClosure.run()
        }
    }

    /**
     * If a predicate exists test whether the plugin should be applied, otherwise return true.
     */
    private static boolean shouldApplyPlugin(GlobalDependency dependency, String plugin) {
        Predicate predicate = dependency.getPredicate()
        return predicate == null || predicate.test(plugin)
    }
}