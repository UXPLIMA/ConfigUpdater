package com.uxplima.configupdater;

public interface UpdateProvider {

    boolean shouldUpdate(ConfigUpdater configUpdater);

    void update(ConfigUpdater updater);

    default int configVerToInt(String ver) {
        return Integer.parseInt(ver.replace(".", "").replace("-SNAPSHOT", ""));
    }

}