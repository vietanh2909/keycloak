package org.example.infrastructure.persistance;

import org.example.infrastructure.domain.common.entity.ServicePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServicePermissionRepository extends JpaRepository<ServicePermission, Long> {
}
