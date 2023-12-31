package be.vinci.pae.domaine.user;

import java.util.List;
import be.vinci.pae.domaine.address.AddressDTO;

public interface UserUCC {

  UserDTO login(String username, String password);

  UserDTO register(UserDTO userDTO, AddressDTO adress);

  UserDTO getUser(int id);

  List<UserDTO> getAll();

  void updateConfirmed(UserDTO userDTO);

  List<UserDTO> getAllConfirmed(boolean isConfirmed);

  AddressDTO getAddressById(int id);

  List<UserDTO> getAllSearchedUser(String search);

  AddressDTO getVisitAddress(int addressId, int userId);

}

