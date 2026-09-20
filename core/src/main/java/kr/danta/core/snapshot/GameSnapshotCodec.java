package kr.danta.core.snapshot;

import kr.danta.core.army.ArmyStatus;
import kr.danta.core.diplomacy.DiplomaticStatus;
import kr.danta.core.army.ExpeditionSupplyLevel;
import kr.danta.core.army.ArmyOrderStatus;
import kr.danta.core.army.ArmyOrderType;
import kr.danta.core.nation.NationStatus;
import kr.danta.core.general.GeneralGrade;
import kr.danta.core.general.GeneralHealthStatus;
import kr.danta.core.economy.StrategicResource;
import kr.danta.core.facility.FacilityTier;
import kr.danta.core.research.DoctrineSelection;
import kr.danta.core.research.ResearchField;
import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicPointType;
import kr.danta.core.combat.TroopType;
import kr.danta.core.siege.SiegeEngagementProfile;
import kr.danta.core.siege.SiegePhase;
import kr.danta.core.siege.SiegeSide;
import kr.danta.core.siege.SiegeStage;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Dependency-free snapshot codec. Reads schema v1-v22; DEV-120 writes v22. */
public final class GameSnapshotCodec {
    private GameSnapshotCodec() {}

    public static String encode(GameSnapshot s) {
        List<String> fields = new ArrayList<>(List.of(
                Integer.toString(s.schemaVersion()),
                Long.toString(s.createdAtEpochMillis()),
                Long.toString(s.runtimeElapsedMillis()),
                Boolean.toString(s.runtimePaused()),
                Double.toString(s.runtimeSpeedMultiplier()),
                enc(s.seasonId()), enc(s.seasonDisplayName()),
                encodeNations(s.nations()), encodeStrategicPoints(s.strategicPoints()),
                encodeStrategicEdges(s.strategicEdges()), encodeArmies(s.armies()), encodeArmyOrders(s.armyOrders()),
                encodeOperationQueues(s.armyOperationQueues()), encodePersonalWallets(s.personalWallets()),
                encodeStrategicResources(s.strategicResourceStockpiles()), encodeLocalResources(s.localResourceStockpiles()),
                encodeGenerals(s.generals()), encodeFacilities(s.facilities()), encodeFacilityConstructions(s.facilityConstructions()),
                encodeResearchStates(s.researchStates()), encodeDiplomaticRelations(s.diplomaticRelations()),
                encodeVassalRelations(s.vassalRelations()), encodeIndependenceWars(s.independenceWars()),
                encodeFameScores(s.fameScores()), encodeChronicleEntries(s.chronicleEntries())));
        if (s.schemaVersion() >= 22) fields.add(encodeSiegeRuntime(s.siegeRuntime()));
        return String.join("|", fields);
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
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), List.of());
        }
        if (schema == 12) {
            if (p.length != 17) throw new IllegalArgumentException("invalid schema v12 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]));
        }
        if (schema == 13) {
            if (p.length != 19) throw new IllegalArgumentException("invalid schema v13 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]));
        }
        if (schema == 14) {
            if (p.length != 20) throw new IllegalArgumentException("invalid schema v14 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStates(p[19]), List.of(), List.of(), List.of());
        }
        if (schema == 15) {
            if (p.length != 20) throw new IllegalArgumentException("invalid schema v15 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStatesV15(p[19]), List.of(), List.of(), List.of());
        }
        if (schema == 16) {
            if (p.length != 21) throw new IllegalArgumentException("invalid schema v16 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStatesV15(p[19]),
                    decodeDiplomaticRelations(p[20]), List.of(), List.of());
        }
        if (schema == 17) {
            if (p.length != 22) throw new IllegalArgumentException("invalid schema v17 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStates(p[19]),
                    decodeDiplomaticRelations(p[20]), decodeVassalRelations(p[21]), List.of());
        }
        if (schema == 18) {
            if (p.length != 23) throw new IllegalArgumentException("invalid schema v18 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStates(p[19]),
                    decodeDiplomaticRelations(p[20]), decodeVassalRelations(p[21]), decodeIndependenceWars(p[22]));
        }
        if (schema == 19) {
            if (p.length != 24) throw new IllegalArgumentException("invalid schema v19 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStates(p[19]),
                    decodeDiplomaticRelations(p[20]), decodeVassalRelations(p[21]), decodeIndependenceWars(p[22]),
                    decodeFameScores(p[23]));
        }
        if (schema == 20 || schema == 21) {
            if (p.length != 25) throw new IllegalArgumentException("invalid schema v" + schema + " field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStates(p[19]),
                    decodeDiplomaticRelations(p[20]), decodeVassalRelations(p[21]), decodeIndependenceWars(p[22]),
                    decodeFameScores(p[23]), decodeChronicleEntries(p[24]));
        }
        if (schema == 22) {
            if (p.length != 26) throw new IllegalArgumentException("invalid schema v22 field count");
            return new GameSnapshot(GameSnapshot.CURRENT_SCHEMA,
                    Long.parseLong(p[1]), Long.parseLong(p[2]), Boolean.parseBoolean(p[3]),
                    Double.parseDouble(p[4]), dec(p[5]), dec(p[6]), decodeNations(p[7]),
                    decodeStrategicPoints(p[8]), decodeStrategicEdges(p[9]), decodeArmies(p[10]),
                    decodeArmyOrders(p[11]), decodeOperationQueues(p[12]), decodePersonalWallets(p[13]),
                    decodeStrategicResources(p[14]), decodeLocalResources(p[15]), decodeGenerals(p[16]),
                    decodeFacilities(p[17]), decodeFacilityConstructions(p[18]), decodeResearchStates(p[19]),
                    decodeDiplomaticRelations(p[20]), decodeVassalRelations(p[21]), decodeIndependenceWars(p[22]),
                    decodeFameScores(p[23]), decodeChronicleEntries(p[24]), decodeSiegeRuntime(p[25]));
        }
        throw new IllegalArgumentException("unsupported snapshot schema: " + schema);
    }

    private static String encodeDiplomaticRelations(List<DiplomaticRelationSnapshot> relations) {
        if (relations == null || relations.isEmpty()) return "-";
        return relations.stream().map(r -> String.join(",", enc(r.nationAId()), enc(r.nationBId()), r.status().name()))
                .reduce((a,b)->a+";"+b).orElse("-");
    }
    private static List<DiplomaticRelationSnapshot> decodeDiplomaticRelations(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<DiplomaticRelationSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) { String[] f=row.split(",",-1); if(f.length!=3) throw new IllegalArgumentException("invalid diplomacy snapshot row"); result.add(new DiplomaticRelationSnapshot(dec(f[0]),dec(f[1]),DiplomaticStatus.valueOf(f[2]))); }
        return List.copyOf(result);
    }

    private static String encodeResearchStates(List<ResearchStateSnapshot> states) {
        if (states == null || states.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (ResearchStateSnapshot state : states) {
            String completed = String.join("+", state.completed());
            String queue = state.queue().stream().map(q -> String.join("~", q.entryId().toString(), enc(q.researchId()),
                    q.taskId() == null ? "-" : q.taskId().toString(),
                    q.dueRuntimeMillis() == null ? "-" : Long.toString(q.dueRuntimeMillis()))).reduce((a,b)->a+":"+b).orElse("-");
            String doctrines = state.doctrines().stream().map(d -> enc(d.doctrineKey()) + "~" + d.field().name())
                    .reduce((a,b)->a+":"+b).orElse("-");
            rows.add(String.join(",", enc(state.nationId()), Integer.toString(state.researchSlots()), enc(completed),
                    enc(queue), Integer.toString(state.doctrineSlots()), enc(doctrines)));
        }
        return String.join(";", rows);
    }

    private static List<ResearchStateSnapshot> decodeResearchStates(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<ResearchStateSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 4) throw new IllegalArgumentException("invalid research state snapshot row");
            result.add(new ResearchStateSnapshot(dec(f[0]), Integer.parseInt(f[1]), decodeCompleted(f[2]), decodeResearchQueue(f[3])));
        }
        return List.copyOf(result);
    }

    private static List<ResearchStateSnapshot> decodeResearchStatesV15(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<ResearchStateSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 6) throw new IllegalArgumentException("invalid v15 research state snapshot row");
            List<DoctrineSelection> doctrines = new ArrayList<>();
            String doctrineText = dec(f[5]);
            if (doctrineText != null && !doctrineText.equals("-") && !doctrineText.isEmpty()) {
                for (String item : doctrineText.split(":", -1)) {
                    String[] d = item.split("~", -1);
                    if (d.length != 2) throw new IllegalArgumentException("invalid doctrine snapshot row");
                    doctrines.add(new DoctrineSelection(dec(d[0]), ResearchField.valueOf(d[1])));
                }
            }
            result.add(new ResearchStateSnapshot(dec(f[0]), Integer.parseInt(f[1]), decodeCompleted(f[2]),
                    decodeResearchQueue(f[3]), Integer.parseInt(f[4]), doctrines));
        }
        return List.copyOf(result);
    }

    private static Set<String> decodeCompleted(String encoded) {
        Set<String> completed = new LinkedHashSet<>();
        String text = dec(encoded);
        if (text != null && !text.isEmpty()) completed.addAll(List.of(text.split("\\+")));
        return completed;
    }

    private static List<ResearchQueueSnapshot> decodeResearchQueue(String encoded) {
        List<ResearchQueueSnapshot> queue = new ArrayList<>();
        String text = dec(encoded);
        if (text != null && !text.equals("-") && !text.isEmpty()) {
            for (String item : text.split(":", -1)) {
                String[] q = item.split("~", -1);
                if (q.length != 4) throw new IllegalArgumentException("invalid research queue snapshot row");
                queue.add(new ResearchQueueSnapshot(java.util.UUID.fromString(q[0]), dec(q[1]),
                        q[2].equals("-") ? null : java.util.UUID.fromString(q[2]),
                        q[3].equals("-") ? null : Long.parseLong(q[3])));
            }
        }
        return queue;
    }

    private static String encodeFacilities(List<FacilitySnapshot> facilities) {
        if (facilities == null || facilities.isEmpty()) return "-";
        return facilities.stream()
                .map(f -> String.join(",", enc(f.pointId()), enc(f.facilityId()), f.tier().name()))
                .reduce((a, b) -> a + ";" + b).orElse("-");
    }

    private static List<FacilitySnapshot> decodeFacilities(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<FacilitySnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 3) throw new IllegalArgumentException("invalid facility snapshot row");
            result.add(new FacilitySnapshot(dec(f[0]), dec(f[1]), FacilityTier.valueOf(f[2])));
        }
        return List.copyOf(result);
    }

    private static String encodeFacilityConstructions(List<FacilityConstructionSnapshot> constructions) {
        if (constructions == null || constructions.isEmpty()) return "-";
        return constructions.stream()
                .map(x -> String.join(",", x.constructionId().toString(), enc(x.pointId()), enc(x.facilityId()),
                        x.targetTier().name(), Long.toString(x.dueRuntimeMillis())))
                .reduce((a, b) -> a + ";" + b).orElse("-");
    }

    private static List<FacilityConstructionSnapshot> decodeFacilityConstructions(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<FacilityConstructionSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 5) throw new IllegalArgumentException("invalid facility construction snapshot row");
            result.add(new FacilityConstructionSnapshot(java.util.UUID.fromString(f[0]), dec(f[1]), dec(f[2]),
                    FacilityTier.valueOf(f[3]), Long.parseLong(f[4])));
        }
        return List.copyOf(result);
    }

    private static String encodeGenerals(List<GeneralSnapshot> generals) {
        if (generals == null || generals.isEmpty()) return "-";
        List<String> rows = new ArrayList<>();
        for (GeneralSnapshot g : generals) {
            String traits = String.join("+", g.traitIds());
            String abilities = String.join("+", g.abilityIds());
            rows.add(String.join(",", enc(g.generalId()), enc(g.ownerNationId()), g.grade().name(),
                    Integer.toString(g.level()), Integer.toString(g.command()), Integer.toString(g.martial()),
                    Integer.toString(g.strategy()), Integer.toString(g.logistics()), enc(traits), enc(abilities),
                    enc(g.commandedArmyId()), enc(g.assignedPointId()), g.healthStatus().name(),
                    nullableLong(g.injuredAtRuntimeMillis()), nullableLong(g.recoveryReadyAtRuntimeMillis()),
                    enc(g.captorNationId()), nullableLong(g.capturedAtRuntimeMillis()), nullableLong(g.detentionEndsAtRuntimeMillis())));
        }
        return String.join(";", rows);
    }

    private static List<GeneralSnapshot> decodeGenerals(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<GeneralSnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 18) throw new IllegalArgumentException("invalid general snapshot row");
            result.add(new GeneralSnapshot(dec(f[0]), dec(f[1]), GeneralGrade.valueOf(f[2]), Integer.parseInt(f[3]),
                    Integer.parseInt(f[4]), Integer.parseInt(f[5]), Integer.parseInt(f[6]), Integer.parseInt(f[7]),
                    splitIds(dec(f[8])), splitIds(dec(f[9])), dec(f[10]), dec(f[11]), GeneralHealthStatus.valueOf(f[12]),
                    parseNullableLong(f[13]), parseNullableLong(f[14]), dec(f[15]), parseNullableLong(f[16]), parseNullableLong(f[17])));
        }
        return List.copyOf(result);
    }

    private static List<String> splitIds(String value) {
        return value == null || value.isEmpty() ? List.of() : List.of(value.split("\\+", -1));
    }
    private static String nullableLong(Long value) { return value == null ? "-" : value.toString(); }
    private static Long parseNullableLong(String value) { return value.equals("-") ? null : Long.parseLong(value); }

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
                    army.expeditionSupplyLevel() == null ? "-" : army.expeditionSupplyLevel().name(),
                    Long.toString(army.carriedFood()), encodeTroopComposition(army.troopComposition())));
        }
        return String.join(";", rows);
    }

    private static List<ArmySnapshot> decodeArmies(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return List.of();
        List<ArmySnapshot> result = new ArrayList<>();
        for (String row : payload.split(";", -1)) {
            String[] f = row.split(",", -1);
            if (f.length != 5 && f.length != 7 && f.length != 8) {
                throw new IllegalArgumentException("invalid army snapshot row");
            }
            ExpeditionSupplyLevel supply = f.length >= 7 && !f[5].equals("-")
                    ? ExpeditionSupplyLevel.valueOf(f[5]) : null;
            long carriedFood = f.length >= 7 ? Long.parseLong(f[6]) : 0L;
            long baseTroops = Long.parseLong(f[4]);
            if (f.length == 8) {
                result.add(new ArmySnapshot(dec(f[0]), dec(f[1]), dec(f[2]),
                        ArmyStatus.valueOf(f[3]), baseTroops, supply, carriedFood,
                        decodeTroopComposition(f[7])));
            } else {
                result.add(new ArmySnapshot(dec(f[0]), dec(f[1]), dec(f[2]),
                        ArmyStatus.valueOf(f[3]), baseTroops, supply, carriedFood));
            }
        }
        return List.copyOf(result);
    }

    private static String encodeTroopComposition(Map<TroopType, Long> composition) {
        if (composition == null || composition.isEmpty()) return "-";
        List<String> parts = new ArrayList<>();
        for (TroopType type : TroopType.values()) {
            long count = composition.getOrDefault(type, 0L);
            parts.add(type.name() + "~" + count);
        }
        return String.join("+", parts);
    }

    private static Map<TroopType, Long> decodeTroopComposition(String payload) {
        if (payload.equals("-") || payload.isEmpty()) return Map.of();
        Map<TroopType, Long> result = new LinkedHashMap<>();
        for (String part : payload.split("\\+", -1)) {
            String[] fields = part.split("~", -1);
            if (fields.length != 2) throw new IllegalArgumentException("invalid troop composition row");
            TroopType type = TroopType.valueOf(fields[0]);
            long count = Long.parseLong(fields[1]);
            if (count < 0L) throw new IllegalArgumentException("negative troop composition count");
            result.put(type, count);
        }
        return Map.copyOf(result);
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

    private static String encodeChronicleEntries(List<ChronicleEntrySnapshot> values) {
        if (values == null || values.isEmpty()) return "-";
        return values.stream().map(v -> v.runtimeMillis() + "," + enc(v.type()) + "," + enc(v.summary()))
                .collect(java.util.stream.Collectors.joining(";"));
    }

    private static List<ChronicleEntrySnapshot> decodeChronicleEntries(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) return List.of();
        List<ChronicleEntrySnapshot> out = new ArrayList<>();
        for (String row : value.split(";", -1)) {
            String[] p = row.split(",", -1);
            if (p.length != 3) throw new IllegalArgumentException("invalid chronicle snapshot row");
            out.add(new ChronicleEntrySnapshot(Long.parseLong(p[0]), dec(p[1]), dec(p[2])));
        }
        return List.copyOf(out);
    }

    private static String encodeFameScores(List<FameScoreSnapshot> values) {
        if (values == null || values.isEmpty()) return "-";
        return values.stream()
                .map(v -> enc(v.nationId()) + "," + v.score())
                .collect(java.util.stream.Collectors.joining(";"));
    }

    private static List<FameScoreSnapshot> decodeFameScores(String value) {
        if (value == null || value.isEmpty() || value.equals("-")) return List.of();
        List<FameScoreSnapshot> out = new ArrayList<>();
        for (String row : value.split(";", -1)) {
            String[] p = row.split(",", -1);
            if (p.length != 2) throw new IllegalArgumentException("invalid fame score snapshot row");
            out.add(new FameScoreSnapshot(dec(p[0]), Long.parseLong(p[1])));
        }
        return List.copyOf(out);
    }

    private static String encodeIndependenceWars(List<IndependenceWarSnapshot> values) {
        if(values==null||values.isEmpty()) return "-";
        return values.stream().map(v->String.join(",",enc(v.vassalNationId()),enc(v.overlordNationId()),Long.toString(v.declaredAtRuntimeMillis()),Long.toString(v.holdUntilRuntimeMillis()),Long.toString(v.redeclareAfterRuntimeMillis()),Boolean.toString(v.active()))).collect(java.util.stream.Collectors.joining(";"));
    }
    private static List<IndependenceWarSnapshot> decodeIndependenceWars(String value) {
        if(value==null||value.isEmpty()||value.equals("-")) return List.of();
        List<IndependenceWarSnapshot> out=new ArrayList<>();
        for(String row:value.split(";")){String[] p=row.split(",",-1);if(p.length!=6)throw new IllegalArgumentException("invalid independence war");out.add(new IndependenceWarSnapshot(dec(p[0]),dec(p[1]),Long.parseLong(p[2]),Long.parseLong(p[3]),Long.parseLong(p[4]),Boolean.parseBoolean(p[5])));}
        return List.copyOf(out);
    }

    private static String encodeVassalRelations(List<VassalRelationSnapshot> values) {
        return values.stream().map(v -> enc(v.vassalNationId()) + "," + enc(v.overlordNationId()) + "," + v.vassalizedAtRuntimeMillis()).collect(java.util.stream.Collectors.joining(";"));
    }
    private static List<VassalRelationSnapshot> decodeVassalRelations(String value) {
        if (value == null || value.isEmpty()) return List.of();
        List<VassalRelationSnapshot> out = new ArrayList<>();
        for (String row : value.split(";")) { String[] p=row.split(",",-1); if(p.length!=3) throw new IllegalArgumentException("invalid vassal relation"); out.add(new VassalRelationSnapshot(dec(p[0]),dec(p[1]),Long.parseLong(p[2]))); }
        return out;
    }

    private static String encodeSiegeRuntime(SiegeRuntimeSnapshot snapshot) {
        if (snapshot == null || snapshot.isEmpty()) return enc("-#-#-#-#-");
        String instances = snapshot.instances().isEmpty() ? "-" : snapshot.instances().stream()
                .map(x -> String.join(",", enc(x.siegeId()), enc(x.pointId()), enc(x.attackerNationId()),
                        enc(x.defenderNationId()), x.phase().name(), enc(x.result())))
                .reduce((a,b)->a+";"+b).orElse("-");
        String reservations = snapshot.reservations().isEmpty() ? "-" : snapshot.reservations().stream()
                .map(x -> enc(x.siegeId()) + "," + x.scheduledAtEpochMillis() + ","
                        + (x.confirmedAtEpochMillis() == null ? "-" : x.confirmedAtEpochMillis()))
                .reduce((a,b)->a+";"+b).orElse("-");
        String progress = snapshot.progress().isEmpty() ? "-" : snapshot.progress().stream()
                .map(x -> String.join(",", enc(x.pointId()), x.profile().name(), x.stage().name(),
                        Boolean.toString(x.resumeRequired())))
                .reduce((a,b)->a+";"+b).orElse("-");
        String participants = snapshot.participants().isEmpty() ? "-" : snapshot.participants().stream()
                .map(x -> String.join(",", enc(x.pointId()), x.playerId().toString(), x.side().name(),
                        Boolean.toString(x.eliminated()), enc(x.previousGameMode())))
                .reduce((a,b)->a+";"+b).orElse("-");
        String morale = snapshot.morale().isEmpty() ? "-" : snapshot.morale().stream()
                .map(x -> String.join(",", enc(x.pointId()), x.side().name(), Integer.toString(x.moraleDelta())))
                .reduce((a,b)->a+";"+b).orElse("-");
        return enc(String.join("#", instances, reservations, progress, participants, morale));
    }

    private static SiegeRuntimeSnapshot decodeSiegeRuntime(String encoded) {
        String raw = dec(encoded);
        if (raw == null || raw.isEmpty()) return SiegeRuntimeSnapshot.empty();
        String[] sections = raw.split("#", -1);
        if (sections.length != 5) throw new IllegalArgumentException("invalid siege runtime snapshot");

        List<SiegeInstanceSnapshot> instances = new ArrayList<>();
        if (!sections[0].equals("-") && !sections[0].isEmpty()) {
            for (String row : sections[0].split(";", -1)) {
                String[] x = row.split(",", -1);
                if (x.length != 6) throw new IllegalArgumentException("invalid siege instance snapshot row");
                instances.add(new SiegeInstanceSnapshot(dec(x[0]), dec(x[1]), dec(x[2]), dec(x[3]),
                        SiegePhase.valueOf(x[4]), dec(x[5])));
            }
        }

        List<SiegeReservationSnapshot> reservations = new ArrayList<>();
        if (!sections[1].equals("-") && !sections[1].isEmpty()) {
            for (String row : sections[1].split(";", -1)) {
                String[] x = row.split(",", -1);
                if (x.length != 3) throw new IllegalArgumentException("invalid siege reservation snapshot row");
                reservations.add(new SiegeReservationSnapshot(dec(x[0]), Long.parseLong(x[1]),
                        x[2].equals("-") ? null : Long.parseLong(x[2])));
            }
        }

        List<SiegeProgressSnapshot> progress = new ArrayList<>();
        if (!sections[2].equals("-") && !sections[2].isEmpty()) {
            for (String row : sections[2].split(";", -1)) {
                String[] x = row.split(",", -1);
                if (x.length != 4) throw new IllegalArgumentException("invalid siege progress snapshot row");
                progress.add(new SiegeProgressSnapshot(dec(x[0]), SiegeEngagementProfile.valueOf(x[1]),
                        SiegeStage.valueOf(x[2]), Boolean.parseBoolean(x[3])));
            }
        }

        List<SiegeParticipantSnapshot> participants = new ArrayList<>();
        if (!sections[3].equals("-") && !sections[3].isEmpty()) {
            for (String row : sections[3].split(";", -1)) {
                String[] x = row.split(",", -1);
                if (x.length != 5) throw new IllegalArgumentException("invalid siege participant snapshot row");
                participants.add(new SiegeParticipantSnapshot(dec(x[0]), UUID.fromString(x[1]),
                        SiegeSide.valueOf(x[2]), Boolean.parseBoolean(x[3]), dec(x[4])));
            }
        }

        List<SiegeMoraleSnapshot> morale = new ArrayList<>();
        if (!sections[4].equals("-") && !sections[4].isEmpty()) {
            for (String row : sections[4].split(";", -1)) {
                String[] x = row.split(",", -1);
                if (x.length != 3) throw new IllegalArgumentException("invalid siege morale snapshot row");
                morale.add(new SiegeMoraleSnapshot(dec(x[0]), SiegeSide.valueOf(x[1]), Integer.parseInt(x[2])));
            }
        }
        return new SiegeRuntimeSnapshot(instances, reservations, progress, participants, morale);
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
