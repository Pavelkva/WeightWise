package cz.paful.weightwise.service;

import cz.paful.weightwise.data.dto.MeasurementDTO;
import cz.paful.weightwise.data.jpa.Measurement;
import cz.paful.weightwise.data.jpa.MeasurementRepository;
import cz.paful.weightwise.data.jpa.UserWeight;
import cz.paful.weightwise.data.jpa.UserWeightRepository;
import cz.paful.weightwise.util.JwtTokenUtil;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class MeasurementService {

    private static final Logger LOG = LoggerFactory.getLogger(MeasurementService.class);

    private final CsvDataService csvDataService;
    private final UserWeightRepository userWeightRepository;
    private final MeasurementRepository measurementRepository;

    @Autowired
    public MeasurementService(CsvDataService csvDataService,
                              JwtTokenUtil jwtTokenUtil,
                              UserWeightRepository userWeightRepository,
                              MeasurementRepository measurementRepository) {

        this.csvDataService = csvDataService;
        this.userWeightRepository = userWeightRepository;
        this.measurementRepository = measurementRepository;
    }

    public void newData(InputStream inputStream, String username) {
        UserWeight userWeight = userWeightRepository.findUserWeightByUsername(username);
        if (userWeight == null) {
            throw new RuntimeException(String.format(
                    "No valid user found from token username: %s!", username));
        }

        List<Measurement> measurements = new ArrayList<>();
        measurementRepository.deleteAllByUserWeight(userWeight);

        List<MeasurementDTO> measurementDTOList = csvDataService.read(inputStream);
        for (MeasurementDTO measurementDTO : measurementDTOList) {
            Measurement measurement = measurementDTO.getMeasurement();
            measurement.setUserWeight(userWeight);
            measurements.add(measurement);
        }

        measurementRepository.saveAll(measurements);
    }

    public byte[] getMuscleMassChart(String username, int averagePerDays) throws IOException {
        List<Measurement> measurements = measurementRepository.findMeasurementsForUsername(username);
        List<MeasurementDTO> measurementsDTO = new ArrayList<>();
        for (Measurement measurement : measurements) {
            measurementsDTO.add(new MeasurementDTO(measurement));
        }

        measurementsDTO = removeOldestMeasurementsFromSameDay(measurementsDTO);
        measurementsDTO = getAverageMeasurementPerDays(measurementsDTO, averagePerDays);

        List<LocalDate> xData = new ArrayList<>();
        List<Float> yData = new ArrayList<>();

        // Fill x and y data for chart
        for (MeasurementDTO measurement : measurementsDTO) {
            xData.add(measurement.getDate());
            yData.add(measurement.getMuscleMassKG());
        }

        // x data needs to be date type
        List<Date> xDataDate = new ArrayList<>();
        for (LocalDate localDate : xData) {
            xDataDate.add(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        // Create Chart
        XYChart chart = new XYChart(500, 400);
        chart.setTitle("Sample Chart");
        chart.setXAxisTitle("Time");
        chart.setXAxisTitle("Muscle");
        XYSeries series = chart.addSeries("y(x)", xDataDate, yData);
        series.setMarker(SeriesMarkers.CIRCLE);
        series.setFillColor(Color.RED);

        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    private List<MeasurementDTO> getAverageMeasurementPerDays(List<MeasurementDTO> measurements, int days) {
        if (days == 1) {
            return measurements;
        }

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

    /**
     * Keeps only first measurement of day
     * @param measurements list of measurements
     */
    private List<MeasurementDTO> removeOldestMeasurementsFromSameDay(List<MeasurementDTO> measurements) {
        MeasurementDTO previousMeaserument = new MeasurementDTO();
        List<MeasurementDTO> measurementToRemove = new ArrayList<>();
        for (MeasurementDTO measurementDTO : measurements) {
            if (measurementDTO.getDate().equals(previousMeaserument.getDate())) {
                measurementToRemove.add(measurementDTO);
            }
            previousMeaserument = measurementDTO;
        }

        measurementToRemove.forEach(m -> LOG.info("removing measurement " + m.getDate() + " " + m.getTime() + " " + m.getWeightKg()));

        // Create new list and remove all candidates
        List<MeasurementDTO> result = new ArrayList<>(measurements);
        result.removeAll(measurementToRemove);

        return result;
    }
}