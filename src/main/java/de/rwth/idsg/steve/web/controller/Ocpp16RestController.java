/*
 * SteVe - SteckdosenVerwaltung - https://github.com/RWTH-i5-IDSG/steve
 * Copyright (C) 2013-2022 RWTH Aachen University - Information Systems - Intelligent Distributed Systems Group (IDSG).
 * All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package de.rwth.idsg.steve.web.controller;

import de.rwth.idsg.steve.ocpp.CommunicationTask;
import de.rwth.idsg.steve.repository.TaskStore;
import de.rwth.idsg.steve.repository.dto.TaskOverview;
import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.dto.ocpp.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 15.03.2018
 */

@Slf4j
@RestController
@RequestMapping(value = "/manager/operations/v1.6/rest")
public class Ocpp16RestController {
    @Autowired
    @Qualifier("ChargePointService16_Client")
    private ChargePointService16_Client client16;

    @Autowired
    private TaskStore taskStore;

    @Autowired
    protected ChargePointHelperService chargePointHelperService;

    private static final String GET_COMPOSITE_PATH = "/GetCompositeSchedule";
    private static final String CLEAR_CHARGING_PATH = "/ClearChargingProfile";
    private static final String SET_CHARGING_PATH = "/SetChargingProfile";
    private static final String TRIGGER_MESSAGE_PATH = "/TriggerMessage";
    private static final String CHANGE_AVAIL_PATH = "/ChangeAvailability";
    protected static final String CHANGE_CONF_PATH = "/ChangeConfiguration";
    private static final String REMOTE_START_TX_PATH = "/RemoteStartTransaction";
    private static final String REMOTE_STOP_TX_PATH = "/RemoteStopTransaction";

    protected ChargePointService16_Client getClient16() {
        return this.client16;
    }

    @PostMapping(
            value = {"/TriggerMessage"},
            consumes = {"application/json"}
    )
    public ResponseEntity<String> postTriggerMessage(@RequestBody @Valid TriggerMessageParams params, BindingResult result) {
        return result.hasErrors() ? new ResponseEntity(result, HttpStatus.BAD_REQUEST) : ResponseEntity.ok("TaskId: " + this.getClient16().triggerMessage(params));
    }

    @PostMapping(
            value = {"/ChangeAvailability"},
            consumes = {"application/json"}
    )
    public ResponseEntity<Object> postChangeAvail(@RequestBody @Valid ChangeAvailabilityParams params, BindingResult result) {
        log.info("Received Change Avail Request: {}", params.getConnectorId());
        if (result.hasErrors()) {
            log.error("Request is having errors.");
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        } else {
            int id = this.getClient16().changeAvailability(params);
            CommunicationTask r = taskStore.get(id);
            return ResponseEntity.ok(TaskOverview.builder()
                    .taskId(id)
                    .origin(r.getOrigin())
                    .start(r.getStartTimestamp())
                    .end(r.getEndTimestamp())
                    .responseCount(r.getResponseCount().get())
                    .requestCount(r.getResultMap().size())
                    .build());
        }
    }

    @PostMapping(value = {"/ChangeConfiguration"}, consumes = {"application/json"})
    public ResponseEntity<Object> postChangeConf(@RequestBody @Valid ChangeConfigurationParams params, BindingResult result) {
        log.info("Received Change Config Request: {}", params.getConfKey());
        if (result.hasErrors()) {
            log.error("Request is having errors.");
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        } else {
            int id = this.getClient16().changeConfiguration(params);
            CommunicationTask r = taskStore.get(id);
            return ResponseEntity.ok(TaskOverview.builder()
                    .taskId(id)
                    .origin(r.getOrigin())
                    .start(r.getStartTimestamp())
                    .end(r.getEndTimestamp())
                    .responseCount(r.getResponseCount().get())
                    .requestCount(r.getResultMap().size())
                    .build());
        }
    }

    @PostMapping(
            value = {"/RemoteStartTransaction"},
            consumes = {"application/json"}
    )
    public ResponseEntity<Object> postRemoteStartTx(@RequestBody @Valid RemoteStartTransactionParams params, BindingResult result) {
        log.info("Received Remote Start Txn Request: {}", params.getIdTag());
        if (result.hasErrors()) {
            log.error("Request is having errors. {}", result);
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        } else {
            int id = this.getClient16().remoteStartTransaction(params);
            CommunicationTask r = taskStore.get(id);
            return ResponseEntity.ok(TaskOverview.builder()
                    .taskId(id)
                    .origin(r.getOrigin())
                    .start(r.getStartTimestamp())
                    .end(r.getEndTimestamp())
                    .responseCount(r.getResponseCount().get())
                    .requestCount(r.getResultMap().size())
                    .build());
        }
    }

    @PostMapping(
            value = {"/RemoteStopTransaction"},
            consumes = {"application/json"}
    )
    public ResponseEntity<Object> postRemoteStopTx(@RequestBody @Valid RemoteStopTransactionParams params, BindingResult result) {
        log.info("Received Remote Stop Txn Request: {}", params.getTransactionId());
        if (result.hasErrors()) {
            log.error("Request is having errors.");
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        } else {
            int id = this.getClient16().remoteStopTransaction(params);
            CommunicationTask r = taskStore.get(id);
            return ResponseEntity.ok(TaskOverview.builder()
                    .taskId(id)
                    .origin(r.getOrigin())
                    .start(r.getStartTimestamp())
                    .end(r.getEndTimestamp())
                    .responseCount(r.getResponseCount().get())
                    .requestCount(r.getResultMap().size())
                    .build());
        }
    }
}
