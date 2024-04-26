package cz.paful.weightwise.service;

import cz.paful.weightwise.data.dto.MeasurementDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.time.temporal.ChronoField.*;

@Service
public class CsvDataService {

    private static final Logger LOG = LoggerFactory.getLogger(CsvDataService.class);

    private static final String DELIMETER = ";";

    public List<MeasurementDTO> read(InputStream inputStream) {
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

    private float parseFloat(String value) {
        return Float.parseFloat(value.replace(",", "."));
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
