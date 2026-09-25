package plus.dragons.quicksand.mixin;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
public @interface LoadWhen {
    String modId();
}
