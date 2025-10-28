package com.prestek.davivienda.repository;

import com.prestek.davivienda.model.Application;
import com.prestek.davivienda.model.Application.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    // Por nombre de método (coincide con los campos del entity)
    List<Application> findByUserId(String userId);
    //List<Application> findByCreditOfferId(Long creditOfferId);
    List<Application> findByStatus(Application.ApplicationStatus status);

    // JPQL explícito usando los campos escalares
    @Query("SELECT a FROM Application a WHERE a.userId = :userId AND a.status = :status")
    List<Application> findByUserIdAndStatus(@Param("userId") String userId,
                                            @Param("status") ApplicationStatus status);

//    @Query("SELECT a FROM Application a WHERE a.creditOfferId = :creditOfferId AND a.status = :status")
//    List<Application> findByCreditOfferIdAndStatus(@Param("creditOfferId") Long creditOfferId,
//                                                   @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.userId = :userId")
    Long countByUserId(@Param("userId") String userId);
}