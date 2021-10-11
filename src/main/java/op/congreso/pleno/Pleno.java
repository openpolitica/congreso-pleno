package op.congreso.pleno;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;

record Pleno(
        String periodoParlamentario, String periodoAnual, String legislatura,
        String fecha, String titulo, String url, String filename, int paginas) {
    String csvEntry() {
        return "%s,%s,%s,%s,\"%s\",%s,%s%n".formatted(
                periodoParlamentario, periodoAnual, legislatura,
                fecha, titulo, url, filename
        );
    }
     String directory() {
        return periodoParlamentario + "/" + periodoAnual + "/" + legislatura;
    }

    String path() {
        return directory() + "/" + filename;
    }

    Pleno withPaginas() {
        return new Pleno(periodoParlamentario, periodoAnual, legislatura,
                fecha, titulo, url, filename,
                countPages());
    }

    String id() {
        return "%s:%s:%s:%s:%s".formatted(periodoParlamentario, periodoAnual, legislatura, fecha, titulo);
    }

    int countPages() {
        try (PDDocument doc = PDDocument.load(Path.of("out/"+path()).toFile())){
            return doc.getNumberOfPages();
        } catch (Exception | NoClassDefFoundError e) {
            System.out.println("ERROR with path: " + path());
            e.printStackTrace();
            return -1;
        }
    }

    void download() {
        try {
            ReadableByteChannel readableByteChannel = Channels.newChannel(new URL(url()).openStream());
            FileOutputStream fileOutputStream = new FileOutputStream("out/" + path());
            fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
