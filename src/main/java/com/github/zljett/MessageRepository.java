package com.github.zljett;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Class for allowing the use of the Spring Data JPA repository interface.
 */
@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {
}