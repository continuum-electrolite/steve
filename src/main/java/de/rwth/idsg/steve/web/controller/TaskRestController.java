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

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.ocpp.CommunicationTask;
import de.rwth.idsg.steve.ocpp.RequestResult;
import de.rwth.idsg.steve.ocpp.task.GetCompositeScheduleTask;
import de.rwth.idsg.steve.ocpp.task.GetConfigurationTask;
import de.rwth.idsg.steve.repository.TaskStore;
import de.rwth.idsg.steve.repository.dto.TaskOverview;
import lombok.extern.slf4j.Slf4j;
import ocpp.cp._2015._10.GetCompositeScheduleResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 29.12.2014
 */
@Slf4j
@RestController
@RequestMapping(value = "/manager/operations/rest/tasks")
public class TaskRestController {

    @Autowired private TaskStore taskStore;

    // -------------------------------------------------------------------------
    // Paths
    // -------------------------------------------------------------------------

    private static final String TASK_ID_PATH = "/{taskId}";
    private static final String TASK_DETAILS_PATH = TASK_ID_PATH + "/details/{chargeBoxId}/";

    // -------------------------------------------------------------------------
    // HTTP methods
    // -------------------------------------------------------------------------

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskOverview>> getOverview() {
        return ResponseEntity.ok(taskStore.getOverview());
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<TaskOverview>> clearFinished() {
        taskStore.clearFinished();
        return getOverview();
    }

    @GetMapping(value = TASK_ID_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<TaskOverview> getTaskDetails(@PathVariable("taskId") Integer taskId) {
        CommunicationTask r = taskStore.get(taskId);
        log.info("Is task found for taskId: {}? {}", taskId, Objects.nonNull(r));
        return ResponseEntity.ok(TaskOverview.builder()
                .taskId(taskId)
                .origin(r.getOrigin())
                .start(r.getStartTimestamp())
                .end(r.getEndTimestamp())
                .responseCount(r.getResponseCount().get())
                .requestCount(r.getResultMap().size())
                .build());
    }

    @GetMapping(value = TASK_DETAILS_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getDetailsForChargeBox(@PathVariable("taskId") Integer taskId,
                                         @PathVariable("chargeBoxId") String chargeBoxId) {
        CommunicationTask r = taskStore.get(taskId);
        log.info("Is task found for taskId: {}, chargeBoxId: {}? {}", taskId, chargeBoxId, Objects.nonNull(r));
        if (r instanceof GetCompositeScheduleTask) {
            return processForGetCompositeScheduleTask((GetCompositeScheduleTask) r, chargeBoxId);
        } else if (r instanceof GetConfigurationTask) {
            return processForGetConfigurationTask((GetConfigurationTask) r, chargeBoxId);
        } else {
            throw new SteveException("Task not found");
        }
    }

    private GetCompositeScheduleResponse processForGetCompositeScheduleTask(GetCompositeScheduleTask k, String chargeBoxId) {
        RequestResult result = extractResult(k, chargeBoxId);
        return  result.getDetails();
    }

    private String processForGetConfigurationTask(GetConfigurationTask k, String chargeBoxId) {
        RequestResult result = extractResult(k, chargeBoxId);
        return result.getDetails();
    }

    private static RequestResult extractResult(CommunicationTask<?, ?> task, String chargeBoxId) {
        RequestResult result = task.getResultMap().get(chargeBoxId);
        if (result == null) {
            throw new SteveException("Result not found");
        }
        return result;
    }
}