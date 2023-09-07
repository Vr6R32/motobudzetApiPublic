package pl.motobudzet.api.resources.controller;


import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.motobudzet.api.resources.service.ResourceImageService;

import static pl.motobudzet.api.fileManager.service.PublicAdvertisementImageService.PRIVATE_FILE_PATH;

@RequestMapping("api/resources")
@RestController
public class ResourceImageController {


    private final ResourceImageService resourceImageService;


    public ResourceImageController(ResourceImageService resourceImageService) {
        this.resourceImageService = resourceImageService;
    }

    @GetMapping(value = "/engineType/{engineType}",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getEngineType(@PathVariable String engineType) {
        return new FileSystemResource(PRIVATE_FILE_PATH + engineType.toLowerCase() + ".png");
    }

    @GetMapping(value = "/transmissionType/{transmissionType}",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getTransmissionType(@PathVariable String transmissionType) {
        return new FileSystemResource(PRIVATE_FILE_PATH + transmissionType.toLowerCase() + ".png");
    }

    @GetMapping(value = "/engineHorsePower",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getEngineHorsePower() {
        return new FileSystemResource(PRIVATE_FILE_PATH + "engineHorsePower.png");
    }

    @GetMapping(value = "/fuelType",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getFuel() {
        return new FileSystemResource(PRIVATE_FILE_PATH + "fuel.png");
    }

    @GetMapping(value = "/mileage", produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getMileage() {
        return new FileSystemResource(PRIVATE_FILE_PATH + "mileage.png");
    }

    @GetMapping(value = "/productionDate",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getProduction() {
        return new FileSystemResource(PRIVATE_FILE_PATH + "production.png");
    }

    @GetMapping(value = "/construction",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getConstructionSite() {
        return new FileSystemResource(PRIVATE_FILE_PATH + "construction.png");
    }

    @GetMapping(value = "/logo",produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getLogo() {
        return new FileSystemResource(PRIVATE_FILE_PATH + "logo.png");
    }

    @GetMapping(value = "/advertisementPhoto/{imageUrl}",produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getOriginalPhoto(@PathVariable String imageUrl) {

        Resource photoResource = resourceImageService.getAdvertisementPhoto(imageUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoResource);
    }

    @GetMapping(value = "/advertisementPhoto/miniature/{imageUrl}",produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getMiniaturePhoto400x300(@PathVariable String imageUrl) {

        Resource photoResource = resourceImageService.getAdvertisementPhotoMiniature400x300(imageUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoResource);
    }

    @GetMapping(value = "/advertisementPhoto/miniature/150/{imageUrl}",produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getMiniaturePhoto150x150(@PathVariable String imageUrl) {

        Resource photoResource = resourceImageService.getAdvertisementPhotoMiniature150x150(imageUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoResource);
    }

    @GetMapping(value = "/advertisementPhoto/half/miniature/{imageUrl}",produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<Resource> getHalfMiniaturePhoto(@PathVariable String imageUrl) {

        Resource photoResource = resourceImageService.getAdvertisementPhotoHalfMiniature(imageUrl);

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(photoResource);
    }
}