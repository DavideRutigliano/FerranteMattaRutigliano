package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.constant.Message;
import com.github.ferrantemattarutigliano.software.server.model.entity.*;
import com.github.ferrantemattarutigliano.software.server.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;

@Service
public class IndividualDataService {

    @Autowired
    private IndividualRepository individualRepository;
    @Autowired
    private HealthDataRepository healthDataRepository;
    @Autowired
    private PositionRepository positionRepository;
    @Autowired
    private GroupRequestRepository groupRequestRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public String insertData(Collection<HealthData> healthData) {

        for (HealthData data : healthData) {
            addCurrentDateTime(data);
        }

        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user == null || !individualRepository.existsByUser(user)) {
            return Message.BAD_REQUEST.toString();
        }

        Individual individual = individualRepository.findByUser(user);

        for (HealthData data : healthData) {
            data.setIndividual(individual);
            healthDataRepository.save(data);
            simpMessagingTemplate
                    .convertAndSend("/healthdata/" + individual.getUser().getUsername(),
                            "Name: " + data.getName() + ", value: " + data.getValue());
        }

        return Message.INSERT_DATA_SUCCESS.toString();
    }

    public void insertPosition(Position position) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (user == null || !individualRepository.existsByUser(user)) {
            return;
        }

        Individual individual = individualRepository.findByUser(user);

        position.setIndividual(individual);

        addCurrentDateTime(position);

        positionRepository.save(position);
    }

    @Scheduled(cron = "0 0 8 * * *") //scheduled at 8 o' clock every day
    public void updateGroupRequestTopics() {

        Collection<GroupRequest> groupRequests = groupRequestRepository.findSubscriptionRequest();

        for (GroupRequest groupRequest : groupRequests) {

            Collection<Individual> receivers = new LinkedHashSet<>();
            Collection<HealthData> healthData = new LinkedHashSet<>();

            String criteria = groupRequest.getCriteria();

            Specification<Individual> specification = IndividualSpecification.findByCriteriaSpecification(criteria.split(";"));

            if (specification != null)
                Optional.ofNullable(individualRepository.findAll(specification)).ifPresent(receivers::addAll);

            for (Individual i : receivers) {
                healthData.addAll(healthDataRepository.findByIndividual(individualRepository.findBySsn(i.getSsn())));
            }

            for (HealthData data : healthData) {
                simpMessagingTemplate
                        .convertAndSend("/healthdata/groupreq-" + groupRequest.getId(),
                                "Name: " + data.getName() + ", value: " + data.getValue());
            }
        }
    }

    private void addCurrentDateTime(HealthData healthData) {
        java.util.Date date = new java.util.Date();
        healthData.setDate(new Date(date.getTime()));
        healthData.setTime(new Time(date.getTime()));
    }

    private void addCurrentDateTime(Position position) {
        java.util.Date date = new java.util.Date();
        position.setDate(new Date(date.getTime()));
        position.setTime(new Time(date.getTime()));
    }


}
