package foundry.veil.api.client.color.theme;

import org.jetbrains.annotations.ApiStatus;

/**
 * @author amo
 */
@ApiStatus.ScheduledForRemoval(inVersion = "2.0.0")
@Deprecated
public interface IThemeProperty<T> {

    String getName();

    void setName(String name);

    T getValue();

    Class<?> getType();
}
