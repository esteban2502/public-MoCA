package com.ucp.moca.repository;

import com.ucp.moca.entity.PermissionEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PermissionRepository extends CrudRepository<PermissionEntity, Integer> {

    Optional<PermissionEntity> findByName(String name);
}


