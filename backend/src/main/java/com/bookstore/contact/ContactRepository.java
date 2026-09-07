package com.bookstore.contact;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<ContactRequest, Long> {
    Page<ContactRequest> findByStatus(ContactStatus status, Pageable pageable);
}
