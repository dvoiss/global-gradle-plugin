package com.dvoiss.globalplugins

import java.util.function.Predicate

class GlobalDependency {
    String classpath
    String id
    Predicate predicate

    public GlobalDependency(String classpath, String id, Predicate predicate) {
        this.classpath = classpath
        this.id = id
        this.predicate = predicate
    }
}