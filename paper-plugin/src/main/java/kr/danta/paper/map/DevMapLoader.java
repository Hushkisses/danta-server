package kr.danta.paper.map;

import kr.danta.core.territory.BattlefieldTag;
import kr.danta.core.territory.StrategicPointType;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class DevMapLoader {
    private static final String RESOURCE_PATH = "maps/dev-test-map.yml";

    private final JavaPlugin plugin;

    public DevMapLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public DevMapDefinition loadDefault() {
        ensureExternalCopy();
        File file = new File(plugin.getDataFolder(), RESOURCE_PATH);
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        String mapId = requireText(yaml.getString("map-id"), "map-id");
        String worldName = requireText(yaml.getString("world"), "world");

        List<DevMapDefinition.NationDef> nations = new ArrayList<>();
        for (Map<?, ?> raw : yaml.getMapList("nations")) {
            String id = requireText(raw.get("id"), "nation.id");
            String name = requireText(raw.get("name"), "nation.name");
            String capital = requireText(raw.get("capital"), "nation.capital");
            nations.add(new DevMapDefinition.NationDef(id, name, capital));
        }

        List<DevMapDefinition.PointDef> points = new ArrayList<>();
        for (Map<?, ?> raw : yaml.getMapList("points")) {
            String id = requireText(raw.get("id"), "point.id");
            String name = requireText(raw.get("name"), "point.name");
            StrategicPointType type = StrategicPointType.valueOf(
                    requireText(raw.get("type"), "point.type").toUpperCase(Locale.ROOT));
            String owner = optionalId(raw.get("owner"));
            int x = requireInt(raw.get("x"), "point.x");
            int y = requireInt(raw.get("y"), "point.y");
            int z = requireInt(raw.get("z"), "point.z");
            int slots = requireInt(raw.get("facility-slots"), "point.facility-slots");

            Map<String, Long> production = new LinkedHashMap<>();
            Object productionRaw = raw.get("production");
            if (productionRaw instanceof Map<?, ?> productionMap) {
                for (Map.Entry<?, ?> entry : productionMap.entrySet()) {
                    String key = requireText(entry.getKey(), "production.key").toLowerCase(Locale.ROOT);
                    production.put(key, requireLong(entry.getValue(), "production." + key));
                }
            }

            points.add(new DevMapDefinition.PointDef(
                    id, name, type, owner, x, y, z, slots, Map.copyOf(production)));
        }

        List<DevMapDefinition.EdgeDef> edges = new ArrayList<>();
        for (Map<?, ?> raw : yaml.getMapList("edges")) {
            String id = requireText(raw.get("id"), "edge.id");
            String a = requireText(raw.get("a"), "edge.a");
            String b = requireText(raw.get("b"), "edge.b");
            long travelSeconds = requireLong(raw.get("travel-seconds"), "edge.travel-seconds");
            if (travelSeconds <= 0L) throw new IllegalArgumentException("edge travel-seconds must be > 0: " + id);

            Set<BattlefieldTag> tags = new LinkedHashSet<>();
            Object tagsRaw = raw.get("tags");
            if (tagsRaw instanceof Iterable<?> iterable) {
                for (Object token : iterable) {
                    tags.add(BattlefieldTag.valueOf(requireText(token, "edge.tag").toUpperCase(Locale.ROOT)));
                }
            }

            edges.add(new DevMapDefinition.EdgeDef(id, a, b, travelSeconds, Set.copyOf(tags)));
        }

        if (points.isEmpty()) throw new IllegalArgumentException("dev map has no strategic points");
        return new DevMapDefinition(mapId, worldName, List.copyOf(nations), List.copyOf(points), List.copyOf(edges));
    }

    private void ensureExternalCopy() {
        File file = new File(plugin.getDataFolder(), RESOURCE_PATH);
        if (file.exists()) {
            File backup = new File(file.getParentFile(), "dev-test-map.previous.yml");
            try {
                java.nio.file.Files.copy(file.toPath(), backup.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            } catch (java.io.IOException ex) {
                throw new IllegalStateException("failed to back up existing dev map definition", ex);
            }
        }
        plugin.saveResource(RESOURCE_PATH, true);
    }

    private static String optionalId(Object value) {
        if (value == null) return null;
        String text = value.toString().trim();
        if (text.isEmpty() || text.equalsIgnoreCase("none") || text.equals("-")) return null;
        return text;
    }

    private static String requireText(Object value, String label) {
        if (value == null) throw new IllegalArgumentException("missing " + label);
        String text = value.toString().trim();
        if (text.isEmpty()) throw new IllegalArgumentException("blank " + label);
        return text;
    }

    private static int requireInt(Object value, String label) {
        long parsed = requireLong(value, label);
        if (parsed < Integer.MIN_VALUE || parsed > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(label + " is outside integer range");
        }
        return (int) parsed;
    }

    private static long requireLong(Object value, String label) {
        if (value instanceof Number number) return number.longValue();
        try {
            return Long.parseLong(requireText(value, label));
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("invalid number for " + label + ": " + value, ex);
        }
    }
}
