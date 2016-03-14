package com.dvoiss.globalplugins

class GlobalDependencyExtension {
    Map<String, String> dependencies = new LinkedHashMap()
    def callback

    private Closure<Void> configClosure

    void add(Map<String, String> dependencies) {
        this.dependencies.putAll(dependencies)
    }

    void onApply(def callback) {
        this.callback = callback
    }

    boolean shouldApply(String pluginClassAdded, String pluginId) {
        return callback == null || callback(pluginClassAdded, pluginId)
    }
}