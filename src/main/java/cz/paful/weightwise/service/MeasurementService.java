package cz.paful.weightwise.service;

import cz.paful.weightwise.WeightWiseConfig;
import cz.paful.weightwise.controller.dto.CommentResponseDTO;
import cz.paful.weightwise.data.dto.MeasurementDTO;
import cz.paful.weightwise.data.jpa.Measurement;
import cz.paful.weightwise.data.jpa.MeasurementRepository;
import cz.paful.weightwise.data.jpa.UserWeight;
import cz.paful.weightwise.data.jpa.UserWeightRepository;
import cz.paful.weightwise.util.JwtTokenUtil;
import jakarta.transaction.Transactional;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
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

    @Transactional
    @CacheEvict(value = WeightWiseConfig.USER_WEIGHT_CACHE_KEY, key = "#username")
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
        userWeight.setLastImport(new Date().toInstant());
    }

    public byte[] getMuscleKgChart(String username, int averagePerDays) throws IOException {
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

        return getChart(xData, yData, "Muscle", "Date", "Muscle", "y(x)");
    }

    public byte[] getWeightKgChart(String username, int averagePerDays) throws IOException {
        List<MeasurementDTO> measurementsDTO = getMeasurementsFirstForEachDay(username, averagePerDays);

        List<LocalDate> xData = new ArrayList<>();
        List<Float> yData = new ArrayList<>();

        // Fill x and y data for chart
        for (MeasurementDTO measurement : measurementsDTO) {
            xData.add(measurement.getDate());
            yData.add(measurement.getWeightKg());
        }

        return getChart(xData, yData, "Weight", "Date", "Weight", "y(x)");
    }

    public byte[] getWaterKgChart(String username, int averagePerDays) throws IOException {
        List<MeasurementDTO> measurementsDTO = getMeasurementsFirstForEachDay(username, averagePerDays);

        List<LocalDate> xData = new ArrayList<>();
        List<Float> yData = new ArrayList<>();

        // Fill x and y data for chart
        for (MeasurementDTO measurement : measurementsDTO) {
            xData.add(measurement.getDate());
            yData.add(measurement.getWaterKG());
        }

        return getChart(xData, yData, "Water", "Date", "Water", "y(x)");
    }

    public byte[] getFatKgChart(String username, int averagePerDays) throws IOException {
        List<MeasurementDTO> measurementsDTO = getMeasurementsFirstForEachDay(username, averagePerDays);

        List<LocalDate> xData = new ArrayList<>();
        List<Float> yData = new ArrayList<>();

        // Fill x and y data for chart
        for (MeasurementDTO measurement : measurementsDTO) {
            xData.add(measurement.getDate());
            yData.add(measurement.getBodyFatKG());
        }

        return getChart(xData, yData, "Fat", "Date", "Fat", "y(x)");
    }

    public List<CommentResponseDTO> getComments(String username) {
        List<Measurement> measurements = measurementRepository.findMeasurementsForUsername(username);

        List<CommentResponseDTO> comments = new ArrayList<>();
        for (Measurement measurement : measurements) {
            if (measurement.getComment() == null || measurement.getComment().isEmpty()) {
                continue;
            }

            CommentResponseDTO commentResponseDTO = new CommentResponseDTO();
            commentResponseDTO.setTime(measurement.getTime());
            commentResponseDTO.setDate(measurement.getDate());
            commentResponseDTO.setComment(measurement.getComment());
            comments.add(commentResponseDTO);
        }

        return comments;
    }

    private List<MeasurementDTO> getMeasurementsFirstForEachDay(String username, int averagePerDays) {
        List<Measurement> measurements = measurementRepository.findMeasurementsForUsername(username);
        List<MeasurementDTO> measurementsDTO = new ArrayList<>();
        for (Measurement measurement : measurements) {
            measurementsDTO.add(new MeasurementDTO(measurement));
        }

        measurementsDTO = removeOldestMeasurementsFromSameDay(measurementsDTO);
        measurementsDTO = getAverageMeasurementPerDays(measurementsDTO, averagePerDays);

        return measurementsDTO;
    }

    private byte[] getChart(List<LocalDate> xData, List<Float> yData, String Title, String xTitle, String yTitle, String seriesName) throws IOException {
        // x data needs to be date type
        List<Date> xDataDate = new ArrayList<>();
        for (LocalDate localDate : xData) {
            xDataDate.add(Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }

        // Create Chart
        XYChart chart = new XYChart(500, 400);
        chart.setTitle(Title);
        chart.setXAxisTitle(xTitle);
        chart.setYAxisTitle(yTitle);
        XYSeries series = chart.addSeries(seriesName, xDataDate, yData);
        series.setMarker(SeriesMarkers.CIRCLE);
        series.setFillColor(Color.RED);

        return BitmapEncoder.getBitmapBytes(chart, BitmapEncoder.BitmapFormat.PNG);
    }

    public List<MeasurementDTO> getAverageMeasurementPerDays(List<MeasurementDTO> measurements, int days) {
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
