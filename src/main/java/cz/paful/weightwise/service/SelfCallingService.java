package cz.paful.weightwise.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SelfCallingService {

    private static final Logger LOG = LoggerFactory.getLogger(SelfCallingService.class);

    private final RestTemplate restTemplate;

    @Autowired

    public SelfCallingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    // Běží každých 14 minut od 6:00 do 23:59
    @Scheduled(cron = "0 0/14 6-23 * * *")
    public void runFromMorningToNight() {
        selfCall();
    }

    // Běží každých 14 minut od 0:00 do 2:00
    @Scheduled(cron = "0 0/14 0-2 * * *")
    public void runEarlyMorning() {
        selfCall();
    }

    public void selfCall() {
        LOG.info("Calling the self-call...");

        String url = "https://weightwise.onrender.com/health-check"; // Změňte URL podle vaší potřeby

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                LOG.info("The self-call was successful with status code: {}", response.getStatusCode());
            } else {
                LOG.warn("The self-call returned non-success status code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            LOG.error("Error during the self-call: {}", e.getMessage(), e);
        }
    }
}
