package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.constant.Message;
import com.github.ferrantemattarutigliano.software.server.constant.Role;
import com.github.ferrantemattarutigliano.software.server.model.entity.Individual;
import com.github.ferrantemattarutigliano.software.server.model.entity.Run;
import com.github.ferrantemattarutigliano.software.server.model.entity.ThirdParty;
import com.github.ferrantemattarutigliano.software.server.model.entity.User;
import com.github.ferrantemattarutigliano.software.server.repository.IndividualRepository;
import com.github.ferrantemattarutigliano.software.server.repository.RunRepository;
import com.github.ferrantemattarutigliano.software.server.repository.ThirdPartyRepository;
import com.github.ferrantemattarutigliano.software.server.repository.UserRepository;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.print.attribute.standard.RequestingUserName;
import java.security.Principal;
import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;

public class RunServiceTest {
    @InjectMocks
    private RunService runService;
    @Mock
    private SecurityContext mockSecurityContext;
    @Mock
    private Authentication mockAuthentication;
    @Mock
    private Principal mockPrincipal;
    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private IndividualRepository mockIndividualRepository;
    @Mock
    private RunRepository mockRunRepository;

    @Before
    public void initTest() {
        MockitoAnnotations.initMocks(this);
    }

    private void mockIndividualAuthorized(User expectedUser, Individual expectedIndividual) {
        SecurityContextHolder.setContext(mockSecurityContext);

        Mockito.when(mockSecurityContext.getAuthentication())
                .thenReturn(mockAuthentication);
        Mockito.when(mockAuthentication.getPrincipal())
                .thenReturn(mockPrincipal);
        Mockito.when(mockSecurityContext.getAuthentication().getPrincipal())
                .thenReturn(expectedUser);
        Mockito.when(mockUserRepository.existsByUsername(expectedUser.getUsername()))
                .thenReturn(true);
        //mock the existing individual associated with the user
        Mockito.when(mockIndividualRepository.existsByUser(expectedUser))
                .thenReturn(true);
        Mockito.when(mockIndividualRepository.findByUser(expectedUser))
                .thenReturn(expectedIndividual);
    }

/* //TODO if you need to mock third party. This isn't needed in this class.
    private void mockThirdPartyAuthorized(User expectedUser, ThirdParty expectedThirdParty) {
        SecurityContextHolder.setContext(mockSecurityContext);

        Mockito.when(mockSecurityContext.getAuthentication())
                .thenReturn(mockAuthentication);
        Mockito.when(mockAuthentication.getPrincipal())
                .thenReturn(mockPrincipal);
        Mockito.when(mockSecurityContext.getAuthentication().getPrincipal())
                .thenReturn(expectedUser);
        Mockito.when(mockUserRepository.existsByUsername(expectedUser.getUsername()))
                .thenReturn(true);
        //mock the existing third party associated with the user
        Mockito.when(mockThirdPartyRepository.existsByUser(expectedUser))
                .thenReturn(true);
        Mockito.when(mockThirdPartyRepository.findByUser(expectedUser))
                .thenReturn(expectedThirdParty);
    }
*/

    private Run createMockRun(Individual organizer) {
        Run run = new Run();
        Date date = new Date(1);
        Time time = new Time(1);
        run.setState("created");
        run.setTitle("marathon");
        run.setDate(date);
        run.setTime(time);
        run.setOrganizer(organizer);
        return run;
        
    }


    @Test
    public void showCreatedRunsTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        Run secondRun = createMockRun(mockedIndividual);
        Run thirdRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        createdRuns.add(secondRun);
        createdRuns.add(thirdRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);

        Collection<Run> result = runService.showCreatedRuns();

