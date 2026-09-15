package kr.danta.core.general;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.*;

/** DEV-076A strict YAML loader for data-driven general content. */
public final class GeneralCatalogLoader {
    public List<GeneralDefinition> load(InputStream input) {
        Objects.requireNonNull(input, "input");
        Object root = new Yaml().load(input);
        if (!(root instanceof Map<?, ?> rootMap))
            throw new IllegalArgumentException("general catalog root must be a mapping");
        requireOnlyKeys(rootMap, Set.of("generals"), "root");
        Object rawGenerals = rootMap.get("generals");
        if (!(rawGenerals instanceof List<?> list))
            throw new IllegalArgumentException("generals must be a list");

        List<GeneralDefinition> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (Object raw : list) {
            if (!(raw instanceof Map<?, ?> map))
                throw new IllegalArgumentException("each general must be a mapping");
            GeneralDefinition definition = parseGeneral(map);
            if (!ids.add(definition.id()))
                throw new IllegalArgumentException("duplicate general id: " + definition.id());
            result.add(definition);
        }
        return List.copyOf(result);
    }

    private GeneralDefinition parseGeneral(Map<?, ?> map) {
        requireOnlyKeys(map, Set.of("id","displayName","grade","level","stats","traits","abilities"), "general");
        String id = string(map, "id");
        String displayName = string(map, "displayName");
        GeneralGrade grade;
        try { grade = GeneralGrade.valueOf(string(map, "grade")); }
        catch (IllegalArgumentException e) { throw new IllegalArgumentException("invalid grade for " + id, e); }
        int level = integer(map, "level");
        Object rawStats = map.get("stats");
        if (!(rawStats instanceof Map<?, ?> stats))
            throw new IllegalArgumentException("stats must be a mapping for " + id);
        requireOnlyKeys(stats, Set.of("command","martial","strategy","logistics"), "stats");
        GeneralStats generalStats = new GeneralStats(
                integer(stats, "command"), integer(stats, "martial"),
                integer(stats, "strategy"), integer(stats, "logistics"));
        return new GeneralDefinition(id, displayName, grade, level, generalStats,
                strings(optional(map, "traits", List.of()), "traits"),
                strings(optional(map, "abilities", List.of()), "abilities"));
    }

    private static Object optional(Map<?, ?> map, String key, Object defaultValue) {
        return map.containsKey(key) ? map.get(key) : defaultValue;
    }

    private static String string(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof String s) || s.isBlank())
            throw new IllegalArgumentException(key + " must be a non-blank string");
        return s;
    }

    private static int integer(Map<?, ?> map, String key) {
        Object value = map.get(key);
        if (!(value instanceof Number n))
            throw new IllegalArgumentException(key + " must be an integer");
        long l = n.longValue();
        if (l != n.doubleValue() || l < Integer.MIN_VALUE || l > Integer.MAX_VALUE)
            throw new IllegalArgumentException(key + " must be an integer");
        return (int) l;
    }

    private static List<String> strings(Object value, String label) {
        if (!(value instanceof List<?> list))
            throw new IllegalArgumentException(label + " must be a list");
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof String s)) throw new IllegalArgumentException(label + " entries must be strings");
            result.add(s);
        }
        return result;
    }

    private static void requireOnlyKeys(Map<?, ?> map, Set<String> allowed, String label) {
        for (Object key : map.keySet()) {
            if (!(key instanceof String s) || !allowed.contains(s))
                throw new IllegalArgumentException("unknown " + label + " key: " + key);
        }
    }
}
