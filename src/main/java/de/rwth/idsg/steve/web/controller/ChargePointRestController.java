package de.rwth.idsg.steve.web.controller;

import com.neovisionaries.i18n.CountryCode;
import de.rwth.idsg.steve.repository.ChargePointRepository;
import de.rwth.idsg.steve.service.ChargePointHelperService;
import de.rwth.idsg.steve.web.dto.Address;
import de.rwth.idsg.steve.web.dto.ChargePointDetailsDTO;
import de.rwth.idsg.steve.web.dto.ChargePointForm;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/manager/chargepoints/rest"})
public class ChargePointRestController {
    private static final Logger log = LoggerFactory.getLogger(ChargePointRestController.class);
    @Autowired
    protected ChargePointRepository chargePointRepository;
    @Autowired
    protected ChargePointHelperService chargePointHelperService;

    public ChargePointRestController() {
    }

    @PostMapping(
            consumes = {"application/json"}
    )
    public ResponseEntity<String> add(@RequestBody ChargePointDetailsDTO chargePointDetailsDTO) {
        log.info("ChargePoint Location Request body: {}", chargePointDetailsDTO);
        if (chargePointDetailsDTO.isValid()) {
            int id = this.chargePointRepository.addChargePoint(this.toChargePointForm(chargePointDetailsDTO));
            this.chargePointHelperService.removeUnknown(Collections.singletonList(chargePointDetailsDTO.getChargeBoxId()));
            return new ResponseEntity("ChangePoint added with id: " + id, HttpStatus.CREATED);
        } else {
            return new ResponseEntity("Invalid Request Body [latitude (-90 <-> +90), longitude(-180 <-> 180), chargeBoxId, Address mandatory.]", HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping(
            value = {"/{chargeBoxId}"},
            consumes = {"application/json"}
    )
    public ResponseEntity<String> update(@RequestBody ChargePointDetailsDTO chargePointDetailsDTO, @PathVariable("chargeBoxPKID") int chargeBoxPKId) {
        if (chargePointDetailsDTO.isValid()) {
            ChargePointForm chargePointForm = this.toChargePointForm(chargePointDetailsDTO);
            chargePointForm.setChargeBoxPk(chargeBoxPKId);
            this.chargePointRepository.updateChargePoint(chargePointForm);
            this.chargePointHelperService.removeUnknown(Collections.singletonList(chargePointDetailsDTO.getChargeBoxId()));
            return new ResponseEntity("ChangePoint added", HttpStatus.CREATED);
        } else {
            return new ResponseEntity("Invalid Request Body [latitude (-90 <-> +90), longitude(-180 <-> 180), chargeBoxId, Address mandatory.]", HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping({"/{chargeBoxPKID}"})
    public ResponseEntity<String> delete(@PathVariable("chargeBoxPKID") int chargeBoxPKId) {
        this.chargePointRepository.deleteChargePoint(chargeBoxPKId);
        return new ResponseEntity("ChangePoint deleted.", HttpStatus.OK);
    }

    @GetMapping(
            value = {"/{chargeBoxPKID}"},
            produces = {"application/json"}
    )
    public ResponseEntity<ChargePointDetailsDTO> get(@PathVariable("chargeBoxPKID") int chargeBoxPKID) {
        try {
            return new ResponseEntity(this.chargePointRepository.getChargePointDetails(chargeBoxPKID), HttpStatus.OK);
        } catch (Exception var3) {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping(
            value = {"/list"},
            produces = {"application/json"}
    )
    public ResponseEntity<List<ChargePointDetailsDTO>> list(@RequestParam("status") String status) {
        return new ResponseEntity(this.chargePointRepository.list(status), HttpStatus.OK);
    }

    private ChargePointForm toChargePointForm(ChargePointDetailsDTO chargePointDetailsDTO) {
        ChargePointForm chargePointForm = new ChargePointForm();
        chargePointForm.setChargeBoxId(chargePointDetailsDTO.getChargeBoxId());
        chargePointForm.setRegistrationStatus(chargePointDetailsDTO.getRegistrationStatus());
        chargePointForm.setDescription(chargePointDetailsDTO.getDescription());
        chargePointForm.setNote(chargePointDetailsDTO.getAdditionalNotes());
        chargePointForm.setLocationLatitude(BigDecimal.valueOf(chargePointDetailsDTO.getLatitude()));
        chargePointForm.setLocationLongitude(BigDecimal.valueOf(chargePointDetailsDTO.getLongitude()));
        chargePointForm.setInsertConnectorStatusAfterTransactionMsg(chargePointDetailsDTO.isInsertConnectorStatusAfterTxnMsg());
        chargePointForm.setAdminAddress(chargePointDetailsDTO.getAdminAddress());
        Address address = new Address();
        address.setCity(chargePointDetailsDTO.getAddress().getCity());
        address.setStreet(chargePointDetailsDTO.getAddress().getStreet());
        address.setHouseNumber(chargePointDetailsDTO.getAddress().getHouseNumber());
        address.setZipCode(chargePointDetailsDTO.getAddress().getZipCode());
        address.setCountry(CountryCode.getByCode(chargePointDetailsDTO.getAddress().getCountry()));
        chargePointForm.setAddress(address);
        return chargePointForm;
    }
}
