package foundry.veil.api.client.color.theme;

import org.jetbrains.annotations.ApiStatus;

import java.util.Objects;

/**
 * @author amo
 */
@ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
@Deprecated
public class StringThemeProperty implements IThemeProperty<String> {

    private String name;
    private String value;

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void setName(String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public Class<?> getType() {
        return StringThemeProperty.class;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
