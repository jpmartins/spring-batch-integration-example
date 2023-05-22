package net.oldgeek;

import java.io.File;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.support.TaskExecutorJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.file.filters.SimplePatternFileListFilter;
import org.springframework.integration.handler.LoggingHandler;

@Configuration
public class IntegrationConfig {

	@Autowired
	private Job sampleJob;

	/*protected DirectChannel inputChannel() {
		return new DirectChannel();
	}
	
	protected DirectChannel outputChannel() {
		return new DirectChannel();
	}*/

    @Bean
    IntegrationFlow sampleFlow() {
		// @formatter:off
		return IntegrationFlow // IntegrationFlows deprecated since Spring Integration 6 > methods relocated directly to the IntegrationFlow interface
				//.from(fileReadingMessageSource(), c -> c.poller(Pollers.fixedDelay(5000)))//
				.from(Files.inboundAdapter(new File("dropfolder")).filter(new SimplePatternFileListFilter("*.txt")),
						c -> c.poller(Pollers.fixedRate(15000).maxMessagesPerPoll(1)))
				//.channel(inputChannel()) //
				.transform(fileMessageToJobRequest()) //
				//.channel(outputChannel())
				/*.handle(jobLaunchingMessageHandler()) //
				.handle(jobExecution -> {
					System.out.println(jobExecution.getPayload());
				}) //*/
				.handle(jobLaunchingGateway(null)). // null replaced by @Autowired
					log(LoggingHandler.Level.WARN, "headers.id + ': ' + payload")
				.handle(message -> {
					System.out.println("payload:"+message.getPayload());
				})
				.get();
		// @formatter:on
	}

    /*@Bean
    MessageSource<File> fileReadingMessageSource() {
		FileReadingMessageSource source = new FileReadingMessageSource();
		source.setDirectory(new File("dropfolder"));
		source.setFilter(new SimplePatternFileListFilter("*.txt"));
		source.setUseWatchService(true);
		source.setWatchEvents(WatchEventType.CREATE);
		return source;
	}*/

	@Bean
	FileMessageToJobRequest fileMessageToJobRequest() {
		FileMessageToJobRequest transformer = new FileMessageToJobRequest();
		transformer.setJob(sampleJob);
		transformer.setFileParameterName("file_path");
		return transformer;
	}

	/*@Bean
	JobLaunchingMessageHandler jobLaunchingMessageHandler() {
		JobLaunchingMessageHandler handler = new JobLaunchingMessageHandler(jobLauncher);
		return handler;
	}*/
	
	@Bean
	//@ServiceActivator(inputChannel = "queueChannel", poller = @Poller(fixedRate="1000"))
	public JobLaunchingGateway jobLaunchingGateway(@Autowired JobRepository jobRepository) {
	    TaskExecutorJobLauncher jobLauncher = new TaskExecutorJobLauncher();
	    jobLauncher.setJobRepository(jobRepository);
	    jobLauncher.setTaskExecutor(new SyncTaskExecutor());
	    JobLaunchingGateway jobLaunchingGateway = new JobLaunchingGateway(jobLauncher);

	    return jobLaunchingGateway;
	}
	
	/*
	@Bean
	@ServiceActivator(inputChannel = "stepExecutionsChannel")
	public LoggingHandler loggingHandler() {
	    LoggingHandler adapter = new LoggingHandler(LoggingHandler.Level.WARN);
	    adapter.setLoggerName("TEST_LOGGER");
	    adapter.setLogExpressionString("headers.id + ': ' + payload");
	    return adapter;
	}
	
	@MessagingGateway(name = "notificationExecutionsListener", defaultRequestChannel = "stepExecutionsChannel")
	public interface NotificationExecutionListener extends StepExecutionListener {}
	*/
}