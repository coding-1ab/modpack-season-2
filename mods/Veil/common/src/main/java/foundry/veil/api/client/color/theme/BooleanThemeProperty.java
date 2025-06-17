package foundry.veil.api.client.color.theme;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author amo
 */
@ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
@Deprecated
public class BooleanThemeProperty implements IThemeProperty<Boolean> {

    private String name;
    private boolean value;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public Boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public Class<?> getType() {
        return BooleanThemeProperty.class;
    }
}
