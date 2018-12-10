package com.github.ferrantemattarutigliano.software.client.task.individual;

import com.github.ferrantemattarutigliano.software.client.httprequest.AsyncResponse;
import com.github.ferrantemattarutigliano.software.client.httprequest.HttpParameterContainer;
import com.github.ferrantemattarutigliano.software.client.httprequest.HttpRequestType;
import com.github.ferrantemattarutigliano.software.client.httprequest.HttpTask;
import com.github.ferrantemattarutigliano.software.client.model.IndividualDTO;
import com.github.ferrantemattarutigliano.software.client.model.IndividualRegistrationDTO;
import com.github.ferrantemattarutigliano.software.client.model.UserDTO;

public class IndividualRegisterTask extends HttpTask<String> {

    public IndividualRegisterTask(IndividualRegistrationDTO individualRegistrationDTO, AsyncResponse<String> asyncResponse) {
        super(String.class, asyncResponse);
        String path = "/registration/individual";
        HttpRequestType type = HttpRequestType.POST;
        HttpParameterContainer container = new HttpParameterContainer(path, type, individualRegistrationDTO);
        setHttpParameterContainer(container);
    }
}