package su.nepom.cash.server.remote.mapper;

import org.mapstruct.*;
import su.nepom.cash.dto.RecordDto;
import su.nepom.cash.dto.RecordPartDto;
import su.nepom.cash.server.domain.Record;
import su.nepom.cash.server.domain.RecordPart;

@Mapper(config = MapperConfig.class)
public interface RecordMapper {
    @Mapping(target = "creator", source = "creator.id")
    RecordDto map(Record record);
    @InheritInverseConfiguration
    Record map(RecordDto record);

    @Mapping(target = "account", source = "account.id")
    RecordPartDto map(RecordPart part);
    @InheritInverseConfiguration
    @Mapping(target = "record", ignore = true)
    RecordPart map(RecordPartDto part);

    @AfterMapping
    static void afterMapping(@MappingTarget Record record) {
        for (var part : record.getParts())
            part.setRecord(record);
    }
}
