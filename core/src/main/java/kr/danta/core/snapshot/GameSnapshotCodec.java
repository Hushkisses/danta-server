package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyStatus;
import kr.danta.core.army.ExpeditionSupplyLevel;
import kr.danta.core.army.ArmyOrderStatus;
import kr.danta.core.army.ArmyOrderType;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.economy.StrategicResource;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicPointType;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Dependency-free snapshot codec. Reads schema v1-v11; DEV-056 writes v11. */
public final class GameSnapshotCodec {
    private GameSnapshotCodec() {}

    public static String encode(GameSnapshot s) {
        return String.join("|",
                Integer.toString(s.schemaVersion()),
                Long.toString(s.createdAtEpochMillis()),
                Long.toString(s.runtimeElapsedMillis()),
                Boolean.toString(s.runtimePaused()),
                Double.toString(s.runtimeSpeedMultiplier()),
                enc(s.seasonId()), enc(s.seasonDisplayName()),
                encodeNations(s.nations()), encodeStrategicPoints(s.strategicPoints()),
                encodeStrategicEdges(s.strategicEdges()), encodeArmies(s.armies()), encodeArmyOrders(s.armyOrders()),
                encodeOperationQueues(s.armyOperationQueues()), encodePersonalWallets(s.personalWallets()), encodeStrategicResources(s.strategicResourceStockpiles()), encodeLocalResources(s.localResourceStockpiles()));
    }

