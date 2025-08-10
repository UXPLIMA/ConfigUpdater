package com.uxplima.configupdater;

public interface UpdateProdiver {

    boolean checkVersion(ConfigUpdater configUpdater);

    void update(ConfigUpdater updater);

}