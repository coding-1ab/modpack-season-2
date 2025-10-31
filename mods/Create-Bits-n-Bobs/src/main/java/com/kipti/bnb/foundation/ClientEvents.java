package com.kipti.bnb.foundation;

import com.kipti.bnb.content.cogwheel_chain.item.CogwheelChainPlacementEffect;
import com.kipti.bnb.content.girder_strut.GirderStrutPlacementEffects;
import com.kipti.bnb.content.weathered_girder.WeatheredGirderWrenchBehaviour;
import net.createmod.catnip.data.Pair;
import net.createmod.catnip.outliner.Outliner;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    static final List<Pair<Vec3, Vec3>> deferredDebugRenderOutlines = Collections.synchronizedList(new ArrayList<>());

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        WeatheredGirderWrenchBehaviour.tick();

        //Render deferred debug outlines
        synchronized (deferredDebugRenderOutlines) {
            for (Pair<Vec3, Vec3> outline : deferredDebugRenderOutlines) {
                Outliner.getInstance().showLine(outline, outline.getFirst(), outline.getSecond());
            }
        }
    }

    public static void pushNewDeferredDebugRenderOutline(Pair<Vec3, Vec3> outline) {
        //Synchronized list to avoid concurrent modification exceptions
        synchronized (deferredDebugRenderOutlines) {
            deferredDebugRenderOutlines.add(outline);
        }
    }

    public static void clearDeferredDebugRenderOutlines() {
        synchronized (deferredDebugRenderOutlines) {
            deferredDebugRenderOutlines.clear();
        }
    }

    @SubscribeEvent
    public static void onTickPre(ClientTickEvent.Pre event) {
        //If in a level, there is a player, and the player is holding a girder strut block item, update the preview
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            GirderStrutPlacementEffects.tick(mc.player);
            CogwheelChainPlacementEffect.tick(mc.player);
        }
    }

}
