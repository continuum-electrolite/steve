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
package de.rwth.idsg.steve.repository.dto;

import de.rwth.idsg.steve.ocpp.OcppTransport;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 29.12.2014
 */
@Getter
@Setter
@NoArgsConstructor
public final class ChargePointSelect {
    private OcppTransport ocppTransport;
    private String chargeBoxId;
    private String endpointAddress;

    public ChargePointSelect(OcppTransport ocppTransport, String chargeBoxId) {
        // Provide a non-null value (or placeholder if you will) to frontend for JSON charge points.
        // This is clearly a hack. Not my proudest moment.
        this.ocppTransport = ocppTransport;
        this.chargeBoxId = chargeBoxId;
        this.endpointAddress = "-";
    }

    public ChargePointSelect(OcppTransport ocppTransport, String chargeBoxId, String endpointAddress) {
        // Provide a non-null value (or placeholder if you will) to frontend for JSON charge points.
        // This is clearly a hack. Not my proudest moment.
        this.ocppTransport = ocppTransport;
        this.chargeBoxId = chargeBoxId;
        this.endpointAddress = endpointAddress;
    }

    public boolean isEndpointAddressSet() {
        return !("-".equals(endpointAddress));
    }

    public boolean isSoap() {
        return OcppTransport.SOAP == ocppTransport;
    }

    @Override
    public String toString() {
        return "ChargePointSelect {" +
                "ocppTransport=" + ocppTransport +
                ", chargeBoxId='" + chargeBoxId + '\'' +
                ", endpointAddress='" + endpointAddress + '\'' +
                '}';
    }
}
