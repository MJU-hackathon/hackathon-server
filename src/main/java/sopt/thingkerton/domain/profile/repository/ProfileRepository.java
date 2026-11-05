package sopt.thingkerton.domain.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sopt.thingkerton.domain.profile.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, String> {
}
