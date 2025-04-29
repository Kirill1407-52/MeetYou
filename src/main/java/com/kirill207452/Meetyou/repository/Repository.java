package com.kirill207452.Meetyou.repository;

import com.example.demo.model.Interest;
import com.example.demo.model.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface Repository extends JpaRepository<User, Long> {
    @Query(value = "select * from users where email = :email", nativeQuery = true)
    Optional<User> findByEmail(String email);

    // Поиск пользователей по типу интереса (с фильтрацией по вложенной сущности)
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i WHERE i.interestType = :interestType")
    List<User> findUsersByInterestType(@Param("interestType")
                                       Interest.InterestType interestType);


    
    // Поиск пользователей по нескольким интересам (AND логика)
    @Query("SELECT u FROM User u JOIN u.interests i WHERE i.interestType IN "
            + ":interestTypes GROUP BY u HAVING COUNT(DISTINCT i) = :interestCount")
    List<User> findUsersByAllInterestTypes(
            @Param("interestTypes") Set<Interest.InterestType> interestTypes,
            @Param("interestCount") long interestCount);

    // Поиск пользователей по любому из интересов (OR логика)
    @Query("SELECT DISTINCT u FROM User u JOIN u.interests i "
            + "WHERE i.interestType IN :interestTypes")
    List<User> findUsersByAnyInterestTypes(
            @Param("interestTypes") Set<Interest.InterestType> interestTypes);
}
