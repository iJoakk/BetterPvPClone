package me.mykindos.betterpvp.clans.champions.skills.skills.paladin.axe;

import me.mykindos.betterpvp.clans.Clans;
import me.mykindos.betterpvp.clans.champions.ChampionsManager;
import me.mykindos.betterpvp.clans.champions.roles.Role;
import me.mykindos.betterpvp.clans.champions.skills.Skill;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillActions;
import me.mykindos.betterpvp.clans.champions.skills.data.SkillType;
import me.mykindos.betterpvp.clans.champions.skills.skills.paladin.data.CustomArmourStand;
import me.mykindos.betterpvp.clans.champions.skills.types.CooldownSkill;
import me.mykindos.betterpvp.clans.champions.skills.types.InteractSkill;
import me.mykindos.betterpvp.core.combat.events.CustomDamageEvent;
import me.mykindos.betterpvp.core.framework.updater.UpdateEvent;
import me.mykindos.betterpvp.core.listener.BPvPListener;
import me.mykindos.betterpvp.core.utilities.*;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.WeakHashMap;

@Singleton
@BPvPListener
public class Rupture extends Skill implements Listener, InteractSkill, CooldownSkill {

    private final WeakHashMap<Player, ArrayList<LivingEntity>> cooldownJump = new WeakHashMap<>();
    private final WeakHashMap<ArmorStand, Long> stands = new WeakHashMap<>();

    @Inject
    public Rupture(Clans clans, ChampionsManager championsManager) {
        super(clans, championsManager);
    }

    @Override
    public String getName() {
        return "Rupture";
    }

    @Override
    public String[] getDescription(int level) {

        return new String[]{
                "Right click with a axe to activate.",
                "",
                "Rupture the earth in the direction",
                "you are facing, damaging, knocking up",
                "and slowing any enemies hit.",
                "",
                "Cooldown: " + ChatColor.GREEN + getCooldown(level)
        };
    }

    @Override
    public Role getClassType() {
        return Role.PALADIN;
    }

    @Override
    public SkillType getType() {
        return SkillType.AXE;
    }


    @UpdateEvent
    public void onUpdate() {
        stands.entrySet().removeIf(entry -> {
            if (entry.getValue() - System.currentTimeMillis() <= 0) {
                entry.getKey().remove();
                return true;
            }
            return false;
        });
    }


    @Override
    public double getCooldown(int level) {

        return cooldown - ((level - 1));
    }

    @Override
    public void activate(Player player, int level) {
        final Vector vector = player.getLocation().getDirection().normalize().multiply(0.3D);
        vector.setY(0);
        final Location loc = player.getLocation().subtract(0.0D, 1.0D, 0.0D).add(vector);
        cooldownJump.put(player, new ArrayList<>());
        final BukkitTask runnable = new BukkitRunnable() {

            @Override
            public void run() {

                if ((!UtilBlock.airFoliage(loc.getBlock())) && UtilBlock.solid(loc.getBlock())) {

                    loc.add(0.0D, 1.0D, 0.0D);
                    if ((!UtilBlock.airFoliage(loc.getBlock())) && UtilBlock.solid(loc.getBlock())) {
                        cancel();
                        return;
                    }

                }

                if (loc.getBlock().getType().name().contains("DOOR")) {
                    cancel();
                    return;
                }


                if ((loc.clone().add(0.0D, -1.0D, 0.0D).getBlock().getType() == Material.AIR)) {
                    Block halfBlock = loc.clone().add(0, -0.5, 0).getBlock();
                    if (!halfBlock.getType().name().contains("SLAB") && !halfBlock.getType().name().contains("STAIR")) {
                        loc.add(0.0D, -1.0D, 0.0D);
                    }
                }

                for (int i = 0; i < 3; i++) {
                    loc.add(vector);
                    Location tempLoc = new Location(player.getWorld(), loc.getX() + UtilMath.randDouble(-1.5D, 1.5D), loc.getY() + UtilMath.randDouble(0.3D, 0.8D) - 0.75,
                            loc.getZ() + UtilMath.randDouble(-1.5D, 1.5D));

                    CustomArmourStand as = new CustomArmourStand(((CraftWorld) loc.getWorld()).getHandle());
                    ArmorStand armourStand = (ArmorStand) as.spawn(tempLoc);
                    armourStand.getEquipment().setHelmet(new ItemStack(Material.PACKED_ICE));
                    armourStand.setGravity(false);
                    armourStand.setVisible(false);
                    armourStand.setSmall(true);
                    armourStand.setHeadPose(new EulerAngle(UtilMath.randomInt(360), UtilMath.randomInt(360), UtilMath.randomInt(360)));


                    player.getWorld().playEffect(loc, Effect.STEP_SOUND, Material.PACKED_ICE);


                    stands.put(armourStand, System.currentTimeMillis() + 4000);

                    for (LivingEntity ent : UtilEntity.getNearbyEntities(player, armourStand.getLocation(), 1)) {
                        if (ent instanceof ArmorStand) continue;
                        if (ent.equals(player)) continue;

                        if (!cooldownJump.get(player).contains(ent)) {

                            UtilVelocity.velocity(ent, 0.5, 1, 2.0, false);
                            ent.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 30, 2));
                            UtilDamage.doCustomDamage(new CustomDamageEvent(ent, player, null, DamageCause.CUSTOM, 8.0, false, getName()));

                            cooldownJump.get(player).add(ent);
                        }

                    }


                }

            }

        }.runTaskTimer(clans, 0, 2);

        new BukkitRunnable() {

            @Override
            public void run() {
                runnable.cancel();
                cooldownJump.get(player).clear();

            }

        }.runTaskLater(clans, 40);
    }

    @Override
    public Action[] getActions() {
        return SkillActions.RIGHT_CLICK;
    }
}
