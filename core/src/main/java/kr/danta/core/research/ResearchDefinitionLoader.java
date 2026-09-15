package kr.danta.core.research;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/** DEV-083 strict YAML loader. It validates content references but owns no nation research state. */
public final class ResearchDefinitionLoader {
    public Map<String, ResearchDefinition> load(InputStream input) {
        Objects.requireNonNull(input, "input");
        Object rootObject = new Yaml(new SafeConstructor(new LoaderOptions())).load(input);
        if (!(rootObject instanceof Map<?, ?> root)) throw new IllegalArgumentException("research YAML root must be a map");
        Object entriesObject = root.get("researches");
        if (!(entriesObject instanceof List<?> entries)) throw new IllegalArgumentException("researches must be a list");
        Map<String, ResearchDefinition> result = new LinkedHashMap<>();
        for (Object entryObject : entries) {
            if (!(entryObject instanceof Map<?, ?> entry)) throw new IllegalArgumentException("research entry must be a map");
            ResearchDefinition definition = parse(entry);
            if (result.putIfAbsent(definition.researchId(), definition) != null) throw new IllegalArgumentException("duplicate research id: " + definition.researchId());
        }
        validateReferences(result);
        return Map.copyOf(result);
    }

    private ResearchDefinition parse(Map<?, ?> entry) {
        String id = string(entry, "id", true);
        String name = string(entry, "name", true);
        ResearchField field = enumValue(ResearchField.class, string(entry, "field", true), id);
        ResearchTier tier = enumValue(ResearchTier.class, string(entry, "tier", true).replace("-", "_"), id);
        long minutes = longValue(entry, "durationMinutes", 0L);
        long gold = longValue(entry, "goldCost", 0L);
        Map<String, Long> resources = longMap(entry.get("resourceCosts"), "resourceCosts", id);
        List<String> prerequisites = stringList(entry.get("prerequisites"), "prerequisites", id);
        return new ResearchDefinition(id, name, field, tier, Duration.ofMinutes(minutes).toMillis(), gold, resources,
                prerequisites, string(entry, "doctrineKey", false), string(entry, "requiredMajorPointType", false));
    }

    private void validateReferences(Map<String, ResearchDefinition> definitions) {
        for (ResearchDefinition definition : definitions.values()) {
            for (String prerequisite : definition.prerequisites()) {
                if (prerequisite.equals(definition.researchId())) throw new IllegalArgumentException("research cannot require itself: " + prerequisite);
                if (!definitions.containsKey(prerequisite)) throw new IllegalArgumentException("unknown prerequisite " + prerequisite + " for " + definition.researchId());
            }
        }
        detectCycles(definitions);
    }

    private void detectCycles(Map<String, ResearchDefinition> definitions) {
        Map<String, Integer> state = new LinkedHashMap<>();
        for (String id : definitions.keySet()) visit(id, definitions, state);
    }
    private void visit(String id, Map<String, ResearchDefinition> definitions, Map<String, Integer> state) {
        int current = state.getOrDefault(id, 0);
        if (current == 1) throw new IllegalArgumentException("research prerequisite cycle at: " + id);
        if (current == 2) return;
        state.put(id, 1);
        for (String prerequisite : definitions.get(id).prerequisites()) visit(prerequisite, definitions, state);
        state.put(id, 2);
    }

    private static String string(Map<?, ?> map, String key, boolean required) {
        Object value = map.get(key);
        if (value == null) { if (required) throw new IllegalArgumentException("missing " + key); return null; }
        if (!(value instanceof String text) || text.isBlank()) throw new IllegalArgumentException(key + " must be a non-blank string");
        return text.trim();
    }
    private static long longValue(Map<?, ?> map, String key, long defaultValue) {
        Object value = map.get(key); if (value == null) return defaultValue;
        if (!(value instanceof Number number)) throw new IllegalArgumentException(key + " must be a number");
        long result = number.longValue(); if (result < 0) throw new IllegalArgumentException(key + " must be >= 0"); return result;
    }
    private static List<String> stringList(Object value, String label, String id) {
        if (value == null) return List.of(); if (!(value instanceof List<?> list)) throw new IllegalArgumentException(label + " must be a list: " + id);
        List<String> result = new ArrayList<>(); for (Object item : list) { if (!(item instanceof String text) || text.isBlank()) throw new IllegalArgumentException(label + " contains invalid id: " + id); result.add(text.trim().toLowerCase()); } return result;
    }
    private static Map<String, Long> longMap(Object value, String label, String id) {
        if (value == null) return Map.of(); if (!(value instanceof Map<?, ?> map)) throw new IllegalArgumentException(label + " must be a map: " + id);
        Map<String, Long> result = new LinkedHashMap<>(); for (var entry : map.entrySet()) { if (!(entry.getKey() instanceof String key) || !(entry.getValue() instanceof Number number) || number.longValue() < 0) throw new IllegalArgumentException(label + " contains invalid value: " + id); result.put(key.trim().toLowerCase(), number.longValue()); } return result;
    }
    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, String id) {
        try { return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT)); } catch (IllegalArgumentException ex) { throw new IllegalArgumentException("invalid " + type.getSimpleName() + " for " + id + ": " + value, ex); }
    }
}
