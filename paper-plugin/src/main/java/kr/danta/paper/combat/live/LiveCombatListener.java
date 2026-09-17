package kr.danta.paper.combat.live;

import io.papermc.paper.event.entity.EntityPushedByEntityAttackEvent;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.Objects;
import java.util.UUID;

/** Paper event boundary for DEV-115 temporary combat entities. */
public final class LiveCombatListener implements Listener {
    private final LiveCombatRuntime runtime;
    private final CombatKnockbackPolicy knockbackPolicy = CombatKnockbackPolicy.developmentDefaults();

    public LiveCombatListener(LiveCombatRuntime runtime) {
        this.runtime = Objects.requireNonNull(runtime, "runtime");
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        runtime.onTrackedEntityDeath(event.getEntity().getUniqueId());
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetLivingEntityEvent event) {
        if (CombatMobNavigationPolicy.shouldSuppressVanillaTargeting(isDemoOwned(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (CombatDemoEnvironmentPolicy.preventCombustion(isDemoOwned(event.getEntity()))) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        UUID attackerId = combatAttackerId(event.getDamager());
        if (attackerId == null) return;

        Entity victim = event.getEntity();
        if (!(victim instanceof LivingEntity)) {
            event.setCancelled(true);
            return;
        }

        if (!runtime.mayDamage(attackerId, victim.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityPushedByAttack(EntityPushedByEntityAttackEvent event) {
        Entity pushedBy = event.getPushedBy();
        boolean directAttackerOwned = isDemoOwned(pushedBy);
        boolean projectileShooterOwned = isDemoOwnedProjectileShooter(pushedBy);
        boolean victimOwned = isDemoOwned(event.getEntity());

        if (knockbackPolicy.shouldSuppress(
                directAttackerOwned,
                projectileShooterOwned,
                victimOwned)) {
            event.setCancelled(true);
        }
    }

    private static UUID combatAttackerId(Entity damager) {
        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Entity entity && isDemoOwned(entity)) {
                return entity.getUniqueId();
            }
            return null;
        }
        return isDemoOwned(damager) ? damager.getUniqueId() : null;
    }

    private static boolean isDemoOwnedProjectileShooter(Entity entity) {
        if (!(entity instanceof Projectile projectile)) return false;
        ProjectileSource shooter = projectile.getShooter();
        return shooter instanceof Entity shooterEntity && isDemoOwned(shooterEntity);
    }

    private static boolean isDemoOwned(Entity entity) {
        return entity != null && entity.getScoreboardTags().contains("danta_combat_demo");
    }
}
