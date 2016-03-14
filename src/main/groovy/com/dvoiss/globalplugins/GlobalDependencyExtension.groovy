package com.dvoiss.globalplugins

import java.util.function.Predicate

import static com.dvoiss.globalplugins.GlobalDependencyUtils.isAndroid

class GlobalDependencyExtension {
    List<GlobalDependency> dependencies = new LinkedList()
    Closure reposClosure

    void repos(Closure reposClosure) {
        this.reposClosure = reposClosure
    }

    void add(String classpath, String id) {
        add(classpath, id, null)
    }

    void addAndroid(String classpath, String id) {
        dependencies.add(new GlobalDependency(classpath, id, isAndroid))
    }

    void add(String classpath, String id, Predicate predicate) {
        dependencies.add(new GlobalDependency(classpath, id, predicate))
    }

    List<String> getClasspaths() {
        return dependencies.collect({ it.classpath })
    }

    List<String> getIds() {
        return dependencies.collect({ it.id })
    }
}