package op.congreso.pleno;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DownloadPdfs {
    public static void main(String[] args) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        var bytes = Files.readAllBytes(Path.of("plenos.json"));
        var plenos = jsonMapper.readValue(bytes,  new TypeReference<List<Pleno>>() {});

        plenos.stream()
                .parallel()
                .forEach(Pleno::download);
    }


}
