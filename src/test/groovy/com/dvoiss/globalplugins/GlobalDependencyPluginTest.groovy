package com.dvoiss.globalplugins

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertTrue

class GlobalDependencyPluginTest {

    Gradle gradle

    @Before
    public void setupProject() {
        Project project = ProjectBuilder.builder().build()
        gradle = project.getGradle()
    }

    @Test
    public void globalDependencyPluginIsAddedToProject() {
        gradle.getPluginManager().apply GlobalDependencyPlugin
        assertTrue(gradle.getPluginManager().hasPlugin("com.dvoiss.globalplugins"))
    }
}