        Assert.assertEquals(createdRuns, result);

    }


    @Test
    public void showRunsTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create a mock user
        User mockedUser2 = new User("username2", "password2", "aa2@aa.com", role);
        Individual mockedIndividual2 = new Individual();
        mockedIndividual.setUser(mockedUser2);
        mockedIndividual.setFirstname("pippo2");
        mockedIndividual.setLastname("pippetti2");
        //create runs associated with the  user
        Run firstRun = createMockRun(mockedIndividual);
        Run secondRun = createMockRun(mockedIndividual);
        Run thirdRun = createMockRun(mockedIndividual2);
        //create collections of runs
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        createdRuns.add(secondRun);
        Collection<Run> createdRuns2 = new ArrayList<>();
        createdRuns2.add(thirdRun);
        Collection<Run> enrolledRuns = new ArrayList<>();
        enrolledRuns.add(firstRun);
        Collection<Run> watchedRuns = new ArrayList<>();
        watchedRuns.add(secondRun);
        //mock created, enrolled and watched runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(enrolledRuns);
        mockedIndividual.setWatchedRuns(watchedRuns);
        mockedIndividual2.setCreatedRuns(createdRuns2);
        //create combined collection to return the respective Mokito command
        Iterable<Run> combinedIterables = Iterables.unmodifiableIterable(
                Iterables.concat(createdRuns, createdRuns2));
        List<Run> collectionCombined = Lists.newArrayList(combinedIterables);

        /* TEST STARTS HERE */

        Mockito.when(mockRunRepository.findAll()).thenReturn(collectionCombined);

        Collection<Run> result = runService.showRuns();

        Assert.assertEquals(collectionCombined, result);

    }

    @Test
    public void showEnrolledRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create runs associated with the  user
        Run firstRun = createMockRun(mockedIndividual);
        Run secondRun = createMockRun(mockedIndividual);
        Run thirdRun = createMockRun(mockedIndividual);
        //create collections of runs
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        createdRuns.add(secondRun);
        createdRuns.add(thirdRun);
        Collection<Run> enrolledRuns = new ArrayList<>();
        enrolledRuns.add(firstRun);
        //mock created, enrolled and watched runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(enrolledRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);

        Collection<Run> result = runService.showEnrolledRuns();
        //create collection with the expected result
        Collection<Run> expectedResult = new ArrayList<>();
        expectedResult.add(firstRun);
        Assert.assertEquals(expectedResult, result);

    }


    @Test
    public void showNewRunsTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create a mock user
        User mockedUser2 = new User("username2", "password2", "aa2@aa.com", role);
        Individual mockedIndividual2 = new Individual();
        mockedIndividual.setUser(mockedUser2);
        mockedIndividual.setFirstname("pippo2");
        mockedIndividual.setLastname("pippetti2");
        //create runs associated with the  user
        Run firstRun = createMockRun(mockedIndividual);
        Run secondRun = createMockRun(mockedIndividual);
        Run thirdRun = createMockRun(mockedIndividual2);
        //create collections of runs
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        createdRuns.add(secondRun);
        Collection<Run> createdRuns2 = new ArrayList<>();
        createdRuns2.add(thirdRun);
        Collection<Run> enrolledRuns = new ArrayList<>();
        enrolledRuns.add(firstRun);
        Collection<Run> watchedRuns = new ArrayList<>();
        watchedRuns.add(secondRun);
        //mock created, enrolled and watched runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(enrolledRuns);
        mockedIndividual.setWatchedRuns(watchedRuns);
        mockedIndividual2.setCreatedRuns(createdRuns2);
        //create combined collection to return the respective Mokito command
        Iterable<Run> combinedIterables = Iterables.unmodifiableIterable(
                Iterables.concat(createdRuns, createdRuns2));
        List<Run> collectionCombined = Lists.newArrayList(combinedIterables);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.findAll()).thenReturn(collectionCombined);

        Collection<Run> result = runService.showNewRuns();
        //create collection with the expected result
        Collection<Run> expectedResult = new ArrayList<>();
        expectedResult.add(thirdRun);
        Assert.assertEquals(expectedResult, result);

    }

    @Test
    public void showEnrolledRunsTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create runs associated with the  user
        Run firstRun = createMockRun(mockedIndividual);
        Run secondRun = createMockRun(mockedIndividual);
        Run thirdRun = createMockRun(mockedIndividual);
        //create collections of runs
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        createdRuns.add(secondRun);
        createdRuns.add(thirdRun);
        Collection<Run> enrolledRuns = new ArrayList<>();
        enrolledRuns.add(firstRun);
        //mock created, enrolled and watched runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(enrolledRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);

        Collection<Run> result = runService.showEnrolledRuns();
        //create collection with the expected result
        Collection<Run> expectedResult = new ArrayList<>();
        expectedResult.add(firstRun);
        Assert.assertEquals(expectedResult, result);

    }

    @Test
    public void showWatchedRunsTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create runs associated with the  user
        Run firstRun = createMockRun(mockedIndividual);
        Run secondRun = createMockRun(mockedIndividual);
        Run thirdRun = createMockRun(mockedIndividual);
        //create collections of runs
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        createdRuns.add(secondRun);
        createdRuns.add(thirdRun);
        Collection<Run> watchedRuns = new ArrayList<>();
        watchedRuns.add(firstRun);
        //mock created, enrolled and watched runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setWatchedRuns(watchedRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);

        Collection<Run> result = runService.showWatchedRuns();
        //create collection with the expected result
        Collection<Run> expectedResult = new ArrayList<>();
        expectedResult.add(firstRun);
        Assert.assertEquals(expectedResult, result);

    }

    @Test
    public void createRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);
        String result = runService.createRun(firstRun);
        Assert.assertEquals(Message.RUN_CREATED.toString(), result);

    }


    @Test
    public void startRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);

        String result = runService.startRun(firstRun.getId());
        System.out.println(result);
        Assert.assertEquals(Message.RUN_STARTED.toString(), result);

    }

    @Test
    public void startRunTest_RunNotExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);


        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);

        String result = runService.startRun(firstRun.getId());
        System.out.println(result);
        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }

    @Test
    public void startRunTest_RunNotOrganizer() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create a mock user2
        User mockedUser2 = new User("username2", "password2", "aa@2aa.com", role);
        Individual mockedIndividual2 = new Individual();
        mockedIndividual.setUser(mockedUser2);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999990");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        firstRun.setOrganizer(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual2.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);

        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual2);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);

        String result = runService.startRun(firstRun.getId());
        System.out.println(result);
        Assert.assertEquals(Message.RUN_NOT_ORGANIZER.toString() + "marathon", result);

    }

    @Test
    public void editRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);

        Run editedFirstRun = firstRun;
        editedFirstRun.setTitle("maranello");
        runService.createRun(firstRun);
        String result = runService.editRun(firstRun);

        Assert.assertEquals(Message.RUN_EDITED.toString(), result);

    }

    @Test
    public void editRunTest_notExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);

        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);

        Run editedFirstRun = firstRun;
        editedFirstRun.setTitle("maranello");
        runService.createRun(firstRun);
        String result = runService.editRun(firstRun);

        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }

    @Test
    public void deleteRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);



        String result = runService.deleteRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_DELETED.toString(), result);

    }

    @Test
    public void deleteRunTest_NotExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);


        String result = runService.deleteRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }


    @Test
    public void deleteRunTest_NotOrganizer() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create a mock user2
        User mockedUser2 = new User("username2", "password2", "aa@2aa.com", role);
        Individual mockedIndividual2 = new Individual();
        mockedIndividual.setUser(mockedUser2);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999990");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual2);


        String result = runService.deleteRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_NOT_ORGANIZER.toString() + "marathon", result);

    }



    @Test
    public void enrollRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.enrollRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_ENROLLED.toString() + firstRun.getTitle(), result);

    }


    @Test
    public void enrollRunTest_notExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.enrollRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }

    @Test
    public void enrollRunTest_altreadyAtlethe() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        firstRun.enrollAthlete(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.enrollRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_ALREADY_ATHLETE.toString(), result);

    }

    @Test
    public void unenrollRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        firstRun.setId(0L);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(createdRuns);
        //add of athlete to the run
        firstRun.enrollAthlete(mockedIndividual);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.unenrollRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_UNENROLLED.toString() + firstRun.getTitle(), result);

    }

    @Test
    public void unenrollRunTest_notExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(createdRuns);
        //add of athlete to the run
        firstRun.enrollAthlete(mockedIndividual);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.unenrollRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }

    @Test
    public void unenrollRunTest_notAtlethe() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(createdRuns);


        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.unenrollRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_NOT_ATHLETE.toString(), result);

    }



    @Test
    public void watchRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.watchRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_WATCHED.toString() + firstRun.getTitle(), result);

    }

    @Test
    public void watchRunTest_NotExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);

        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.watchRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }

    @Test
    public void watchRunTest_alreadySpectator() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        firstRun.addSpectator(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);


        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.watchRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_ALREADY_SPECTATOR.toString(), result);

    }

    @Test
    public void unwatchRunTest() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        firstRun.setId(0L);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setWatchedRuns(createdRuns);
        //add of athlete to the run
        firstRun.addSpectator(mockedIndividual);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.unwatchRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_UNWATCHED.toString() + firstRun.getTitle(), result);

    }

    @Test
    public void unwatchRunTest_notExists() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(createdRuns);
        //add of athlete to the run
        firstRun.addSpectator(mockedIndividual);

        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        ;
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.unwatchRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_DOES_NOT_EXISTS.toString(), result);

    }

    @Test
    public void unwatchRunTest_notSpectetor() {
        //create a mock user
        String role = Role.ROLE_INDIVIDUAL.toString();
        User mockedUser = new User("username", "password", "aa@aa.com", role);
        Individual mockedIndividual = new Individual();
        mockedIndividual.setUser(mockedUser);
        mockedIndividual.setFirstname("pippo");
        mockedIndividual.setLastname("pippetti");
        mockedIndividual.setSsn("999999999");
        //create runs with the associated user
        Run firstRun = createMockRun(mockedIndividual);
        //pack them in a collection
        Collection<Run> createdRuns = new ArrayList<>();
        createdRuns.add(firstRun);
        //mock created runs in database
        mockedIndividual.setCreatedRuns(createdRuns);
        mockedIndividual.setEnrolledRuns(createdRuns);


        /* TEST STARTS HERE */
        mockIndividualAuthorized(mockedUser, mockedIndividual);
        Mockito.when(mockRunRepository.findById(firstRun.getId()))
                .thenReturn(Optional.of(firstRun));
        Mockito.when(mockIndividualRepository.findByUser(mockedUser))
                .thenReturn(mockedIndividual);
        Mockito.when(mockRunRepository.save(firstRun))
                .thenReturn(firstRun);


        String result = runService.unwatchRun(firstRun.getId());

        Assert.assertEquals(Message.RUN_NOT_SPECTATOR.toString(), result);

    }


}



