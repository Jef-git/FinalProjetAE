package be.vinci.pae.domaine;

public interface UserUCC {

  UserDTO login(String username, String password);

  UserDTO register(UserDTO userDTO, Address address);
  
  UserDTO getUser(int id);
}
