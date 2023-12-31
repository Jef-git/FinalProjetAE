package be.vinci.pae.domaine.user;

import java.util.List;
import be.vinci.pae.api.utils.BusinessException;
import be.vinci.pae.domaine.address.AddressDTO;
import be.vinci.pae.services.DalServices;
import be.vinci.pae.services.UserDAO;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

public class UserUCCImpl implements UserUCC {

  @Inject
  private UserDAO userDao;

  @Inject
  private DalServices dalservices;

  @Override
  public UserDTO login(String username, String password) {
    dalservices.startTransaction();
    User user = (User) this.userDao.findByUserName(username);
    if (user == null || !user.checkPassword(password)) {
      dalservices.rollbackTransaction();
      throw new BusinessException("Username or password incorrect", Status.BAD_REQUEST);
    }
    dalservices.commitTransaction();
    return (UserDTO) user;
  }

  @Override
  public UserDTO register(UserDTO userDTO, AddressDTO addressDTO) {
    dalservices.startTransaction();
    int adresseId = userDao.getAddressByInfo(addressDTO.getStreet(), addressDTO.getBuildingNumber(),
        addressDTO.getCommune(), addressDTO.getCountry());
    if (adresseId == -1) {
      adresseId = userDao.registerAddress(addressDTO);
    }
    User user = (User) userDTO;
    user.setAdressID(adresseId);
    String password = user.hashPassword(userDTO.getPassword());
    user.setPassword(password);
    user = (User) userDao.registerUser(user);
    if (user == null) {
      dalservices.rollbackTransaction();
      throw new BusinessException("User already exists", Status.BAD_REQUEST);
    }
    dalservices.commitTransaction();
    return (UserDTO) user;
  }

  @Override
  public UserDTO getUser(int id) {
    dalservices.startTransaction();
    User user = (User) this.userDao.findById(id);
    if (user == null) {
      dalservices.rollbackTransaction();
      throw new BusinessException("User doesn't exist", Status.BAD_REQUEST);
    }
    dalservices.commitTransaction();
    return (UserDTO) user;
  }

  @Override
  public List<UserDTO> getAll() {
    dalservices.startTransaction();
    List<UserDTO> list = userDao.getAll();
    dalservices.commitTransaction();
    return list;
  }

  @Override
  public List<UserDTO> getAllSearchedUser(String search) {
    dalservices.startTransaction();
    List<UserDTO> list = userDao.getAllSearchedUser(search);
    dalservices.commitTransaction();
    return list;
  }



  @Override
  public List<UserDTO> getAllConfirmed(boolean isConfirmed) {
    dalservices.startTransaction();
    List<UserDTO> list = userDao.getAllConfirmed(isConfirmed);
    dalservices.commitTransaction();
    return list;
  }


  @Override
  public void updateConfirmed(UserDTO user) {
    dalservices.startTransaction();
    this.userDao.updateConfirmed(user);
    dalservices.commitTransaction();
  }

  @Override
  public AddressDTO getAddressById(int id) {
    dalservices.startTransaction();
    AddressDTO address = userDao.getAddressById(id);
    dalservices.commitTransaction();
    return address;
  }

  @Override
  public AddressDTO getVisitAddress(int addressId, int userId) {
    dalservices.startTransaction();
    AddressDTO address = userDao.getVisitAddress(addressId, userId);
    dalservices.commitTransaction();
    return address;
  }

}
