package be.vinci.pae.domaine.option;

import be.vinci.pae.api.utils.BusinessException;
import be.vinci.pae.services.DalServices;
import be.vinci.pae.services.OptionDAO;
import be.vinci.pae.services.UserDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

public class OptionUCCImpl implements OptionUCC {


  @Inject
  private OptionDAO optionDao;

  @Inject
  private UserDAO userDao;

  @Inject
  private DalServices dalservices;

  @Override
  public void introduceOption(OptionDTO option) {
    dalservices.startTransaction();
    int optionID = optionDao.findOptionByInfo(option);
    if (optionID > 0) {
      dalservices.rollbackTransaction();
      throw new BusinessException("This furniture is already reserved", Status.BAD_REQUEST);
    }
    int userID = option.getId();
    if (userDao.findById(userID) == null) {
      throw new BusinessException("User ID does not exist!");
    }
    optionDao.introduceOption(option);
    optionDao.changeFurnitureState("O", option.getFurniture());
    dalservices.commitTransaction();
  }

  @Override
  public OptionDTO stopOption() {
    return null;
  }

}
