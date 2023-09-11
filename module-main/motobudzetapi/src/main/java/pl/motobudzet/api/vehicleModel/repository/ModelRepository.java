package pl.motobudzet.api.vehicleModel.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.motobudzet.api.vehicleModel.entity.Model;

import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    @Cacheable(value = "vehicle_model_cache_by_name")
    Optional<Model> findByName(String name);

    @Cacheable(value = "vehicle_model_cache")
    @Query("select m from Model m where m.id = ?1")
    Optional<Model> findByAjdi(Long modelId);
}