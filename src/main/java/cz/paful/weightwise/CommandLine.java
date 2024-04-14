package cz.paful.weightwise;


import cz.paful.weightwise.service.CsvDataService;
import cz.paful.weightwise.data.dto.MeasurementDTO;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.List;

@Profile("!test")
@Component
public class CommandLine implements CommandLineRunner {

    private static Logger LOG = LoggerFactory
            .getLogger(CommandLine.class);


    @Override
    public void run(String... args) throws IOException {
        LOG.info("EXECUTING : command line runner");

        for (int i = 0; i < args.length; ++i) {
            LOG.info("args[{}]: {}", i, args[i]);
        }
        
        Scanner scanner = new Scanner(System.in);

        CsvDataService csvDataService = new CsvDataService(false);

        boolean exit = false;
        while (!exit) {
            System.out.println("1 - add measurement");
            System.out.println("2 - show measurements");
            System.out.println("3 - exit");

            int option = 0;
            option = scanner.nextInt();
            scanner.nextLine();

            switch (option) {
                case 1:
                    registerMeasurement(scanner, csvDataService);
                    break;
                case 2:
                    System.out.println("average days: ");
                    option = scanner.nextInt();
                    scanner.nextLine();
                    showMeasurements(csvDataService, option);
                    break;
                case 3:
                    exit = true;
                    break;
            }

        }
    }

