package com.smart.DAO;

import com.smart.entities.Contact;
import com.smart.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ContactRepository extends JpaRepository<Contact, Integer> {
    @Query("from Contact as c where c.user.userId =:userId")
    public Page<Contact> findContactByUser(@Param("userId") int userId, Pageable pageable);

    // Method for Search
    public List<Contact> findContactByNameContainingAndUser(String name, User user);
}

