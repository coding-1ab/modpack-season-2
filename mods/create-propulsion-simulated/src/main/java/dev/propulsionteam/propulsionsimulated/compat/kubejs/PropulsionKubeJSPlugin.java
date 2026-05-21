package dev.propulsionteam.propulsionsimulated.compat.kubejs;

import dev.propulsionteam.propulsionsimulated.content.thruster.SolidThrusterFuelManager;
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterFuelManager;

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class PropulsionKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("ThrusterFuelManager", ThrusterFuelManager.class);
        bindings.add("SolidThrusterFuelManager", SolidThrusterFuelManager.class);
    }
}


