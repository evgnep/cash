package su.nepom.cash.importaccess.job;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@JobScope
@Component
public class JobParams {
    private final String inputDir;

    public JobParams(@Value("#{jobParameters['inputDir']}") String inputDir) {
        this.inputDir = inputDir;
    }

    public String getInputDir() {
        return inputDir;
    }
}