    private void showMeasurements(CsvDataService csvDataService, int averageDays) throws IOException {
        List<MeasurementDTO> measurements = csvDataService.read();
        measurements.sort(Collections.reverseOrder());

        removeOldestMeasurementsFromSameDay(measurements);

        List<MeasurementDTO> averageMeasurements = measurements;
        if (averageDays > 1) {
            averageMeasurements = getAverageMeasurementPerDays(measurements, averageDays);
        }

        printResults(averageMeasurements);


        List<LocalDate> xData = new ArrayList<>();
        List<Float> yData = new ArrayList<>();

        // Fill x and y data for chart
        for (MeasurementDTO measurement : averageMeasurements) {
            xData.add(measurement.getDate());
            yData.add(measurement.getMuscleMassKG());
        }

        // x data needs to be date type
        List<Date> xDataDate = new ArrayList<>();
        for (LocalDate localDate : xData) {
            xDataDate.add(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        savePngChart(xDataDate, yData, csvDataService.getFullPath());
    }

    private void registerMeasurement(Scanner scanner, CsvDataService csvDataService) throws IOException {

        System.out.println("Date in format dd/MM/yyyy");
        LocalDate localDate = getDateValue(scanner, csvDataService);

        System.out.println("Time in format hh:mm");
        LocalTime localTime = getTimeValue(scanner, csvDataService);

        System.out.println("Weight KG: ");
        float weight = getFloatValue(scanner);

        System.out.println("BMI: ");
        float bmi = getFloatValue(scanner);

        System.out.println("Body fat percentage: ");
        float fat = getFloatValue(scanner);

        System.out.println("Water percentage: ");
        float water = getFloatValue(scanner);

        System.out.println("Muscle mass percentage: ");
        float muscle = getFloatValue(scanner);

        System.out.println("Bones KG: ");
        float bones = getFloatValue(scanner);

        System.out.println("Comment: ");
        String comment = scanner.nextLine();

        System.out.println("Medication: ");
        String medication = scanner.nextLine();

        List<MeasurementDTO> measurements = csvDataService.read();

        MeasurementDTO measurementDTO = new MeasurementDTO();
        measurementDTO.setDate(localDate);
        measurementDTO.setTime(localTime);
        measurementDTO.setWeightKg(weight);
        measurementDTO.setBmi(bmi);
        measurementDTO.setBodyFatProcentage(fat);
        measurementDTO.setWaterProcentage(water);
        measurementDTO.setMuscleMassProcentage(muscle);
        measurementDTO.setBonesKg(bones);
        measurementDTO.setComment(comment);
        measurementDTO.setMedication(medication);

        measurements.add(measurementDTO);

        csvDataService.write(measurements);
    }

    private LocalDate getDateValue(Scanner input, CsvDataService csvDataService) {
        while (true) {
            String date = input.nextLine();

            if (date.isEmpty()) {
                return LocalDate.now();
            }

            try {
                return LocalDate.parse(date, csvDataService.getDateFormatter());
            } catch (DateTimeParseException ignored) {
                System.out.println("date = " + date + " expected format = " + csvDataService.getDateFormatter().toString());
            }
        }
    }

    private LocalTime getTimeValue(Scanner input, CsvDataService csvDataService) {
        while (true) {
            String time = input.nextLine();

            if (time.isEmpty()) {
                return LocalTime.now();
            }

            try {
                return LocalTime.parse(time, csvDataService.getDateFormatter());
            } catch (DateTimeParseException ignored) {
                System.out.println("time = " + time + " expected format = " + csvDataService.getTimeFormatter().toString());
            }
        }
    }

    private float getFloatValue(Scanner input) {
        Float value = null;
        while (value == null) {
            value = parseFloat(input.nextLine());
        }

        return value;
    }

    private Float parseFloat(String number) {
        try {
            return Float.parseFloat(number);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    /**
     * Keeps only first measurement of day
     * @param measurements list of measurements
     */
    private void removeOldestMeasurementsFromSameDay(List<MeasurementDTO> measurements) {
        MeasurementDTO previousMeaserument = new MeasurementDTO();
        List<MeasurementDTO> measurementToRemove = new ArrayList<>();
        for (MeasurementDTO measurementDTO : measurements) {
            if (measurementDTO.getDate().equals(previousMeaserument.getDate())) {
                measurementToRemove.add(measurementDTO);
            }
            previousMeaserument = measurementDTO;
        }

        measurementToRemove.forEach(m -> LOG.info("removing measurement " + m.getDate() + " " + m.getTime() + " " + m.getWeightKg()));
        measurements.removeAll(measurementToRemove);
    }

    private List<MeasurementDTO> getAverageMeasurementPerDays(List<MeasurementDTO> measurements, int days) {
        List<MeasurementDTO> averageMuscleList = new ArrayList<>();
        int firstIndex = -days;
        int lastIndex = 0;

        // Get average muscle mass for x days
        for(int i = 0; measurements.size() > i; i = i + days) {
            firstIndex += days;
            lastIndex += days;

            if (lastIndex > measurements.size() - 1) {
                break;
            }

            float averageMuscle = 0f;
            float averageFat = 0f;
            float averageWater = 0f;
            float averageWeight = 0f;

            MeasurementDTO averageMeasurement = new MeasurementDTO();

            for (int index = firstIndex; index < lastIndex; index++) {
                MeasurementDTO measurementDTO = measurements.get(index);
                averageMuscle += measurementDTO.getMuscleMassProcentage();
                averageFat += measurementDTO.getBodyFatProcentage();
                averageWater += measurementDTO.getWaterProcentage();
                averageWeight += measurementDTO.getWeightKg();
            }

            averageMeasurement.setDate(measurements.get(firstIndex).getDate());
            averageMeasurement.setMuscleMassProcentage(averageMuscle / days);
            averageMeasurement.setBodyFatProcentage(averageFat / days);
            averageMeasurement.setWaterProcentage(averageWater / days);
            averageMeasurement.setWeightKg(averageWeight / days);

            averageMuscleList.add(averageMeasurement);
        }

        return averageMuscleList;
    }

    private void printResults(List<MeasurementDTO> measurements) {
        List<String> weight = new ArrayList<>();
        List<String> fat = new ArrayList<>();
        List<String> muscle = new ArrayList<>();
        List<String> water = new ArrayList<>();

        for (MeasurementDTO measurement : measurements) {
            weight.add(measurement.getWeightKg() + "kg");
            fat.add(measurement.getBodyFatKG() + "kg");
            muscle.add(measurement.getMuscleMassKG() + "kg");
            water.add(measurement.getWaterProcentage() + "%");
        }

        System.out.print("weight: ");
        weight.forEach(v -> System.out.print(v + " <- "));
        System.out.print("\n");
        System.out.print("fat: ");
        fat.forEach(v -> System.out.print(v + " <- "));
        System.out.print("\n");
        System.out.print("muscle: ");
        muscle.forEach(v -> System.out.print(v + " <- "));
        System.out.print("\n");
        System.out.print("water: ");
        water.forEach(v -> System.out.print(v + " <- "));
        System.out.print("\n");
    }

    private void savePngChart(List<?> xData, List<? extends Number> yData, String path) throws IOException {

        // Create Chart
        XYChart chart = new XYChart(500, 400);
        chart.setTitle("Sample Chart");
        chart.setXAxisTitle("Time");
        chart.setXAxisTitle("Muscle");
        XYSeries series = chart.addSeries("y(x)", xData, yData);
        series.setMarker(SeriesMarkers.CIRCLE);
        series.setFillColor(Color.RED);

        BitmapEncoder.saveBitmap(chart, path, BitmapEncoder.BitmapFormat.PNG);
    }

}