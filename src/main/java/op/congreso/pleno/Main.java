package op.congreso.pleno;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Main {
    public static final String BASE_URL = "https://www2.congreso.gob.pe";

    public static Map<String, String> collect(String url, int colspan) throws IOException {
        var root = new LinkedHashMap<String, String>();
        {
            var jsoup = Jsoup.connect(BASE_URL + url);
            var doc = jsoup.get();
            var main = doc.body().select("table[cellpadding=2]").first();

            assert main != null;
            for (var td : main.select("td[colspan=%s]".formatted(colspan))) {
                var table = td.child(0).selectFirst("table");
                assert table != null;
                var tds = table.select("td");
                if (tds.size() == 2) {
                    var a = tds.get(0).selectFirst("a");
                    assert a != null;
                    var href = a.attr("href");
                    var periodo = tds.get(1).text();
                    root.put(periodo, href);
                }
            }
            return root;
        }
    }

    private static final Pattern p = Pattern.compile("javascript:openWindow\\('(.+)'\\)");

    public static Map<String, Pleno> collectPleno(String pp, String pa, String l, String url) throws IOException {
        var root = new LinkedHashMap<String, Pleno>();
        {
            var jsoup = Jsoup.connect(BASE_URL + url);
            var doc = jsoup.get();
            var main = doc.body().select("table[cellpadding=2]").first();
            assert main != null;
            var trs = main.select("tr[valign=top]");
            for (var tr : trs) {
                if (tr.children().size() == 6) {
                    var fonts = tr.select("font[size=4]");
                    var fecha = fonts.get(0).text();
                    var second = fonts.get(2).children().first();
                    assert second != null;
                    var titulo = second.text();
                    var href = fonts.get(2).select("a").attr("href");
                    var matcher = p.matcher(href);
                    if (matcher.find()) {
                        var u = matcher.group(1);
                        var fullUrl = BASE_URL + "/Sicr/RelatAgenda/PlenoComiPerm20112016.nsf/" + u;
                        root.put(titulo, new Pleno(
                                pp, pa, l,
                                fecha, titulo, fullUrl,
                                fullUrl.split("/")[9],
                                0));
                    }
                }
            }
            return root;
        }
    }

    public static void main(String[] args) throws IOException {
        var root = collect("/Sicr/RelatAgenda/PlenoComiPerm20112016.nsf/new_asistenciavotacion", 5);

        var plenosList = new LinkedList<Pleno>();

        for (var entry : root.entrySet()) {
            System.out.println(entry.getKey());
            var year = collect(entry.getValue(), 4);
            for (var e2 : year.entrySet()) {
                System.out.println(e2.getKey());
                var periodo = collect(e2.getValue(), 3);
                for (var e3 : periodo.entrySet()) {
                    System.out.println(e3.getKey());

                    var plenos = collectPleno(entry.getKey(), e2.getKey(), e3.getKey(), e3.getValue());
                    plenosList.addAll(plenos.values());
                }
            }
        }

        var jsonMapper = new ObjectMapper();

        var bytes = Files.readAllBytes(Path.of("plenos.json"));
        var existing = jsonMapper.readValue(bytes, new TypeReference<List<Pleno>>() {
                })
                .stream().collect(Collectors.toMap(Pleno::id, p -> p));

        if (plenosList.size() > existing.size()) {
            var updated = plenosList.stream().map(p -> existing.getOrDefault(p.id(), p))
                    .map(p -> {
                        if (p.paginas() < 1) {
                            p.download();
                            return p.withPaginas();
                        } else {
                            return p;
                        }
                    })
                    .sorted(Comparator.comparing(Pleno::id))
                    .collect(Collectors.toList());
            Files.writeString(Path.of("plenos.json"),  jsonMapper.writerWithDefaultPrettyPrinter().writeValueAsString(updated));
        }
    }
}
