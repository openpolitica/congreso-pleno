package op.congreso.pleno;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

public class DownloadPdfs {
    public static void main(String[] args) throws IOException {
        ObjectMapper jsonMapper = new ObjectMapper();
        var bytes = Files.readAllBytes(Path.of("plenos.json"));
        var plenos = jsonMapper.readValue(bytes,  new TypeReference<List<Pleno>>() {});

        plenos.stream()
                .filter(pleno -> pleno.paginas() < 1)
                .map(Pleno::directory)
                .collect(Collectors.toSet())
                .forEach(s -> {
                    try {
                        Files.createDirectories(Path.of("out/" + s));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        plenos.stream()
                .filter(pleno -> pleno.paginas() < 1)
                .parallel()
                .forEach(s -> {
                    download(s);
                });
    }

    private static void download(Pleno s) {
        try {
            ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(s.url()).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("out/" + s.path());
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
