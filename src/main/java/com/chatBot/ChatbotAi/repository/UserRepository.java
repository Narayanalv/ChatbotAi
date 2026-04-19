package com.chatBot.ChatbotAi.repository;

import com.chatBot.ChatbotAi.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.otpId = :otpId WHERE u.id = :id")
    public void updateOtpId(@Param("id") Long id, @Param("otpId") Long otpId);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.token = :token WHERE u.id = :id")
    public void updateToken(@Param("id") Long id, @Param("token") String token);

//    @Modifying
//    @Query("Update User u SET u.name = :name, u.password = :password where u.email = :email")
//    int updateUser(@Param("name") String name, @Param("password") String password, @Param("email") String email);
}
