package com.neteacher.user.repository;

import com.neteacher.user.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByPhone(String phone);

    Optional<UserAccount> findByWxOpenid(String wxOpenid);

    List<UserAccount> findByParentId(Long parentId);

    List<UserAccount> findByClassId(Long classId);

    List<UserAccount> findByClassIdIn(List<Long> classIds);

    List<UserAccount> findByRole(String role);

    List<UserAccount> findBySchoolId(Long schoolId);
}
