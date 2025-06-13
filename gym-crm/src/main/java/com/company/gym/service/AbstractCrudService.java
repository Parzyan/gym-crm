package com.company.gym.service;

import com.company.gym.dao.BaseDAO;
import com.company.gym.dao.TraineeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractCrudService<T> extends AbstractBaseAndUpdateService<T> implements CrudService<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractCrudService.class);

    protected TraineeDAO dao;

    @Override
    public void setDao(BaseDAO<T> dao) {
        super.setDao(dao);
        this.dao = (TraineeDAO) dao;
    }

    @Override
    public void delete(Long id) {
        dao.delete(id);
        logger.debug("Deleted " + id);
    }
}
