package cz.paful.weightwise.service;

import cz.paful.weightwise.data.dto.MeasurementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.time.temporal.ChronoField.*;

@Service
public class CsvDataService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvDataService.class);

    private static final String DELIMETER = ";";
    private static String FILE_NAME;

    private final boolean isTest;

    public CsvDataService(@Value("${app.isTest}") boolean isTest) {
        this.isTest = isTest;
        FILE_NAME = isTest ? "measurements_test.csv" : "measurements.csv";
    }

    public List<MeasurementDTO> read() throws FileNotFoundException {
        return read(new FileInputStream(getFullPath()));
    }

    public List<MeasurementDTO> read(InputStream inputStream) {
        String path = getFullPath();
        LOG.info("Reading data from " + path);

        if (!Files.exists(Paths.get(path))) {
            LOG.error("File doesnt exits!");
            return new ArrayList<>();
        }

        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains(";"))
                    continue;
                String[] values = line.split(DELIMETER, -1); // -1 means reads empty data ("")
                records.add(Arrays.asList(values));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        List<MeasurementDTO> measurements = new ArrayList<>();

        for (List<String> record : records) {
            int counter = 0;

            LOG.info("READING: " + Arrays.toString(record.toArray()));

            MeasurementDTO measurementDTO = new MeasurementDTO();
            measurementDTO.setDate(LocalDate.parse(record.get(counter++), getDateFormatter()));
            measurementDTO.setTime(LocalTime.parse(record.get(counter++), getTimeFormatter()));
            measurementDTO.setWeightKg(parseFloat(record.get(counter++)));
            measurementDTO.setBmi(parseFloat(record.get(counter++)));
            measurementDTO.setBodyFatProcentage(parseFloat(record.get(counter++)));
            measurementDTO.setWaterProcentage(parseFloat(record.get(counter++)));
            measurementDTO.setMuscleMassProcentage(parseFloat(record.get(counter++)));
            measurementDTO.setBonesKg(parseFloat(record.get(counter++)));
            measurementDTO.setComment(record.get(counter++));
            measurementDTO.setMedication(record.get(counter++));

            measurements.add(measurementDTO);
        }

        return measurements;
    }

    public void write(List<MeasurementDTO> measurements) throws IOException {
        List<String[]> dataLines = new ArrayList<>();
        for (MeasurementDTO measurement : measurements) {
            String[] data = {
                    getDateFormatter().format(measurement.getDate()),
                    getTimeFormatter().format(measurement.getTime()),
                    String.valueOf(measurement.getWeightKg()),
                    String.valueOf(measurement.getBmi()),
                    String.valueOf(measurement.getBodyFatProcentage()),
                    String.valueOf(measurement.getWaterProcentage()),
                    String.valueOf(measurement.getMuscleMassProcentage()),
                    String.valueOf(measurement.getBonesKg()),
                    measurement.getComment(),
                    measurement.getMedication()
            };
            dataLines.add(data);
        }

        for (String[] line : dataLines) {
            LOG.info("WRITING: " + Arrays.toString(line));
        }

        File csvOutputFile = new File(getFullPath());
        try (PrintWriter pw = new PrintWriter(csvOutputFile)) {
            dataLines.stream()
                    .map(this::convertToCSV)
                    .forEach(pw::println);
        }
    }

    private String convertToCSV(String[] data) {
        return Stream.of(data)
                .map(this::escapeSpecialCharacters)
                .collect(Collectors.joining(DELIMETER));
    }

    private String escapeSpecialCharacters(String data) {
        if (data == null) {
            throw new IllegalArgumentException("Input data cannot be null");
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private float parseFloat(String value) {
        return Float.parseFloat(value.replace(",", "."));
    }

    public String getFullPath() {
        String path = isTest ? "src/test/resources" : "src/main/resources";
        File file = new File(path + "/" + FILE_NAME);
        return file.getAbsolutePath();
        //return new File(FILE_NAME).getAbsolutePath();
    }

    public DateTimeFormatter getDateFormatter() {
        return new DateTimeFormatterBuilder()
                .appendValue(DAY_OF_MONTH, 2)
                .appendLiteral('/')
                .appendValue(MONTH_OF_YEAR, 2)
                .appendLiteral('/')
                .appendValue(YEAR, 4, 10,SignStyle.EXCEEDS_PAD)
                .toFormatter();
    }

    public DateTimeFormatter getTimeFormatter() {
        return new DateTimeFormatterBuilder()
                .appendValue(HOUR_OF_DAY, 2)
                .appendLiteral(':')
                .appendValue(MINUTE_OF_HOUR, 2)
                .toFormatter();
    }

}
