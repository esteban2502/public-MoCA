package com.ucp.moca.repository;

import com.ucp.moca.entity.RoleEntity;
import com.ucp.moca.entity.RoleEnum;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<RoleEntity, Integer> {

    List<RoleEntity> findRoleEntitiesByRoleEnumIn(List<String> rolesNames);

    RoleEntity findByRoleEnum(RoleEnum roleEnum);

}