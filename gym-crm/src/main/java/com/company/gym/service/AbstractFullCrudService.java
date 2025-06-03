package com.company.gym.service;

import com.company.gym.dao.BaseDAO;
import com.company.gym.dao.TraineeDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractFullCrudService<T> extends AbstractBaseAndUpdateService<T> implements FullCrudService<T> {
    private static final Logger logger = LoggerFactory.getLogger(AbstractFullCrudService.class);

    protected TraineeDAO dao;

    @Override
    public void setDao(BaseDAO<T> dao) {
        super.setDao(dao);
        this.dao = (TraineeDAO) dao;
    }

    @Override
    public void delete(Long id) {
        dao.delete(id);
        logger.warn("Deleted " + id);
    }
}
