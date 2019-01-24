package ca.pcraig3.holidays.holiday;

import ca.pcraig3.holidays.province.Province;
import ca.pcraig3.holidays.province.ProvinceBadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/v1/holidays")
public class HolidayController {

    private final HolidayRepository repository;
    HolidayController(HolidayRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    HashMap<String, List<Holiday>> all(
            HttpServletRequest request,
            @RequestParam(value = "national", required=false) Boolean national,
            @RequestParam(value = "province", required=false) String provinceId
    ) {
        List<Holiday> holidays = null;

        if(national != null) {
            holidays = this.repository.findByIsNational(national);
            log.info(String.format("Get '%s'. Found: %d.", request.getRequestURI(), holidays.size()));
        }

        // string should not be null or empty
        if(!StringUtils.isEmpty(provinceId)){
            provinceId = provinceId.toUpperCase();

            // make sure we have a valid province ID
            if(!Province.isProvinceId(provinceId))
                throw new ProvinceBadRequestException(provinceId);

            holidays = this.repository.findByProvinceId(provinceId);
            log.info(String.format("Get '%s'. Found: %d.", request.getRequestURI(), holidays.size()));
        }

        if (holidays == null){
            holidays = this.repository.findAll();
            log.info(String.format("Get '%s'. Found: %d.", request.getRequestURI(), holidays.size()));
        }

        HashMap<String, List<Holiday>> responseMap = new HashMap<>();
        responseMap.put("holidays", holidays);
        return responseMap;
    }

    @GetMapping("/{idParam}")
    HashMap<String, Holiday> one(HttpServletRequest request, @PathVariable String idParam) {
        log.info(String.format("Get '%s'.", request.getRequestURI()));
        final Long id;

        try {
            id = Long.parseLong(idParam);
        }
        catch(NumberFormatException e) {
            throw new NumberFormatException("Error: '" + idParam + "' is not an integer value.");
        }

        Holiday holiday = this.repository.findById(id).orElseThrow(() -> new HolidayNotFoundException(id));

        HashMap<String, Holiday> responseMap = new HashMap<>();
        responseMap.put("holiday", holiday);
        return responseMap;
    }

}

