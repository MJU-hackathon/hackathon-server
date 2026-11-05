package sopt.thingkerton.domain.profile.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.profile.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, String> {

    @EntityGraph(attributePaths = "interests")
    Optional<Profile> findWithInterestsByUserKey(String userKey);
}
