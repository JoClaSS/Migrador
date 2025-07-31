package com.br.transaction;

import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;
import jakarta.persistence.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;


@Configuration
public class BatchConfig {

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Bean
    public Job importarPessoasJob(Step step1) {
        return new JobBuilder("importarPessoasJob", jobRepository)
                .start(step1)
                .build();
    }

    @Bean
    public Step step1(ItemReader<PessoaUsuarioDTO> reader,
                      ItemProcessor<PessoaUsuarioDTO, Usuario> processor,
                      ItemWriter<Usuario> writer) {
        return new StepBuilder("step1", jobRepository)
                .<PessoaUsuarioDTO, Usuario>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }



    @Bean
    public FlatFileItemReader<PessoaUsuarioDTO> reader() {
        return new FlatFileItemReaderBuilder<PessoaUsuarioDTO>()
                .name("pessoaUsuarioReader")
                .resource(new FileSystemResource("C:/Users/José/Desktop/Project/pessoa.csv"))
                .linesToSkip(1)// skipar o header
                .delimited()
                .names("nome", "email", "sexo", "ativo", "login", "senha", "role")
                .fieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                    setTargetType(PessoaUsuarioDTO.class);
                }})
                .build();
    }

    @Bean
    public ItemProcessor<PessoaUsuarioDTO, Usuario> pessoaComUsuarioProcessor() {
        return dto -> {
            System.out.println("Processando DTO: " + dto);

            Pessoa pessoa = new Pessoa();
            pessoa.setNome(dto.getNome().toUpperCase());
            pessoa.setEmail(dto.getEmail());
            pessoa.setSexo(dto.getSexo());
            pessoa.setAtivo(dto.getAtivo());

            Usuario usuario = new Usuario();
            usuario.setLogin(dto.getLogin());
            usuario.setPassword(new BCryptPasswordEncoder().encode(dto.getSenha()));
            usuario.setAtivo(dto.getAtivo());
            usuario.setRole(dto.getRole());
            usuario.setPessoa(pessoa);

            return usuario;
        };
    }

    @Bean
    public JpaItemWriter<Usuario> writer() {
        JpaItemWriter<Usuario> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        return writer;
    }

    public void verificarArquivoCSV() {
        System.out.println("Verificando CSV...");
        String caminho = "C:/Users/José/Desktop/Project/pessoa.csv";
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String line;
            int lineNumber = 1;
            boolean encontrado = false;

            while ((line = br.readLine()) != null) {
                if (line.contains("\0")) {
                    System.out.println("Linha com caractere nulo encontrada: " + lineNumber);
                    System.out.println("Conteúdo: " + line);
                    encontrado = true;
                }
                lineNumber++;
            }

            if (!encontrado) {
                System.out.println("Nenhum caractere nulo encontrado no arquivo.");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Bean //Vou executar o runner após rodar o projeto
    public CommandLineRunner runJob(JobLauncher jobLauncher, Job job) {
        return args -> {
            verificarArquivoCSV(); // <- aqui

            jobLauncher.run(job, new JobParametersBuilder()
                    .addLong("startAt", System.currentTimeMillis())
                    .toJobParameters());
        };
    }
}
