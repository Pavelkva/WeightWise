package cz.paful.weightwise.data.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MeasurementRepository extends JpaRepository<Measurement, Long> {
    void deleteAllByUserWeight(UserWeight userWeight);
    @Query("SELECT m FROM Measurement m JOIN m.userWeight u WHERE u.username = :username")
    List<Measurement> findMeasurementsForUsername(@Param("username") String username);
}
