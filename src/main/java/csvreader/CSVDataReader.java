package csvreader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class CSVDataReader {

    private static final String FILENAME = "data.csv";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter monthKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    
    
    Map<LocalDate, Double> data = new HashMap<>();
    
    private void readData() {
        Function<? super String[], ? extends LocalDate> keyMapper = strings -> LocalDate.parse(strings[0], formatter);
        Function<? super String[], ? extends Double> valueMapper = strings -> Double.parseDouble(StringUtils.deleteWhitespace(strings[1]).replace(',', '.'));
        
        try (Stream<String> stream = Files.lines(Paths.get(ClassLoader.getSystemResource(FILENAME).toURI()))) {
            stream
                    .map(record -> record.split(";", 2))
                    .filter(strings -> strings.length == 2)
                    .forEachOrdered(strings -> {
                        LocalDate key = keyMapper.apply(strings);
                        if (!this.data.containsKey(key)) {
                            this.data.put(key, valueMapper.apply(strings));
                        }
                    });
            
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void summarizeData() {
        Map<String, StatisticSummary> summaries = new HashMap<>();
        
        this.data.forEach((key, value) -> {
            String monthKey = key.format(monthKeyFormatter);
            if (!summaries.containsKey(monthKey)) {
                summaries.put(monthKey, new StatisticSummary());
            }
            summaries.get(monthKey).addValue(value);
        });
        
        summaries.forEach((monthKey, summary) -> System.out.printf("%s -> %.2f\n", monthKey, summary.getAverage()));
    }

    public void printDataSummary() {
        readData();
        summarizeData();
    }
    
    public static void main(String[] args) {
        CSVDataReader reader = new CSVDataReader();
        reader.printDataSummary();
    }
}
