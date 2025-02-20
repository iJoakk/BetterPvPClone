package me.mykindos.betterpvp.champions.champions.skills.skills.brute.passives;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import me.mykindos.betterpvp.champions.Champions;
import me.mykindos.betterpvp.champions.champions.ChampionsManager;
import me.mykindos.betterpvp.champions.champions.skills.Skill;
import me.mykindos.betterpvp.champions.champions.skills.types.PassiveSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.components.champions.Role;
import me.mykindos.betterpvp.core.components.champions.SkillType;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.UtilTime;
import me.mykindos.betterpvp.core.utilities.UtilVelocity;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.WeakHashMap;

@Singleton
@BPvPListener
public class Stampede extends Skill implements PassiveSkill {

    private final WeakHashMap<Player, Long> sprintTime = new WeakHashMap<>();
    private final WeakHashMap<Player, Integer> sprintStr = new WeakHashMap<>();

    private double durationPerStack;
    private double damage;

    @Inject
    public Stampede(Champions champions, ChampionsManager championsManager) {
        super(champions, championsManager);
    }

    @Override
    public String getName() {
        return "Stampede";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "You slowly build up speed as you",
                "sprint, gaining one level of <effect>Speed</effect>",
                "for every <val>" + (durationPerStack - level) + "</val> seconds, up to a max",
                "of <effect>Speed II</effect>",
                "",
                "Attacking during stampede deals",
                "<stat>" + damage + "</stat> bonus damage per speed level"};
    }

    @Override
    public Role getClassType() {
        return Role.BRUTE;
    }

    @Override
    public SkillType getType() {

        return SkillType.PASSIVE_B;
    }

    @UpdateEvent(delay = 250)
    public void updateSpeed() {

        for (Player player : Bukkit.getOnlinePlayers()) {
            int level = getLevel(player);
            if (level > 0) {
                if (!sprintTime.containsKey(player)) {
                    sprintTime.put(player, System.currentTimeMillis());
                    sprintStr.put(player, 0);
                }

                if (!player.isSprinting() || player.isInWater()) {
                    sprintTime.remove(player);
                    sprintStr.remove(player);
                    player.removePotionEffect(PotionEffectType.SPEED);
                } else {
                    long time = sprintTime.get(player);
                    int str = sprintStr.get(player);
                    if (str > 0) {
                        if (player.hasPotionEffect(PotionEffectType.SPEED)) {
                            player.removePotionEffect(PotionEffectType.SPEED);
                        }

                        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 40, str - 1));
                    }
                    if (UtilTime.elapsed(time, (long) ((durationPerStack - level) * 1000L))) {
                        sprintTime.put(player, System.currentTimeMillis());
                        if (str < 2) {
                            sprintStr.put(player, str + 1);

                            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ZOMBIE_AMBIENT, 2.0F, 0.2F * str + 1.0F);
                        }
                    }
                }
            }
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDamage(CustomDamageEvent event) {
        if (event.getCause() != DamageCause.ENTITY_ATTACK) return;
        if (!(event.getDamager() instanceof Player damager)) return;
        int str = sprintStr.getOrDefault(damager, 0);
        if (str <= 0) return;

        sprintTime.remove(damager);
        sprintStr.remove(damager);
        damager.removePotionEffect(PotionEffectType.SPEED);

        event.setKnockback(false);
        UtilVelocity.velocity(event.getDamagee(), UtilVelocity.getTrajectory2d(damager, event.getDamagee()), 2.0D, true, 0.0D, 0.4D, 1.0D, true, true);
        event.setDamage(event.getDamage() + (str * damage));
    }

    @Override
    public void loadSkillConfig() {
        durationPerStack = getConfig("durationPerStack", 8.0, Double.class);
        damage = getConfig("damageMultiplier", 2.0, Double.class);
    }
}
