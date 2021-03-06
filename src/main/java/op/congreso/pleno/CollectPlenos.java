package op.congreso.pleno;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.stream.Collectors;

import static op.congreso.pleno.Pleno.collect;
import static op.congreso.pleno.Pleno.collectPleno;

public class CollectPlenos {

    public static void main(String[] args) throws IOException {
        var root = collect("/Sicr/RelatAgenda/PlenoComiPerm20112016.nsf/new_asistenciavotacion", 5);

        var content = Pleno.csvHeader();

        var plenosList = new LinkedList<Pleno>();

        for (var entry : root.entrySet()) {
            System.out.println(entry.getKey());
            var year = collect(entry.getValue(), 4);
            for (var e2: year.entrySet()) {
                System.out.println(e2.getKey());
                var periodo = collect(e2.getValue(), 3);
                for (var e3 : periodo.entrySet()) {
                    System.out.println(e3.getKey());

                    var plenos = collectPleno(entry.getKey(), e2.getKey(), e3.getKey(), e3.getValue());
                    var csv = plenos.values().stream()
                            .map(Pleno::csvEntry)
                            .collect(Collectors.joining());
                    content.append(csv);
                    plenosList.addAll(plenos.values());
                }
            }
        }

        var jsonMapper = new ObjectMapper();
        Files.writeString(Path.of("plenos.json"),  jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(plenosList));
        Files.writeString(Path.of("plenos.csv"), content.toString());
    }
}
