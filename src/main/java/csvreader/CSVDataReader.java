package csvreader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class CSVDataReader {

    private static final String FILENAME = "data.csv";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter monthKeyFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter yearKeyFormatter = DateTimeFormatter.ofPattern("yyyy");
    
    
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

    private void summarizeData(DateTimeFormatter keyFormatter) {
        Map<String, StatisticSummary> summaries = new TreeMap<>();
        
        this.data.forEach((key, value) -> {
            String monthKey = key.format(keyFormatter);
            if (!summaries.containsKey(monthKey)) {
                summaries.put(monthKey, new StatisticSummary());
            }
            summaries.get(monthKey).addValue(value);
        });
        
        summaries.forEach((monthKey, summary) -> System.out.printf("%s -> %.2f\n", monthKey, summary.getAverage()));
    }
    
    private void fillInGaps() {
        LocalDate earliestDate = this.data.keySet().stream().min(LocalDate::compareTo).get();
        LocalDate latestDate = this.data.keySet().stream().max(LocalDate::compareTo).get();
        LocalDate analysedDate = earliestDate;
        while (analysedDate.isBefore(latestDate)) {
            if (!this.data.containsKey(analysedDate)) {
                LocalDate previousDay = analysedDate.minusDays(1);
                final LocalDate finalAnalysedDate = analysedDate;
                LocalDate nextAvailableDay = this.data.keySet().stream()
                        .filter(date -> date.isAfter(finalAnalysedDate))
                        .min(LocalDate::compareTo)
                        .get();
                double interpolatedValue = (this.data.get(previousDay)+this.data.get(nextAvailableDay))/2.0;
                this.data.put(analysedDate, interpolatedValue);
            }
            analysedDate = analysedDate.plusDays(1);
        }
    }

    public void printDataSummary() {
        readData();
        fillInGaps();
        summarizeData(monthKeyFormatter);
        summarizeData(yearKeyFormatter);
    }

    public static void main(String[] args) {
        CSVDataReader reader = new CSVDataReader();
        reader.printDataSummary();
    }
}
