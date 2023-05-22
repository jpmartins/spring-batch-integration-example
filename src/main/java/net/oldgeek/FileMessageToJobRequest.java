package net.oldgeek;

import java.io.File;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;

// See http://docs.spring.io/spring-batch/trunk/reference/html/springBatchIntegration.html#launching-batch-jobs-through-messages
public class FileMessageToJobRequest {

	private Job job;

	private String fileParameterName = "input.file.name";

	public void setFileParameterName(String fileParameterName) {
		this.fileParameterName = fileParameterName;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	@Transformer// inputChannel and outputChannel setup in sampleFlow Bean (check IntegrationConfig.class)
	public JobLaunchRequest toRequest(Message<File> message) {
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

		jobParametersBuilder.addString(fileParameterName, message.getPayload().getAbsolutePath());
		// to force importing files existing in the folder at each poll (even if they were imported before)
		//jobParametersBuilder.addDate("timestamp", new Date());
		// Warning currently the integration does not remove the source file, and as such default behavior, only first time, seems plausible 
		
		return new JobLaunchRequest(job, jobParametersBuilder.toJobParameters());
	}
}