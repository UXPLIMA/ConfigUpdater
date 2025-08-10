package com.uxplima.configupdater;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Logger;

public class ConfigUpdater {

    private final JavaPlugin plugin;
    private final FileConfiguration pluginConfig;
    private final Logger logger;
    private final String configVersion;
    private final String jarVersion;
    private final Collection<String> files;
    private final Collection<UpdateProdiver> updateProdivers;
    private final long backupStart = System.currentTimeMillis();

    private Map<String, FileConfiguration> diskConfigs = new HashMap<>();
    private Map<String, FileConfiguration> resourceConfigs = new HashMap<>();

    ConfigUpdater(JavaPlugin plugin, Collection<String> files, String configVersion, String jarVersion, Collection<UpdateProdiver> updateProdivers) {
        this.plugin = plugin;
        this.pluginConfig = plugin.getConfig();
        this.files = files;
        this.updateProdivers = updateProdivers;
        this.logger = plugin.getLogger();
        this.configVersion = configVersion;
        this.jarVersion = jarVersion;
    }

    public void update() {
        if (configVersion.equalsIgnoreCase(jarVersion)) return;

        logger.info("New version detected!");
        logger.info("Starting to update config files...\n");

        backupFiles();
        cacheFiles();
        runProvidedUpdates();
        merge();
    }

    private void backupFiles() {
        for (String file : files) {
            file = file.replace("%lang%", pluginConfig.getString("language"));
            file = file + ".yml";

            File diskFile = new File(plugin.getDataFolder(), file);
            if (!diskFile.exists()) {
                continue;
            }

            backup(file, diskFile);
        }
        logger.info("Backed up old files.\n");
    }

    private void cacheFiles() {
        for (String file : files) {
            file = file.replace("%lang%", pluginConfig.getString("language"));
            file = file + ".yml";

            File diskFile = new File(plugin.getDataFolder(), file);
            if (!diskFile.exists()) {
                continue;
            }

            YamlConfiguration diskConfig = YamlConfiguration.loadConfiguration(diskFile);
            YamlConfiguration resourceConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(file)));

            diskConfigs.put(file, diskConfig);
            resourceConfigs.put(file, resourceConfig);
        }
    }

    private void runProvidedUpdates() {
        for (UpdateProdiver prodiver : updateProdivers) {
            if (!prodiver.checkVersion(this)) continue;

            prodiver.update(this);
        }
    }

    private void merge() {
        cacheFiles(); // Cache again since providers may have changed the files.

        for (String file : files) {
            file = file.replace("%lang%", pluginConfig.getString("language"));
            file = file + ".yml";

            File diskFile = new File(plugin.getDataFolder(), file);
            if (!diskFile.exists()) {
                continue;
            }

            FileConfiguration diskConfig = diskConfigs.get(file);
            FileConfiguration resourceConfig = resourceConfigs.get(file);

            if (diskConfig == null || resourceConfig == null) continue;

            logger.info("Trying to update " + file);

            boolean anyChange = false;

            for (String key : resourceConfig.getKeys(true)) {
                if (!diskConfig.isSet(key)) {
                    diskConfig.set(key, resourceConfig.get(key));
                    diskConfig.setComments(key, resourceConfig.getComments(key));
                    diskConfig.setInlineComments(key, resourceConfig.getInlineComments(key));
                    anyChange = true;
                }
            }

            if (file.equals("config.yml")) {
                diskConfig.set("config-version", jarVersion);
                save(diskFile, diskConfig);

                logger.info("Updated " + file + "!\n");
                continue;
            }

            if (!anyChange) {
                logger.info("No changes in " + file + ", skipping...\n");
                continue;
            }

            save(diskFile, diskConfig);
            logger.info("Updated " + file + "!\n");
        }
    }

    private void backup(String name, File file) {
        try {
            File backupFile = new File(plugin.getDataFolder(), "backup-" + backupStart + "/" + name);
            backupFile.getParentFile().mkdirs();
            backupFile.createNewFile();
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void save(File file, FileConfiguration config) {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileConfiguration getDiskConfigFrom(String file) {
        return diskConfigs.get(file);
    }

    public FileConfiguration getResourceConfigFrom(String file) {
        return resourceConfigs.get(file);
    }

    public Collection<String> getFiles() {
        return files;
    }

    public Map<String, FileConfiguration> getDiskConfigs() {
        return diskConfigs;
    }

    public Map<String, FileConfiguration> getResourceConfigs() {
        return resourceConfigs;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public static ConfigUpdaterBuilder builder(JavaPlugin plugin) {
        return new ConfigUpdaterBuilder(plugin);
    }

}