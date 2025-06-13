package foundry.veil.api.client.render.profiler;

import foundry.veil.impl.client.render.profiler.VeilRenderProfilerImpl;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

import static org.lwjgl.opengl.ARBPipelineStatisticsQuery.*;

/**
 * Collects render information.
 * @since 1.4.0
 */
@ApiStatus.Experimental
public interface VeilRenderProfiler {

    static VeilRenderProfiler get() {
        return VeilRenderProfilerImpl.get();
    }

    void push(String name, StatisticType... statistics);

    void push(Supplier<String> name, StatisticType... statistics);

    void pop();

    void popPush(String name, StatisticType... statistics);

    void popPush(Supplier<String> name, StatisticType... statistics);

    enum StatisticType {
        VERTICES_SUBMITTED(GL_VERTICES_SUBMITTED_ARB),
        PRIMITIVES_SUBMITTED(GL_PRIMITIVES_SUBMITTED_ARB),
        VERTEX_SHADER_INVOCATIONS(GL_VERTEX_SHADER_INVOCATIONS_ARB),
        TESS_CONTROL_SHADER_PATCHES(GL_TESS_CONTROL_SHADER_PATCHES_ARB),
        TESS_EVALUATION_SHADER_INVOCATIONS(GL_TESS_EVALUATION_SHADER_INVOCATIONS_ARB),
        GEOMETRY_SHADER_INVOCATIONS(GL_GEOMETRY_SHADER_INVOCATIONS),
        GEOMETRY_SHADER_PRIMITIVES_EMITTED(GL_GEOMETRY_SHADER_PRIMITIVES_EMITTED_ARB),
        FRAGMENT_SHADER_INVOCATIONS(GL_FRAGMENT_SHADER_INVOCATIONS_ARB),
        COMPUTE_SHADER_INVOCATIONS(GL_COMPUTE_SHADER_INVOCATIONS_ARB),
        CLIPPING_INPUT_PRIMITIVES(GL_CLIPPING_INPUT_PRIMITIVES_ARB),
        CLIPPING_OUTPUT_PRIMITIVES(GL_CLIPPING_OUTPUT_PRIMITIVES_ARB);

        /**
         * All statistic types.
         */
        public static final StatisticType[] ALL = StatisticType.values();

        /**
         * All normal statistic types for rendering.
         */
        public static final StatisticType[] STANDARD_GEOMETRY = {
                VeilRenderProfiler.StatisticType.VERTICES_SUBMITTED,
                VeilRenderProfiler.StatisticType.PRIMITIVES_SUBMITTED,
                VeilRenderProfiler.StatisticType.VERTEX_SHADER_INVOCATIONS,
                VeilRenderProfiler.StatisticType.FRAGMENT_SHADER_INVOCATIONS,
                VeilRenderProfiler.StatisticType.CLIPPING_INPUT_PRIMITIVES,
                VeilRenderProfiler.StatisticType.CLIPPING_OUTPUT_PRIMITIVES
        };

        private final int id;

        StatisticType(int id) {
            this.id = id;
        }

        /**
         * @return The OpenGL id of this statistic
         */
        public int getId() {
            return this.id;
        }
    }
}
