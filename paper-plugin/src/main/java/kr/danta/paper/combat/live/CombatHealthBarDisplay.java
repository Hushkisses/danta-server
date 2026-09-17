package kr.danta.paper.combat.live;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.LivingEntity;

/** Updates one live combat unit's visible overhead health label. */
public final class CombatHealthBarDisplay {
    private CombatHealthBarDisplay() {}

    public static void sync(LiveCombatUnit unit, LivingEntity entity) {
        if (unit == null || entity == null) return;
        entity.customName(Component.text(CombatHealthBarFormatter.format(
                unit.side(), unit.troopType(), entity.getHealth(), entity.getMaxHealth())));
        entity.setCustomNameVisible(true);
    }
}