    public static GameSnapshot decode(String value) {
        if (value == null) throw new IllegalArgumentException("snapshot is null");
        String[] p = value.split("\\|", -1);
        int schema = Integer.parseInt(p[0]);

        if (schema == 1) {
            if (p.length != 7) throw new IllegalArgumentException("invalid schema v1 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
        }
        if (schema == 2) {
            if (p.length != 8) throw new IllegalArgumentException("invalid schema v2 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]), List.of(), List.of(), List.of(), List.of(), List.of());
        }
        if (schema == 3) {
            if (p.length != 9) throw new IllegalArgumentException("invalid schema v3 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]),
                    decodeNations(p[7]), decodeStrategicPoints(p[8]), List.of(), List.of(), List.of(), List.of());
        }
        if (schema == 4) {
            if (p.length != 10) throw new IllegalArgumentException("invalid schema v4 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), List.of(), List.of(), List.of());
        }
        if (schema == 5) {
            if (p.length != 11) throw new IllegalArgumentException("invalid schema v5 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]), List.of(), List.of());
        }
        if (schema == 6) {
            if (p.length != 12) throw new IllegalArgumentException("invalid schema v6 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), List.of());
        }
        if (schema == 7) {
            if (p.length != 13) throw new IllegalArgumentException("invalid schema v7 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]));
        }
        if (schema == 8) {
            if (p.length != 14) throw new IllegalArgumentException("invalid schema v8 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]));
        }
        if (schema == 9) {
            if (p.length != 15) throw new IllegalArgumentException("invalid schema v9 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]));
        }
        if (schema == 10 || schema == 11) {
            if (p.length != 16) throw new IllegalArgumentException("invalid schema v" + schema + " field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]));
        }
        throw new IllegalArgumentException("unsupported snapshot schema: " + schema);
    }




    private static String encodeLocalResources(List<LocalResourceStockpileSnapshot> stockpiles) {
        if (stockpiles == null || stockpiles.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (LocalResourceStockpileSnapshot stockpile : stockpiles) {
            String values = java.util.Arrays.stream(StrategicResource.values())
                    .map(r -> r.name() + ":" + stockpile.amounts().getOrDefault(r, 0L))
                    .reduce((a, b) -> a + "+" + b).orElse("");
            rows.add(enc(stockpile.pointId()) + "," + enc(values));
        }
        return String.join(";", rows);
    }

    private static List<LocalResourceStockpileSnapshot> decodeLocalResources(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<LocalResourceStockpileSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 2) throw new IllegalArgumentException("invalid local resource row");
            Map<StrategicResource, Long> values = new java.util.EnumMap<>(StrategicResource.class);
            String decoded = dec(f[1]);
            if (decoded != null && !decoded.isEmpty()) {
                for (String pair : decoded.split("\\+", -1)) {
                    int colon = pair.lastIndexOf(':');
                    values.put(StrategicResource.valueOf(pair.substring(0, colon)), Long.parseLong(pair.substring(colon + 1)));
                }
            }
            result.add(new LocalResourceStockpileSnapshot(dec(f[0]), values));
        }
        return List.copyOf(result);
    }

    private static String encodeStrategicResources(List<StrategicResourceStockpileSnapshot> stockpiles) {
        if (stockpiles == null || stockpiles.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (StrategicResourceStockpileSnapshot stockpile : stockpiles) {
            String values = java.util.Arrays.stream(StrategicResource.values())
                    .map(r -> r.name() + ":" + stockpile.amounts().getOrDefault(r, 0L))
                    .reduce((a, b) -> a + "+" + b).orElse("");
            rows.add(enc(stockpile.nationId()) + "," + enc(values));
        }
        return String.join(";", rows);
    }

    private static List<StrategicResourceStockpileSnapshot> decodeStrategicResources(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<StrategicResourceStockpileSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 2) throw new IllegalArgumentException("invalid strategic resource row");
            Map<StrategicResource, Long> values = new java.util.EnumMap<>(StrategicResource.class);
            String decoded = dec(f[1]);
            if (decoded != null && !decoded.isEmpty()) {
                for (String pair : decoded.split("\\+", -1)) {
                    int colon = pair.lastIndexOf(':');
                    values.put(StrategicResource.valueOf(pair.substring(0, colon)), Long.parseLong(pair.substring(colon + 1)));
                }
            }
            result.add(new StrategicResourceStockpileSnapshot(dec(f[0]), values));
        }
        return List.copyOf(result);
    }

    private static String encodePersonalWallets(List<PersonalWalletSnapshot> wallets) {
        if (wallets == null || wallets.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (PersonalWalletSnapshot wallet : wallets) {
            rows.add(enc(wallet.playerId()) + "," + wallet.balance());
        }
        return String.join(";", rows);
    }

    private static List<PersonalWalletSnapshot> decodePersonalWallets(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<PersonalWalletSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 2) throw new IllegalArgumentException("invalid personal wallet snapshot row");
            result.add(new PersonalWalletSnapshot(dec(f[0]), Long.parseLong(f[1])));
        }
        return List.copyOf(result);
    }

    private static String encodeNations(List<NationSnapshot> nations) {
        if (nations == null || nations.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (NationSnapshot n : nations) {
            rows.add(String.join(",", enc(n.nationId()), enc(n.displayName()), enc(n.capitalPointId()),
                    Long.toString(n.treasury()), n.status().name()));
        }
        return String.join(";", rows);
    }

    private static List<NationSnapshot> decodeNations(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<NationSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 5) throw new IllegalArgumentException("invalid nation snapshot row");
            result.add(new NationSnapshot(dec(f[0]), dec(f[1]), dec(f[2]), Long.parseLong(f[3]), NationStatus.valueOf(f[4])));
        }
        return List.copyOf(result);
    }

    private static String encodeStrategicPoints(List<StrategicPointSnapshot> points) {
        if (points == null || points.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (StrategicPointSnapshot p : points) {
            rows.add(String.join(",", enc(p.pointId()), enc(p.displayName()), p.type().name(), enc(p.ownerNationId()),
                    enc(p.worldName()), Integer.toString(p.x()), Integer.toString(p.y()), Integer.toString(p.z()),
                    Integer.toString(p.facilitySlots()), enc(encodeProduction(p.baseProductionPerHour()))));
        }
        return String.join(";", rows);
    }

    private static List<StrategicPointSnapshot> decodeStrategicPoints(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<StrategicPointSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 10) throw new IllegalArgumentException("invalid strategic point snapshot row");
            result.add(new StrategicPointSnapshot(dec(f[0]), dec(f[1]), StrategicPointType.valueOf(f[2]), dec(f[3]), dec(f[4]),
                    Integer.parseInt(f[5]), Integer.parseInt(f[6]), Integer.parseInt(f[7]), Integer.parseInt(f[8]), decodeProduction(dec(f[9]))));
        }
        return List.copyOf(result);
    }

    private static String encodeStrategicEdges(List<StrategicEdgeSnapshot> edges) {
        if (edges == null || edges.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (StrategicEdgeSnapshot e : edges) {
            rows.add(String.join(",", enc(e.edgeId()), enc(e.pointAId()), enc(e.pointBId()),
                    Long.toString(e.baseTravelMillis()), enc(encodeTags(e.battlefieldTags()))));
        }
        return String.join(";", rows);
    }

    private static List<StrategicEdgeSnapshot> decodeStrategicEdges(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<StrategicEdgeSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 5) throw new IllegalArgumentException("invalid strategic edge snapshot row");
            result.add(new StrategicEdgeSnapshot(dec(f[0]), dec(f[1]), dec(f[2]), Long.parseLong(f[3]), decodeTags(dec(f[4]))));
        }
        return List.copyOf(result);
    }

    private static String encodeArmies(List<ArmySnapshot> armies) {
        if (armies == null || armies.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (ArmySnapshot army : armies) {
            rows.add(String.join(",", enc(army.armyId()), enc(army.ownerNationId()),
                    enc(army.locationPointId()), army.status().name(), Long.toString(army.baseTroops()),
                    army.expeditionSupplyLevel() == null ? "-" : army.expeditionSupplyLevel().name(), Long.toString(army.carriedFood())));
        }
        return String.join(";", rows);
    }

    private static List<ArmySnapshot> decodeArmies(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<ArmySnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 5 && f.length != 7) throw new IllegalArgumentException("invalid army snapshot row");
            ExpeditionSupplyLevel supply = f.length == 7 && !f[5].equals("-") ? ExpeditionSupplyLevel.valueOf(f[5]) : null;
            long carriedFood = f.length == 7 ? Long.parseLong(f[6]) : 0L;
            result.add(new ArmySnapshot(dec(f[0]), dec(f[1]), dec(f[2]),
                    ArmyStatus.valueOf(f[3]), Long.parseLong(f[4]), supply, carriedFood));
        }
        return List.copyOf(result);
    }

    private static String encodeOperationQueues(List<ArmyOperationQueueSnapshot> queues) {
        if (queues == null || queues.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (ArmyOperationQueueSnapshot queue : queues) {
            String destinations = queue.destinations().stream().map(GameSnapshotCodec::enc)
                    .reduce((a, b) -> a + "+" + b).orElse("");
            rows.add(enc(queue.armyId()) + "," + destinations);
        }
        return String.join(";", rows);
    }

    private static List<ArmyOperationQueueSnapshot> decodeOperationQueues(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<ArmyOperationQueueSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 2) throw new IllegalArgumentException("invalid army operation queue snapshot row");
            List<String> destinations = f[1].isEmpty() ? List.of() :
                    java.util.Arrays.stream(f[1].split("\\+", -1)).map(GameSnapshotCodec::dec).toList();
            result.add(new ArmyOperationQueueSnapshot(dec(f[0]), destinations));
        }
        return List.copyOf(result);
    }

    private static String encodeArmyOrders(List<ArmyOrderSnapshot> orders) {
        if (orders == null || orders.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (ArmyOrderSnapshot order : orders) {
            rows.add(String.join(",", enc(order.orderId()), enc(order.armyId()), order.type().name(),
                    enc(order.originPointId()), enc(order.destinationPointId()), enc(order.edgeId()),
                    order.status().name(), Long.toString(order.dueRuntimeMillis())));
        }
        return String.join(";", rows);
    }

    private static List<ArmyOrderSnapshot> decodeArmyOrders(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<ArmyOrderSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 8) throw new IllegalArgumentException("invalid army order snapshot row");
            result.add(new ArmyOrderSnapshot(dec(f[0]), dec(f[1]), ArmyOrderType.valueOf(f[2]),
                    dec(f[3]), dec(f[4]), dec(f[5]), ArmyOrderStatus.valueOf(f[6]), Long.parseLong(f[7])));
        }
        return List.copyOf(result);
    }

