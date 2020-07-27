package su.nepom.cash.server.domain;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Проводка.
 * <p>Может быть создана или изменена без связи с сервером
 */
@Data
@Accessors(chain = true)
@Entity
public class Record {
    @Id
    @GeneratedValue
    private UUID id;
    private Instant time; // время к которому относится запись
    @Column(updatable = false, insertable = false)
    private Instant createTime; // время создания проводки
    @Column(updatable = false, insertable = false)
    private Instant updateTime; // время изменения проводки
    private String note;
    private boolean validated;
    @ManyToOne(fetch = FetchType.EAGER)
    private User creator;
    @OneToMany(fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL, mappedBy = "record")
    @Setter(AccessLevel.NONE)
    @OrderBy("no")
    @EqualsAndHashCode.Exclude
    private List<RecordPart> parts = new ArrayList<>();

    public Record addPart(RecordPart part) {
        parts.add(part);
        part.setRecord(this);
        return this;
    }

    public boolean removePartsUnavailableToChild() {
        parts.removeIf(part -> !part.getAccount().isAvailableToChild());
        return !parts.isEmpty();
    }

    public boolean isAvailableToChild() {
        return parts.stream().allMatch(p -> p.getAccount().isAvailableToChild());
    }
}
