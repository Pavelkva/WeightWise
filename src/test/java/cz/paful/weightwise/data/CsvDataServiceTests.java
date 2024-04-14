package cz.paful.weightwise.data;

import cz.paful.weightwise.data.dto.MeasurementDTO;
import cz.paful.weightwise.service.CsvDataService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.Assert;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@SpringBootTest
public class CsvDataServiceTests {

    @Test
    void readWriteDataTest() throws IOException {
        writeData();
        Assert.notEmpty(readData(), "NO DATA TO READ!");
    }

    @Test
    void readAppDataTest() throws FileNotFoundException {
        CsvDataService csvDataService = new CsvDataService(true);
        try (PrintWriter out = new PrintWriter(csvDataService.getFullPath())) {
            out.println(getDataString());
        }
        List<MeasurementDTO> result = readData();
        Assert.isTrue(result.size() == 6, "Data size not match.");
    }

    private float getRandom(float min, float max) {
        return (float) (min + Math.random() * (max - min));
    }

    private List<MeasurementDTO> readData() throws FileNotFoundException {
        CsvDataService csvDataService = new CsvDataService(true);
        return csvDataService.read();
    }

    private void writeData() throws IOException {
        List<MeasurementDTO> measurementsToWrite = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MeasurementDTO measurementDTO = new MeasurementDTO();
            measurementDTO.setDate(LocalDate.now());
            measurementDTO.setTime(LocalTime.now());
            measurementDTO.setWeightKg(getRandom(30,150));
            measurementDTO.setBmi(getRandom(10,40));
            measurementDTO.setBodyFatProcentage(getRandom(1,90));
            measurementDTO.setWaterProcentage(getRandom(30,80));
            measurementDTO.setMuscleMassProcentage(getRandom(10,90));
            measurementDTO.setBonesKg(getRandom(2,5));
            measurementDTO.setComment("");
            measurementDTO.setMedication("");

            measurementsToWrite.add(measurementDTO);
        }

        CsvDataService csvDataService = new CsvDataService(true);
        csvDataService.write(measurementsToWrite);
    }

    private String getDataString() {
        return "Podrobnosti o uživateli\n" +
                "Pohlaví muž\n" +
                "Jméno Pavel\n" +
                "Příjmení Kvarda\n" +
                "Datum narození 09/12/1994\n" +
                "Výška 173 cm\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "Hmotnost\n" +
                "Datum, Čas, kg, BMI, Tělesný tuk, Voda, Svaly, Kosti, Komentář, Medikace\n" +
                "14/03/2024;14:30;75,6;25,2;15,9;61,8;42,6;3,3;;\n" +
                "15/03/2024;05:15;75,6;25,2;15,4;62,4;42,9;3,3;;\n" +
                "16/03/2024;07:40;74,4;24,8;14,2;63,4;43,6;3,3;;\n" +
                "17/03/2024;07:49;74,2;24,7;15,0;62,3;43,1;3,3;;\n" +
                "18/03/2024;07:24;75,4;25,1;13,4;65,1;44,1;3,4;;\n" +
                "19/03/2024;08:06;75,0;25,0;15,0;62,8;43,1;3,3;;"  ;
    }


}
