package cz.paful.weightwise;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication
public class WeightWiseApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeightWiseApplication.class, args);
    }

}
