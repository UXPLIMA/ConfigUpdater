package com.uxplima.configupdater;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Logger;

public class ConfigUpdater {

    private final JavaPlugin plugin;
    private final FileConfiguration pluginConfig;
    private final Logger logger;
    private final String configVersion, jarVersion;
    private final Collection<String> files;
    private final String[] supportedLangs;
    private final String currentLang, configVersionPath;
    private final Collection<UpdateProvider> updateProviders;
    private final boolean mergeMissingNodes, deleteUnknownNodes, updateConfigVersion;
    private final long backupStart;
    private final List<Predicate<String>> deleteConfigNodeConditions, mergeConfigNodeConditions;

    private Map<String, FileConfiguration> diskConfigs = new HashMap<>();
    private Map<String, FileConfiguration> resourceConfigs = new HashMap<>();

    ConfigUpdater(JavaPlugin plugin, Collection<String> files,
                  String[] supportedLangs, String currentLang,
                  String configVersionPath,
                  String configVersion, String jarVersion,
                  Collection<UpdateProvider> updateProviders, boolean mergeMissingNodes,
                  boolean deleteUnknownNodes, boolean updateConfigVersion,
                  long backupStart,
                  List<Predicate<String>> deleteConfigNodeConditions, List<Predicate<String>> mergeConfigNodeConditions) {
        this.plugin = plugin;
        this.pluginConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "config.yml"));
        this.files = files;
        this.supportedLangs = supportedLangs;
        this.currentLang = currentLang;
        this.configVersionPath = configVersionPath;
        this.logger = plugin.getLogger();
        this.configVersion = configVersion;
        this.jarVersion = jarVersion;
        this.updateProviders = updateProviders;
        this.mergeMissingNodes = mergeMissingNodes;
        this.updateConfigVersion = updateConfigVersion;
        this.backupStart = backupStart;
        this.deleteUnknownNodes = deleteUnknownNodes;
        this.deleteConfigNodeConditions = deleteConfigNodeConditions == null ? List.of() : deleteConfigNodeConditions;
        this.mergeConfigNodeConditions = mergeConfigNodeConditions == null ? List.of() : mergeConfigNodeConditions;
    }

    public void update() {
        if (!requiresUpdate()) return;

        backupFiles();
        cacheFiles();
        runProvidedUpdates();
        updateConfigVersion();
        merge();
    }

    private void backupFiles() {
        for (String file : files) {
            file = file.replace("%lang%", currentLang);
            if (!file.contains(".")) file = file + ".yml";

            File diskFile = new File(plugin.getDataFolder(), file);
            if (!diskFile.exists()) {
                continue;
            }

            backup(file, diskFile);
        }
        logger.info("Backed up old files.\n");
    }

    private void cacheFiles() {
        for (String lang : supportedLangs) {
            for (String file : files) {
                if (file.contains(".")) continue;

                file = file + ".yml";
                file = file.replace("%lang%", lang);

                try {
                    YamlConfiguration resourceConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource(file)));
                    resourceConfigs.put(file, resourceConfig);
                } catch (Exception e) {
                    plugin.getLogger().warning("No resource file for " + file + ".");
                }

                File diskFile = new File(plugin.getDataFolder(), file);
                if (!diskFile.exists()) {
                    continue;
                }

                YamlConfiguration diskConfig = YamlConfiguration.loadConfiguration(diskFile);
                diskConfigs.put(file, diskConfig);
            }
        }
    }

    private void runProvidedUpdates() {
        for (UpdateProvider prodiver : updateProviders) {
            if (!prodiver.shouldUpdate(this)) continue;

            prodiver.update(this);
        }
    }

    private void updateConfigVersion() {
        if (!updateConfigVersion) return;

        File diskFile = new File(plugin.getDataFolder(), "config.yml");
        if (!diskFile.exists()) return;

        FileConfiguration diskConfig = YamlConfiguration.loadConfiguration(diskFile);
        diskConfig.set(configVersionPath, jarVersion);
        save(diskFile, diskConfig);

        logger.info("Updated config version!\n");
    }

    private void merge() {
        cacheFiles(); // Cache again since providers may have changed the files.

        for (String file : files) {
            if (file.contains(".")) continue;

            file = file.replace("%lang%", currentLang);
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
                    if (mergeConfigNodeConditions.stream().anyMatch(condition -> condition.test(key))) {
                        continue;
                    }

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
                    if (deleteConfigNodeConditions.stream().anyMatch(condition -> condition.test(key))) {
                        continue;
                    }

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
            File backupFile = new File(plugin.getDataFolder(), "backups/backup-" + backupStart + "/" + name);
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

    public boolean requiresUpdate() {
        return !configVersion.equalsIgnoreCase(jarVersion);
    }

    public FileConfiguration getDiskConfigFrom(String file) {
        return diskConfigs.get(file);
    }

    public FileConfiguration getResourceConfigFrom(String file) {
        return resourceConfigs.get(file);
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public Collection<String> getFiles() {
        return files;
    }

    public String[] getSupportedLangs() {
        return supportedLangs;
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public Map<String, FileConfiguration> getDiskConfigs() {
        return diskConfigs;
    }

    public Map<String, FileConfiguration> getResourceConfigs() {
        return resourceConfigs;
    }

    public FileConfiguration getPluginConfig() {
        return pluginConfig;
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public static ConfigUpdaterBuilder builder(JavaPlugin plugin) {
        return new ConfigUpdaterBuilder(plugin);
    }

}