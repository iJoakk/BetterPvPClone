package me.mykindos.betterpvp.core.command.commands.qol;

import com.google.inject.Singleton;
import me.mykindos.betterpvp.core.client.Client;
import me.mykindos.betterpvp.core.command.Command;
import me.mykindos.betterpvp.core.framework.annotations.WithReflection;
import me.mykindos.betterpvp.core.utilities.UtilMessage;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

@Singleton
public class TeleportWorldCommand extends Command {

    @WithReflection
    public TeleportWorldCommand() {
        aliases.add("tpworld");
        aliases.add("tpw");
    }

    @Override
    public String getName() {
        return "teleportworld";
    }

    @Override
    public String getDescription() {
        return "Teleport to a specific world.";
    }

    @Override
    public void execute(Player player, Client client, String... args) {
        if (args.length > 0) {
            World world = Bukkit.getWorld(args[0]);
            if (world == null) {
                UtilMessage.message(player, "Teleport", "World does not exist, creating it...");
                world = new WorldCreator(args[0]).createWorld();
            }

            if (world != null) {
                player.teleport(world.getSpawnLocation());
            }else{
                UtilMessage.message(player, "Teleport", "Could not find world, something went wrong");
            }
        }else{
            UtilMessage.message(player, "Teleport", "You must specify a world name");
        }
    }

    @Override
    public String getArgumentType(int arg) {
        if(arg == 1) {
            return ArgumentType.WORLD.name();
        }

        return ArgumentType.NONE.name();
    }
}
