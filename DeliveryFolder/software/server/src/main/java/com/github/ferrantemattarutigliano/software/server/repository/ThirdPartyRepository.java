package com.github.ferrantemattarutigliano.software.server.repository;

import com.github.ferrantemattarutigliano.software.server.model.entity.ThirdParty;
import com.github.ferrantemattarutigliano.software.server.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ThirdPartyRepository extends JpaRepository<ThirdParty, Long> {
    Boolean existsByUser(User user);

    ThirdParty findByUser(User user);
    ThirdParty findByVat(String vat);
    Boolean existsByVat(String vat);
}
