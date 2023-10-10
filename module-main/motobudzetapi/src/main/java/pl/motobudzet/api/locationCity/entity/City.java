package pl.motobudzet.api.locationCity.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import pl.motobudzet.api.locationState.entity.CityState;

@Entity
@Data
@RequiredArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "cities")
public class City {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "name", nullable = false, length = 150)
    private String name;
    @Column(name = "n_latitude", nullable = false, length = 20)
    private Double nLatitude;
    @Column(name = "e_longitude", nullable = false, length = 20)
    private Double eLongitude;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", referencedColumnName = "id", insertable = false, updatable = false)
    private CityState cityState;

}