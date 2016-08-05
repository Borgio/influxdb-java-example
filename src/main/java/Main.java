import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;

import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) {

        // Connect to InfluxDB
        InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");

        // Create a database
        String dbName = "aTimeSeries";
        influxDB.createDatabase(dbName);

        // Create a 'batch' of example 'points'
        BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .tag("async", "true")
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .tag("BatchTag", "BatchTagValue") // tag each point in the batch
                .build();
        Point point1 = Point.measurement("cpu")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("idle", 90L)
                .addField("user", 9L)
                .addField("system", 1L)
                .tag("CpuTag", "CpuTagValue") // tag the individual point
                .build();
        Point point2 = Point.measurement("disk")
                .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .addField("used", 80L)
                .addField("free", 1L)
                .build();
        batchPoints.point(point1);
        batchPoints.point(point2);

        // Write them to InfluxDB
        influxDB.write(batchPoints);

        Query query = new Query("SELECT * FROM cpu", dbName);
        QueryResult queryResult = influxDB.query(query);

        // iterate the results and print details
        for (QueryResult.Result result : queryResult.getResults()) {

            // print details of the entire result
            System.out.println(result.toString());

            // iterate the series within the result
            for (QueryResult.Series series : result.getSeries()) {
                System.out.println("series.getName() = " + series.getName());
                System.out.println("series.getColumns() = " + series.getColumns());
                System.out.println("series.getValues() = " + series.getValues());
                System.out.println("series.getTags() = " + series.getTags());
            }
        }

        // Delete the database
        influxDB.deleteDatabase(dbName);
    }
}
