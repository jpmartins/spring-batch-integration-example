package net.oldgeek;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@EnableAutoConfiguration // auto-configure jobRepository and dataSource, since Spring Boot Test 3: fix No bean named 'dataSource' available
@SpringBootTest(classes = BatchConfig.class ) // initialize SampleJob
@SpringBatchTest // auto-configure JobLauncherTestUtils
public class BatchTests {

	@Test
	public void testSampleJob(@Autowired JobLauncherTestUtils jobLauncherTestUtils) throws Exception {
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString("file_path", "src/test/resources/sample.txt");

		JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParametersBuilder.toJobParameters());
		System.out.println(jobExecution.getExitStatus());
		assertEquals(jobExecution.getExitStatus(), ExitStatus.COMPLETED);
	}
}