    private static String encodeTags(Set<BattlefieldTag> tags) {
        if (tags == null || tags.isEmpty()) return "";
        return tags.stream().map(Enum::name).sorted().reduce((a, b) -> a + "+" + b).orElse("");
    }

    private static Set<BattlefieldTag> decodeTags(String payload) {
        if (payload == null || payload.isEmpty()) return Set.of();
        Set<BattlefieldTag> result = new LinkedHashSet<>();
        for (String name : payload.split("\\+", -1)) result.add(BattlefieldTag.valueOf(name));
        return Set.copyOf(result);
    }

    private static String encodeProduction(Map<String, Long> production) {
        if (production == null || production.isEmpty()) return "";
        List<String> rows = new ArrayList<>();
        production.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(e -> rows.add(e.getKey() + ":" + e.getValue()));
        return String.join("+", rows);
    }

    private static Map<String, Long> decodeProduction(String payload) {
        if (payload == null || payload.isEmpty()) return Map.of();
        Map<String, Long> result = new LinkedHashMap<>();
        for (String row : payload.split("\\+", -1)) {
            int colon = row.lastIndexOf(':');
            if (colon <= 0 || colon == row.length() - 1) throw new IllegalArgumentException("invalid production row");
            result.put(row.substring(0, colon), Long.parseLong(row.substring(colon + 1)));
        }
        return Map.copyOf(result);
    }

    private static String enc(String s) {
        if (s == null) return "-";
        return Base64.getUrlEncoder().withoutPadding().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String dec(String s) {
        if (s.equals("-")) return null;
        return new String(Base64.getUrlDecoder().decode(s), StandardCharsets.UTF_8);
    }
}
