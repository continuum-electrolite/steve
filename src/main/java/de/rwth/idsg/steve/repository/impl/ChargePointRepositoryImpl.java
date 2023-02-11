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
package de.rwth.idsg.steve.repository.impl;

import de.rwth.idsg.steve.SteveException;
import de.rwth.idsg.steve.ocpp.OcppProtocol;
import de.rwth.idsg.steve.repository.AddressRepository;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.repository.dto.ChargePoint;
import de.rwth.idsg.steve.repository.dto.ChargePointSelect;
import de.rwth.idsg.steve.repository.dto.ConnectorStatus;
import de.rwth.idsg.steve.utils.CustomDSL;
import de.rwth.idsg.steve.utils.DateTimeUtils;
import de.rwth.idsg.steve.web.dto.ChargePointDetailsDTO;
import de.rwth.idsg.steve.web.dto.ChargePointForm;
import de.rwth.idsg.steve.web.dto.ChargePointQueryForm;
import de.rwth.idsg.steve.web.dto.ConnectorStatusForm;
import jooq.steve.db.tables.ChargeBox;
import jooq.steve.db.tables.Connector;
import jooq.steve.db.tables.records.AddressRecord;
import jooq.steve.db.tables.records.ChargeBoxRecord;
import lombok.extern.slf4j.Slf4j;
import ocpp.cs._2015._10.RegistrationStatus;
import org.apache.cxf.common.util.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.joda.time.DateTime;
import org.jooq.*;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

import static de.rwth.idsg.steve.utils.CustomDSL.date;
import static de.rwth.idsg.steve.utils.CustomDSL.includes;
import static jooq.steve.db.tables.ChargeBox.CHARGE_BOX;
import static jooq.steve.db.tables.Connector.CONNECTOR;
import static jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS;

/**
 * @author Sevket Goekay <sevketgokay@gmail.com>
 * @since 14.08.2014
 */
@Slf4j
@Repository
public class ChargePointRepositoryImpl implements ChargePointRepository {
    private final DSLContext ctx;
    private final AddressRepository addressRepository;

    @Autowired
    public ChargePointRepositoryImpl(DSLContext ctx, AddressRepository addressRepository) {
        this.ctx = ctx;
        this.addressRepository = addressRepository;
    }

    public Optional<String> getRegistrationStatus(String chargeBoxId) {
        String status = (String)this.ctx.select(ChargeBox.CHARGE_BOX.REGISTRATION_STATUS).from(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID.eq(chargeBoxId)).fetchOne(ChargeBox.CHARGE_BOX.REGISTRATION_STATUS);
        return Optional.ofNullable(status);
    }

    public List<ChargePointSelect> getChargePointSelect(OcppProtocol protocol, List<String> inStatusFilter) {
        return this.ctx.select(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID, ChargeBox.CHARGE_BOX.ENDPOINT_ADDRESS).from(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.OCPP_PROTOCOL.equal(protocol.getCompositeValue())).and(ChargeBox.CHARGE_BOX.ENDPOINT_ADDRESS.isNotNull()).and(ChargeBox.CHARGE_BOX.REGISTRATION_STATUS.in(inStatusFilter)).fetch().map((r) -> {
            return new ChargePointSelect(protocol.getTransport(), (String)r.value1(), (String)r.value2());
        });
    }

    public List<String> getChargeBoxIds() {
        return this.ctx.select(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID).from(ChargeBox.CHARGE_BOX).fetch(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID);
    }

    public Map<String, Integer> getChargeBoxIdPkPair(List<String> chargeBoxIdList) {
        return this.ctx.select(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID, ChargeBox.CHARGE_BOX.CHARGE_BOX_PK).from(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID.in(chargeBoxIdList)).fetchMap(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID, ChargeBox.CHARGE_BOX.CHARGE_BOX_PK);
    }

    public List<ChargePoint.Overview> getOverview(ChargePointQueryForm form) {
        return this.getOverviewInternal(form).map((r) -> {
            return ChargePoint.Overview.builder().chargeBoxPk((Integer)r.value1()).chargeBoxId((String)r.value2()).description((String)r.value3()).ocppProtocol((String)r.value4()).lastHeartbeatTimestampDT((DateTime)r.value5()).lastHeartbeatTimestamp(DateTimeUtils.humanize((DateTime)r.value5())).build();
        });
    }

