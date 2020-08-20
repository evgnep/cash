package su.nepom.cash.importaccess.job;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.listener.StepListenerSupport;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;
import su.nepom.cash.importaccess.Split;
import su.nepom.cash.importaccess.domain.Currency;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * Экспорт валют
 * Обычный chunk-step
 * В access нет кодов, они подставляются из словаря.
 * Все валюты сохраняются в SharedData для использования в других шагах
 */
@Configuration
@AllArgsConstructor
public class CurrencyStepConfig {
    private static final int CHUNK_SIZE = 10;
    private final static Map<String, Currency> converted = new HashMap<>() {{
        put("р", new Currency(0, "руб", "RUB"));
        put("usd", new Currency(0, "доллар", "USD"));
        put("евро", new Currency(0, "евро", "EUR"));
        put("золото", new Currency(0, "золото", "GOLD"));
        put("серебро", new Currency(0, "серебро", "SILVER"));
        put("платина", new Currency(0, "платина", "PLATINUM"));
        put("палладий", new Currency(0, "палладий", "PALLADIUM"));
        put("гривны", new Currency(0, "гривна", "UAH"));
    }};
    private final StepBuilderFactory stepBuilderFactory;

    @StepScope
    @Bean
    public FlatFileItemReader<Currency> currencyReader(JobParams params) {
        return new FlatFileItemReaderBuilder<Currency>()
                .name("currencyItemReader")
                .resource(new FileSystemResource(params.getInputDir() + "/Валюта.txt"))
                .lineMapper((s, i) -> {
                    var values = Split.splitAndConvert(s, "is");
                    return new Currency((Integer) values.get(0), (String) values.get(1), "");
                })
                .build();
    }

    @Bean
    public ItemProcessor<Currency, Currency> currencyProcessor() {
        return in -> {
            var converted = CurrencyStepConfig.converted.getOrDefault(in.getName(), in);
            return new Currency(in.getId(), converted.getName(), converted.getCode());
        };
    }

    @Bean
    public JdbcBatchItemWriter<Currency> currencyWriter(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Currency>()
                .dataSource(dataSource)
                .sql("insert into currency(id, name, code) values(:id, :name, :code)")
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .build();
    }

    @Bean
    public Step currencyStep(JdbcBatchItemWriter<Currency> writer, FlatFileItemReader<Currency> reader,
                             ItemProcessor<Currency, Currency> processor,
                             CurrencyItemProcessListener currencyItemProcessListener) {
        return stepBuilderFactory.get("stepCurrency")
                .<Currency, Currency>chunk(CHUNK_SIZE)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .listener((ItemProcessListener<Currency, Currency>) currencyItemProcessListener)
                .listener((StepExecutionListener) currencyItemProcessListener)
                .build();
    }

    @Component
    @AllArgsConstructor
    public static class CurrencyItemProcessListener extends StepListenerSupport<Currency, Currency> {
        private final SharedData sharedData;

        @Override
        public void afterProcess(Currency item, Currency result) {
            sharedData.addCurrency(result);
        }
    }
}
