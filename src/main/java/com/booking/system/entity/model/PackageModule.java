package com.booking.system.entity.model;

import lombok.*;
import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import java.time.ZonedDateTime;

@Entity
@Table(name = "package")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageModule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name",columnDefinition = "TEXT")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country", nullable = false)
    private Country country;

    @Column(name = "price")
    private Double price;

    @Column(name = "credit_amount")
    private Integer creditAmount;

    @Column(name = "expiration_days")
    private Integer expirationDays;

    @Column(name = "created_on")
    private ZonedDateTime createdOn;

    @Column(name = "updated_on")
    private ZonedDateTime updatedOn;

    @Column(name = "status")
    private String status;
}
