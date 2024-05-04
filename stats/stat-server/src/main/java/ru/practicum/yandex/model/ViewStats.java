package ru.practicum.yandex.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewStats {

    private String app;

    private String uri;

    private Long hits;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ViewStats viewStats = (ViewStats) o;

        if (!app.equals(viewStats.app)) return false;
        if (!uri.equals(viewStats.uri)) return false;
        return hits.equals(viewStats.hits);
    }

    @Override
    public int hashCode() {
        int result = app.hashCode();
        result = 31 * result + uri.hashCode();
        result = 31 * result + hits.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ViewStats{" +
                "app='" + app + '\'' +
                ", uri='" + uri + '\'' +
                ", hits=" + hits +
                '}';
    }
}
