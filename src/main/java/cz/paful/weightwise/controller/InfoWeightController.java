package cz.paful.weightwise.controller;

import cz.paful.weightwise.service.MeasurementService;
import cz.paful.weightwise.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/info")
public class InfoWeightController {

    private final UserService userService;

    private final MeasurementService measurementService;

    @Autowired
    public InfoWeightController(UserService userService, MeasurementService measurementService) {
        this.userService = userService;
        this.measurementService = measurementService;
    }

    @GetMapping("/test")
    public ResponseEntity<String> register(HttpServletRequest request) {
        return ResponseEntity
                .ok()
                .body(userService.getUserNameByAuthorizationHeader(request));
    }

    @PostMapping(value ="/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file, HttpServletRequest request) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        measurementService.newData(file.getInputStream(), userService.getUserNameByAuthorizationHeader(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    @GetMapping("/muscle-kg-chart.png")
    public ResponseEntity<byte[]> getMuscleKgChart(HttpServletRequest request, @RequestParam(name = "averageDays") int averageDays) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(measurementService.getMuscleKgChart(userService.getUserNameByAuthorizationHeader(request),averageDays));
    }

    @GetMapping("/weight-chart.png")
    public ResponseEntity<byte[]> getWeightChart(HttpServletRequest request, @RequestParam(name = "averageDays") int averageDays) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(measurementService.getWeightKgChart(userService.getUserNameByAuthorizationHeader(request),averageDays));
    }

    @GetMapping("/water-kg-chart.png")
    public ResponseEntity<byte[]> getWaterKgValue(HttpServletRequest request, @RequestParam(name = "averageDays") int averageDays) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(measurementService.getWaterKgChart(userService.getUserNameByAuthorizationHeader(request),averageDays));
    }

    @GetMapping("/fat-kg-chart.png")
    public ResponseEntity<byte[]> getFatKgChart(HttpServletRequest request, @RequestParam(name = "averageDays") int averageDays) throws IOException {
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(MediaType.IMAGE_PNG_VALUE))
                .body(measurementService.getFatKgChart(userService.getUserNameByAuthorizationHeader(request),averageDays));
    }
}
