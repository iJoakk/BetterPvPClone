package me.mykindos.betterpvp.champions;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import me.mykindos.betterpvp.champions.champions.skills.SkillManager;
import me.mykindos.betterpvp.champions.champions.skills.injector.SkillInjectorModule;
import me.mykindos.betterpvp.champions.commands.ChampionsCommandLoader;
import me.mykindos.betterpvp.champions.injector.ChampionsInjectorModule;
import me.mykindos.betterpvp.champions.listeners.ChampionsListenerLoader;
import me.mykindos.betterpvp.core.Core;
import me.mykindos.betterpvp.core.config.Config;
import me.mykindos.betterpvp.core.config.ConfigInjectorModule;
import me.mykindos.betterpvp.core.database.Database;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.framework.ModuleLoadedEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEventExecutor;
import me.mykindos.betterpvp.core.items.ItemHandler;
import org.bukkit.Bukkit;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.Set;

@Singleton
public class Champions extends BPvPPlugin {

    private final String PACKAGE = getClass().getPackageName();

    @Getter
    @Setter
    private Injector injector;

    @Inject
    private Database database;

    @Inject
    private UpdateEventExecutor updateEventExecutor;

    @Inject
    @Config(path = "champions.database.prefix", defaultValue = "champions_")
    @Getter
    private String databasePrefix;

    private ChampionsListenerLoader championsListenerLoader;


    @Override
    public void onEnable() {
        saveDefaultConfig();

        var core = (Core) Bukkit.getPluginManager().getPlugin("Core");
        if (core != null) {

            Reflections reflections = new Reflections(PACKAGE, Scanners.FieldsAnnotated);
            Set<Field> fields = reflections.getFieldsAnnotatedWith(Config.class);

            injector = core.getInjector().createChildInjector(new ChampionsInjectorModule(this),
                    new ConfigInjectorModule(this, fields),
                    new SkillInjectorModule(this));
            injector.injectMembers(this);

            database.getConnection().runDatabaseMigrations(getClass().getClassLoader(), "classpath:champions-migrations", databasePrefix);

            Bukkit.getPluginManager().callEvent(new ModuleLoadedEvent("Champions"));

            var championsListenerLoader = injector.getInstance(ChampionsListenerLoader.class);
            championsListenerLoader.registerListeners(PACKAGE);

            var championsCommandLoader = injector.getInstance(ChampionsCommandLoader.class);
            championsCommandLoader.loadCommands(PACKAGE);

            var skillManager = injector.getInstance(SkillManager.class);
            skillManager.loadSkills();

            var itemHandler = injector.getInstance(ItemHandler.class);
            itemHandler.loadItemData("Champions");

            updateEventExecutor.loadPlugin(this);
        }
    }

}
