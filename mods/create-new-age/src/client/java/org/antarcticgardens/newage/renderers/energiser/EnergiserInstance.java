package org.antarcticgardens.newage.renderers.energiser;

import com.jozufozu.flywheel.api.MaterialManager;
import com.simibubi.create.content.kinetics.base.ShaftInstance;
import org.antarcticgardens.newage.content.energiser.EnergiserBlockEntity;

public class EnergiserInstance extends ShaftInstance<EnergiserBlockEntity> {
    public EnergiserInstance(MaterialManager materialManager, EnergiserBlockEntity energiserBlockEntity) {
        super(materialManager, energiserBlockEntity);
    }

}
