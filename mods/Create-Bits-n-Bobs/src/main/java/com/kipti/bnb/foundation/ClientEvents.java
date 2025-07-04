package com.kipti.bnb.foundation;

import com.kipti.bnb.content.weathered_girder.WeatheredGirderWrenchBehaviour;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onTickPost(ClientTickEvent.Post event) {
        WeatheredGirderWrenchBehaviour.tick();
    }

}
