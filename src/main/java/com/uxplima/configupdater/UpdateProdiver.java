package com.uxplima.configupdater;

public interface UpdateProdiver {

    boolean check(ConfigUpdater configUpdater);

    void update(ConfigUpdater updater);

    default int configVerToInt(String ver) {
        return Integer.parseInt(ver.replace(".", ""));
    }

}