    private Result<Record5<Integer, String, String, String, DateTime>> getOverviewInternal(ChargePointQueryForm form) {
        SelectQuery selectQuery = this.ctx.selectQuery();
        selectQuery.addFrom(ChargeBox.CHARGE_BOX);
        selectQuery.addSelect(new SelectFieldOrAsterisk[]{ChargeBox.CHARGE_BOX.CHARGE_BOX_PK, ChargeBox.CHARGE_BOX.CHARGE_BOX_ID, ChargeBox.CHARGE_BOX.DESCRIPTION, ChargeBox.CHARGE_BOX.OCPP_PROTOCOL, ChargeBox.CHARGE_BOX.LAST_HEARTBEAT_TIMESTAMP});
        if (form.isSetOcppVersion()) {
            selectQuery.addConditions(ChargeBox.CHARGE_BOX.OCPP_PROTOCOL.like(form.getOcppVersion().getValue() + "_"));
        }

        if (form.isSetDescription()) {
            selectQuery.addConditions(CustomDSL.includes(ChargeBox.CHARGE_BOX.DESCRIPTION, form.getDescription()));
        }

        if (form.isSetChargeBoxId()) {
            selectQuery.addConditions(CustomDSL.includes(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID, form.getChargeBoxId()));
        }

        switch (form.getHeartbeatPeriod()) {
            case ALL:
                break;
            case TODAY:
                selectQuery.addConditions(CustomDSL.date(ChargeBox.CHARGE_BOX.LAST_HEARTBEAT_TIMESTAMP).eq(CustomDSL.date(DateTime.now())));
                break;
            case YESTERDAY:
                selectQuery.addConditions(CustomDSL.date(ChargeBox.CHARGE_BOX.LAST_HEARTBEAT_TIMESTAMP).eq(CustomDSL.date(DateTime.now().minusDays(1))));
                break;
            case EARLIER:
                selectQuery.addConditions(CustomDSL.date(ChargeBox.CHARGE_BOX.LAST_HEARTBEAT_TIMESTAMP).lessThan(CustomDSL.date(DateTime.now().minusDays(1))));
                break;
            default:
                throw new SteveException("Unknown enum type");
        }

        selectQuery.addOrderBy(new OrderField[]{ChargeBox.CHARGE_BOX.CHARGE_BOX_PK.asc()});
        return selectQuery.fetch();
    }

    public ChargePoint.Details getDetails(int chargeBoxPk) {
        ChargeBoxRecord cbr = (ChargeBoxRecord)this.ctx.selectFrom(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_PK.equal(chargeBoxPk)).fetchOne();
        if (cbr == null) {
            throw new SteveException("Charge point not found");
        } else {
            AddressRecord ar = this.addressRepository.get(this.ctx, cbr.getAddressPk());
            return new ChargePoint.Details(cbr, ar);
        }
    }

    public ChargePointDetailsDTO getChargePointDetails(int chargeBoxPK) {
        ChargeBoxRecord cbr = (ChargeBoxRecord)this.ctx.selectFrom(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_PK.equal(chargeBoxPK)).fetchOne();
        if (cbr == null) {
            throw new SteveException("Charge point not found");
        } else {
            ChargePointDetailsDTO chargePointDetailsDTO = buildChargePointDetailsDTO(cbr);
            chargePointDetailsDTO.setAddress(buildAddressDTO(this.addressRepository.get(this.ctx, cbr.getAddressPk())));
            return chargePointDetailsDTO;
        }
    }

