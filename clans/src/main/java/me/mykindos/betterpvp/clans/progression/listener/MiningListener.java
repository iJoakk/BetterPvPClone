package me.mykindos.betterpvp.clans.progression.listener;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import me.mykindos.betterpvp.clans.fields.event.FieldsInteractableUseEvent;
import me.mykindos.betterpvp.clans.fields.model.FieldsInteractable;
import me.mykindos.betterpvp.clans.fields.model.FieldsOre;
import me.mykindos.betterpvp.core.config.Config;
import me.mykindos.betterpvp.progression.tree.mining.Mining;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Slf4j
@Singleton
public class MiningListener implements Listener {

    @Config(path = "fields.fieldsXpMultiplier", defaultValue = "5.0")
    @Inject(optional = true)
    private double xpMultiplier = 5;

    @Inject(optional = true)
    private Mining mining;

    @EventHandler
    public void onFieldsOreMine(FieldsInteractableUseEvent event) {
        final FieldsInteractable type = event.getType();
        if (!(type instanceof FieldsOre)) {
            return;
        }

        final Player player = event.getPlayer();
        final Block block = event.getBlock().getBlock();
        mining.getMiningService().attemptMineOre(player, block, experience -> (long) (experience * xpMultiplier));
    }

}
