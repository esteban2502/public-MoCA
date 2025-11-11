package com.ucp.moca;

import com.ucp.moca.entity.PermissionEntity;
import com.ucp.moca.entity.RoleEntity;
import com.ucp.moca.entity.RoleEnum;
import com.ucp.moca.entity.UserEntity;
import com.ucp.moca.repository.UserEntityRepository;
import com.ucp.moca.repository.RoleRepository;
import com.ucp.moca.repository.PermissionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Set;

@SpringBootApplication
public class MocaApplication {


	@Value("${admin.email}")
	private String emailAdmin;

	@Value("${admin.password}")
	private String passwordAdmin;

	private PasswordEncoder passwordEncoder;

	public MocaApplication(@Value("${admin.password}") String passwordAdmin,
						   @Value("${admin.email}") String emailAdmin,
						   PasswordEncoder passwordEncoder) {
		this.passwordAdmin = passwordAdmin;
		this.emailAdmin = emailAdmin;
		this.passwordEncoder = passwordEncoder;
	}

	public static void main(String[] args) {
		SpringApplication.run(MocaApplication.class, args);
	}

	@Bean
	CommandLineRunner init(UserEntityRepository userEntityRepository, RoleRepository roleRepository, PermissionRepository permissionRepository){
		return args->{

			// Ensure base permissions exist
			PermissionEntity createPermission = permissionRepository.findByName("CREATE")
					.orElseGet(() -> permissionRepository.save(PermissionEntity.builder().name("CREATE").build()));
			PermissionEntity readPermission = permissionRepository.findByName("READ")
					.orElseGet(() -> permissionRepository.save(PermissionEntity.builder().name("READ").build()));
			PermissionEntity updatePermission = permissionRepository.findByName("UPDATE")
					.orElseGet(() -> permissionRepository.save(PermissionEntity.builder().name("UPDATE").build()));
			PermissionEntity deletePermission = permissionRepository.findByName("DELETE")
					.orElseGet(() -> permissionRepository.save(PermissionEntity.builder().name("DELETE").build()));

			// Ensure roles exist with permissions
			RoleEntity adminRole = roleRepository.findByRoleEnum(RoleEnum.ADMIN);
			if (adminRole == null) {
				adminRole = RoleEntity.builder()
						.roleEnum(RoleEnum.ADMIN)
						.permissionList(Set.of(createPermission, readPermission, updatePermission, deletePermission))
						.build();
				adminRole = roleRepository.save(adminRole);
			}

	
			RoleEntity user2Role = roleRepository.findByRoleEnum(RoleEnum.USER);
			if (user2Role == null) {
				user2Role = RoleEntity.builder()
						.roleEnum(RoleEnum.USER)
						.permissionList(Set.of(readPermission, updatePermission))
						.build();
				roleRepository.save(user2Role);
			}

			// Create admin user only if not present, and assign existing ADMIN role
			if (userEntityRepository.findUserEntityByEmail(emailAdmin).isEmpty()) {
				UserEntity admin = UserEntity.builder()
						.email(emailAdmin)
						.password(passwordEncoder.encode(passwordAdmin))
						.isEnabled(true)
						.accountNoExpired(true)
						.accountNoLocked(true)
						.credentialNoExpired(true)
						.roles(Set.of(adminRole))
						.build();
				userEntityRepository.saveAll(List.of(admin));
			}

		};

	}
}