    public List<ConnectorStatus> getChargePointConnectorStatus(ConnectorStatusForm form) {
        Field<Integer> t1Pk = jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.CONNECTOR_PK.as("t1_pk");
        Field<DateTime> t1TsMax = DSL.max(jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.STATUS_TIMESTAMP).as("t1_ts_max");
        Table<?> t1 = this.ctx.select(t1Pk, t1TsMax).from(jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS).groupBy(new GroupField[]{jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.CONNECTOR_PK}).asTable("t1");
        Field<Integer> t2Pk = jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.CONNECTOR_PK.as("t2_pk");
        Field<DateTime> t2Ts = jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.STATUS_TIMESTAMP.as("t2_ts");
        Field<String> t2Status = jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.STATUS.as("t2_status");
        Field<String> t2Error = jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.ERROR_CODE.as("t2_error");
        Table<?> t2 = this.ctx.selectDistinct(t2Pk, t2Ts, t2Status, t2Error).from(jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS).join(t1).on(jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.CONNECTOR_PK.equal(t1.field(t1Pk))).and(jooq.steve.db.tables.ConnectorStatus.CONNECTOR_STATUS.STATUS_TIMESTAMP.equal(t1.field(t1TsMax))).asTable("t2");
        Condition chargeBoxCondition = ChargeBox.CHARGE_BOX.REGISTRATION_STATUS.eq(RegistrationStatus.ACCEPTED.value());
        if (form != null && form.getChargeBoxId() != null) {
            chargeBoxCondition = chargeBoxCondition.and(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID.eq(form.getChargeBoxId()));
        }

        Condition statusCondition;
        if (form != null && form.getStatus() != null) {
            statusCondition = t2.field(t2Status).eq(form.getStatus());
        } else {
            statusCondition = DSL.noCondition();
        }

        return this.ctx.select(ChargeBox.CHARGE_BOX.CHARGE_BOX_PK, Connector.CONNECTOR.CHARGE_BOX_ID, Connector.CONNECTOR.CONNECTOR_ID, t2.field(t2Ts), t2.field(t2Status), t2.field(t2Error), ChargeBox.CHARGE_BOX.OCPP_PROTOCOL).from(t2).join(Connector.CONNECTOR).on(Connector.CONNECTOR.CONNECTOR_PK.eq(t2.field(t2Pk))).join(ChargeBox.CHARGE_BOX).on(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID.eq(Connector.CONNECTOR.CHARGE_BOX_ID)).where(new Condition[]{chargeBoxCondition, statusCondition}).orderBy(t2.field(t2Ts).desc()).fetch().map((r) -> {
            return ConnectorStatus.builder().chargeBoxPk((Integer)r.value1()).chargeBoxId((String)r.value2()).connectorId((Integer)r.value3()).timeStamp(DateTimeUtils.humanize((DateTime)r.value4())).statusTimestamp((DateTime)r.value4()).status((String)r.value5()).errorCode((String)r.value6()).ocppProtocol(r.value7() == null ? null : OcppProtocol.fromCompositeValue((String)r.value7())).build();
        });
    }

    public List<Integer> getNonZeroConnectorIds(String chargeBoxId) {
        return this.ctx.select(Connector.CONNECTOR.CONNECTOR_ID).from(Connector.CONNECTOR).where(Connector.CONNECTOR.CHARGE_BOX_ID.equal(chargeBoxId)).and(Connector.CONNECTOR.CONNECTOR_ID.notEqual(0)).fetch(Connector.CONNECTOR.CONNECTOR_ID);
    }

    public List<ChargePointDetailsDTO> list(String status) {
        try {
            Result<ChargeBoxRecord> cbr = this.ctx.selectFrom(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.REGISTRATION_STATUS.eq(status)).fetch();
            return CollectionUtils.isEmpty(cbr) ? Collections.emptyList() : (List)cbr.parallelStream().filter(Objects::nonNull).map((chargeBoxRecord) -> {
                ChargePointDetailsDTO chargePointDetailsDTO = buildChargePointDetailsDTO(chargeBoxRecord);
                chargePointDetailsDTO.setAddress(buildAddressDTO(this.addressRepository.get(this.ctx, chargeBoxRecord.getAddressPk())));
                return chargePointDetailsDTO;
            }).collect(Collectors.toList());
        } catch (Exception var3) {
            log.error("Exception while listing charge_box details: ", var3);
            return Collections.emptyList();
        }
    }

