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

import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.service.ChargePointService16_Client;
import de.rwth.idsg.steve.web.dto.ocpp.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@RestController
@RequestMapping(value = "/manager/operations/v1.6/rest")
public class Ocpp16RestController {

    @Autowired
    @Qualifier("ChargePointService16_Client")
    private ChargePointService16_Client client16;

    @Autowired
    protected ChargePointHelperService chargePointHelperService;

    // -------------------------------------------------------------------------
    // Paths
    // -------------------------------------------------------------------------
    private static final String TRIGGER_MESSAGE_PATH = "/TriggerMessage";
    private static final String CHANGE_AVAIL_PATH = "/ChangeAvailability";
    protected static final String CHANGE_CONF_PATH = "/ChangeConfiguration";
    private static final String REMOTE_START_TX_PATH = "/RemoteStartTransaction";
    private static final String REMOTE_STOP_TX_PATH = "/RemoteStopTransaction";

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    protected ChargePointService16_Client getClient16() {
        return client16;
    }

    // -------------------------------------------------------------------------
    // Http methods (POST)
    // -------------------------------------------------------------------------

    @PostMapping(value = TRIGGER_MESSAGE_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postTriggerMessage(@Valid @RequestBody TriggerMessageParams params,
                                                     BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("TaskId: " + getClient16().triggerMessage(params));
    }

    @PostMapping(value = CHANGE_AVAIL_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postChangeAvail(@Valid @RequestBody ChangeAvailabilityParams params,
                                                  BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("TaskId: " + getClient16().changeAvailability(params));
    }

    @PostMapping(value = CHANGE_CONF_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postChangeConf(@Valid @RequestBody ChangeConfigurationParams params,
                                                 BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("TaskId: " + getClient16().changeConfiguration(params));
    }

    @PostMapping(value = REMOTE_START_TX_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postRemoteStartTx(@Valid @RequestBody RemoteStartTransactionParams params,
                                                    BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("TaskId: " + getClient16().remoteStartTransaction(params));
    }

    @PostMapping(value = REMOTE_STOP_TX_PATH, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> postRemoteStopTx(@Valid @RequestBody RemoteStopTransactionParams params,
                                                   BindingResult result) {
        if (result.hasErrors()) {
            return new ResponseEntity<>(result.toString(), HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok("TaskId: " + getClient16().remoteStopTransaction(params));
    }
}
