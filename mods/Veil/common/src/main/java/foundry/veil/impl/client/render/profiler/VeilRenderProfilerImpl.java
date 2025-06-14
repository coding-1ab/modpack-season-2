package foundry.veil.impl.client.render.profiler;

import foundry.veil.Veil;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.profiler.VeilRenderProfiler;
import foundry.veil.impl.client.editor.PipelineStatisticsViewer;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL15C.*;
import static org.lwjgl.opengl.GL33C.glGetQueryObjecti64;

@ApiStatus.Internal
public final class VeilRenderProfilerImpl {

    private static final Map<String, PathEntry> ENTRIES = new Object2ObjectOpenHashMap<>();
    private static final List<String> paths = new LinkedList<>();
    private static final BitSet ENABLED_STATISTICS = new BitSet(VeilRenderProfiler.StatisticType.ALL.length);
    private static final Set<String> ENABLED_PATHS = new HashSet<>();
    private static String path = "";
    private static int[] queries;

    private VeilRenderProfilerImpl() {
    }

    public static VeilRenderProfiler get() {
        return isEnabled() ? ActiveProfiler.INSTANCE : InactiveProfiler.INSTANCE;
    }

    public static void setEnabled(Collection<String> enabledPaths, VeilRenderProfiler.StatisticType[] statistics) {
        ENABLED_PATHS.clear();
        ENABLED_PATHS.addAll(enabledPaths);
        ENABLED_STATISTICS.clear();
        for (VeilRenderProfiler.StatisticType statistic : statistics) {
            ENABLED_STATISTICS.set(statistic.ordinal());
        }
    }

    public static void endFrame() {
        if (!paths.isEmpty()) {
            Veil.LOGGER.error("Mismatched push() and pop()");
            paths.clear();
        }

        ENTRIES.clear();
        if (queries != null && !isEnabled()) {
            glDeleteQueries(queries);
            queries = null;
        }
    }

    private static boolean isEnabled() {
        return VeilRenderSystem.pipelineStatisticsQuerySupported() && VeilRenderSystem.renderer().getEditorManager().isVisible(inspector -> inspector instanceof PipelineStatisticsViewer);
    }

    public static List<ResultField> getCounters(String sectionPath, VeilRenderProfiler.StatisticType sortedStatistic) {
        List<ResultField> list = new LinkedList<>();
        if (!sectionPath.isEmpty()) {
            sectionPath = sectionPath + '\u001e';
        }

        long[] globalCount = new long[VeilRenderProfiler.StatisticType.ALL.length];
        long[] localCount = new long[VeilRenderProfiler.StatisticType.ALL.length];
        for (Map.Entry<String, VeilRenderProfilerImpl.PathEntry> entry : ENTRIES.entrySet()) {
            String section = entry.getKey();
            boolean directChild = isDirectChild(sectionPath, section);
            boolean isRoot = section.indexOf('\u001e') == -1;
            if (directChild || isRoot) {
                PathEntry value = entry.getValue();
                long[] counters = value.accumulatedCounters;
                for (VeilRenderProfiler.StatisticType statistic : value.statistics) {
                    int index = statistic.ordinal();
                    long counter = counters[index];
                    if (directChild) {
                        localCount[index] += counter;
                    }
                    if (isRoot) {
                        globalCount[index] += counter;
                    }
                }
            }
        }

        for (Map.Entry<String, VeilRenderProfilerImpl.PathEntry> entry : ENTRIES.entrySet()) {
            String section = entry.getKey();
            if (isDirectChild(sectionPath, section)) {
                PathEntry value = entry.getValue();
                list.add(new ResultField(section.substring(sectionPath.length()), value.accumulatedCounters, localCount, globalCount, value.statistics, sortedStatistic));
            }
        }

//        if ((float) l > f) {
//            list.add(new ResultField("unspecified", (double) ((float) l - f) * 100.0 / (double) l, (double) ((float) l - f) * 100.0 / (double) i, localCount));
//        }

        return list;
    }

    private static boolean isDirectChild(String sectionPath, String entry) {
        return entry.length() > sectionPath.length() && entry.startsWith(sectionPath) && entry.indexOf(30, sectionPath.length() + 1) < 0;
    }

