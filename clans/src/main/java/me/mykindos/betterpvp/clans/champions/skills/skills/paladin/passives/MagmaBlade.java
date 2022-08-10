package me.mykindos.betterpvp.clans.champions.skills.skills.paladin.passives;

import com.google.inject.Inject;
import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.Skill;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillWeapons;
import me.mykindos.betterpvp.clans.champions.skills.types.PassiveSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import java.util.Arrays;

@BPvPListener
public class MagmaBlade extends Skill implements PassiveSkill {

    @Inject
    public MagmaBlade(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Magma Blade";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Your sword scorches opponents,",
                "dealing an additional " + ChatColor.GREEN + (level) + ChatColor.GRAY + " damage",
                "to players who are on fire."};
    }

    @Override
    public Role getClassType() {
        return Role.PALADIN;
    }

    @Override
    public SkillType getType() {
        return SkillType.PASSIVE_A;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(CustomDamageEvent event) {
        if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!UtilPlayer.isHoldingItem(player, SkillWeapons.SWORDS)) return;

        int level = getLevel(player);
        if (level > 0) {
            LivingEntity ent = event.getDamagee();
            if (ent.getFireTicks() > 0) {
                event.setDamage(event.getDamage() + level);
            }
        }

    }

}



