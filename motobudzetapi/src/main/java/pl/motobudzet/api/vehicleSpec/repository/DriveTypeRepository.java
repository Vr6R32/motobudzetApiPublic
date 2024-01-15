package pl.motobudzet.api.vehicleSpec.repository;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.motobudzet.api.vehicleSpec.entity.DriveType;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriveTypeRepository extends JpaRepository<DriveType, Long> {
    @Cacheable("drive_type_cache_by_name")
    @Query("select d from DriveType d where d.name = ?1")
    Optional<DriveType> findByNejm(String driveTypeName);

    @Cacheable(value = "drive_type_cache_by_id")
    @Query("select d from DriveType d where d.id = ?1")
    Optional<DriveType> findByAjdi(Long driveTypeId);

    @Cacheable(value = "drive_type_cache_all")
    @Query("select d from DriveType d")
    List<DriveType> findAllCached();
}