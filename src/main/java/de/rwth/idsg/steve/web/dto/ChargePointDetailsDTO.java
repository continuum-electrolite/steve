package de.rwth.idsg.steve.web.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.apache.cxf.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Objects;

@JsonIgnoreProperties(
        value = {"valid"},
        ignoreUnknown = true
)
public class ChargePointDetailsDTO {
    private static final Logger log = LoggerFactory.getLogger(ChargePointDetailsDTO.class);
    private int chargeBoxPKId;
    private String chargeBoxId;
    private boolean insertConnectorStatusAfterTxnMsg;
    private String registrationStatus = "Pending";
    private Double latitude;
    private Double longitude;
    private String description;
    private String additionalNotes;
    private String adminAddress;
    private String oCPPProtocol;
    private String vendor;
    private String model;
    private String serialNumber;
    private String boxSerialNumber;
    private String firmWareVersion;
    private String firmWareUpdateStatus;
    private LocalDateTime firmWareUpdatedTime;
    private String iccid;
    private String imsi;
    private String meterType;
    private String meterSerialNumber;
    private String diagnosticsStatus;
    private LocalDateTime diagnosticsTime;
    private LocalDateTime lastHeartBeatTime;
    private Address address;

    public ChargePointDetailsDTO() {
    }

    public boolean isValid() {
        if (this.latitude > -90.0 && this.latitude < 90.0) {
            if (this.longitude > -180.0 && this.longitude < 180.0) {
                if (StringUtils.isEmpty(this.chargeBoxId)) {
                    return false;
                } else if (Objects.isNull(this.address)) {
                    return false;
                } else {
                    return !this.address.isEmpty();
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setChargeBoxPKId(int chargeBoxPKId) {
        this.chargeBoxPKId = chargeBoxPKId;
    }

    public void setChargeBoxId(String chargeBoxId) {
        this.chargeBoxId = chargeBoxId;
    }

    public void setInsertConnectorStatusAfterTxnMsg(boolean insertConnectorStatusAfterTxnMsg) {
        this.insertConnectorStatusAfterTxnMsg = insertConnectorStatusAfterTxnMsg;
    }

    public void setRegistrationStatus(String registrationStatus) {
        this.registrationStatus = registrationStatus;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAdditionalNotes(String additionalNotes) {
        this.additionalNotes = additionalNotes;
    }

    public void setAdminAddress(String adminAddress) {
        this.adminAddress = adminAddress;
    }

    public void setOCPPProtocol(String oCPPProtocol) {
        this.oCPPProtocol = oCPPProtocol;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public void setBoxSerialNumber(String boxSerialNumber) {
        this.boxSerialNumber = boxSerialNumber;
    }

    public void setFirmWareVersion(String firmWareVersion) {
        this.firmWareVersion = firmWareVersion;
    }

    public void setFirmWareUpdateStatus(String firmWareUpdateStatus) {
        this.firmWareUpdateStatus = firmWareUpdateStatus;
    }

    public void setFirmWareUpdatedTime(LocalDateTime firmWareUpdatedTime) {
        this.firmWareUpdatedTime = firmWareUpdatedTime;
    }

    public void setIccid(String iccid) {
        this.iccid = iccid;
    }

    public void setImsi(String imsi) {
        this.imsi = imsi;
    }

    public void setMeterType(String meterType) {
        this.meterType = meterType;
    }

    public void setMeterSerialNumber(String meterSerialNumber) {
        this.meterSerialNumber = meterSerialNumber;
    }

    public void setDiagnosticsStatus(String diagnosticsStatus) {
        this.diagnosticsStatus = diagnosticsStatus;
    }

    public void setDiagnosticsTime(LocalDateTime diagnosticsTime) {
        this.diagnosticsTime = diagnosticsTime;
    }

    public void setLastHeartBeatTime(LocalDateTime lastHeartBeatTime) {
        this.lastHeartBeatTime = lastHeartBeatTime;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public int getChargeBoxPKId() {
        return this.chargeBoxPKId;
    }

    public String getChargeBoxId() {
        return this.chargeBoxId;
    }

    public boolean isInsertConnectorStatusAfterTxnMsg() {
        return this.insertConnectorStatusAfterTxnMsg;
    }

    public String getRegistrationStatus() {
        return this.registrationStatus;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public String getDescription() {
        return this.description;
    }

    public String getAdditionalNotes() {
        return this.additionalNotes;
    }

    public String getAdminAddress() {
        return this.adminAddress;
    }

    public String getOCPPProtocol() {
        return this.oCPPProtocol;
    }

    public String getVendor() {
        return this.vendor;
    }

    public String getModel() {
        return this.model;
    }

    public String getSerialNumber() {
        return this.serialNumber;
    }

    public String getBoxSerialNumber() {
        return this.boxSerialNumber;
    }

    public String getFirmWareVersion() {
        return this.firmWareVersion;
    }

    public String getFirmWareUpdateStatus() {
        return this.firmWareUpdateStatus;
    }

    public LocalDateTime getFirmWareUpdatedTime() {
        return this.firmWareUpdatedTime;
    }

    public String getIccid() {
        return this.iccid;
    }

    public String getImsi() {
        return this.imsi;
    }

    public String getMeterType() {
        return this.meterType;
    }

    public String getMeterSerialNumber() {
        return this.meterSerialNumber;
    }

    public String getDiagnosticsStatus() {
        return this.diagnosticsStatus;
    }

    public LocalDateTime getDiagnosticsTime() {
        return this.diagnosticsTime;
    }

    public LocalDateTime getLastHeartBeatTime() {
        return this.lastHeartBeatTime;
    }

    public Address getAddress() {
        return this.address;
    }

    public String toString() {
        int var10000 = this.getChargeBoxPKId();
        return "ChargePointDetailsDTO(chargeBoxPKId=" + var10000 + ", chargeBoxId=" + this.getChargeBoxId() + ", insertConnectorStatusAfterTxnMsg=" + this.isInsertConnectorStatusAfterTxnMsg() + ", registrationStatus=" + this.getRegistrationStatus() + ", latitude=" + this.getLatitude() + ", longitude=" + this.getLongitude() + ", description=" + this.getDescription() + ", additionalNotes=" + this.getAdditionalNotes() + ", adminAddress=" + this.getAdminAddress() + ", oCPPProtocol=" + this.getOCPPProtocol() + ", vendor=" + this.getVendor() + ", model=" + this.getModel() + ", serialNumber=" + this.getSerialNumber() + ", boxSerialNumber=" + this.getBoxSerialNumber() + ", firmWareVersion=" + this.getFirmWareVersion() + ", firmWareUpdateStatus=" + this.getFirmWareUpdateStatus() + ", firmWareUpdatedTime=" + this.getFirmWareUpdatedTime() + ", iccid=" + this.getIccid() + ", imsi=" + this.getImsi() + ", meterType=" + this.getMeterType() + ", meterSerialNumber=" + this.getMeterSerialNumber() + ", diagnosticsStatus=" + this.getDiagnosticsStatus() + ", diagnosticsTime=" + this.getDiagnosticsTime() + ", lastHeartBeatTime=" + this.getLastHeartBeatTime() + ", address=" + this.getAddress() + ")";
    }

    @JsonIgnoreProperties({"empty"})
    public static class Address {
        private String street;
        private String houseNumber;
        private String zipCode;
        private String city;
        private String country;

        public Address() {
        }

        public boolean isEmpty() {
            return this.street == null && this.houseNumber == null && this.zipCode == null && this.city == null && this.country == null;
        }

        public String getStreet() {
            return this.street;
        }

        public String getHouseNumber() {
            return this.houseNumber;
        }

        public String getZipCode() {
            return this.zipCode;
        }

        public String getCity() {
            return this.city;
        }

        public String getCountry() {
            return this.country;
        }

        public void setStreet(String street) {
            this.street = street;
        }

        public void setHouseNumber(String houseNumber) {
            this.houseNumber = houseNumber;
        }

        public void setZipCode(String zipCode) {
            this.zipCode = zipCode;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public String toString() {
            String var10000 = this.getStreet();
            return "ChargePointDetailsDTO.Address(street=" + var10000 + ", houseNumber=" + this.getHouseNumber() + ", zipCode=" + this.getZipCode() + ", city=" + this.getCity() + ", country=" + this.getCountry() + ")";
        }
    }
}