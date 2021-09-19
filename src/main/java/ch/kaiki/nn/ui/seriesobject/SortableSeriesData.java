package ch.kaiki.nn.ui.seriesobject;

import org.jetbrains.annotations.NotNull;

public interface SortableSeriesData extends Comparable<SortableSeriesData> {

    double getZ();

    void render();

    @Override
    default int compareTo(@NotNull SortableSeriesData o) {
        return Double.compare(this.getZ(), o.getZ());
    }
}
