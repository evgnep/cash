package su.nepom.cash.dto;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class RecordDto {
    private UUID id;
    private Instant time; // время к которому относится запись
    private Instant createTime; // время создания проводки
    private Instant updateTime; // время изменения проводки
    private String note;
    private boolean validated;
    private long creator;
    private List<RecordPartDto> parts = new ArrayList<>();
}
