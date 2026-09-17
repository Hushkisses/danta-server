package kr.danta.paper.combat.live;

import java.util.Locale;
import java.util.UUID;
import kr.danta.core.combat.TroopType;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

/**
 * Paper-side factory that materializes one logical live combat unit as temporary Minecraft entities.
 * Vanilla entity choices come from {@link CombatUnitVisualCatalog}; they are not troop-domain truth.
 */
public final class PaperCombatUnitFactory {
    private static final String OWNED_TAG = "danta_combat_demo";

    private final CombatUnitVisualCatalog visualCatalog;

    public PaperCombatUnitFactory() {
        this(CombatUnitVisualCatalog.developmentDefaults());
    }

    public PaperCombatUnitFactory(CombatUnitVisualCatalog visualCatalog) {
        if (visualCatalog == null) throw new NullPointerException("visualCatalog");
        this.visualCatalog = visualCatalog;
    }

    public SpawnedCombatUnit spawn(
            World world,
            Location location,
            CombatSide side,
            TroopType troopType
    ) {
        if (world == null) throw new NullPointerException("world");
        if (location == null) throw new NullPointerException("location");
        if (side == null) throw new NullPointerException("side");
        if (troopType == null) throw new NullPointerException("troopType");
        if (location.getWorld() != null && !world.equals(location.getWorld())) {
            throw new IllegalArgumentException("location world does not match spawn world");
        }

        CombatUnitVisualProfile profile = visualCatalog.profileFor(troopType);
        UUID unitId = UUID.randomUUID();
        LivingEntity primary = null;
        LivingEntity mount = null;

        try {
            if (profile.mountEntityKey() != null && !profile.mountEntityKey().isBlank()) {
                mount = spawnLiving(world, location, profile.mountEntityKey());
                primary = spawnLiving(world, location, profile.bodyEntityKey());
                mount.addPassenger(primary);
            } else {
                primary = spawnLiving(world, location, profile.bodyEntityKey());
            }

            configureEntity(primary, unitId, side, profile.displayName());
            equip(primary, profile.mainHandMaterialKey());
            primary.customName(Component.text(CombatHealthBarFormatter.format(
                    side, troopType, primary.getHealth(), primary.getMaxHealth())));

            if (mount != null) {
                configureEntity(mount, unitId, side, profile.displayName() + " 탈것");
                mount.setCustomNameVisible(false);
                if (mount instanceof Horse horse) {
                    horse.setTamed(true);
                    horse.setAdult();
                }
            }

            LiveCombatUnit unit = new LiveCombatUnit(
                    unitId,
                    side,
                    troopType,
                    primary.getUniqueId(),
                    mount == null ? null : mount.getUniqueId(),
                    profile.profileId());
            return new SpawnedCombatUnit(unit, primary, mount);
        } catch (RuntimeException ex) {
            if (primary != null && primary.isValid()) primary.remove();
            if (mount != null && mount.isValid()) mount.remove();
            throw ex;
        }
    }

    private static LivingEntity spawnLiving(World world, Location location, String entityKey) {
        EntityType type;
        try {
            type = EntityType.valueOf(entityKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported combat entity type: " + entityKey, ex);
        }
        if (!type.isAlive()) {
            throw new IllegalArgumentException("combat entity type is not living: " + entityKey);
        }
        return (LivingEntity) world.spawnEntity(location, type);
    }

    private static void configureEntity(
            LivingEntity entity,
            UUID unitId,
            CombatSide side,
            String roleLabel
    ) {
        entity.setPersistent(false);
        entity.setRemoveWhenFarAway(false);
        entity.customName(Component.text(sideLabel(side) + " " + roleLabel));
        entity.setCustomNameVisible(true);
        entity.addScoreboardTag(OWNED_TAG);
        entity.addScoreboardTag("danta_side_" + side.name().toLowerCase(Locale.ROOT));
        entity.addScoreboardTag("danta_unit_" + unitId);
    }

    private static void equip(LivingEntity entity, String materialKey) {
        Material material;
        try {
            material = Material.valueOf(materialKey.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("unsupported combat equipment material: " + materialKey, ex);
        }
        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            equipment.setItemInMainHand(new ItemStack(material));
            equipment.setItemInMainHandDropChance(0.0f);
        }
    }

    private static String sideLabel(CombatSide side) {
        return switch (side) {
            case RED -> "[적]";
            case BLUE -> "[청]";
        };
    }

    public record SpawnedCombatUnit(
            LiveCombatUnit unit,
            LivingEntity primary,
            LivingEntity mount
    ) {
        public SpawnedCombatUnit {
            if (unit == null) throw new NullPointerException("unit");
            if (primary == null) throw new NullPointerException("primary");
        }
    }
}
