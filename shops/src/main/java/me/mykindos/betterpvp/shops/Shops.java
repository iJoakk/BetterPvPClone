package me.mykindos.betterpvp.shops;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import lombok.Getter;
import lombok.Setter;
import me.mykindos.betterpvp.core.Core;
import me.mykindos.betterpvp.core.config.Config;
import me.mykindos.betterpvp.core.config.ConfigInjectorModule;
import me.mykindos.betterpvp.core.database.Database;
import me.mykindos.betterpvp.core.framework.BPvPPlugin;
import me.mykindos.betterpvp.core.framework.ModuleLoadedEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEventExecutor;
import me.mykindos.betterpvp.shops.commands.loader.ShopsCommandLoader;
import me.mykindos.betterpvp.shops.injector.ShopsInjectorModule;
import me.mykindos.betterpvp.shops.listener.ShopsListenerLoader;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Field;
import java.util.Set;

@Singleton
public class Shops extends BPvPPlugin {

    private final String PACKAGE = getClass().getPackageName();

    @Getter
    @Setter
    private Injector injector;

    @Inject
    private Database database;

    @Inject
    private UpdateEventExecutor updateEventExecutor;

    @Inject
    @Config(path = "shops.database.prefix", defaultValue = "shops_")
    @Getter
    private String databasePrefix;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        var core = (Core) Bukkit.getPluginManager().getPlugin("Core");
        if (core != null) {

            Reflections reflections = new Reflections(PACKAGE, Scanners.FieldsAnnotated);
            Set<Field> fields = reflections.getFieldsAnnotatedWith(Config.class);

            injector = core.getInjector().createChildInjector(new ShopsInjectorModule(this),
                    new ConfigInjectorModule(this, fields));
            injector.injectMembers(this);

            database.getConnection().runDatabaseMigrations(getClass().getClassLoader(), "classpath:shops-migrations", databasePrefix);

            Bukkit.getPluginManager().callEvent(new ModuleLoadedEvent("Shops"));

            var shopsListenerLoader = injector.getInstance(ShopsListenerLoader.class);
            shopsListenerLoader.registerListeners(PACKAGE);

            var shopsCommandLoader = injector.getInstance(ShopsCommandLoader.class);
            shopsCommandLoader.loadCommands(PACKAGE);



            updateEventExecutor.loadPlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for(World world : Bukkit.getWorlds()) {
            for(LivingEntity entity : world.getLivingEntities()) {
                if(entity instanceof Player) continue;

                if(entity.customName() != null) {
                    entity.remove();
                }
            }
        }
    }
}