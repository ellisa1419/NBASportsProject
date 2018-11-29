package com.se.termproject;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
@Service

public interface UserRepository extends CrudRepository<UserMaster, String>{

}