package org.roadmap.weather.repository;

import org.roadmap.weather.entity.User;

import java.util.Optional;

public interface AuthRepository extends BaseRepository<User> {
    Optional<User> findByLogin(String login);

    Optional<User> findById(Integer id);
}