    private static ChargePointDetailsDTO.Address buildAddressDTO(AddressRecord addressRecord) {
        if (Objects.nonNull(addressRecord)) {
            ChargePointDetailsDTO.Address address = new ChargePointDetailsDTO.Address();
            address.setCity(addressRecord.getCity());
            address.setStreet(addressRecord.getStreet());
            address.setZipCode(addressRecord.getZipCode());
            address.setHouseNumber(address.getHouseNumber());
            address.setCountry(address.getCountry());
            return address;
        } else {
            return null;
        }
    }

    private static @NotNull ChargePointDetailsDTO buildChargePointDetailsDTO(ChargeBoxRecord chargeBoxRecord) {
        ChargePointDetailsDTO chargePointDetailsDTO = new ChargePointDetailsDTO();
        chargePointDetailsDTO.setChargeBoxPKId(chargeBoxRecord.getChargeBoxPk());
        chargePointDetailsDTO.setChargeBoxId(chargeBoxRecord.getChargeBoxId());
        chargePointDetailsDTO.setDescription(chargeBoxRecord.getDescription());
        chargePointDetailsDTO.setLatitude(chargeBoxRecord.getLocationLatitude() != null ? chargeBoxRecord.getLocationLatitude().doubleValue() : 0.0);
        chargePointDetailsDTO.setLongitude(chargeBoxRecord.getLocationLongitude() != null ? chargeBoxRecord.getLocationLongitude().doubleValue() : 0.0);
        chargePointDetailsDTO.setAdminAddress(chargeBoxRecord.getAdminAddress());
        chargePointDetailsDTO.setOCPPProtocol(chargeBoxRecord.getOcppProtocol());
        chargePointDetailsDTO.setVendor(chargeBoxRecord.getChargePointVendor());
        chargePointDetailsDTO.setModel(chargeBoxRecord.getChargePointModel());
        chargePointDetailsDTO.setSerialNumber(chargeBoxRecord.getChargePointSerialNumber());
        chargePointDetailsDTO.setBoxSerialNumber(chargeBoxRecord.getChargeBoxSerialNumber());
        chargePointDetailsDTO.setFirmWareVersion(chargeBoxRecord.getFwVersion());
        chargePointDetailsDTO.setFirmWareUpdateStatus(chargeBoxRecord.getFwUpdateStatus());
        chargePointDetailsDTO.setFirmWareUpdatedTime(DateTimeUtils.toJavaLocalDateTime(chargeBoxRecord.getFwUpdateTimestamp()));
        chargePointDetailsDTO.setIccid(chargeBoxRecord.getIccid());
        chargePointDetailsDTO.setImsi(chargeBoxRecord.getImsi());
        chargePointDetailsDTO.setMeterType(chargeBoxRecord.getMeterType());
        chargePointDetailsDTO.setMeterSerialNumber(chargeBoxRecord.getMeterSerialNumber());
        chargePointDetailsDTO.setDiagnosticsStatus(chargeBoxRecord.getDiagnosticsStatus());
        chargePointDetailsDTO.setDiagnosticsTime(DateTimeUtils.toJavaLocalDateTime(chargeBoxRecord.getDiagnosticsTimestamp()));
        chargePointDetailsDTO.setLastHeartBeatTime(DateTimeUtils.toJavaLocalDateTime(chargeBoxRecord.getLastHeartbeatTimestamp()));
        return chargePointDetailsDTO;
    }

    public void addChargePointList(List<String> chargeBoxIdList) {
        List<ChargeBoxRecord> batch = (List)chargeBoxIdList.stream().map((s) -> {
            return ((ChargeBoxRecord)this.ctx.newRecord(ChargeBox.CHARGE_BOX)).setChargeBoxId(s).setInsertConnectorStatusAfterTransactionMsg(false);
        }).collect(Collectors.toList());
        this.ctx.batchInsert(batch).execute();
    }

    public int addChargePoint(ChargePointForm form) {
        return (Integer)this.ctx.transactionResult((configuration) -> {
            DSLContext ctx = DSL.using(configuration);

            try {
                Integer addressId = this.addressRepository.updateOrInsert(ctx, form.getAddress());
                return this.addChargePointInternal(ctx, form, addressId);
            } catch (DataAccessException var5) {
                throw new SteveException("Failed to add the charge point with chargeBoxId '%s'", form.getChargeBoxId(), var5);
            }
        });
    }

