package com.uxplima.configupdater;

public interface UpdateProdiver {

    boolean checkVersion();

    void update(ConfigUpdater updater);

}