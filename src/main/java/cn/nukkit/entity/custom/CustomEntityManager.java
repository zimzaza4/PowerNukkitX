package cn.nukkit.entity.custom;

import java.util.HashMap;
import java.util.Map;

public class CustomEntityManager {

    public CustomEntityManager() {}
    private final Map<String, CustomEntityDefinition> customEntities = new HashMap<>();

    public Map<String, CustomEntityDefinition> getCustomEntitiesDefinition() {
        return customEntities;
    }

    public void register(String id, CustomEntityDefinition definition) {
        customEntities.put(id, definition);
    }
}
