package me.mykindos.betterpvp.core.utilities.model;

import me.mykindos.betterpvp.core.config.ExtendedYamlConfiguration;

/**
 * Represents a class that can access its modules config file
 */
public interface ConfigAccessor {

    /**
     * Load the config file into memory
     * @param config The config file
     */
    void loadConfig(ExtendedYamlConfiguration config);

}