    public void updateChargePoint(ChargePointForm form) {
        this.ctx.transaction((configuration) -> {
            DSLContext ctx = DSL.using(configuration);

            try {
                Integer addressId = this.addressRepository.updateOrInsert(ctx, form.getAddress());
                this.updateChargePointInternal(ctx, form, addressId);
            } catch (DataAccessException var5) {
                throw new SteveException("Failed to update the charge point with chargeBoxId '%s'", form.getChargeBoxId(), var5);
            }
        });
    }

    public void deleteChargePoint(int chargeBoxPk) {
        this.ctx.transaction((configuration) -> {
            DSLContext ctx = DSL.using(configuration);

            try {
                this.addressRepository.delete(ctx, this.selectAddressId(chargeBoxPk));
                this.deleteChargePointInternal(ctx, chargeBoxPk);
            } catch (DataAccessException var5) {
                throw new SteveException("Failed to delete the charge point", var5);
            }
        });
    }

    private SelectConditionStep<Record1<Integer>> selectAddressId(int chargeBoxPk) {
        return this.ctx.select(ChargeBox.CHARGE_BOX.ADDRESS_PK).from(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_PK.eq(chargeBoxPk));
    }

    private int addChargePointInternal(DSLContext ctx, ChargePointForm form, Integer addressPk) {
        return ((ChargeBoxRecord)ctx.insertInto(ChargeBox.CHARGE_BOX).set(ChargeBox.CHARGE_BOX.CHARGE_BOX_ID, form.getChargeBoxId()).set(ChargeBox.CHARGE_BOX.DESCRIPTION, form.getDescription()).set(ChargeBox.CHARGE_BOX.LOCATION_LATITUDE, form.getLocationLatitude()).set(ChargeBox.CHARGE_BOX.LOCATION_LONGITUDE, form.getLocationLongitude()).set(ChargeBox.CHARGE_BOX.INSERT_CONNECTOR_STATUS_AFTER_TRANSACTION_MSG, form.getInsertConnectorStatusAfterTransactionMsg()).set(ChargeBox.CHARGE_BOX.REGISTRATION_STATUS, form.getRegistrationStatus()).set(ChargeBox.CHARGE_BOX.NOTE, form.getNote()).set(ChargeBox.CHARGE_BOX.ADMIN_ADDRESS, form.getAdminAddress()).set(ChargeBox.CHARGE_BOX.ADDRESS_PK, addressPk).returning(new SelectFieldOrAsterisk[]{ChargeBox.CHARGE_BOX.CHARGE_BOX_PK}).fetchOne()).getChargeBoxPk();
    }

    private void updateChargePointInternal(DSLContext ctx, ChargePointForm form, Integer addressPk) {
        ctx.update(ChargeBox.CHARGE_BOX).set(ChargeBox.CHARGE_BOX.DESCRIPTION, form.getDescription()).set(ChargeBox.CHARGE_BOX.LOCATION_LATITUDE, form.getLocationLatitude()).set(ChargeBox.CHARGE_BOX.LOCATION_LONGITUDE, form.getLocationLongitude()).set(ChargeBox.CHARGE_BOX.INSERT_CONNECTOR_STATUS_AFTER_TRANSACTION_MSG, form.getInsertConnectorStatusAfterTransactionMsg()).set(ChargeBox.CHARGE_BOX.REGISTRATION_STATUS, form.getRegistrationStatus()).set(ChargeBox.CHARGE_BOX.NOTE, form.getNote()).set(ChargeBox.CHARGE_BOX.ADMIN_ADDRESS, form.getAdminAddress()).set(ChargeBox.CHARGE_BOX.ADDRESS_PK, addressPk).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_PK.equal(form.getChargeBoxPk())).execute();
    }

    private void deleteChargePointInternal(DSLContext ctx, int chargeBoxPk) {
        ctx.delete(ChargeBox.CHARGE_BOX).where(ChargeBox.CHARGE_BOX.CHARGE_BOX_PK.equal(chargeBoxPk)).execute();
    }
}
