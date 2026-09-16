package kr.danta.paper.world;

/**
 * DEV-MAP-004A deterministic edge-blending policy.
 * Keeps shared tile edges coherent while allowing authored terrain to spill over nominal bounds.
 */
public final class TerrainBlendPolicy {
    private TerrainBlendPolicy() {}

    public static int edgeOffset(int tileX, int tileZ, int sample, boolean positiveSide) {
        int edgeX = positiveSide ? tileX + 1 : tileX;
        int edgeZ = tileZ;
        int hash = mix(edgeX, edgeZ, sample);
        return Math.floorMod(hash, 7) - 3;
    }

    public static int spillDistance(int tileX, int tileZ, TerrainTileType type) {
        if (type == null || type == TerrainTileType.PLAINS) return 0;
        int base = switch (type) {
            case FOREST -> 2;
            case MOUNTAIN -> 3;
            case RIVER -> 2;
            case ROAD -> 1;
            case PLAINS -> 0;
        };
        int variation = Math.floorMod(mix(tileX, tileZ, type.ordinal()), 3);
        return Math.min(5, base + variation);
    }

    public static int lateralJitter(int tileX, int tileZ, int sample, int amplitude) {
        if (amplitude <= 0) return 0;
        int hash = mix(tileX, tileZ, sample * 31 + amplitude);
        return Math.floorMod(hash, amplitude * 2 + 1) - amplitude;
    }

    private static int mix(int a, int b, int c) {
        int h = 0x9E3779B9;
        h ^= a * 0x85EBCA6B;
        h = Integer.rotateLeft(h, 13);
        h ^= b * 0xC2B2AE35;
        h = Integer.rotateLeft(h, 11);
        h ^= c * 0x27D4EB2D;
        h ^= (h >>> 16);
        h *= 0x7FEB352D;
        h ^= (h >>> 15);
        return h;
    }
}
