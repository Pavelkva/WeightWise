package cz.paful.weightwise.data.jpa;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserWeightRepository extends JpaRepository<UserWeight, Long> {
    UserWeight findUserWeightByUsername(String username);
    boolean existsByUsername(String username);
}
