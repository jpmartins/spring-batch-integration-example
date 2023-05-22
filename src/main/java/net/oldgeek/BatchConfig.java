package net.oldgeek;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.PassThroughLineMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
//@EnableBatchProcessing : Spring-Boot-3.0 @EnableBatchProcessing is now discouraged > Caused erro "BATCH_JOB_INSTANCE" not found (this database is empty) 
public class BatchConfig {

	@Autowired
	private JobRepository jobRepository;
	@Autowired
	private PlatformTransactionManager transactionManager;

	@Bean
	Step sampleStep() {
		return new StepBuilder("sampleStep",jobRepository)//
				.<String, String>chunk(5,transactionManager) // In Spring Batch 5.0, chunk without transactionManager was depprecated 
				.reader(itemReader(null)) // null will be replaced by #{jobParameters[file_path]}
				.writer(i -> i.forEach(j -> System.out.println(j))) //
				.build();
	}

	@Bean
	Job sampleJob() {
		Job job = new JobBuilder("sampleJob",jobRepository) //
				.incrementer(new RunIdIncrementer()) //
				.start(sampleStep()) //
				.build();
		return job;
	}

	@Bean
	@StepScope
	FlatFileItemReader<String> itemReader(@Value("#{jobParameters[file_path]}") String filePath) {
		FlatFileItemReader<String> reader = new FlatFileItemReader<String>();
		final FileSystemResource fileResource = new FileSystemResource(filePath);
		reader.setResource(fileResource);
		reader.setLineMapper(new PassThroughLineMapper());
		return reader;
	}

}
