package Models;

import com.opencsv.bean.CsvBindByName;

public class FlightData {

    @CsvBindByName(column = "DepartureTime")
    private String departureTime;

    @CsvBindByName(column = "ArrivalTime")
    private String arrivalTime;

    @CsvBindByName(column = "Airline")
    private String airline;

    @CsvBindByName(column = "Price")
    private double price;

    @CsvBindByName(column = "Connection")
    private String connection;

    @CsvBindByName(column = "Duration")
    private String duration;

    public FlightData() {}

    public FlightData(String departureTime, String arrivalTime, String airline, double price, String connection, String duration) {
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.airline = airline;
        this.price = price;
        this.connection = connection;
        this.duration = duration;
    }

    // Getters and Setters
    public String getDepartureTime() { return departureTime; }
    public void setDepartureTime(String departureTime) { this.departureTime = departureTime; }

    public String getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(String arrivalTime) { this.arrivalTime = arrivalTime; }

    public String getAirline() { return airline; }
    public void setAirline(String airline) { this.airline = airline; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getConnection() { return connection; }
    public void setConnection(String connection) { this.connection = connection; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    @Override
    public String toString() {
        return "FlightData{" +
                "airline='" + airline + '\'' +
                ", departure='" + departureTime + '\'' +
                ", arrival='" + arrivalTime + '\'' +
                ", price=" + price +
                ", connection='" + connection + '\'' +
                ", duration='" + duration + '\'' +
                '}';
    }
}
