package su.nepom.cash.importaccess;

import lombok.AllArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;

@ShellComponent
@AllArgsConstructor
public class ShellCommands {
    private final Job importJob;
    private final JobLauncher jobLauncher;
    private final JobExplorer jobExplorer;

    // import-all d:
    @ShellMethod("importAll")
    public void importAll(String dir) throws Exception {
        JobExecution execution = jobLauncher.run(importJob, new JobParametersBuilder(jobExplorer)
                .addString("inputDir", dir)
                .getNextJobParameters(importJob)
                .toJobParameters());
        System.out.println(execution);
    }
}
