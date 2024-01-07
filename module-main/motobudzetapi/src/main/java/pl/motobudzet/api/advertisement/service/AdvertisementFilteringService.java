package pl.motobudzet.api.advertisement.service;


import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import pl.motobudzet.api.advertisement.dto.AdvertisementDTO;
import pl.motobudzet.api.advertisement.dto.AdvertisementFilterRequest;
import pl.motobudzet.api.advertisement.entity.Advertisement;
import pl.motobudzet.api.advertisement.repository.AdvertisementRepository;
import pl.motobudzet.api.location_city.City;
import pl.motobudzet.api.location_city.CityService;
import pl.motobudzet.api.vehicleBrand.BrandService;
import pl.motobudzet.api.vehicleModel.ModelService;
import pl.motobudzet.api.vehicleSpec.service.SpecificationService;

import java.util.*;
import java.util.stream.Collectors;

import static pl.motobudzet.api.advertisement.service.FilteringHelper.*;
import static pl.motobudzet.api.mappers.AdvertisementMapper.mapToAdvertisementDTO;

@Service
@RequiredArgsConstructor
public class AdvertisementFilteringService {

    private final AdvertisementRepository advertisementRepository;
    private final AdvertisementService advertisementService;
    private final SpecificationService specificationService;
    private final BrandService brandService;
    private final ModelService modelService;
    private final CityService cityService;


    public Page<AdvertisementDTO> getFilteredAdvertisements(
            AdvertisementFilterRequest request,
            Integer pageNumber,
            String sortBy, String sortOrder) {


        Specification<Advertisement> specification = setAdvertisementFilterSpecification(request);

        PageRequest pageable = setPageRequest(pageNumber, sortBy, sortOrder);

        Page<UUID> advertisementSpecificationIds = advertisementRepository.findAll(specification, pageable).map(Advertisement::getId);
        List<UUID> uuidList = advertisementSpecificationIds.getContent();

        List<Advertisement> fetchedAdvertisementDetails = advertisementRepository.findByListOfUUIDs(uuidList);
        List<Advertisement> advertisementDetails = uuidList.stream()
                .map(uuid -> fetchedAdvertisementDetails.stream()
                        .filter(adv -> adv.getId().equals(uuid))
                        .findFirst()
                        .orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return new PageImpl<>(advertisementDetails, pageable, advertisementSpecificationIds.getTotalElements())
                .map(advertisement -> mapToAdvertisementDTO(advertisement, false));
    }

    public long getFilterResultCount(AdvertisementFilterRequest request,
                                     Integer pageNumber,
                                     String sortBy, String sortOrder) {

        Specification<Advertisement> specification = setAdvertisementFilterSpecification(request);

        PageRequest pageable = setPageRequest(pageNumber, sortBy, sortOrder);

        Page<UUID> advertisementSpecificationIds = advertisementRepository.findAll(specification, pageable).map(Advertisement::getId);
        return advertisementSpecificationIds.getTotalElements();
    }


    private Specification<Advertisement> setAdvertisementFilterSpecification(AdvertisementFilterRequest request) {

        Specification<Advertisement> specification = (root, query, criteriaBuilder) ->
                criteriaBuilder.and(
                        criteriaBuilder.isTrue(root.get("isVerified")),
                        criteriaBuilder.isTrue(root.get("isActive")),
                        criteriaBuilder.isFalse(root.get("isDeleted"))
                );

        Map<String, ServiceFunction> serviceFunctionMap = new HashMap<>();
        serviceFunctionMap.put("brand", brandService::getBrand);
        serviceFunctionMap.put("model", modelName -> modelService.getModelByBrand(request.getModel(), request.getBrand()));
        serviceFunctionMap.put("fuelType", specificationService::getFuelType);
        serviceFunctionMap.put("driveType", specificationService::getDriveType);
        serviceFunctionMap.put("engineType", specificationService::getEngineType);
        serviceFunctionMap.put("transmissionType", specificationService::getTransmissionType);

        specification = handleSelectValue(request, specification, serviceFunctionMap);

        specification = handleValueInRangeBetween(specification, "price", request.getPriceMin(), request.getPriceMax());
        specification = handleValueInRangeBetween(specification, "mileage", request.getMileageFrom(), request.getMileageTo());
        specification = handleValueInRangeBetween(specification, "engineCapacity", request.getEngineCapacityFrom(), request.getEngineCapacityTo());
        specification = handleValueInRangeBetween(specification, "engineHorsePower", request.getEngineHorsePowerFrom(), request.getEngineHorsePowerTo());
        specification = handleValueInRangeBetween(specification, "productionDate", request.getProductionDateFrom(), request.getProductionDateTo());


        String city = request.getCity();
        String cityState = request.getCityState();
        Integer distanceFrom = request.getDistanceFrom() != null ? request.getDistanceFrom() : 0;


        if (cityState != null && !cityState.isEmpty() && (city == null || city.isEmpty())) {
            specification = specification.and((root, query, criteriaBuilder) -> {
                Join<Advertisement, City> cityJoin = root.join("city", JoinType.LEFT);
                return criteriaBuilder.equal(cityJoin.get("cityState").get("name"), cityState);
            });
        } else if (city != null && !city.isEmpty() && distanceFrom != null) {
            List<City> cityList = cityService.getCitiesWithinDistance(city, distanceFrom);
            specification = specification.and((root, query, criteriaBuilder) ->
                    root.get("city").in(cityList)
            );
        } else if (city != null && !city.isEmpty()) {
            specification = specification.and((root, query, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("city"), cityService.getCityByNameWithout(city))
            );
        }
        return specification;
    }
}
