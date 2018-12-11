package com.github.ferrantemattarutigliano.software.server.service;

import com.github.ferrantemattarutigliano.software.server.message.Message;
import com.github.ferrantemattarutigliano.software.server.model.dto.HealthDataDTO;
import com.github.ferrantemattarutigliano.software.server.model.entity.GroupRequest;
import com.github.ferrantemattarutigliano.software.server.model.entity.HealthData;
import com.github.ferrantemattarutigliano.software.server.model.entity.Individual;
import com.github.ferrantemattarutigliano.software.server.model.entity.IndividualRequest;
import com.github.ferrantemattarutigliano.software.server.repository.GroupRequestRepository;
import com.github.ferrantemattarutigliano.software.server.repository.HealthDataRepository;
import com.github.ferrantemattarutigliano.software.server.repository.IndividualRepository;
import com.github.ferrantemattarutigliano.software.server.repository.IndividualRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class RequestService {
    @Autowired
    private IndividualRequestRepository individualRequestRepository;
    @Autowired
    private GroupRequestRepository groupRequestRepository;
    @Autowired
    private IndividualRepository individualRepository;
    @Autowired
    private ThirdPartyRepository thirdPartysRepository;
    @Autowired
    private HealthDataRepository healthDataRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    public String individualRequest(IndividualRequest individualRequest){ //TODO Add subscription topic

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        String ssn = individualRequest.getSsn();

        if (user == null || !thirdPartysRepository.existsByUser(user)) {
            return Message.BAD_REQUEST.toString();
        }

        ThirdParty sender = thirdPartysRepository.findByUser(user);

        if (!individualRepository.existsBySsn(ssn)) {
            return Message.REQUEST_INVALID_SSN.toString();
        }

        individualRequest.setThirdParty(sender);
        individualRequestRepository.save(individualRequest);

        Individual receiver = individualRepository.findBySsn(ssn);
        String username = receiver.getUser().getUsername();

        simpMessagingTemplate.setUserDestinationPrefix(username);
        simpMessagingTemplate
                .convertAndSendToUser(username,
                        "/request/",
                        "request from: " +
                                individualRequest.getThirdParty().getOrganizationName() +
                                ", sent: " +
                                individualRequest.getTimestamp());

        return Message.REQUEST_SUCCESS.toString();
    }

    public String groupRequest(GroupRequest groupRequest){ //TODO Add subscription topic

        String criteria = groupRequest.getCriteria();

        return Message.REQUEST_SUCCESS.toString();
    }

    public String insertData(Set<HealthData> healthdata) {
        for (HealthData data : healthdata) {
            healthDataRepository.save(data);
        }
        return "Success";
    }

    public Set<HealthData> showIndividualData(IndividualRequest individualRequest) {
        return individualRepository.findBySsn(individualRequest.getSsn()).getHealthData();
    }

    public Collection<IndividualRequest> showSentIndividualRequest(Long id) {

        return individualRequestRepository.findByThirdParty(id);
    }

    public Collection<GroupRequest> showSentGroupRequest(Long id) {

        return groupRequestRepository.findByThirdParty(id);
    }

    public Collection<IndividualRequest> showIncomingRequest(String ssn) {

        return individualRequestRepository.findBySsn(ssn);
    }

    public String handleRequest(Long id, boolean accepted) {
        if (individualRequestRepository.handleRequest(id, accepted)) {
            return Message.REQUEST_ACCEPTED.toString();
        }
        return Message.REQUEST_REJECTED.toString();
    }
}
