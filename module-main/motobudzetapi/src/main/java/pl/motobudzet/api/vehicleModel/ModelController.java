package pl.motobudzet.api.vehicleModel;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "api/models")
public class ModelController {

    private final ModelService modelService;

    public ModelController(ModelService modelService) {
        this.modelService = modelService;
    }

    @GetMapping("/all")
    public List<Model> findAllModels() {
        return modelService.findAllModels();
    }

    @GetMapping("/{brandName}")
    public List<ModelDTO> findAllModelsFromSpecifiedBrand(@PathVariable String brandName) {
        return modelService.findAllModelsFromSpecifiedBrand(brandName);
    }

    // TODO PREAUTHORIZE HAS ROLE ADMIN

    @PostMapping("/")
    public ResponseEntity<String> insertNewModel(@RequestParam String model, @RequestParam String brand) {
        return modelService.insertNewModel(model, brand);
    }

    // TODO PREAUTHORIZE HAS ROLE ADMIN

    @DeleteMapping("/")
    public ResponseEntity<String> deleteModel(@RequestParam String modelName, @RequestParam String brandName) {
        return modelService.deleteModel(modelName.toUpperCase(),brandName.toUpperCase());
    }

}