package org.antarcticgardens.cna.rendering.fallbackInstance;

import dev.engine_room.flywheel.api.instance.Instancer;
import dev.engine_room.flywheel.api.model.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FallbackMaterial<D extends InstanceData> implements Material<D> {
    private final Batched<D> type;
    private final Map<Object, FallbackInstancer<D>> models = new HashMap<>();
    
    public FallbackMaterial(Batched<D> type) {
        this.type = type;
    }

    @Override
    public Instancer<D> model(Object key, Supplier<Model> modelSupplier) {
        return models.computeIfAbsent(key, $ -> new FallbackInstancer<>(type, modelSupplier.get()));
    }
    
    public Map<Object, FallbackInstancer<D>> getModels() {
        return models;
    }
    
    public void delete() {
        models.values().forEach(FallbackInstancer::delete);
    }
}
