package com.company.gym.dao.impl;

import com.company.gym.entity.Trainee;
import com.company.gym.entity.User;
import com.company.gym.exception.DAOException;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public class UserDAOImpl extends BaseUserDAOImpl<User> {

    private static final String FIND_BY_USERNAME = "SELECT u FROM User u WHERE u.username = :username";

    @Override
    protected Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try {
            TypedQuery<User> query = entityManager.createQuery(
                    FIND_BY_USERNAME, User.class
            );
            query.setParameter("username", username);
            return query.getResultList().stream().findFirst();
        } catch (Exception e) {
            logger.error("Error finding user by username: {}", username, e);
            return Optional.empty();
        }
    }

    @Override
    public void save(User obj) {
        try {
            entityManager.persist(obj);
            logger.info("Saved new user with ID: {}", obj.getId());
        } catch (Exception e) {
            logger.error("Error saving user", e);
            throw new DAOException("Error saving user", e);
        }
    }

    @Override
    public void update(User user) {
        try {
            entityManager.merge(user);
            logger.debug("Updated user with ID: {}", user.getId());
        } catch (Exception e) {
            logger.error("Error updating user with ID: {}", user.getId(), e);
            throw new DAOException("Error updating user", e);
        }
    }
}
