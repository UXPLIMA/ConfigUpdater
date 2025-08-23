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
    private final boolean mergeMissingNodes, deleteUnknownNodes, updateConfigVersion;
    private final long backupStart = System.currentTimeMillis();

    private Map<String, FileConfiguration> diskConfigs = new HashMap<>();
    private Map<String, FileConfiguration> resourceConfigs = new HashMap<>();

    ConfigUpdater(JavaPlugin plugin, Collection<String> files,
                  String configVersion, String jarVersion,
                  Collection<UpdateProdiver> updateProdivers, boolean mergeMissingNodes,
                  boolean deleteUnknownNodes, boolean updateConfigVersion) {
        this.plugin = plugin;
        this.pluginConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        this.files = files;
        this.logger = plugin.getLogger();
        this.configVersion = configVersion;
        this.jarVersion = jarVersion;
        this.updateProdivers = updateProdivers;
        this.mergeMissingNodes = mergeMissingNodes;
        this.updateConfigVersion = updateConfigVersion;
        this.deleteUnknownNodes = deleteUnknownNodes;
    }

    public void update() {
        if (configVersion.equalsIgnoreCase(jarVersion)) return;

        logger.info("New version detected!");
        logger.info("Starting to update config files...\n");

        backupFiles();
        cacheFiles();
        runProvidedUpdates();
        updateConfigVersion();
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
            if (!prodiver.check(this)) continue;

            prodiver.update(this);
        }
    }

    private void updateConfigVersion() {
        if (!updateConfigVersion) return;

        File diskFile = new File(plugin.getDataFolder(), "config.yml");
        if (!diskFile.exists()) return;

        FileConfiguration diskConfig = YamlConfiguration.loadConfiguration(diskFile);
        diskConfig.set("config-version", jarVersion);
        save(diskFile, diskConfig);

        logger.info("Updated config version!\n");
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

            if (mergeMissingNodes) {
                for (String key : resourceConfig.getKeys(true)) {
                    if (!diskConfig.isSet(key)) {
                        diskConfig.set(key, resourceConfig.get(key));
                        diskConfig.setComments(key, resourceConfig.getComments(key));
                        diskConfig.setInlineComments(key, resourceConfig.getInlineComments(key));
                        anyChange = true;
                    }
                }
            }

            if (deleteUnknownNodes) {
                for (String key : diskConfig.getKeys(true)) {
                    if (!resourceConfig.isSet(key)) {
                        diskConfig.set(key, null);
                        diskConfig.setComments(key, null);
                        diskConfig.setInlineComments(key, null);
                        anyChange = true;
                    }
                }
            }

            if (anyChange) {
                save(diskFile, diskConfig);
            }

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