package net.jrdemiurge.simplyswordsoverhaul.util;

import java.util.HashMap;
import java.util.UUID;

public class DamageTracker {
    private static final HashMap<UUID, Float> lastHealthMap = new HashMap<>();

    public static void setLastHealth(UUID entityId, float health) {
        lastHealthMap.put(entityId, health);
    }

    public static float getLastHealth(UUID entityId) {
        return lastHealthMap.getOrDefault(entityId, -1.0F);
    }
}
