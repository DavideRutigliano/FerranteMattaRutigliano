package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.model.entity.Individual;
import com.github.ferrantemattarutigliano.software.server.model.entity.ThirdParty;
import com.github.ferrantemattarutigliano.software.server.model.entity.User;
import com.github.ferrantemattarutigliano.software.server.repository.IndividualRepository;
import com.github.ferrantemattarutigliano.software.server.repository.ThirdPartyRepository;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.Date;
import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class AuthenticatorServiceTest {
    @InjectMocks
    AuthenticatorService authenticator;

    @Mock
    IndividualRepository mockIndividualRepository;
    @Mock
    ThirdPartyRepository mockThirdPartyRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    private Individual createDummyIndivdual() {
        Individual individual;
        Date birthDate = new Date(Calendar.getInstance().getTimeInMillis());
        String ssn = "TESTER00D40V300A";
        individual = new Individual("Jhon", "Snow", ssn, "test@ho.com", "A", "B", birthDate);
        individual.getAuthorities();
        return individual;
    }

    private ThirdParty createDummyThirdParty() {
        ThirdParty thirdParty;
        String vat = "00000000000";
        thirdParty = new ThirdParty("Bilbo", "Baggins", vat, "test@ho.com", "A");
        thirdParty.getAuthorities();
        return thirdParty;
    }

    @Before
    public void initTest() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testIndividualRegistration() {
        Individual dummyIndividual = createDummyIndivdual();

        when(mockIndividualRepository.existsByUsername(dummyIndividual.getUsername())).thenReturn(false);
        when(mockIndividualRepository.existsByEmail(dummyIndividual.getEmail())).thenReturn(false);
        when(mockIndividualRepository.existsBySsn(dummyIndividual.getSsn())).thenReturn(false);

        boolean result = authenticator.individualRegistration(dummyIndividual);
        assertEquals(true, result);
    }

    @Test
    public void testThirdPartyRegistration() {
        ThirdParty dummyThirdParty = createDummyThirdParty();

        when(mockIndividualRepository.existsBySsn(dummyThirdParty.getUsername())).thenReturn(false);
        when(mockIndividualRepository.existsBySsn(dummyThirdParty.getEmail())).thenReturn(false);
        when(mockThirdPartyRepository.existsByVat(dummyThirdParty.getVat())).thenReturn(false);

        boolean result = authenticator.thirdPartyRegistration(dummyThirdParty);
        assertEquals(true, result);
    }

    @Test
    public void testLogin() {
        Individual dummyIndividual = createDummyIndivdual();
        ThirdParty dummyThirdParty = createDummyThirdParty();
        boolean result = true;

        when(mockIndividualRepository.existsByUsername(dummyIndividual.getUsername())).thenReturn(true);
        when(mockIndividualRepository.findByUsername(dummyIndividual.getUsername())).thenReturn(dummyIndividual);
        System.out.println(dummyIndividual.getSsn());
        result &= authenticator.login(dummyIndividual);

        when(mockIndividualRepository.existsByUsername(dummyIndividual.getUsername())).thenReturn(false);
        when(mockThirdPartyRepository.existsByUsername(dummyThirdParty.getUsername())).thenReturn(true);
        when(mockThirdPartyRepository.findByUsername(dummyThirdParty.getUsername())).thenReturn(dummyThirdParty);
        result &= authenticator.login(dummyThirdParty);


        assertEquals(true, result);
    }

    @Test
    public void testLoginFailure() {
        Individual dummyIndividual = createDummyIndivdual();

        when(mockIndividualRepository.existsBySsn(dummyIndividual.getSsn())).thenReturn(false);

        boolean result = authenticator.login(dummyIndividual);
        assertEquals(false, result);
    }

    @Test
    public void testChangeIndividualProfile() {
        Individual dummyIndividual = createDummyIndivdual();

        when(mockIndividualRepository.existsBySsn(dummyIndividual.getSsn())).thenReturn(true);
        when(mockIndividualRepository.existsByEmail(dummyIndividual.getEmail())).thenReturn(true);
        when(mockIndividualRepository.existsByUsername(dummyIndividual.getUsername())).thenReturn(true);
        dummyIndividual.setLastname("Targaryen");
        dummyIndividual.setEmail("dany@love.com");

        boolean result = authenticator.changeIndividualProfile(dummyIndividual);
        assertEquals(true, result);
    }

    @Test
    public void testChangeThirdPartyProfile() {
        ThirdParty dummyThirdParty = createDummyThirdParty();

        when(mockThirdPartyRepository.existsByVat(dummyThirdParty.getVat())).thenReturn(true);
        when(mockThirdPartyRepository.findByVat(dummyThirdParty.getVat())).thenReturn(dummyThirdParty);
        dummyThirdParty.setOrganizationName("Windown");
        dummyThirdParty.setEmail("bilbo@baggins.it");

        boolean result = authenticator.changeThirdPartyProfile(dummyThirdParty);
        assertEquals(true, result);
    }

}
