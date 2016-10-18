package csvreader;

import java.util.ArrayList;
import java.util.List;

public class StatisticSummary {
    
    private List<Double> values = new ArrayList<>();
    
    public void addValue(Double value) {
        values.add(value);
    }
    
    public Double getAverage() {
        return values.stream().mapToDouble(v -> v).average().orElse(0.0);
    }
    
}
