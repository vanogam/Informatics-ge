package ge.freeuni.informatics.common.model.task;

import jakarta.persistence.*;

import java.util.Objects;

/**
 * A shared, staff-curated label a task can be tagged with (e.g. "DP", "Graphs"). Normalized
 * rather than a bare string on {@link Task} so the name has one canonical spelling to filter and
 * autocomplete against, and renaming a tag is a single-row update.
 */
@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(unique = true, nullable = false)
    String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Tag) {
            return Objects.equals(id, ((Tag) obj).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
