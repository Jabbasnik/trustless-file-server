package pl.jackowiak.trustlessfileserver.infrastructure;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pl.jackowiak.trustlessfileserver.domain.Facade;
import pl.jackowiak.trustlessfileserver.domain.ports.in.ServerFiles;
import pl.jackowiak.trustlessfileserver.domain.ports.in.StoreFile;
import pl.jackowiak.trustlessfileserver.domain.ports.out.FileServerRepository;

/**
 * Spring related configuration.
 */
@Configuration
public class FileServerConfig {

    @Bean
    public ServerFiles serverFiles(FileServerRepository fileServerRepository) {
        return new Facade(fileServerRepository);
    }

    @Bean
    public StoreFile storeFile(FileServerRepository fileServerRepository) {
        return new Facade(fileServerRepository);
    }

    @Bean
    public FileServerRepository fileServerRepository() {
        return new InMemoryFileServerRepository();
    }
}
