package plus.dragons.createenchantmentindustry.config;

import plus.dragons.createdragonsplus.config.StressConfig;
import plus.dragons.createenchantmentindustry.common.CEICommon;

public class CEIStressConfig extends StressConfig {
    public CEIStressConfig() {
        super(CEICommon.ID);
    }

    @Override
    protected int getVersion() {
        return 1;
    }
}
