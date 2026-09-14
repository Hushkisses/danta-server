package kr.danta.paper.map;

import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicPointType;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record DevMapDefinition(
        String mapId,
        String worldName,
        List<NationDef> nations,
        List<PointDef> points,
        List<EdgeDef> edges
) {
    public record NationDef(String id, String displayName, String capitalPointId) {}

    public record PointDef(
            String id,
            String displayName,
            StrategicPointType type,
            String ownerNationId,
            int x,
            int y,
            int z,
            int facilitySlots,
            Map<String, Long> production
    ) {}

    public record EdgeDef(
            String id,
            String pointAId,
            String pointBId,
            long travelSeconds,
            Set<BattlefieldTag> tags
    ) {}
}
