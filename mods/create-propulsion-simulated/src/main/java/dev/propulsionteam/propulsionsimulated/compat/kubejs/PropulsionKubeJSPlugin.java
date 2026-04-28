package dev.propulsionteam.propulsionsimulated.compat.kubejs;

<<<<<<< HEAD
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterFuelRegistry;
=======
import dev.propulsionteam.propulsionsimulated.content.thruster.ThrusterFuelManager;
>>>>>>> e8bb33badb65c4431e5c2251e9956708ba1cc7f3

import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;

public class PropulsionKubeJSPlugin implements KubeJSPlugin {
    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("ThrusterFuelRegistry", ThrusterFuelRegistry.class);
    }
}