    public record ResultField(
            String name,
            long[] count,
            long[] localCount,
            long[] globalCount,
            VeilRenderProfiler.StatisticType[] statistics,
            VeilRenderProfiler.StatisticType sortedStatistic
    ) implements Comparable<ResultField> {

        public double getPercentage(VeilRenderProfiler.StatisticType statistic) {
            long total = this.localCount[statistic.ordinal()];
            if (total == 0) {
                return 0.0;
            }
            return 100.0 * this.count[statistic.ordinal()] / total;
        }

        public double getGlobalPercentage(VeilRenderProfiler.StatisticType statistic) {
            long total = this.globalCount[statistic.ordinal()];
            if (total == 0) {
                return 0.0;
            }
            return 100.0 * this.count[statistic.ordinal()] / total;
        }

        @Override
        public int compareTo(ResultField other) {
            int index = this.sortedStatistic.ordinal();
            if (other.count[index] < this.count[index]) {
                return -1;
            } else {
                return other.count[index] > this.count[index] ? 1 : other.name.compareTo(this.name);
            }
        }

        public int getColor() {
            return (this.name.hashCode() & 11184810) + 4473924;
        }
    }

    public static class PathEntry {

        private final VeilRenderProfiler.StatisticType[] statistics;
        private final long[] accumulatedCounters;
        private boolean tracking;

        public PathEntry(VeilRenderProfiler.StatisticType[] statistics) {
            this.statistics = statistics;
            this.accumulatedCounters = new long[VeilRenderProfiler.StatisticType.ALL.length];
            this.tracking = false;
        }

        public void begin() {
            if (queries == null) {
                glGenQueries(queries = new int[VeilRenderProfiler.StatisticType.ALL.length]);
            }

            for (VeilRenderProfiler.StatisticType statistic : this.statistics) {
                if (ENABLED_STATISTICS.get(statistic.ordinal())) {
                    glBeginQuery(statistic.getId(), queries[statistic.ordinal()]);
                }
            }

            this.tracking = true;
        }

        public void stop() {
            for (VeilRenderProfiler.StatisticType statistic : this.statistics) {
                if (ENABLED_STATISTICS.get(statistic.ordinal())) {
                    glEndQuery(statistic.getId());
                    int index = statistic.ordinal();
                    long count = glGetQueryObjecti64(queries[index], GL_QUERY_RESULT);
                    this.accumulatedCounters[index] += count;
                }
            }
        }

        public void end(@Nullable PathEntry previous) {
            for (VeilRenderProfiler.StatisticType statistic : this.statistics) {
                if (ENABLED_STATISTICS.get(statistic.ordinal())) {
                    glEndQuery(statistic.getId());
                    int index = statistic.ordinal();
                    long count = glGetQueryObjecti64(queries[index], GL_QUERY_RESULT);
                    if (previous != null) {
                        previous.accumulatedCounters[index] += count;
                    }
                    this.accumulatedCounters[index] += count;
                }
            }
            this.tracking = false;

            if (previous != null) {
                previous.begin();
            }
        }

        public long[] getAccumulatedCounters() {
            return this.accumulatedCounters;
        }

        public VeilRenderProfiler.StatisticType[] getStatistics() {
            return this.statistics;
        }
    }

    private enum ActiveProfiler implements VeilRenderProfiler {
        INSTANCE;

        @Override
        public void push(String name, StatisticType... statistics) {
            if (statistics.length == 0) {
                statistics = StatisticType.ALL;
            }

            PathEntry old = ENTRIES.get(path);
            if (!path.isEmpty()) {
                path = path + "\u001e" + name;
            } else {
                path = name;
            }

            paths.add(path);
            PathEntry entry = ENTRIES.get(path);
            if (entry == null) {
                entry = new PathEntry(statistics);
                ENTRIES.put(path, entry);
            }
            if (ENABLED_PATHS.contains(path)) {
                if (old != null && old.tracking) {
                    old.stop();
                }
                entry.begin();
            }
        }

        @Override
        public void push(Supplier<String> name, StatisticType... statistics) {
            this.push(name.get(), statistics);
        }

        @Override
        public void pop() {
            if (paths.isEmpty()) {
                Veil.LOGGER.error("Tried to pop one too many times! Mismatched push() and pop()?");
                return;
            }

            PathEntry current = ENTRIES.get(path);
            paths.removeLast();
            path = paths.isEmpty() ? "" : paths.getLast();

            PathEntry previous = ENTRIES.get(path);
            if (current != null && current.tracking) {
                current.end(previous != null && previous.tracking ? previous : null);
            }
        }

        @Override
        public void popPush(String name, StatisticType... statistics) {
            this.pop();
            this.push(name, statistics);
        }

        @Override
        public void popPush(Supplier<String> name, StatisticType... statistics) {
            this.pop();
            this.push(name.get(), statistics);
        }
    }

    private enum InactiveProfiler implements VeilRenderProfiler {
        INSTANCE;

        @Override
        public void push(String name, StatisticType... statistics) {
        }

        @Override
        public void push(Supplier<String> name, StatisticType... statistics) {
        }

        @Override
        public void pop() {
        }

        @Override
        public void popPush(String name, StatisticType... statistics) {
        }

        @Override
        public void popPush(Supplier<String> name, StatisticType... statistics) {
        }
    }
}
